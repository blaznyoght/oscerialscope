package org.blaznyoght.wave;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Sine {
	
	public static Integer SAMPLE_RATE = 44100;
	
	public static short[] getSineWaveFormShortArray(double freq, int ms) {
		int bufferSize = (int)((ms * SAMPLE_RATE) / 400);
		System.out.println(bufferSize);
		short[] shortArray = new short[bufferSize];
		
		double period = (double)SAMPLE_RATE / freq;
	    for (int i = 0; i < bufferSize; i++) {
	    	double angle = 2.0 * Math.PI * i / period;
			shortArray[i] = ((Double) (Math.sin(angle)*Short.MAX_VALUE)).shortValue();
		}
		return shortArray;
	}
	
	public static ByteBuffer getSineWaveFormByteBuffer(double freq, int ms) {
		
		short[] shortArray = getSineWaveFormShortArray(freq, ms);
		ByteBuffer buffer = ByteBuffer.allocate(shortArray.length*2);
		buffer.order(ByteOrder.BIG_ENDIAN);
		for(int i = 0; i < shortArray.length; ++i) {
			buffer.putShort(shortArray[i]);
		}
		System.out.println(buffer.array().length);
		return buffer;
	}
}
