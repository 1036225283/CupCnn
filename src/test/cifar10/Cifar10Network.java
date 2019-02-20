package test.cifar10;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cupcnn.Network;
import cupcnn.active.ReluActivationFunc;
import cupcnn.active.SigmodActivationFunc;
import cupcnn.active.TanhActivationFunc;
import cupcnn.data.Blob;
import cupcnn.data.BlobParams;
import cupcnn.layer.Conv2dLayer;
import cupcnn.layer.FullConnectionLayer;
import cupcnn.layer.InputLayer;
import cupcnn.layer.PoolMaxLayer;
import cupcnn.layer.PoolMeanLayer;
import cupcnn.layer.SoftMaxLayer;
import cupcnn.loss.CrossEntropyLoss;
import cupcnn.loss.LogLikeHoodLoss;
import cupcnn.loss.MSELoss;
import cupcnn.optimizer.Optimizer;
import cupcnn.optimizer.SGDMOptimizer;
import cupcnn.optimizer.SGDOptimizer;
import cupcnn.util.DigitImage;

public class Cifar10Network {
	Network network;
	SGDOptimizer optimizer;
	private void buildFcNetwork(){
		//��network��������
		InputLayer layer1 = new InputLayer(network,32,32,3);
		network.addLayer(layer1);
		FullConnectionLayer layer2 = new FullConnectionLayer(network,32*32*3,300);
		layer2.setActivationFunc(new ReluActivationFunc());
		network.addLayer(layer2);
		FullConnectionLayer layer3 = new FullConnectionLayer(network,300,30);
		layer3.setActivationFunc(new ReluActivationFunc());
		network.addLayer(layer3);
		FullConnectionLayer layer4 = new FullConnectionLayer(network,30,10);
		layer4.setActivationFunc(new ReluActivationFunc());
		network.addLayer(layer4);
		SoftMaxLayer sflayer = new SoftMaxLayer(network,10);
		network.addLayer(sflayer);
	}
	
	private void buildConvNetwork(){
		InputLayer layer1 = new InputLayer(network,32,32,3);
		network.addLayer(layer1);
		
		Conv2dLayer conv1 = new Conv2dLayer(network,32,32,3,6,5,1);
		conv1.setActivationFunc(new ReluActivationFunc());
		network.addLayer(conv1);
		
		PoolMaxLayer pool1 = new PoolMaxLayer(network,32,32,6,2,2);
		network.addLayer(pool1);
		
		Conv2dLayer conv2 = new Conv2dLayer(network,16,16,6,24,3,1);
		conv2.setActivationFunc(new ReluActivationFunc());
		network.addLayer(conv2);
		
		PoolMeanLayer pool2 = new PoolMeanLayer(network,16,16,24,2,2);
		network.addLayer(pool2);
		
		FullConnectionLayer fc1 = new FullConnectionLayer(network,8*8*24,512);
		fc1.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc1);
		
		FullConnectionLayer fc2 = new FullConnectionLayer(network,512,64);
		fc2.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc2);

		FullConnectionLayer fc3 = new FullConnectionLayer(network,64,10);
		fc3.setActivationFunc(new ReluActivationFunc());
		network.addLayer(fc3);
		
		SoftMaxLayer sflayer = new SoftMaxLayer(network,10);
		network.addLayer(sflayer);
		
	}
	public void buildNetwork(int numOfTrainData){
		//���ȹ�����������󣬲����ò���
		network = new Network();
		network.setThreadNum(8);
		network.setBatch(100);
		//network.setLoss(new LogLikeHoodLoss());
		//network.setLoss(new CrossEntropyLoss());
		network.setLoss(new MSELoss());
		optimizer = new SGDOptimizer(0.00f);
		network.setOptimizer(optimizer);
		
		//buildFcNetwork();
		buildConvNetwork();

		network.prepare();
	}
	
	public void train(List<DigitImage> trainLists,int epoes,List<DigitImage> testLists) {
		network.train(trainLists, epoes, testLists);
	}
	
	public void test(List<DigitImage> imgList) {
		network.test(imgList);
	}
	
	public void saveModel(String name){
		network.saveModel(name);
	}
	
	public void loadModel(String name){
		network = new Network();
		network.loadModel(name);
		network.prepare();
	}
}
