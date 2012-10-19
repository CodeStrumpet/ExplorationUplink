package com.phinominal.webbedsocketmain;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.openrobot.common.ADKConnection;
import com.openrobot.common.ADKMessage;

public class ADKControlMessage extends ADKMessage {
	public static final int ADK_CONTROL_MESSAGE_TYPE_NONE = 0;
	public static final int ADK_CONTROL_MESSAGE_TYPE_DIRECTION = 1;
	public static final int ADK_CONTROL_MESSAGE_TYPE_DRIVE = 2;
	
	public int controlType = ADKControlMessage.ADK_CONTROL_MESSAGE_TYPE_NONE;
	public int value = 0;
	
	public ADKControlMessage(byte[] buffer, int retSize) {
		super();
		int i = 0;
		while (i < retSize) {
		
			int len = retSize - i;
			if (len >= 3) {				
				int type = buffer[0];
				if (type != ADKMessage.ADK_MESSAGE_TYPE_CONTROL) {
					// throw error?
					return;
				}
				this.controlType = buffer[1];
				this.value = buffer[2];				
			}
		}
	}
	
	public ADKControlMessage() {
		super();
	}

	@Override
	public void writeToOutputStream(OutputStream outputStream)
			throws IOException {
		
		byte[] buffer = new byte[3];
		
		buffer[0] = (byte)this.messageType(); // message type
		buffer[1] = (byte)this.controlType;
		buffer[2] = (byte)this.value;
		Log.d(ADKConnection.TAG, "Writing control value:  " + buffer[2]);
		
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
		return ADKMessage.ADK_MESSAGE_TYPE_CONTROL;
	}
}
