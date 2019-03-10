package cupcnn.layer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cupcnn.Network;
import cupcnn.data.Blob;

public class RecurrentLayer extends Layer {
	Network mNetwork;
	Cell mCell;
	int seqLen;
	int batch;
	int inSize;
	int outSize;
	
	enum RecurrentType{
		RNN,
		LSTM,
		GRU
	}
	
	RecurrentType type;

	public RecurrentLayer(Network network) {
		super(network);
		// TODO Auto-generated constructor stub
	}
	
	public RecurrentLayer(Network network,RecurrentType type,int seqLen,int inSize,int outSize) {
		super(network);
		this.mNetwork = network;
		this.batch = network.getBatch();
		this.inSize = inSize;
		this.outSize = outSize;
		this.type = type;
	}

	@Override
	public Blob createOutBlob() {
		// TODO Auto-generated method stub
		return new Blob(seqLen,batch,outSize);
	}

	@Override
	public Blob createDiffBlob() {
		// TODO Auto-generated method stub
		return new Blob(seqLen,batch,outSize);
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "RecurrentLayer";
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		switch(type) {
		case RNN:
			mCell = new RnnCell(mNetwork);
			break;
		case LSTM:
			break;
		case GRU:
			break;
		}
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		Blob input = mNetwork.getDatas().get(id-1);
		Blob output = mNetwork.getDatas().get(id);
		float[] inputData = input.getData();
		float[] outputData = output.getData();
		Blob tmpIn = new Blob(batch,inSize);
		Blob tmpOut = new Blob(batch,inSize);
		for(int i=0;i<seqLen;i++) {
			float[] tmpInData = tmpIn.getData();
			int tmpInSize = tmpIn.getSize();
			for(int j=0;j<tmpInSize;j++) {
				tmpInData[j] = inputData[seqLen*tmpInSize+j];
			}
			mCell.forward(tmpIn,tmpOut);
			float[] tmpOutData = tmpOut.getData();
			int tmpOutSize = tmpOut.getSize();
			for(int j=0;j<tmpOutSize;j++) {
				outputData[seqLen*tmpOutSize+j] = tmpOutData[j];
			}
		}
	}

	@Override
	public void backward() {
		// TODO Auto-generated method stub
		Blob inputDiff = mNetwork.getDiffs().get(id);
		Blob outputDiff = mNetwork.getDiffs().get(id-1);
		Blob input = mNetwork.getDatas().get(id-1);
		Blob output = mNetwork.getDatas().get(id);
		Blob tmpIn = new Blob(batch,inSize);
		Blob tmpOut = new Blob(batch,inSize);
		Blob tmpInDiff = new Blob(batch,outSize);
		Blob tmpOutDiff = new Blob(batch,inSize);
		float[] inputData = input.getData();
		float[] outputData = output.getData();
		float[] inputDiffData = inputDiff.getData();
		float[] outputDiffData = outputDiff.getData();
		for(int i=0;i<seqLen;i++) {
			//һ��ȡ�����е�һ��
			float[] tmpInData = tmpIn.getData();
			int tmpInSize = tmpIn.getSize();
			for(int j=0;j<tmpInSize;j++) {
				tmpInData[j] = inputData[seqLen*tmpInSize+j];
			}
			float[] tmpOutData = tmpOut.getData();
			int tmpOutSize = tmpOut.getSize();
			for(int j=0;j<tmpOutSize;j++) {
				tmpOutData[j] = outputData[seqLen*tmpOutSize+j];
			}
			float[] tmpInDiffData = tmpInDiff.getData();
			int tmpInDiffSize = tmpIn.getSize();
			for(int j=0;j<tmpInDiffSize;j++) {
				tmpInDiffData[j] = inputData[seqLen*tmpInDiffSize+j];
			}
			mCell.backward(tmpIn,tmpOut,tmpInDiff,tmpOutDiff);
			//������Ľ����˳�򿽱���outputDiffBlob
			float[] tmpOutDiffData = tmpOutDiff.getData();
			int tmpOutDifftSize = tmpOutDiff.getSize();
			for(int j=0;j<tmpOutDifftSize;j++) {
				outputDiffData[seqLen*tmpOutDifftSize+j] = tmpOutDiffData[j];
			}
		}
	}

	@Override
	public void saveModel(ObjectOutputStream out) {
		// TODO Auto-generated method stub
		try {
			out.writeUTF(getType());
			out.writeInt(seqLen);
			out.writeInt(inSize);
			out.writeInt(outSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void loadModel(ObjectInputStream in) {
		// TODO Auto-generated method stub
		try {
			seqLen = in.readInt();
			inSize = in.readInt();
			outSize = in.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
