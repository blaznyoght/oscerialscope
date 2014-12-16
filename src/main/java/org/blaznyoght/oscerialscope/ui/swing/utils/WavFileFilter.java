package org.blaznyoght.oscerialscope.ui.swing.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class WavFileFilter extends FileFilter {
	@Override
	public String getDescription() {
		return "*.wav (WaveFiles)";
	}
	
	@Override
	public boolean accept(File f) {
		return (f.getName().endsWith(".wav") || f.isDirectory());
	}
}
