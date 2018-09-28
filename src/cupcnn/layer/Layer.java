package cupcnn.layer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cupcnn.data.Blob;
import cupcnn.data.BlobParams;
import cupcnn.Network;
import cupcnn.active.ActivationFunc;

public abstract class Layer{
	protected int id;
	protected Network mNetwork;
	protected ActivationFunc activationFunc;
	protected BlobParams layerParams;
	//BlobParams�е��ĸ�����˵��
	//��һ����batch,����һ���������ж��ٸ�ͼƬ
	//�ڶ�����channel,һ��ͼƬ�ж��ٸ�ͨ��
	//��������ͼƬ�ĸ�
	//���ĸ���ͼƬ�Ŀ�
	public Layer(Network network,BlobParams parames){
		this.mNetwork = network;
		this.layerParams = parames;
		paramsListW = new ArrayList<Blob>();
		gradientListW = new ArrayList<Blob>();
		paramsListB = new ArrayList<Blob>();
		gradientListB = new ArrayList<Blob>();
	}
	
	public BlobParams getLayerParames(){
		return layerParams;
	}
	
	public void setId(int id){
		this.id = id;
	}
	public int getId(){
		return id;
	}
	public void setActivationFunc(ActivationFunc func){
		this.activationFunc = func;
	}
	public List<Blob> getParamsWList(){
		return paramsListW;
	}
	
	public List<Blob> getGradientWList(){
		return gradientListW;
	}
	public List<Blob> getParamsBList(){
		return paramsListB;
	}
	
	public List<Blob> getGradientBList(){
		return gradientListB;
	}	
	//����
	abstract public String getType();

	//׼������
	abstract public void prepare();
	
	//ǰ�򴫲��ͷ��򴫲�
	abstract public void forward();
	abstract public void backward();
	
	//���������װ��
	abstract public void saveModel(ObjectOutputStream out);
	abstract public void loadModel(ObjectInputStream in);
	
	//�������²���
	protected List<Blob> paramsListW;
	protected List<Blob> gradientListW;
	protected List<Blob> paramsListB;
	protected List<Blob> gradientListB;
}