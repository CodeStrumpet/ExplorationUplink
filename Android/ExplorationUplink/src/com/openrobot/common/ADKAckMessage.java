package com.openrobot.common;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

public class ADKAckMessage extends ADKMessage {
	public int value;
	public int[] sensorValues;
	
	public ADKAckMessage(byte[] buffer, int retSize) {
		super();
		
		int i = 0;
		while (i < retSize) {
		
			int len = retSize - i;
			
			if (len >= 1) {
				int flag = (int)buffer[i];
				
				if (flag != ADKMessage.ADK_MESSAGE_TYPE_ACK) {
					// throw error?
					return;
				}
				
				i += 1;
				
			} else {
				i += 1;  // not sure about this, putting it here to avoid infinite loop
			}
			
		}		
	}
	
	public ADKAckMessage() {
		super();
	}

	@Override
	public void writeToOutputStream(OutputStream outputStream)
			throws IOException {
		
		byte[] buffer = new byte[1];
		
		buffer[0] = (byte)this.messageType();
		 
		if (outputStream != null) {
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				Log.e(ADKConnection.TAG, "write failed", e);
			}
		}
	}

	@Override
	public int messageType() {
		// TODO Auto-generated method stub
		return ADKMessage.ADK_MESSAGE_TYPE_ACK;
	}
}
