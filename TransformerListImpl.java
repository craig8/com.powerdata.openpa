package com.powerdata.openpa;

public class TransformerListImpl extends TransformerBaseListImpl<Transformer> implements TransformerList
{
	public static final TransformerList	Empty	= new TransformerListImpl();

	public TransformerListImpl() {super();}
	
	public TransformerListImpl(PAModel model, int[] keys)
	{
		super(model, keys);
	}
	public TransformerListImpl(PAModel model, int size)
	{
		super(model, size);
	}

	@Override
	public Transformer get(int index)
	{
		return new Transformer(this, index);
	}

}
