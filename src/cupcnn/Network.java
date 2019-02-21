package cupcnn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cupcnn.data.Blob;
import cupcnn.data.BlobParams;
import cupcnn.layer.Conv2dLayer;
import cupcnn.layer.DeepWiseConv2dLayer;
import cupcnn.layer.FullConnectionLayer;
import cupcnn.layer.InputLayer;
import cupcnn.layer.Layer;
import cupcnn.layer.PoolMaxLayer;
import cupcnn.layer.PoolMeanLayer;
import cupcnn.layer.SoftMaxLayer;
import cupcnn.loss.Loss;
import cupcnn.optimizer.Optimizer;
import cupcnn.util.DigitImage;



public class Network{
	public static String MODEL_BEGIN = "BEGIN";
	public static String MODEL_END = "END";
	private List<Blob> datas;
	private List<Blob> diffs;
	private List<Layer> layers;
	private Loss loss;
	private Optimizer optimizer;
	private int batch = 1;
	private int threadNum = 4;
	private float lrAttenuation = 0.8f;
	
	public Network(){
		datas = new ArrayList<Blob>();
		diffs = new ArrayList<Blob>();
		layers = new ArrayList<Layer>();
	}
	
	public int getThreadNum() {
		return threadNum;
	}
	
	public void setThreadNum(int num) {
		threadNum = num;
	}
	/*
	 *��Ӵ����Ĳ�
	 */
	public void addLayer(Layer layer){
		layers.add(layer);
	}
	
	/*
	 * ��ȡdatas
	 */
	public List<Blob> getDatas(){
		return datas;
	}
	/*
	 * ��ȡdiffs
	 */
	public List<Blob> getDiffs(){
		return diffs;
	}
	/*
	 * ��ȡLayers
	 */
	public List<Layer> getLayers(){
		return layers;
	}
	
	public float getLrAttenuation() {
		return lrAttenuation;
	}

	public void setLrAttenuation(float lrAttenuation) {
		this.lrAttenuation = lrAttenuation;
	}
	
	public void setLoss(Loss loss){
		this.loss = loss;
	}

	
	public void setBatch(int batch){
		this.batch = batch;
	}
	
	public int getBatch(){
		return this.batch;
	}
	
	public void setOptimizer(Optimizer optimizer){
		this.optimizer = optimizer;
	}
	
	public void updateW(Blob params,Blob gradient) {
		optimizer.updateW(params, gradient);
	}
	
	public void updateB(Blob params,Blob gradient) {
		optimizer.updateW(params, gradient);
	}
	
	public void prepare(){
		for(int i=0;i<layers.size();i++){
			Blob data = layers.get(i).createOutBlob();
			datas.add(data);
			Blob diff = layers.get(i).createDiffBlob();
			diffs.add(diff);
			layers.get(i).setId(i);
			layers.get(i).prepare();
		}
	}
	
	
	public void forward(){
		for(int i=0;i<layers.size();i++){
			layers.get(i).forward();
		}
	}
	

	
	public void backward(){

		for(int i=layers.size()-1;i>-1;i--){
			layers.get(i).backward();
		}
	}
	
	public float  trainOnce(Blob inputData,Blob labelData){
		float lossValue = 0.0f;
		Layer first = layers.get(0);
		assert first instanceof InputLayer:"input layer error";
		((InputLayer)first).setInputData(inputData);
	
		//ǰ�򴫲�
		forward();

		
		//����������
		lossValue = loss.loss(labelData, datas.get(datas.size()-1));
		//���������diff
		loss.diff(labelData, datas.get(datas.size()-1), diffs.get(diffs.size()-1));
		

		
		//���촫��
		backward();
		
		return lossValue;
	}
	
	public Blob predict(Blob inputData){
		Layer first = layers.get(0);
		assert first instanceof InputLayer:"input layer error";
		((InputLayer)first).setInputData(inputData);
	
		//ǰ�򴫲�
		forward();
		//�������һ�������
		return datas.get(datas.size()-1);
	}
	
