package com.openrobot.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONException;

import android.util.Log;

public class ControlWebSocketServer extends WebSocketServer {
	private ControlWebSocketServerInterface delegate;
	public boolean isConnected = false;
	private WebSocket controlWebSocket = null;
	
	
	public ControlWebSocketServer(String ipaddr, int port, ControlWebSocketServerInterface delegate) throws UnknownHostException {
		super(new InetSocketAddress( InetAddress.getByName( ipaddr ), port));
		this.delegate = delegate;
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		isConnected = true;
    	Log.d("OUT", conn + " has connected");
		
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		if (this.connections().size() <= 0) { 
    		isConnected = false;
    		Log.d("OUT", "No WebSocket connections remain");
    	}
    	if (conn == controlWebSocket) {
    		controlWebSocket = null;
    	}
    	
    	Log.d("OUT", conn + " has checked out");
		
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		Log.d("OUT", "error!!!" + ex);
		
	}

    public void onMessage(WebSocket conn, String message) {
    	try {
			if (delegate != null)
			{
				delegate.controlInstructionReceived(message);
				return;
			}

			JSONMessage jsonMessage = new JSONMessage(message);
			
			// Check for Subscriptions
			Integer subscriptionRequest = jsonMessage.getIntForKey(JSONMessage.SUBSCRIPTION_KEY);
			if (subscriptionRequest != null) {
				if ((subscriptionRequest.intValue() & JSONMessage.SUBSCRIPTION_VALUE_CONTROL) == 1) {
					if (controlWebSocket == null) {
						controlWebSocket = conn;
					}
				}
			}
			
			if (conn == controlWebSocket) {
				String controlInstruction = jsonMessage.getStringForKey(JSONMessage.INSTRUCTION_KEY);
				if (delegate != null) {
					delegate.controlInstructionReceived(controlInstruction);
				}
				Log.d("OUT", "Control Instruction:  " + controlInstruction);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		Log.d("OUT", conn + ": " + message);
    }
    
    public void sendMessage(JSONMessage message) {
    	try {
    		String messageString = message.toString();
 
    		this.sendToAll(messageString);
    		
    	} catch (Exception e) {
    		Log.d("OUT", e.toString());
    	}
    }

}
