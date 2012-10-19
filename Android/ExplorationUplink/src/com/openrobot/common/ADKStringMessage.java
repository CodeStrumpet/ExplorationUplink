package com.openrobot.common;

import java.io.IOException;
import java.io.OutputStream;

public class ADKStringMessage extends ADKMessage {
	public String messageString;

	public ADKStringMessage(byte[] buffer, int retSize) {
		super();
		
		// Parse string here...
		
	}

	@Override
	public void writeToOutputStream(OutputStream outputStream)
			throws IOException {
		
		
	}

	@Override
	public int messageType() {
		// TODO Auto-generated method stub
		return ADKMessage.ADK_MESSAGE_TYPE_STRING;
	}

}
