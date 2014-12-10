package org.blaznyoght.oscerialscope.model;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class CaptureResult {
	Calendar startTime;
	Calendar endTime;
	
	final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	
	String portName;
	
	public CaptureResult() {
		super();
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}
}
