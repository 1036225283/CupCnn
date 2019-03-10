package cupcnn.layer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cupcnn.Network;
import cupcnn.active.ActivationFunc;
import cupcnn.active.TanhActivationFunc;
import cupcnn.data.Blob;
import cupcnn.util.MathFunctions;

/* Computes the following operations:
 * y(t-1)    y(t)
 *   ^        ^
 *   |V+c     | V+c
 * h(t-1) -> h(t)
 *   ^ +b W   ^ +b
 *   |U       |U
 * x(t-1)    x(t)
 *
 * h(t) = tanh(b + W*h(t-1) + U*x(t)) (1)
 * y(t) = c + V*h(t)                  (2)
*/ 
public class RnnCell extends Cell{
	private int inSize;
	private int outSize;
	private int batch;
	private Network mNetwork;
	private Blob Ht_1;
	private Blob U;
	private Blob UW;
	private Blob W;
	private Blob WW;
	private Blob V; 
	private Blob VW;
	private Blob bias;
	private Blob biasW;
	private Blob c;
	private Blob cW;
	private Blob z;
	private ActivationFunc activeFunc;

	public RnnCell(Network network) {
		super(network);
		// TODO Auto-generated constructor stub
	}
	
	public RnnCell(Network network,Layer recurrentLayer,int inSize,int outSize) {
		super(network);
		// TODO Auto-generated constructor stub
		this.inSize = inSize;
		this.outSize = outSize;
		this.batch = network.getBatch();
		this.mNetwork = network;
		this.activeFunc = new TanhActivationFunc();
	}
	

	@Override
	public Blob createOutBlob() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Blob createDiffBlob() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		//�����ò㹫��outSize����Ԫ��ÿ����Ԫ��ǰ����inSize����Ԫ����
		U = new Blob(inSize,outSize);
		UW = new Blob(inSize,outSize);

		//�����ò���outSize����Ԫ��ÿ����Ԫ��һ��ƫִ
		bias = new Blob(outSize);
		biasW = new Blob(outSize);
		
		W = new Blob(outSize,outSize);
		WW = new Blob(outSize,outSize);
		
		V = new Blob(outSize,outSize);
		VW = new Blob(outSize,outSize);
		c = new Blob(outSize);
		cW = new Blob(outSize);

		//��ʼ��
		//��˹�ֲ���ʼ��
		MathFunctions.gaussianInitData(U.getData());
		MathFunctions.gaussianInitData(W.getData());
		MathFunctions.gaussianInitData(V.getData());
		//������ʼ��b
		MathFunctions.constantInitData(bias.getData(), 0.0f);
		MathFunctions.constantInitData(c.getData(), 0.0f);
		//z�Ǹ��м�ֵ�������ʱ��Ҫ�õ���
		z = new Blob(mNetwork.getBatch(),outSize);
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		
	}

	public void forward(Blob in,Blob out) {
		
	}
	
	@Override
	public void backward() {
		// TODO Auto-generated method stub
		
	}
	
	public void backward(Blob in,Blob out,Blob inDiff,Blob outDiff) {
		
	}

	@Override
	public void saveModel(ObjectOutputStream out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadModel(ObjectInputStream in) {
		// TODO Auto-generated method stub
		
	}

}
