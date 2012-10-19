package com.phinominal.webbedsocketmain;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.util.Log;

import com.openrobot.common.ADKConnection;
import com.openrobot.common.ADKMessage;

public class ADKSensorMessage extends ADKMessage {
	public int value;
	public int[] sensorValues;
	
	public ADKSensorMessage(byte[] buffer, int retSize) {
		super();
		this.sensorValues = new int[10];
		
		int i = 0;
		while (i < retSize) {
		
			int len = retSize - i;
			
			if (len >= 1) {
				int flag = (int)buffer[i];
				
				if (flag != ADKMessage.ADK_MESSAGE_TYPE_SENSOR) {
					// throw error?
					return;
				}
				
				i += 1;
				
				for (int index = 0; index < this.sensorValues.length; index++) {
					int value = -1;
					if (len >= i + 4) {  // 
						
						ByteBuffer bb = ByteBuffer.wrap(buffer, i, 4); // wrap buffer with offset i and length 4
						i += 4;
						value = bb.getInt();
					}
					this.sensorValues[index] = value;			
				}
				
			} else {
				i += 1;  // not sure about this, putting it here to avoid infinite loop
			}
			
		}		
	}
	
	public ADKSensorMessage() {
		super();
	}

	@Override
	public void writeToOutputStream(OutputStream outputStream)
			throws IOException {
		
		byte[] buffer = new byte[1];
		
		buffer[0] = (byte)1;
		 
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
		return ADKMessage.ADK_MESSAGE_TYPE_SENSOR;
	}
}
