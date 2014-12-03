package org.blaznyoght.oscerialscope.jssc.wrapper;

public enum EventType {
	
    RXCHAR(1, "bytes count in input buffer"),
    RXFLAG(2, "bytes count in input buffer (Not supported in Linux)"),
    TXEMPTY(4, "bytes count in output buffer"),
    CTS(8, "state of CTS line (0 - OFF, 1 - ON)"),
    DSR(16, "state of DSR line (0 - OFF, 1 - ON)"),
    RLSD(32, "state of RLSD line (0 - OFF, 1 - ON)"),
    BREAK(64, "0"),
    ERR(128, "mask of errors"),
    RING(256, "state of RING line (0 - OFF, 1 - ON)"),
    UNKNOWN(512, "Unknown");	
	
	private int eventCode;
	private String valueDesc;
	
	private EventType(int eventCode, String valueDesc) {
		this.eventCode = eventCode;
		this.valueDesc = valueDesc;
	}
	
	private EventType(int eventCode) {
		this(eventCode, "");
	}
	
	public int getCode() {
		return eventCode;
	}
	
	public String getDescription() {
		return valueDesc;
	}
	
	public static EventType getByCode(int code) {
		for(EventType eventType : values()) {
			if (eventType.getCode() == code) {
				return eventType;
			}
		}
		return UNKNOWN;
	}
}
