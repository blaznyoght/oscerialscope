package org.blaznyoght.oscerialscope.model;

import java.nio.ByteBuffer;
import java.util.Calendar;

public class CaptureResult {
	Calendar startTime;
	Calendar endTime;
	
	ByteBuffer buffer;
	
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

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}
}
