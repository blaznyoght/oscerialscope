package org.blaznyoght.graph;

import java.nio.ByteBuffer;

import org.blaznyoght.wave.Sine;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Test;

public class GraphTest {

	@Test
	public void testGraph() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		XYSeries series = new XYSeries("Vout");
		for (int i = 0; i < 1000; ++i) {
			series.add(i, Math.sin(2 * Math.PI * i));
		}
		dataSet.addSeries(series);
		JFreeChart chart = ChartFactory.createXYLineChart("Test", "time (ms)",
				"V (mV)", dataSet);

		ChartFrame frame = new ChartFrame("Results", chart);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		XYSeries serieBB = new XYSeries("ByteBuffer");
		XYSeries serieSh = new XYSeries("ShortArray");

		ByteBuffer buffer = Sine.getSineWaveFormByteBuffer(800, 10);
		for(int i = 0; i < buffer.capacity()*2; i+=2) {
			serieBB.add(i/2, buffer.getShort(i%buffer.capacity()));
		}
		System.out.println(String.format("Serie BB size : %d", serieBB.getItemCount()));

		short[] sArray = Sine.getSineWaveFormShortArray(800, 10);
		for (int j = 0; j < sArray.length*2; ++j) {
			serieSh.add(j, sArray[j%sArray.length]);
		}
		System.out.println(String.format("Serie Sh size : %d", serieSh.getItemCount()));

		dataSet.addSeries(serieBB);
		dataSet.addSeries(serieSh);
		JFreeChart chart = ChartFactory.createXYLineChart("Test", "time (ms)",
				"V (mV)", dataSet);

		ChartFrame frame = new ChartFrame("Results", chart);
		frame.pack();
		frame.setVisible(true);
	}
}
