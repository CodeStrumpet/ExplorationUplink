package com.phinominal.webbedsocketmain;

import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONException;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.openrobot.common.ADKConnection;
import com.openrobot.common.ADKConnection.ADKConnectionException;
import com.openrobot.common.ADKConnection.ADKConnectionState;
import com.openrobot.common.ADKConnectionInterface;
import com.openrobot.common.ADKMessage;
import com.openrobot.common.CameraPreviewFeed;
import com.openrobot.common.ControlWebSocketServer;
import com.openrobot.common.ControlWebSocketServerInterface;
import com.openrobot.common.JSONMessage;
import com.openrobot.common.NetworkHelper;
import com.openrobot.common.VideoWebSocketServer;

public class SocketFeet extends Activity implements
		ControlWebSocketServerInterface, ADKConnectionInterface {

	public ControlWebSocketServer controlWebSocketServer;
	private VideoWebSocketServer videoWebSocketServer;
	private static SurfaceView mSurfaceView;
	public static TextView TextHUD;
	private TextView debugTextView;
	private String debugText = "";
	public static float touchx, touchy;
	private ADKConnection adkConnection;
	static final String appname = "Exploration Uplink";
	private boolean needsAck = false;

	private int currPanPosition = 127;
	private int currTiltPosition = 127;
	public static int CAMERA_POSITION_INCREMENT_AMOUNT = 2;
	public static final String COMMAND_PREFIX = "s";
	public static final String PAN_SERVO_ID = "c";
	public static final String TILT_SERVO_ID = "b";

	Handler logHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (String) msg.obj;
			TextHUD.setText(getLastnCharacters(TextHUD.getText() + "\n" + text,
					300));
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window lWin = getWindow();
		lWin.setFormat(PixelFormat.TRANSLUCENT);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// i want to see battery and gps status
		// lWin.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		TextHUD = (TextView) findViewById(R.id.txtbox);
		TextHUD.setMovementMethod(new ScrollingMovementMethod());
		debugTextView = (TextView) findViewById(R.id.debug_text);
		debugTextView.setVisibility(View.INVISIBLE);

		mSurfaceView = (SurfaceView) findViewById(R.id.imgbox);
		mSurfaceView.setOnClickListener(new OnClickListener() {
			public void onClick(View aView) {
				System.out.println("Surface view was clicked");
				
				if (adkConnection.connectionState == ADKConnectionState.kConnectionStateOpen) {
					if (!needsAck) {
						ADKControlMessage controlMessage = new ADKControlMessage();
						controlMessage.controlType = ADKControlMessage.ADK_CONTROL_MESSAGE_TYPE_DIRECTION;
						controlMessage.value = 3;

						SLog("Sending ControlMessage with value:  "
								+ controlMessage.value);
						adkConnection.sendMessage(controlMessage);
					} else {
						SLog("Discarding command, need Ack... " + 3);
					}
				}				
			}
		});

		String ipaddr = NetworkHelper.getLocalIpAddress();
		
		SLog(appname + "   (found IP:  " + ipaddr + ")");
		

		TextHUD.setText(appname + " Found IPr :" + ipaddr);

		if (ipaddr.length() > 0) {

			int videoPort = 8886;

			try {
				videoWebSocketServer = new VideoWebSocketServer(ipaddr,
						videoPort, mSurfaceView);
				videoWebSocketServer.start();
				SLog("VideoWebSocketServer started on port: "
						+ videoWebSocketServer.getPort());

			} catch (UnknownHostException e) {
				SLog("Couldn't find host for VideoSocketServer, not starting...");
				e.printStackTrace();
			}

			CameraPreviewFeed.Torch(true);
			int controlPort = 8887;
			try {
				controlWebSocketServer = new ControlWebSocketServer(ipaddr,
						controlPort, this);
				controlWebSocketServer.start();
				SLog("ControlWebSocketServer started on port: "
						+ controlWebSocketServer.getPort());
			} catch (UnknownHostException e) {
				SLog("Couldn't find host for VideoSocketServer, not starting...");
				e.printStackTrace();
			}
		} else {
			SLog("Couldn't find IP, not starting any socket servers");
		}

		// Instantiate ADKConnection
		adkConnection = new ADKConnection(this, this);

	}

	@Override
	public void onResume() {
		super.onResume();
		adkConnection.resumeConnection();
	}

	@Override
	public void onDestroy() {

		try {
			if (videoWebSocketServer != null) {
				videoWebSocketServer.stop();
			}
			if (controlWebSocketServer != null) {
				controlWebSocketServer.stop();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adkConnection.destroyConnection();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		adkConnection.pauseConnection();
	}

	// ControlWebSocketServerInterface
	public void controlInstructionReceived(String controlString) {

		String controlValue = "";
		String controlAttribute = "";
		try {

			// options seem to be: pan_control, tilt_control,
			// {"CONTROL_ATTRIBUTE_KEY": "direction_control",
			// "CONTROL_VALUE_KEY": "J2\r"}
			JSONMessage jsonMessage = new JSONMessage(controlString);
			SLog("ControlMessage:  " + controlString);

			controlAttribute = jsonMessage
					.getStringForKey(JSONMessage.CONTROL_ATTRIBUTE_KEY);
			controlValue = jsonMessage
					.getStringForKey(JSONMessage.CONTROL_VALUE_KEY);

			if (controlAttribute != null && controlValue != null) {
				Log.i(controlAttribute, controlValue);

				if (controlAttribute
						.equalsIgnoreCase(JSONMessage.PAN_CONTROL_KEY)) {
					int panChangeValue = jsonMessage
							.getIntForKey(JSONMessage.CONTROL_VALUE_KEY);
					int newValue = currPanPosition += panChangeValue;

					if (newValue < 1) {
						newValue = 1;
					} else if (newValue > 254) {
						newValue = 254;
					}
					this.currPanPosition = newValue;
					String panInstruction = COMMAND_PREFIX + PAN_SERVO_ID
							+ newValue + "\r";
					System.out.println(panInstruction);

					return;
				} else if (controlAttribute
						.equalsIgnoreCase(JSONMessage.TILT_CONTROL_KEY)) {
					int tiltChangeValue = jsonMessage
							.getIntForKey(JSONMessage.CONTROL_VALUE_KEY);
					int newValue = currTiltPosition += tiltChangeValue;
					if (newValue < 1) {
						newValue = 1;
					} else if (newValue > 254) {
						newValue = 254;
					}
					this.currTiltPosition = newValue;
					String tiltInstruction = COMMAND_PREFIX + TILT_SERVO_ID
							+ newValue + "\r";
					System.out.println(tiltInstruction);
					return;
				}
			}

		} catch (JSONException e) {
			System.out.println(e);
		}

		if (controlString == null || controlValue == null) {
			return;
		}

		if (controlValue.startsWith("resetPan")) {

			// send(re.ExecuteCommand(PAN_SERVO_ID + 127 + "\r"));
		}
		if (controlValue.startsWith("resetTilt")) {
			// send(re.ExecuteCommand(TILT_SERVO_ID + 127 + "\r"));
		}

		if (controlValue.startsWith("flashlight,1")) {
			CameraPreviewFeed.Torch(true);
		}
		if (controlValue.startsWith("flashlight,0")) {
			CameraPreviewFeed.Torch(false);
		}

		if (controlValue.startsWith("quit")) {
			nuketheapp();
		} else {

			// Drive instruction...
			if (!needsAck) {
				ADKControlMessage controlMessage = new ADKControlMessage();
				controlMessage.controlType = ADKControlMessage.ADK_CONTROL_MESSAGE_TYPE_DIRECTION;
				controlMessage.value = Integer.parseInt(controlValue);

				SLog("Sending ControlMessage with value:  "
						+ controlMessage.value);
				adkConnection.sendMessage(controlMessage);
			} else {
				SLog("Discarding command, need Ack... " + controlValue);
			}

		}
	}

	private void toggleDebugLogVisibility() {
		if (this.debugTextView.getVisibility() == View.VISIBLE) {
			this.debugTextView.setVisibility(View.INVISIBLE);
			mSurfaceView.setVisibility(View.VISIBLE);
		} else {
			mSurfaceView.setVisibility(View.INVISIBLE);
			this.debugTextView.setVisibility(View.VISIBLE);
		}
	}

	public void ADKMessageReceived(ADKMessage message) {
		needsAck = false;
		SLog("ADKMessage received with type:  " + message.messageType());
	}

	public void ADKConnectionCaughtException(Exception exception,
			ADKConnectionException exceptionType) {
		SLog("adkConnectionCaughtException of type: " + exceptionType
				+ "  exception:  " + exception);
	}

	/**
	 * Logs message both to logcat and to the onscreen debug textview
	 * 
	 * @param message
	 */
	private void SLog(String message) {
		Log.d("OUT", message);
		Message handlerMessage = Message.obtain();
		handlerMessage.obj = message;
		logHandler.sendMessage(handlerMessage);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.debug_log_menu_item:
			toggleDebugLogVisibility();
			return true;
		case R.id.quit_menu_item:
			nuketheapp();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// remove the application (from RE RobotOperations)
	public void nuketheapp() {
		double value = 1.0;
		if (value < (-0.999))
			value = 0.0;
		else if (value < (1.0/128.0)) // droid / eris hack to prevent screen timeout
			value = (1.0/128.0);
		else if (value > 1.0)
			value = 1.0;
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.screenBrightness = (float)value;
		this.getWindow().setAttributes(lp);
		
		android.os.Process.killProcess(android.os.Process.myPid());
		this.finish();
	}

	public String getLastnCharacters(String inputString, int subStringLength) {
		int length = inputString.length();
		if (length <= subStringLength) {
			return inputString;
		}
		int startIndex = length - subStringLength;
		return inputString.substring(startIndex);
	}
}
