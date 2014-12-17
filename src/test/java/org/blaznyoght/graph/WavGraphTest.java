package org.blaznyoght.graph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.blaznyoght.wave.Sine;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class WavGraphTest {
	
	public static void main(String[] args) {
		InputStream is = WavGraphTest.class.getClassLoader().getResourceAsStream("test.wav");
		try {
			AudioInputStream audioStream = AudioSystem
					.getAudioInputStream(is);
			AudioFormat audioFormat = audioStream.getFormat();
			System.out.println(String.format("%s", audioFormat));

			AudioFormat format = new AudioFormat(Sine.SAMPLE_RATE, 16, 1, true, false);
			SourceDataLine sourceLine = AudioSystem.getSourceDataLine(format);
			sourceLine.open();
			sourceLine.start();

			XYSeriesCollection dataSet = new XYSeriesCollection();
			XYSeries serieWav = new XYSeries("WavGraphTest");
			
	        int nBytesRead = 0;
	        int globalCount = 0;
	        byte[] abData = new byte[128000];
	        while (nBytesRead != -1) {
	        	ByteBuffer buffer = ByteBuffer.wrap(abData);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				for(int i = 0; i < buffer.capacity(); i+=2) {
					serieWav.add(globalCount, buffer.getShort(i));
					globalCount++;
				}
	            try {
	                nBytesRead = audioStream.read(abData, 0, abData.length);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            if (nBytesRead >= 0) {
	                @SuppressWarnings("unused")
	                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
	            }
	        }

	        sourceLine.drain();
	        sourceLine.close();
			
			dataSet.addSeries(serieWav);
			JFreeChart chart = ChartFactory.createXYLineChart("Test", "time (ms)",
					"V (mV)", dataSet);

			ChartFrame frame = new ChartFrame("Results", chart);
			frame.pack();
			frame.setVisible(true);
			
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
