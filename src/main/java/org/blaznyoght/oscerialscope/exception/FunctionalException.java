package org.blaznyoght.oscerialscope.exception;

public class FunctionalException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4729441829424787654L;
	
	private String functionalMessage;
	
	public FunctionalException(String functionalMessage) {
		this.setFunctionalMessage(functionalMessage);
	}

	public String getFunctionalMessage() {
		return functionalMessage;
	}

	public void setFunctionalMessage(String functionalMessage) {
		this.functionalMessage = functionalMessage;
	}

	@Override
	public String getLocalizedMessage() {
		// TODO: refactor this part
		return this.functionalMessage;
	}
	
	
}
