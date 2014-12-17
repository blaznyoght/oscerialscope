package org.blaznyoght.oscerialscope.service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundPlayerService {
	private static final int BUFFER_SIZE = 512;
	
	private static final AudioFormat DEFAULT_FORMAT = new AudioFormat(44100, 16, 1, true, false);
	
	private AudioFormat audioFormat;

	public SoundPlayerService(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
	}
	
	public SoundPlayerService() {
		this(DEFAULT_FORMAT);
	}
	
	public void play(byte[] soundBuffer) throws LineUnavailableException {
		SourceDataLine sourceLine = AudioSystem.getSourceDataLine(audioFormat);
		sourceLine.open();
		sourceLine.start();
		
		byte[] abData = new byte[BUFFER_SIZE];
        for(int i = 0; i < soundBuffer.length; i += BUFFER_SIZE) {
        	int length = BUFFER_SIZE;
        	if (i + BUFFER_SIZE > soundBuffer.length) {
        		length = soundBuffer.length - i;
        	}
        	System.arraycopy(soundBuffer, i, abData, 0, length);
        	sourceLine.write(abData, 0, length);
        }

        sourceLine.drain();
        sourceLine.close();
	}
	
}
