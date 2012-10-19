package com.openrobot.common;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract class that encapsulates message reading and writing.
 * All message subclasses must abide by the contract that the first byte of messages always defines the message type.
 * @author skamuter
 *
 */
public abstract class ADKMessage {
	
	public static final int ADK_MESSAGE_TYPE_NONE = 0;
	public static final int ADK_MESSAGE_TYPE_STRING = 1;
	public static final int ADK_MESSAGE_TYPE_SENSOR = 2;
	public static final int ADK_MESSAGE_TYPE_CONTROL = 3;
	public static final int ADK_MESSAGE_TYPE_ACK = 4;
	
	

	// Abstract methods 
	abstract public void writeToOutputStream(OutputStream outputStream) throws IOException;
	abstract public int messageType();
	
	
	/**
	 * Constructor will instantiate if possible
	 * @param buffer
	 */
	public ADKMessage(byte[] buffer, int retSize) {
		super();
	}
	
	public ADKMessage() {
		super();
	}
}
