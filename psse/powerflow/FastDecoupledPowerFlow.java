package com.powerdata.openpa.psse.powerflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.powerdata.openpa.psse.ACBranch;
import com.powerdata.openpa.psse.ACBranchList;
import com.powerdata.openpa.psse.Bus;
import com.powerdata.openpa.psse.BusList;
import com.powerdata.openpa.psse.BusTypeCode;
import com.powerdata.openpa.psse.Gen;
import com.powerdata.openpa.psse.Island;
import com.powerdata.openpa.psse.IslandList;
import com.powerdata.openpa.psse.PsseModel;
import com.powerdata.openpa.psse.PsseModelException;
import com.powerdata.openpa.psse.Shunt;
import com.powerdata.openpa.tools.Complex;
import com.powerdata.openpa.tools.FactorizedBMatrix;
import com.powerdata.openpa.tools.LinkNet;
import com.powerdata.openpa.tools.PAMath;
import com.powerdata.openpa.tools.SparseBMatrix;

public class FastDecoupledPowerFlow
{

	public static final float _Ptol = 0.005f;
	public static final float _Qtol = 0.005f;
	public static final float _Itermax = 40;
	PsseModel _model;
	FactorizedBMatrix _bp, _bpp;
	SparseBMatrix _prepbpp;
	int[] _hotislands;
	float[] _vm, _va;
	
	public FastDecoupledPowerFlow(PsseModel model) throws PsseModelException
	{
		_model = model;
		setupHotIslands();
		buildMatrices();
		
	}

	public PowerFlowConvergenceList runPowerFlow(PowerCalculator pcalc,
			VoltageSource vsrc) throws PsseModelException, IOException
	{
		BusList buses = _model.getBuses();
		int nbus = buses.size();

		float[] vm, va;
		int[] ldbus = _model.getBusNdxForType(BusTypeCode.Load);
		int[] genbus = _model.getBusNdxForType(BusTypeCode.Gen);
		int[][] pbus = new int[][] { ldbus, genbus };
		int[][] qbus = new int[][] { ldbus };

		switch(vsrc)
		{
			case RealTime:
				float[][] rtv = pcalc.getRTVoltages();
				va = rtv[0];
				vm = rtv[1];

			case LastSolved:
				va = _va;
				vm = _vm;
				break;
				
			case Flat:
			default:
				va = new float[nbus];
				vm = flatMag(ldbus);
				break;

		}

		for(Gen g : _model.getGenerators())
		{
			Bus b = g.getBus();
			BusTypeCode btc = b.getBusType();
			if (btc == BusTypeCode.Gen || btc == BusTypeCode.Slack)
			{
				//TODO:  resolve multiple setpoints if found
				int bndx = b.getIndex();
				vm[bndx] = g.getVS();
			}
		}

		boolean nconv=true;
		float[][] mm = pcalc.calculateMismatches(va, vm);
		
		PowerFlowConvergenceList prlist = new PowerFlowConvergenceList(_hotislands.length);
		
		for(int iiter=0; iiter < _Itermax && nconv; ++iiter)
		{
			{
				float[] dp = _bp.solve(mm[0], vm, pbus);
				for(int[] blist : pbus)
				{
					for(int b : blist)
						va[b] += dp[b];
				}
			}
			
			mm = pcalc.calculateMismatches(va, vm);
			
			nconv = notConverged(mm[0], mm[1], _Ptol, _Qtol, prlist, iiter);

			if (nconv)
			{
				float[] dq = _bpp.solve(mm[1], vm, qbus);
				for (int b : ldbus)
				{
					vm[b] += dq[b];
				}
				mm = pcalc.calculateMismatches(va, vm);
				nconv = notConverged(mm[0], mm[1], _Ptol, _Qtol, prlist, iiter);
			}
		}
		_vm = vm;
		_va = va;
		return prlist;
	}
	
	float[] flatMag(int[] qbus) throws PsseModelException
	{
		float[] vm = new float[_model.getBuses().size()];
		for(int b : qbus) vm[b] = 1f;
		return vm;
	}

