package cupcnn.layer;

import cupcnn.Network;
import cupcnn.data.Blob;
/*
 * InputLayer��Ҫ������ռ�ݵ�һ��λ�ã��ǵķ��򴫲����㷨������ʵ��
 */
import cupcnn.data.BlobParams;

public class InputLayer extends Layer{

	public InputLayer(Network network, BlobParams parames) {
		super(network, parames);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "InputLayer";
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		//do nothing
	}

	@Override
	public void backward() {
		// TODO Auto-generated method stub
		//do nothing
	}
	
	public void setInputData(Blob input){
		Blob curData = mNetwork.getDatas().get(id);
		input.cloneTo(curData);
	}
}
