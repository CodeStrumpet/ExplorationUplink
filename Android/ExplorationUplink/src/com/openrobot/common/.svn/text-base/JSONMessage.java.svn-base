package com.openrobot.common;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONMessage extends JSONObject {
	
	
	public static final String INSTRUCTION_KEY = "INSTRUCTION_KEY";
	public static final String SUBSCRIPTION_KEY = "SUBSCRIPTION_KEY";
	public static final String CONTROL_ATTRIBUTE_KEY = "CONTROL_ATTRIBUTE_KEY";
	public static final String CONTROL_VALUE_KEY = "CONTROL_VALUE_KEY";
	
	
	public static final String IMAGE_RESOLUTION_KEY = "camera_resolution";
	public static final String DIRECTION_CONTROL_KEY = "direction_control";
	public static final String CAMERA_CONTROL_KEY = "camera_control";
	public static final String PAN_CONTROL_KEY = "pan_control";
	public static final String TILT_CONTROL_KEY = "tilt_control";
	
	public static final int SUBSCRIPTION_VALUE_CONTROL = 0x1;

	public JSONMessage(String jsonString) throws JSONException {
		super(jsonString);
	}
	
	public Integer getIntForKey(String key) {
		if (this.has(key)) {
			try {
				return this.getInt(key);
			} catch (JSONException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public String getStringForKey(String key) {
		if (this.has(key)) {
			try {
				return this.getString(key);
			} catch (JSONException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}