	public List<Blob> buildBlobByImageList(List<DigitImage> imageList,int start,int batch,int channel,int height,int width){
		Blob input = new Blob(batch,channel,height,width);
		Blob label = new Blob(batch,getDatas().get(getDatas().size()-1).getWidth());
		label.fillValue(0);
		float[] blobData = input.getData();
		float[] labelData = label.getData();
		for(int i=start;i<(batch+start);i++){
			DigitImage img = imageList.get(i);
			byte[] imgData = img.imageData;
			assert img.imageData.length== input.get3DSize():"buildBlobByImageList -- blob size error";
			for(int j=0;j<imgData.length;j++){
				blobData[(i-start)*input.get3DSize()+j] = (imgData[j]&0xff)/128.0f-1;//normalize and centerlize(-1,1)
			}
			int labelValue = img.label;
			for(int j=0;j<label.getWidth();j++){
				if(j==labelValue){
					labelData[(i-start)*label.getWidth()+j] = 1;
				}
			}
		}
		List<Blob> inputAndLabel = new ArrayList<Blob>();
		inputAndLabel.add(input);
		inputAndLabel.add(label);
		return inputAndLabel;
	}
	
	private int getMaxIndexInArray(double[] data){
		int maxIndex = 0;
		double maxValue = 0;
		for(int i=0;i<data.length;i++){
			if(maxValue<data[i]){
				maxValue = data[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	private int[] getBatchOutputLabel(float[] data){
		int[] outLabels = new int[getDatas().get(getDatas().size()-1).getHeight()];
		int outDataSize = getDatas().get(getDatas().size()-1).getWidth();
		for(int n=0;n<outLabels.length;n++){
			int maxIndex = 0;
			double maxValue = 0;
			for(int i=0;i<outDataSize;i++){
				if(maxValue<data[n*outDataSize+i]){
					maxValue = data[n*outDataSize+i];
					maxIndex = i;
				}	
			}
			outLabels[n] = maxIndex;
		}
		return outLabels;
	}
	
	private void testInner(Blob input,Blob label){
		Blob output = predict(input);
		int[] calOutLabels = getBatchOutputLabel(output.getData());
		int[] realLabels = getBatchOutputLabel(label.getData());
		assert calOutLabels.length == realLabels.length:"network train---calOutLabels.length == realLabels.length error";
		int correctCount = 0;
		for(int kk=0;kk<calOutLabels.length;kk++){
			if(calOutLabels[kk] == realLabels[kk]){
				correctCount++;
			}
		}
		double accuracy = correctCount/(1.0*realLabels.length);
		System.out.println("accuracy is "+accuracy);
	}
	
	
	public void train(List<DigitImage> trainLists,int epoes,List<DigitImage> testLists){
		System.out.println("training...... please wait for a moment!");
		int batch = getBatch();
		float loclaLr = optimizer.getLr();
		float lossValue = 0;
		InputLayer input = (InputLayer) layers.get(0);
		for(int e=0;e<epoes;e++){
			Collections.shuffle(trainLists);
			long start = System.currentTimeMillis();
			for(int i=0;i<=trainLists.size()-batch;i+=batch){
				List<Blob> inputAndLabel = buildBlobByImageList(trainLists,i,batch,
						input.getChannel(),input.getHeight(),input.getWidth());
				float tmpLoss = trainOnce(inputAndLabel.get(0), inputAndLabel.get(1));
				lossValue = (lossValue+tmpLoss)/2;
				if((i/batch)%50==0) {
					System.out.print(".");
				}
			}
			//ÿ��epoe��һ�β���
			System.out.println();
			System.out.println("training...... epoe: "+e+" lossValue: "+lossValue
					+"  "+" lr: "+optimizer.getLr()+"  "+" cost "+(System.currentTimeMillis()-start));
		
			test(testLists);
			
			if(loclaLr>0.0001f){
				loclaLr*=lrAttenuation;
				optimizer.setLr(loclaLr);
			}
		}
	}
	

	
	public void test(List<DigitImage> imgList){
		System.out.println("testing...... please wait for a moment!");
		int batch = getBatch();
		int correctCount = 0;
		int allCount = 0;
		int i = 0;
		InputLayer input = (InputLayer) layers.get(0);
		for(i=0;i<=imgList.size()-batch;i+=batch){
			allCount += batch;
			List<Blob> inputAndLabel = buildBlobByImageList(imgList,i,batch,
					input.getChannel(),input.getHeight(),input.getWidth());
			Blob output = predict(inputAndLabel.get(0));
			int[] calOutLabels = getBatchOutputLabel(output.getData());
			int[] realLabels = getBatchOutputLabel(inputAndLabel.get(1).getData());
			for(int kk=0;kk<calOutLabels.length;kk++){
				if(calOutLabels[kk] == realLabels[kk]){
					correctCount++;
				}
			}
		}
		
		float accuracy = correctCount/(float)allCount;
		System.out.println("test accuracy is "+accuracy+" correctCount "+correctCount+" allCount "+allCount);
	}
	
	public void saveModel(String name){
		System.out.println("begin save model");
		ObjectOutputStream out = null;
	    try {
			out = new ObjectOutputStream(new FileOutputStream(name));
			out.writeUTF(MODEL_BEGIN);
			out.writeInt(layers.size());
			for(int i=0;i<layers.size();i++){
				layers.get(i).saveModel(out);
			}
			out.writeUTF(MODEL_END);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("save model finished");
	}
	
	public void loadModel(String name){
		System.out.println("begin load model");
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(name));
			String begin = in.readUTF();
			if(!begin.equals(MODEL_BEGIN)){
				System.out.println("file format error");
				in.close();
				return;
			}
			int layersSize = in.readInt();
			if(layersSize<=0){
				System.out.println("no layers");
				in.close();
				return;			
			}
			String layerType = null;
			for(int i=0;i<layersSize;i++){
				layerType = in.readUTF();
				if(layerType.equals(InputLayer.TYPE)){
					InputLayer inputLayer = new InputLayer(Network.this);
					inputLayer.loadModel(in);
					layers.add(inputLayer);
				}else if(layerType.equals(DeepWiseConv2dLayer.TYPE)){
					DeepWiseConv2dLayer conv = new DeepWiseConv2dLayer(Network.this);
					conv.loadModel(in);
					layers.add(conv);
				}else if(layerType.equals(Conv2dLayer.TYPE)){
					Conv2dLayer conv = new Conv2dLayer(Network.this);
					conv.loadModel(in);
					layers.add(conv);
				}else if(layerType.equals(FullConnectionLayer.TYPE)){
					FullConnectionLayer fc = new FullConnectionLayer(Network.this);
					fc.loadModel(in);
					layers.add(fc);
				}else if(layerType.equals(PoolMaxLayer.TYPE)){
					PoolMaxLayer pMax = new PoolMaxLayer(Network.this);
					pMax.loadModel(in);
					layers.add(pMax);
				}else if(layerType.equals(PoolMeanLayer.TYPE)){
					PoolMeanLayer pMean = new PoolMeanLayer(Network.this);
					pMean.loadModel(in);
					layers.add(pMean);
				}else if(layerType.equals(SoftMaxLayer.TYPE)){
					SoftMaxLayer softMax = new SoftMaxLayer(Network.this);
					softMax.loadModel(in);
					layers.add(softMax);
				}else{
					System.out.println("load model error");
					System.exit(-1);
				}
			}
			String end = in.readUTF();
			if(!end.equals(MODEL_END)){
				System.out.println("end is "+end+" file format error");
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("load model finished");
	}
}
