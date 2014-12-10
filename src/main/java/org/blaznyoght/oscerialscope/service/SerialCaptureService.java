package org.blaznyoght.oscerialscope.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.blaznyoght.oscerialscope.model.CaptureResult;
import org.blaznyoght.oscerialscope.service.exception.InvalidStateException;

public class SerialCaptureService {

	State currentState = State.STOPPED;
	CaptureThread currentCapture = null;

	private List<CaptureResult> captureResultList = new ArrayList<CaptureResult>();

	private static enum State {
		STARTED(), STOPPED();
	}

	private static class CaptureThread implements Runnable,
			SerialPortEventListener {
		private boolean shouldStop;
		private final String portName;
		private SerialPort serialPort;
		private final CaptureResult result = new CaptureResult();

		public CaptureThread(String portName) {
			this.portName = portName;
			getResult().setPortName(portName);
		}

		public void setShouldStop(boolean shouldStop) {
			this.shouldStop = shouldStop;
		}

		public String getPortName() {
			return portName;
		}

		public CaptureResult getResult() {
			return result;
		}

		@Override
		public void run() {
			serialPort = new SerialPort(getPortName());
			try {
				getResult().setStartTime(Calendar.getInstance());
				serialPort.openPort();
				serialPort.setParams(SerialPort.BAUDRATE_115200, 8, 1, 0);
				serialPort.addEventListener(this);
				while (!shouldStop) {
					Thread.sleep(1000);
				}
				getResult().setEndTime(Calendar.getInstance());
			} catch (SerialPortException | InterruptedException e) {
				// TODO handle Exception properly
			}
		}

		@Override
		public void serialEvent(SerialPortEvent serialPortEvent) {
			if (serialPortEvent.isRXCHAR()) {
				try {
					byte[] buf = serialPort.readBytes();
					result.getBuffer().write(buf);
				} catch (SerialPortException | IOException e) {
					// TODO handle Exception properly
				}
			}

		}

	}

	public List<String> listSerialPorts() {
		return Arrays.asList(SerialPortList.getPortNames());
	}

	public synchronized void startCapture(String portName)
			throws InvalidStateException {
		if (currentState.equals(State.STARTED)) {
			throw new InvalidStateException();
		}
		currentCapture = new CaptureThread(portName);
		new Thread(currentCapture).start();
		currentState = State.STARTED;
	}

	public synchronized String stopCapture() throws InvalidStateException {
		if (currentState.equals(State.STOPPED)) {
			throw new InvalidStateException();
		}
		currentCapture.setShouldStop(true);
		captureResultList.add(currentCapture.getResult());
		String message = String.format(
				"Capture %s terminated (%d samples captured)",
				currentCapture.getResult(), 
				currentCapture.getResult().getBuffer().size());
		currentCapture = null;
		currentState = State.STOPPED;
		return message;
	}

	public boolean isRunning() {
		return State.STARTED.equals(currentState);
	}

	public int getCaptureProgress() {
		return currentCapture.getResult().getSampleCount();
	}

	public List<CaptureResult> getCaptureResultList() {
		return captureResultList;
	}

	public void removeCapture(CaptureResult capture) {
		captureResultList.remove(capture);
	}

}
