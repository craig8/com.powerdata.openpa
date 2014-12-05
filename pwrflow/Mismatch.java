package com.powerdata.openpa.pwrflow;

import java.util.HashSet;
import java.util.Set;
import com.powerdata.openpa.Bus;
import com.powerdata.openpa.Island;

/**
 * Manage mismatches
 * 
 * 
 * @author chris@powerdata.com
 *
 */
public class Mismatch
{
	float _wval=Float.MAX_VALUE;
	float[] _mm;
	int _wndx=-1;
	BusRefIndex _bri;
	Set<int[]> _bndx = new HashSet<>();
	
	/**
	 * Create new mismatch.
	 * @param nbus number of buses to manage
	 */
	public Mismatch(BusRefIndex bri, BusTypeUtil btu, Island island,
			Set<BusType> bustypes)
	{
		_bri = bri;
		_mm = new float[bri.getBuses().size()];
		Set<int[]> s = new HashSet<>();
		for (BusType t : bustypes)
		{
			int[] b = btu.getBuses(t, island.getIndex());
			s.add(b);
		}
	}	
	/**
	 * Return the mismatch at a bus (index)
	 * @param ndx Bus index for mismatch value
	 * @return Mismatch value
	 */
	public float get(int ndx) {return _mm[ndx];}
	/**
	 * Access the entire mismatch array
	 * 
	 * @return mismatch array
	 */
	public float[] get() {return _mm;}
	
	public void add(int ndx, float flow) {_mm[ndx] += flow;}
	
	/**
	 * Find the worst mismatch for the set of buses configured
	 * @return worst mismatch value
	 */
	public float test()
	{
		for(int[] set : _bndx)
		{
			for(int i : set)
			{
				float v = _mm[i];
				if (Math.abs(_wval) > Math.abs(v))
				{
					_wval = v;
					_wndx = i;
				}
			}
		}
		return _wval;
	}
	
	/**
	 * get the index where the worst mismatch occurs
	 * @return index of worst mismatch.  -1 if no 
	 */
	Bus getWorstBus() {return (_wndx < 0) ? null : _bri.getBuses().get(_wndx);}
	/**
	 * get worst index value
	 * @return worst index value
	 */
	float getWorstMismatch() {return _wval;}
	
	public BusRefIndex getBusRefIndex() {return _bri;}
}