	boolean notConverged(float[] pmm, float[] qmm, float ptol, float qtol,
			PowerFlowConvergenceList res, int iter) throws PsseModelException
	{
		boolean nc = false;
		IslandList islands = _model.getIslands();
		for (int i=0; i < res.size(); ++i)
		{
			PowerFlowConvergence pr = res.get(i);
			boolean tnc = notConverged(pmm, qmm, islands.get(i), ptol, qtol, pr); 
			nc |= tnc;
			if (!tnc) pr.setIterationCount(iter+1);
		}
		return nc;
	}

	boolean notConverged(float[] pmm, float[] qmm, Island island, float ptol,
			float qtol, PowerFlowConvergence r) throws PsseModelException
	{
		int[] pq = island.getBusNdxsForType(BusTypeCode.Load);
		int[] pv = island.getBusNdxsForType(BusTypeCode.Gen);
		int worstp = findWorst(pmm, new int[][] { pq, pv }, r);
		int worstq = findWorst(qmm, new int[][] {pq}, r);
		boolean conv = (Math.abs(pmm[worstp]) < ptol) && (Math.abs(qmm[worstq]) < qtol);
		r.setWorstPbus(worstp);
		r.setWorstPmm(pmm[worstp]);
		r.setWorstQbus(worstq);
		r.setWorstQmm(qmm[worstq]);

		r.setConverged(conv);
		return !conv;
	}

	int findWorst(float[] mm, int[][] lists, PowerFlowConvergence r)
	{
		float wval = 0f;
		int wb = -1;
		for (int[] list : lists)
		{
			for (int b : list)
			{
				float am = Math.abs(mm[b]);
				if (am > wval)
				{
					wval = am;
					wb = b;
				}
			}
		}
		return wb;
	}

	void setupHotIslands() throws PsseModelException
	{
		IslandList islands = _model.getIslands();
		int nhot = 0;
		int[] hotisl = new int[islands.size()];
		for(int i=0; i < islands.size(); ++i)
		{
			if (islands.get(i).isEnergized())
				hotisl[nhot++] = i; 
		}
		
		_hotislands = Arrays.copyOf(hotisl, nhot);
	}
	
	public float[] getVA() {return _va;}
	public float[] getVM() {return _vm;}

	void buildMatrices() throws PsseModelException
	{
		LinkNet net = new LinkNet();
		ACBranchList branches = _model.getBranches();
		int nbus = _model.getBuses().size(), nbranch = branches.size();
		net.ensureCapacity(nbus-1, nbranch);
		float[] bselfbp = new float[nbus];
		float[] bbranchbp = new float[nbranch];
		float[] bselfbpp = new float[nbus];
		float[] bbranchbpp = new float[nbranch];
		
		for(Shunt shunt : _model.getShunts())
		{
			if (shunt.isInSvc() && shunt.isSwitchedOn())
				bselfbpp[shunt.getBus().getIndex()] -= shunt.getBpu();
		}
		
		for(ACBranch br : branches)
		{
			if (br.isInSvc())
			{
				int fbus = br.getFromBus().getIndex();
				int tbus = br.getToBus().getIndex();
				int brx = net.findBranch(fbus, tbus);
				if (brx == -1)
				{
					brx = net.addBranch(fbus, tbus);
				}
				Complex z = br.getZ();
				float bbp = 1 / z.im();
				Complex y = z.inv();

				bbranchbp[brx] -= bbp;
				bselfbp[fbus] += bbp;
				bselfbp[tbus] += bbp;
				float bbpp = -y.im();
				bbranchbpp[brx] -= bbpp;
				bselfbpp[fbus] += (bbpp - br.getFromBchg() - br.getBmag());
				bselfbpp[tbus] += (bbpp - br.getToBchg() - br.getBmag());
			}
		}

		int[] pv = _model.getBusNdxForType(BusTypeCode.Gen);
		int[] slack = _model.getBusNdxForType(BusTypeCode.Slack);
		int[] bppbus = Arrays.copyOf(pv, pv.length+slack.length);
		System.arraycopy(slack, 0, bppbus, pv.length, slack.length);
		
		
//		try
//		{
//			File tdir = new File(System.getProperty("java.io.tmpdir"));
//			PrintWriter orgbp = openDebug(tdir, "bp-prep.csv");
//			PrintWriter orgbpp = openDebug(tdir, "bpp-prep.csv");
//			String hdr = "\"p\",\"pndx\",\"q\",\"qndx\",\"bbranch\",\"bself\"";
//			orgbp.println(hdr);
//			orgbpp.println(hdr);
//			BusList buses = _model.getBuses();
//			for (int i = 0; i < net.getBranchCount(); ++i)
//			{
//				int[] nodes = net.getBusesForBranch(i);
//				int fbndx = nodes[0], tbndx = nodes[1];
//				if (fbndx >= 0 && tbndx >= 0)
//				{
//					Bus fb = buses.get(fbndx), tb = buses.get(tbndx);
//					orgbp.format("\"%s\",%d,\"%s\",%d,%f,%f\n", fb.getNAME(),
//							fbndx, tb.getNAME(), tbndx, bbranchbp[i],
//							bselfbp[fbndx]);
//					orgbpp.format("\"%s\",%d,\"%s\",%d,%f,%f\n", fb.getNAME(),
//							fbndx, tb.getNAME(), tbndx, bbranchbpp[i],
//							bselfbpp[fbndx]);
//				}
//			}
//			orgbp.close();
//			orgbpp.close();
//		} catch (IOException ioe)
//		{
//			throw new PsseModelException(ioe);
//		}

		SparseBMatrix prepbp = new SparseBMatrix(net.clone(), slack, bbranchbp, bselfbp);
		_prepbpp = new SparseBMatrix(net, bppbus, bbranchbpp, bselfbpp);
		
		_bp = prepbp.factorize();
		_bpp = _prepbpp.factorize();
	}
	
