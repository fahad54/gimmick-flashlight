package com.gimmick.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

	public static final String MYPREFS = "TorchAppPreference";
	private Context context;

	public PreferenceManager(Context context) {
		this.context = context;
	}
	
	public void saveTorchStatus(String torch) {
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = context.getSharedPreferences(
				MYPREFS, mode);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString("Torch", torch);
		editor.commit();
	}
	public String getTorchStatus() {
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences mySharedPreferences = context.getSharedPreferences(
				MYPREFS, mode);
		return mySharedPreferences.getString("Torch", "");
	}
	
}
