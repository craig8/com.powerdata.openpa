package com.powerdata.openpa.pwrflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.powerdata.openpa.ACBranch;
import com.powerdata.openpa.ACBranchList;
import com.powerdata.openpa.ACBranchListIfc;
import com.powerdata.openpa.ListMetaType;
import com.powerdata.openpa.PAModel;
import com.powerdata.openpa.PAModelException;
import com.powerdata.openpa.impl.GroupMap;
import com.powerdata.openpa.pwrflow.ACBranchExtList.ACBranchExt;
import com.powerdata.openpa.tools.LinkNet;

/**
 * Associate the network adjacency with PAModel / ACBranches.
 * 
 * This is a specialized subclass that adds ACBranches from a list. Parallel
 * branches are merged, and the merge operations are tracked in order to map
 * both directions
 * 
 * @author chris@powerdata.com
 *
 */
public class ACBranchAdjacencies extends LinkNet
{
//	int[] _edgendx = new int[0];
//	int _edgecnt = 0;
	List<ACBranchExtList<? extends ACBranchExt>> _lists = new ArrayList<>();
//	int[] _brcnt;
	GroupMap _gmap;
	
	public ACBranchAdjacencies() {}
	
	public ACBranchAdjacencies(ACBranchAdjacencies src)
	{
		super(src);
//		_keys = src._keys.clone();
//		_edgendx = src._edgendx.clone();
	}
	
	
//	@Override
//	public void ensureCapacity(int maxBusNdx, int branchCount)
//	{
//		super.ensureCapacity(maxBusNdx, branchCount);
//		int len = _next.length/2;
//		if (_edgendx.length < len)
//		{
////			_keys = Arrays.copyOf(_keys, len);
//			_edgendx = Arrays.copyOf(_edgendx, len);
//		}
//	}

	public void addBranches(Collection<ACBranchExtList<? extends ACBranchExt>> lists) throws PAModelException
	{
		int nbr = lists.stream().mapToInt(i -> i.size()).reduce(0, (i,j) -> i+j);
		ensureCapacity(0, nbr*2);
//		_brcnt = new int[lists.size()];
//		int brtot = 0;
		
		int[] map = new int[nbr];
		int nm = 0;
		Arrays.fill(map, -1);
		
		for (ACBranchExtList<? extends ACBranchExt> list : lists)
		{
			_lists.add(list);

			for (ACBranchExt b : list)
			{
				int fn = b.getFromBus().getIndex(), 
						tn = b.getToBus().getIndex(),
						br = findBranch(fn, tn);
				if (br == Empty)
				{
					br = addBranch(fn, tn);
				}
				// _keys[br] = makekey(_tval, b.getIndex());
//				_edgendx[_edgecnt++] = br;
				map[nm++] = br;
			}
		}
		
		_gmap = new GroupMap(map, lists.size());
		// ++_tval;
	}
	
//	private long makekey(int tv, int ndx)
//	{
//		return (((long) tv) << 32L) | (long) ndx;
//	}
//
	public ACBranchExt[] getBranches(int brndx)
	{
		int[] b = _gmap.get(brndx);
		int n = b.length;
		Arrays.sort(b);
		ACBranchExt[] rv = new ACBranchExt[n];
		for(int i=0, j=0; i < n; ++i)
		{
			int x = b[i];
			ACBranchExtList<? extends ACBranchExt> l = _lists.get(j++);
			while (x >= l.size())
			{
				x -= l.size();
				l = _lists.get(j++);
			}
			rv[i] = l.get(x);
		}
		return rv;
	}

}
