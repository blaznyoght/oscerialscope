package org.blaznyoght.sound;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.blaznyoght.graph.WavGraphTest;
import org.blaznyoght.oscerialscope.utils.MathUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class WavToDuino {

	public static void main(String[] args) {
		try (
				InputStream is = WavGraphTest.class.getClassLoader().getResourceAsStream("test.wav");
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
				PrintStream writer = new PrintStream(new FileOutputStream(new File("/Users/blaz/sample.cpp")));
			) {
			
			AudioFormat sourceAudioFormat = audioStream.getFormat();
			
			System.out.println(sourceAudioFormat);
			
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len;
			while ((len = audioStream.read(buffer)) > 0) {
				baos.write(buffer, 0, len);
			}

			XYSeriesCollection dataSet = new XYSeriesCollection();
			XYSeries serieWav = new XYSeries("Serie Wav");
			XYSeries serieDuino = new XYSeries("Serie Duino");
			
			ByteBuffer inputByteBuffer = ByteBuffer.wrap(baos.toByteArray());
			inputByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			System.out.println(inputByteBuffer.capacity());
			
			ByteBuffer outputByteBuffer = ByteBuffer.allocate(inputByteBuffer.capacity());
			for(int i = 0; i < inputByteBuffer.capacity(); i+=2) {
				Short inS = inputByteBuffer.getShort(i);
				Short outS = MathUtils.map(inS, Short.MIN_VALUE, Short.MAX_VALUE, 1, 4095).shortValue();
				outputByteBuffer.putShort(outS);
				if (i < 5) {
					System.out.println(String.format("Input: 0x%x\nOutput: 0x%x", inS, outS));
				}
				
				serieWav.add(i, inS);
				serieDuino.add(i, outS);
				
			}
			
			dataSet.addSeries(serieWav);
			dataSet.addSeries(serieDuino);
			JFreeChart chart = ChartFactory.createXYLineChart("Test", "time (ms)",
					"V (mV)", dataSet);

			ChartFrame frame = new ChartFrame("Results", chart);
			frame.pack();
			frame.setVisible(true);

			writer.println("PROGMEM uint16_t wav[] = {");
			int bufLen = outputByteBuffer.capacity();
			for(int i = 0; i < bufLen-2; i+=2) {
				writer.print(String.format("0x%x, ", outputByteBuffer.getShort(i)));
				if ((i % 51) == 50) {
					writer.print("\n\t");
				}
			}
			writer.println(String.format("0x%x, ", outputByteBuffer.getShort(bufLen-2)));
			writer.println("};\n");
			
			writer.println("\n");
			writer.println(String.format("int wavLength = %d;", bufLen/2));
			
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
}
