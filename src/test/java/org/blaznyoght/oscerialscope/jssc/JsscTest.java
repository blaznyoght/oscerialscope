package org.blaznyoght.oscerialscope.jssc;

import org.blaznyoght.oscerialscope.jssc.wrapper.EventType;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class JsscTest implements SerialPortEventListener {

	private SerialPort serialPort;
	
	public JsscTest(SerialPort port) {
		this.serialPort = port;
	}

	@Override
	public void serialEvent(SerialPortEvent serialPortEvent) {
		EventType eventType = EventType.getByCode(serialPortEvent.getEventType());
		int eventValue = serialPortEvent.getEventValue();
		System.out.println(String.format("%s: %d %s", eventType, eventValue, eventType.getDescription()));
		if (serialPortEvent.isRXCHAR()) {
			try {
				byte[] buf = serialPort.readBytes();
				System.out.println(String.format("Read: [%s]", new String(buf)));
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * @param args
	 * @throws SerialPortException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws SerialPortException,
			InterruptedException {
		System.out.println("Serial port list");
		for (String name : SerialPortList.getPortNames()) {
			System.out.println(name);
		}
		SerialPort port = new SerialPort(SerialPortList.getPortNames()[0]);
		port.openPort();
		port.setParams(SerialPort.BAUDRATE_115200, 8, 1, 0);
		port.addEventListener(new JsscTest(port));
		while (true) {
			System.out.println("Je continue");
			Thread.sleep(10000);
		}
	}
}
