package com.openrobot.common;

import com.openrobot.common.ADKConnection.ADKConnectionException;

public interface ADKConnectionInterface {
	
	public void ADKMessageReceived(ADKMessage message);
	public void ADKConnectionCaughtException(Exception exception, ADKConnectionException exceptionType);
	
}
