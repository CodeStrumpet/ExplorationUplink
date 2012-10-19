package com.openrobot.common;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.phinominal.webbedsocketmain.ADKSensorMessage;

public class ADKConnection implements Runnable {
	
	public enum ADKConnectionState {
		kConnectionStateNone,
		kConnectionStateOpen,
		kConnectionStateClosed
	}
	
	public enum ADKConnectionException {
		kConnectionExceptionUnknown,
		kConnectionExceptionMessageParse,
		kConnectionExceptionPermission,
		kConnectionExceptionNoAccessory,
		kConnectionExeptionOpeningFailed,
		kConnectionExceptionCloseFailed
	}
	
	 // TAG is used to debug in Android logcat console
	public static final String TAG = "OUT";//"ArduinoAccessory";
 
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
 
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	
	private  ADKConnectionInterface connectionInterface;
	private Context context;
	
	public ADKConnectionState connectionState = ADKConnectionState.kConnectionStateNone;
	
	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
 
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
						connectionInterface.ADKConnectionCaughtException(null, ADKConnectionException.kConnectionExceptionPermission);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};
	
	
	public ADKConnection(Context context, ADKConnectionInterface connectionInterface) {
		this(context, connectionInterface, null);
	}
	
	public ADKConnection(Context context, ADKConnectionInterface connectionInterface, UsbAccessory lastAccessory) {
		super();
		this.context = context;
		this.connectionInterface = connectionInterface;
		initialize(lastAccessory);
	}
	
	
	/**
	 * initializes class, optionally using an already instantiated UsbAccessory if a non-null value is passed in
	 * @param lastAccessory
	 */
	private void initialize(UsbAccessory lastAccessory) {
	       mUsbManager = UsbManager.getInstance(this.context);
	       mPermissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
	       IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	       filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
	       this.context.registerReceiver(mUsbReceiver, filter);
	 
	       if (lastAccessory != null) {
	    	   mAccessory = (UsbAccessory) lastAccessory;
	    	   openAccessory(mAccessory);
	       }
	}
	
	public void resumeConnection() {
		// input and output streams can potentially be null if a UsbAccessory was passed into initialize method 
		if (mInputStream != null && mOutputStream != null) {
			return;
		}
 
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
	}
	
	public void pauseConnection() {
		closeAccessory();
	}
 
	public void destroyConnection() {
		this.context.unregisterReceiver(mUsbReceiver);
	}
	
	public void sendMessage(ADKMessage message) {
		 
		/*
		byte[] buffer = new byte[1];

		if(buttonLED.isChecked())
			buffer[0]=(byte)0; // button says on, turn light off
		else
			buffer[0]=(byte)1; // button says off, turn light on
 */
		
		if (mOutputStream != null) {
			try {
				message.writeToOutputStream(mOutputStream);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}	
	}

	
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;
 
		while (true) { // read data
			try {
				ret = mInputStream.read(buffer);
				
				if (ret > 0) {
					
					int flag = (int) buffer[0];
					
					ADKMessage message = null;
					
					// TODO:  Get the message class names from ADKConnectionInterface and use something like the following newInstance code to instantiate
					//Compressor.class.getConstructor(Class.class).newInstance(Some.class);
					
					if (flag == ADKMessage.ADK_MESSAGE_TYPE_SENSOR) {
						message = new ADKSensorMessage(buffer, ret);
					} 					
					
					//ADKMessage message = ADKMessage.messageWithBuffer(buffer, ret);
					if (message != null) {
						connectionInterface.ADKMessageReceived(message);	
					}
				}
			} catch (IOException ioException) {
				connectionInterface.ADKConnectionCaughtException(ioException, ADKConnectionException.kConnectionExceptionMessageParse);
				break;
			} catch (Exception exception) {
				connectionInterface.ADKConnectionCaughtException(exception, ADKConnectionException.kConnectionExceptionUnknown);
				break;
			}
		}
	}
	
 
	private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Log.d(TAG, "accessory opened");
			connectionState = ADKConnectionState.kConnectionStateOpen;
			
			Thread thread = new Thread(null, this, "OpenAccessoryTest");
			thread.start();
			
		} else {
			connectionInterface.ADKConnectionCaughtException(null, ADKConnectionException.kConnectionExeptionOpeningFailed);
			Log.d(TAG, "accessory open fail");
		}
	}
 
	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
			connectionInterface.ADKConnectionCaughtException(e, ADKConnectionException.kConnectionExceptionCloseFailed);
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
			connectionState = ADKConnectionState.kConnectionStateClosed;
		}
	}
 
}