	public static void main(String[] args) throws Exception
	{
		String uri = null;
		String svstart = "Flat";
		for(int i=0; i < args.length;)
		{
			String s = args[i++].toLowerCase();
			int ssx = 1;
			if (s.startsWith("--")) ++ssx;
			switch(s.substring(ssx))
			{
				case "uri":
					uri = args[i++];
					break;
				case "voltage":
					svstart = args[i++];
					break;
					
			}
		}
		
		VoltageSource vstart = VoltageSource.fromConfig(svstart);

		if (uri == null)
		{
			System.err.format("Usage: -uri model_uri [-voltage flat|realtime]");
			System.exit(1);
		}
		
		PsseModel model = PsseModel.Open(uri);

		File tdir = new File("/tmp");
		File[] list = tdir.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.startsWith("mismatch") && name.endsWith(".csv");
			}
		});
		for (File f : list) f.delete();
		
		
		
		
		FastDecoupledPowerFlow pf = new FastDecoupledPowerFlow(model);
//		pf.dumpMatrices(ddir);
		
		PowerFlowConvergenceList pslist = pf.runPowerFlow(new PowerCalculator(model), vstart);
		
		System.out.println("Island Converged Iterations WorstPBus  Pmm  WorstQBus  Qmm");
		IslandList islands = model.getIslands();
		BusList buses = model.getBuses();
		for(PowerFlowConvergence psol : pslist)
		{
			Island i = islands.get(psol.getIslandNdx());
			System.out.format(" %s    %5s       %2d      %9s %7.2f %9s %7.2f\n",
				i.getObjectName(),
				String.valueOf(psol.getConverged()),
				psol.getIterationCount(),
				buses.get(psol.getWorstPbus()).getObjectName(),
				PAMath.pu2mw(psol.getWorstPmm()),
				buses.get(psol.getWorstQbus()).getObjectName(),
				PAMath.pu2mvar(psol.getWorstQmm()));
				
		}
		File ddir = new File (System.getProperty("java.io.tmpdir"));
		MismatchReport mmr = new MismatchReport(model, ddir);
		PowerCalculator pc = new PowerCalculator(model, mmr);
		pc.calculateMismatches(pf.getVA(), pf.getVM());
		mmr.report("final");
		
	}

	public void dumpMatrices(File tdir) throws IOException, PsseModelException
	{
		dumpMatrix(_bp, tdir, "factbp.csv");
		dumpMatrix(_bpp, tdir, "factbpp.csv");
	}
	
	void dumpMatrix(FactorizedBMatrix b, File tdir, String nm)
			throws IOException, PsseModelException
	{
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
				new File(tdir, nm))));
		b.dump(_model, pw);
		pw.close();
	}

	static PrintWriter openDebug(File tdir, String name) throws IOException
	{
		return new PrintWriter(new BufferedWriter(new FileWriter(new File(tdir, name))));
	}
}
