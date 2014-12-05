package com.powerdata.openpa.pwrflow;

import com.powerdata.openpa.PAModelException;
import com.powerdata.openpa.pwrflow.ACBranchExtList.ACBranchExt;

public interface ACBranchFlows extends ACBranchExtList<com.powerdata.openpa.pwrflow.ACBranchFlows.ACBranchFlow>
{
	public static class ACBranchFlow extends ACBranchExt
	{
		ACBranchFlows _list;
		public ACBranchFlow(ACBranchFlows list, int index)
		{
			super(list, index);
			_list = list;
		}
		public float getFromPpu() {return _list.getFromPpu(_ndx);}
		public float getFromQpu() {return _list.getFromQpu(_ndx);}
		public float getToPpu() {return _list.getToPpu(_ndx);}
		public float getToQpu() {return _list.getToQpu(_ndx);}
	}

	float getFromPpu(int ndx);

	float getToQpu(int ndx);

	float getToPpu(int ndx);

	float getFromQpu(int ndx);
	
	ACBranchFlows calc(float[] vmpu, float[] varad) throws PAModelException;
	
	void applyMismatches(Mismatch pmm, Mismatch qmm) throws PAModelException;
	
}
