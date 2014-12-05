package com.powerdata.openpa.pwrflow;

import java.util.List;
import com.powerdata.openpa.tools.Complex;

public interface ACBranchYMtrxList extends List<com.powerdata.openpa.pwrflow.ACBranchYMtrxList.ACBranchYMtrx>
{
	public static class ACBranchYMtrx
	{

		private int _ndx;
		private ACBranchYMtrxList _list;

		public ACBranchYMtrx(ACBranchYMtrxList list, int index)
		{
			_ndx = index;
			_list = list;
		}
		
		public Complex getFromSelfY() {return _list.getFromSelfY(_ndx);}
		public Complex getToSelfY() {return _list.getToSelfY(_ndx);}
		public Complex getTransferY() {return _list.getTransferY(_ndx);}
		public float getFromSelfB() {return _list.getFromSelfB(_ndx);}
		public float getToSelfB() {return _list.getToSelfB(_ndx);}
		public float getTransferB() {return _list.getTransferB(_ndx);}
	}

	float getTransferB(int ndx);
	float getToSelfB(int ndx);
	float getFromSelfB(int ndx);
	Complex getFromSelfY(int ndx);
	Complex getTransferY(int ndx);
	Complex getToSelfY(int ndx);	

}
