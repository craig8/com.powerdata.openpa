package com.powerdata.openpa;

import com.powerdata.openpa.TwoTermDev.Side;
import com.powerdata.openpa.impl.TransformerListI;

public interface TransformerList extends ACBranchListIfc<Transformer>
{

	static final TransformerList Empty = new TransformerListI();
	
	boolean isRegEnabled(int ndx) throws PAModelException;
	void setRegEnabled(int ndx, boolean enabl) throws PAModelException;
	boolean[] isRegEnabled() throws PAModelException;
	void setRegEnabled(boolean[] enabl) throws PAModelException;
	Side getRegSide(int ndx) throws PAModelException;
	Side[] getRegSide() throws PAModelException;
	void setRegSide(int ndx, Side s) throws PAModelException;
	void setRegSide(Side[] s) throws PAModelException;
	float getMinKV(int ndx) throws PAModelException;
	void setMinKV(int ndx, float kv) throws PAModelException;
	float[] getMinKV() throws PAModelException;
	void setMinKV(float[] kv) throws PAModelException;
	float getMaxKV(int ndx) throws PAModelException;
	void setMaxKV(int ndx, float kv) throws PAModelException;
	float[] getMaxKV() throws PAModelException;
	void setMaxKV(float[] kv) throws PAModelException;
	Bus getRegBus(int ndx) throws PAModelException;
	void setRegBus(int ndx, Bus b) throws PAModelException;
	Bus[] getRegBus() throws PAModelException;
	void setRegBus(Bus[] b) throws PAModelException;
	boolean hasLTC(int ndx) throws PAModelException;
	boolean[] hasLTC() throws PAModelException;
	void setHasLTC(int ndx, boolean b) throws PAModelException;
	void setHasLTC(boolean[] b) throws PAModelException;
	float getFromMinTap(int ndx) throws PAModelException;
	void setFromMinTap(int ndx, float a) throws PAModelException;
	float[] getFromMinTap() throws PAModelException;
	void setFromMinTap(float[] a) throws PAModelException;
	float getToMinTap(int ndx) throws PAModelException;
	float[] getToMinTap() throws PAModelException;
	void setToMinTap(int ndx, float a) throws PAModelException;
	void setToMinTap(float[] a) throws PAModelException;
	float getFromMaxTap(int ndx) throws PAModelException;
	float[] getFromMaxTap() throws PAModelException;
	void setFromMaxTap(int ndx, float a) throws PAModelException;
	void setFromMaxTap(float[] a) throws PAModelException;
	float getToMaxTap(int ndx) throws PAModelException;
	float[] getToMaxTap() throws PAModelException;
	void setToMaxTap(int ndx, float a) throws PAModelException;
	void setToMaxTap(float[] a) throws PAModelException;
	float getFromStepSize(int ndx) throws PAModelException;
	float[] getFromStepSize() throws PAModelException;
	void setFromStepSize(int ndx, float step) throws PAModelException;
	void setFromStepSize(float[] step) throws PAModelException;
	float getToStepSize(int ndx) throws PAModelException;
	float[] getToStepSize() throws PAModelException;
	void setToStepSize(int ndx, float step) throws PAModelException;
	void setToStepSize(float[] step) throws PAModelException;
}
