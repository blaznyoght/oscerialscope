package org.blaznyoght.oscerialscope.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import org.blaznyoght.oscerialscope.utils.MathUtils;

public class SampleSourceFileGenerator {
	private static final short DEFAULT_MIN_VALUE = 1;
	private static final short DEFAULT_MAX_VALUE = 4095;

	private File sourceFile = null;
	private File targetFile = null;

	private short minValue = DEFAULT_MIN_VALUE;
	private short maxValue = DEFAULT_MAX_VALUE;

	public SampleSourceFileGenerator() {
		this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE, null, null);
	}

	public SampleSourceFileGenerator(short defaultMinValue,
			short defaultMaxValue, File sourceFile, File targetFile) {
		this.minValue = DEFAULT_MIN_VALUE;
		this.maxValue = DEFAULT_MAX_VALUE;
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
	}

	public SampleSourceFileGenerator(File sourceFile, File targetFile) {
		this(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE, sourceFile, targetFile);
	}

	public boolean generateFile() throws IOException,
			UnsupportedAudioFileException {
		if ((sourceFile == null) || (targetFile == null)) {
			return false;
		}
		try (PrintStream writer = new PrintStream(new FileOutputStream(
				targetFile));) {

			ByteArrayOutputStream baos = generateBuffer();

			ByteBuffer inputByteBuffer = ByteBuffer.wrap(baos.toByteArray());
			inputByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			ByteBuffer outputByteBuffer = ByteBuffer.allocate(inputByteBuffer
					.capacity());
			for (int i = 0; i < inputByteBuffer.capacity(); i += 2) {
				Short inS = inputByteBuffer.getShort(i);
				Short outS = MathUtils.map(inS, Short.MIN_VALUE,
						Short.MAX_VALUE, minValue, maxValue).shortValue();
				outputByteBuffer.putShort(outS);
			}

			writer.println("PROGMEM uint16_t wav[] = {");
			int bufLen = outputByteBuffer.capacity();
			for (int i = 0; i < bufLen - 2; i += 2) {
				writer.print(String.format("0x%x, ",
						outputByteBuffer.getShort(i)));
				if ((i % 51) == 50) {
					writer.print("\n\t");
				}
			}
			writer.println(String.format("0x%x, ",
					outputByteBuffer.getShort(bufLen - 2)));
			writer.println("};\n");

			writer.println("\n");
			writer.println(String.format("int wavLength = %d;", bufLen / 2));

		}
		return true;
	}

	public ByteArrayOutputStream generateBuffer() 
			throws FileNotFoundException, IOException, UnsupportedAudioFileException {
		if (sourceFile == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (
			InputStream is = new BufferedInputStream(new FileInputStream(sourceFile));
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
		) {
			AudioFormat sourceAudioFormat = audioStream.getFormat();

			System.out.println(sourceAudioFormat);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = audioStream.read(buffer)) > 0) {
				baos.write(buffer, 0, len);
			}
		}
		return baos;
	}
}
