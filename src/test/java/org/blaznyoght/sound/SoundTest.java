package org.blaznyoght.sound;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.blaznyoght.wave.Sine;
import org.junit.Test;

public class SoundTest {

	@Test
	public void test() throws LineUnavailableException {
		
		AudioFormat format = new AudioFormat(Sine.SAMPLE_RATE, 16, 1, true, true);
		SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(format);
		System.out.println(sourceDataLine);
		
		ByteBuffer buffer = Sine.getSineWaveFormByteBuffer(400, 100);

		sourceDataLine.open();
		int j = 0;
		while(j<10000) {
			sourceDataLine.write(buffer.array(), 0, buffer.array().length);
			sourceDataLine.flush();
			if (j==0) {
				sourceDataLine.start();
			}
			j++;
		}
		sourceDataLine.drain();
		sourceDataLine.stop();
	}

}
