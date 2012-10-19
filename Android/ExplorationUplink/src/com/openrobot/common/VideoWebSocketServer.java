package com.openrobot.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;


import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONException;

import android.util.Log;
import android.view.SurfaceView;

public class VideoWebSocketServer extends WebSocketServer implements CameraPreviewFeedInterface {
	CameraPreviewFeed cameraFeed;

	public boolean isConnected = false;
	
	public VideoWebSocketServer(String ipaddr, int port, SurfaceView surfaceView) throws UnknownHostException {
		super(new InetSocketAddress( InetAddress.getByName( ipaddr ), port));
		cameraFeed = new CameraPreviewFeed(surfaceView, this);
	}


	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		
		if (this.connections().size() <= 0) { 
    		isConnected = false;
    		Log.d("OUT", "No WebSocket connections remain");
    	}
    	
        System.out.println(conn + " has checked out");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		Log.d("OUT", "error!!!" + ex);
		
	}
	
	@Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    	isConnected = true;
        try {
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.d("OUT", conn + " entered the room!");
        System.out.println(conn + " entered the room!");
    }

    public void onMessage(WebSocket conn, String message) {
    	Log.d("OUT", conn + ": " + message);
        
        try {
			JSONMessage jsonMessage = new JSONMessage(message);
			
			String controlAttribute = jsonMessage.getStringForKey(JSONMessage.CONTROL_ATTRIBUTE_KEY);
		
			if (controlAttribute.equalsIgnoreCase(JSONMessage.IMAGE_RESOLUTION_KEY)) {
				
				//String controlValue = jsonMessage.getStringForKey(JSONMessage.CONTROL_VALUE_KEY);
				//int value = Integer.parseInt(controlValue);
				int value = jsonMessage.getIntForKey(JSONMessage.CONTROL_VALUE_KEY);
				cameraFeed.setImageQuality(value);	
				Log.d("OUT", "Changed Image Quality to value:  " + value);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    // CameraPreviewFeedInterface
    public void newImageFromCameraPreviewFeed(CameraPreviewFeed theFeed, byte[] theImage) {
    	if (isConnected) {
    		sendToAll(A15C_Base64.encodeToString(theImage, A15C_Base64.DEFAULT));
    	}
    }

}
