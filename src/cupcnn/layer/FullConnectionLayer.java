package cupcnn.layer;

import cupcnn.data.Blob;
import cupcnn.data.BlobParams;
import cupcnn.util.MathFunctions;
import cupcnn.Network;

public class FullConnectionLayer extends Layer{
	private Blob w;
	private Blob wGradient;
	private Blob b;
	private Blob bGradient;
	private Blob z;
	
	public FullConnectionLayer(Network network,BlobParams layerParams){
		super(network,layerParams);
	}
	
	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		Blob input = mNetwork.getDatas().get(id-1);
		Blob output = mNetwork.getDatas().get(id);
		//�����ò㹫��output.get3DSize()����Ԫ��ÿ����Ԫ��ǰ����input.get3DSize()����Ԫ����
		w = new Blob(output.get3DSize(),input.get3DSize(),1,1);
		wGradient = new Blob(w.getNumbers(),w.getChannels(),1,1);
		//��������output.getChannels()����Ԫ��ÿ����Ԫ��һ��ƫִ
		b = new Blob(output.get3DSize(),1,1,1);
		bGradient = new Blob(b.getNumbers(),b.getChannels(),1,1);
		//z�Ǹ��м�ֵ�������ʱ��Ҫ�õ���
		z = new Blob(output.getNumbers(),output.get3DSize(),1,1);
		//��ʼ��
		double[] wData = w.getData();
		double[] bData = b.getData();
		//��˹�ֲ���ʼ��w
		MathFunctions.gaussianInitData(wData);
		//������ʼ��b
		MathFunctions.constantInitData(bData, 0.1);
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		Blob input = mNetwork.getDatas().get(id-1);
		Blob output = mNetwork.getDatas().get(id);
		double[] inputData = input.getData();
		double[] outputData = output.getData();
		double[] wData = w.getData();
		double[] bData = b.getData();
		double[] zData = z.getData();
		z.fillValue(0);
		for(int n=0;n<input.getNumbers();n++){
			for(int os=0;os<output.get3DSize();os++){//�ж��ٸ��������ǰ����ж��ٸ���Ԫ
				//��ÿ����Ԫ��Ȩ�����
				for(int is=0;is<input.get3DSize();is++){
					//zData[n*output.get3DSize()+os] ��ʾһ�������еĵ�n���ĵ�os����Ԫ
					zData[n*output.get3DSize()+os] += inputData[n*input.get3DSize()+is]*wData[os*input.get3DSize()+is];
				}
				//ƫִ
				zData[n*output.get3DSize()+os] += bData[os];
				//�����
				if(activationFunc!=null){
					outputData[n*output.get3DSize()+os] = activationFunc.active(zData[n*output.get3DSize()+os]);
				}
			}
		}

	}

	@Override
	public void backward() {
		// TODO Auto-generated method stub
		Blob inputDiff = mNetwork.getDiffs().get(id);
		Blob outputDiff = mNetwork.getDiffs().get(id-1);
		Blob input = mNetwork.getDatas().get(id-1);
		double[] inputData = input.getData();
		double[] inputDiffData = inputDiff.getData();
		double[] outputDiffData = outputDiff.getData();
		double[] wData = w.getData();
		double[] wGradientData = wGradient.getData();
		double[] bGradientData = bGradient.getData();
		double[] zData = z.getData();
		
		//update diff
		//�ȳ˼������ƫ����,���������ǰ������
		assert inputDiff.getSize()==z.getSize():"inputDiff.getSize()==z.getSize() error";
		if(activationFunc != null){
			for(int n=0; n < inputDiff.getNumbers();n++){
				for(int ids = 0; ids < inputDiff.get3DSize(); ids++){
					inputDiffData[n*inputDiff.get3DSize()+ids] *= activationFunc.diffActive(zData[n*inputDiff.get3DSize()+ids]);
				}
			}		
		}

		//update weight
		wGradient.fillValue(0);
		for(int n = 0; n < inputDiff.getNumbers(); n++){
			for(int ids = 0; ids < inputDiff.get3DSize(); ids++){
				for(int is = 0; is < input.get3DSize(); is++){
					//�൱��һ����Ԫ������ÿһ�����ӳ˼�
					wGradientData[ids*input.get3DSize()+is] += inputData[n*input.get3DSize()+is] * inputDiffData[n*inputDiff.get3DSize()+ids];
				}
			}
		}
		//ƽ��
		MathFunctions.dataDivConstant(wGradientData, wGradient.getNumbers());
		
		//update bias
		bGradient.fillValue(0);
		for(int n=0;n<inputDiff.getNumbers();n++){
			for(int bs = 0; bs < bGradient.getSize(); bs++){
				bGradientData[bs] += inputDiffData[n*bGradient.getSize()+bs];
			}
		}

		//ƽ��
		MathFunctions.dataDivConstant(bGradientData, bGradient.getNumbers());
		
		//��󣬳��Ե�ǰ���Ȩ�غ����
		//ÿһ�����=ÿһ����Ԫ����������Ȩ�صĳ˼�
		if(id<=1)return;
		
		for(int n = 0; n < outputDiff.getNumbers();n++){
			for(int ids = 0; ids < inputDiff.get3DSize(); ids++){
				for(int ods = 0; ods < outputDiff.get3DSize(); ods++){
					outputDiffData[n*outputDiff.get3DSize()+ods] += inputDiffData[n*inputDiff.get3DSize()+ids]*wData[ids*w.get3DSize()+ods];
				}
			}
		}	
		
		paramsList.clear();
		paramsList.add(w);
		paramsList.add(b);
		
		gradientList.clear();
		gradientList.add(wGradient);
		gradientList.add(bGradient);

	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "FullConnectionLayer";
	}



}
