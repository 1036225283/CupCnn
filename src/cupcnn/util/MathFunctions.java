/*
 * 
 * 
 */

package cupcnn.util;
import java.util.Random;

import cupcnn.data.Blob;

public class MathFunctions {
	
	public static void gaussianInitData(double[] data){
		Random random = new Random();
		for(int i=0;i<data.length;i++){
			data[i] = random.nextGaussian();
		}
	}
	
	public static void constantInitData(double[] data,double value){
		for(int i=0;i<data.length;i++){
			data[i] = value;
		}
	}
	
	public static void randomInitData(double[] data){
		Random random = new Random();
		for(int i=0;i<data.length;i++){
			data[i] = random.nextDouble();
		}
	}
	
	public static void dataDivConstant(double[] data,double constant){
		for(int i=0; i<data.length;i++){
			data[i] /= constant;
		}
	}
	
	public static void convolutionBlobSame(Blob input,Blob kernel,Blob bias,Blob output){
		double[] inputData = input.getData();
		double[] kernelData = kernel.getData();
		double[] outputData = output.getData();
		double[] biasData = bias.getData();
		
		for(int n=0;n<output.getNumbers();n++){
			for(int c=0;c<output.getChannels();c++){
				for(int h=0;h<output.getHeight();h++){
					for(int w=0;w<output.getWidth();w++){
						//�ȶ�λ�������λ��
						//Ȼ�����kernel,ͨ��kernel��λ�����λ��
						//Ȼ���������kernel
						int inStartX = w - kernel.getWidth()/2;
						int inStartY = h - kernel.getHeight() / 2;
						//�;���˳˼�
						for(int kh=0;kh<kernel.getHeight();kh++){
							for(int kw=0;kw<kernel.getWidth();kw++){
								int inY = inStartY + kh;
								int inX = inStartX + kw;
								if (inY >= 0 && inY < input.getHeight() && inX >= 0 && inX < input.getWidth()){
									int inputChannelIndex = c/(output.getChannels()/input.getChannels());
									outputData[output.getIndexByParams(n,c,h,w)] += kernelData[kernel.getIndexByParams(0,c,kh,kw)]*
											inputData[input.getIndexByParams(n,inputChannelIndex,inY,inX)];
								}
							}
						}
						
						//��ƫ��
						outputData[output.getIndexByParams(n,c,h,w)] += biasData[bias.getIndexByParams(0, c, 0, 0)];
					}
				}
			}
		}
	}

	public static void convolutionBlobSame(Blob input,Blob kernel,Blob output){
		double[] inputData = input.getData();
		double[] kernelData = kernel.getData();
		double[] outputData = output.getData();
		
		for(int n=0;n<output.getNumbers();n++){
			for(int c=0;c<output.getChannels();c++){
				for(int h=0;h<output.getHeight();h++){
					for(int w=0;w<output.getWidth();w++){
						//�ȶ�λ�������λ��
						//Ȼ�����kernel,ͨ��kernel��λ�����λ��
						//Ȼ���������kernel
						int inStartX = w - kernel.getWidth()/2;
						int inStartY = h - kernel.getHeight() / 2;
						//�;���˳˼�
						for(int kc=0;kc<(input.getChannels()/output.getChannels());kc++){
							for(int kh=0;kh<kernel.getHeight();kh++){
								for(int kw=0;kw<kernel.getWidth();kw++){
									int inY = inStartY + kh;
									int inX = inStartX + kw;
									if (inY >= 0 && inY < input.getHeight() && inX >= 0 && inX < input.getWidth()){
										int inputChannelIndex = c*(input.getChannels()/output.getChannels())+kc;
										outputData[output.getIndexByParams(n,c,h,w)] += kernelData[kernel.getIndexByParams(0,inputChannelIndex,kh,kw)]*
												inputData[input.getIndexByParams(n,inputChannelIndex,inY,inX)];
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static Blob rotate180Blob(Blob input){
		Blob output = new Blob(input.getNumbers(),input.getChannels(),input.getHeight(),input.getWidth());
		double[] inputData = input.getData();
		double[] outputData = output.getData();
		/*
		 * ��ת180�Ⱦ������µߵ���ͬʱ���Ҿ���
		 */
		for(int n=0;n<output.getNumbers();n++){
			for(int c=0;c<output.getChannels();c++){
				for(int h=0;h<output.getHeight();h++){
					for(int w=0;w<output.getWidth();w++){
						outputData[output.getIndexByParams(n, c, h, w)] = inputData[input.getIndexByParams(n, c, input.getHeight()-h-1, input.getWidth()-w-1)];
					}
				}
			}
		}
		return output;
	}
}
