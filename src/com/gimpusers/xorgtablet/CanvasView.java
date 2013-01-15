package com.gimpusers.xorgtablet;

import java.util.concurrent.ExecutionException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

@SuppressLint("ViewConstructor")
public class CanvasView extends View implements OnSharedPreferenceChangeListener {
	final static int PRESSURE_RESOLUTION = 10000;
	
	XorgClient xorgClient;
	SharedPreferences settings;
	boolean acceptStylusOnly;
	
	public CanvasView(Context context, XorgClient xorgClient) {
		super(context);
		
		// disable until networking has been configured
		setEnabled(false);
		setBackgroundColor(0xFFD0D0D0);

		settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings.registerOnSharedPreferenceChangeListener(this);
		reconfigureAcceptedInputDevices();
		
		this.xorgClient = xorgClient;
		new ConfigureNetworkingTask().execute();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.equals(SettingsActivity.KEY_PREF_HOST))
			new ConfigureNetworkingTask().execute();
		else if (key.equals(SettingsActivity.KEY_PREF_STYLUS_ONLY))
			reconfigureAcceptedInputDevices();
	}
		
	void reconfigureAcceptedInputDevices() {
		acceptStylusOnly = settings.getBoolean(SettingsActivity.KEY_PREF_STYLUS_ONLY, false);
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		Toast.makeText(getContext(), String.format("%dx%d", w, h), Toast.LENGTH_SHORT).show();
		xorgClient.getQueue().add(new XConfigurationEvent(w, h, PRESSURE_RESOLUTION));
	}
	
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (isEnabled()) {
			for (int ptr = 0; ptr < event.getPointerCount(); ptr++)
				if (!acceptStylusOnly || (event.getToolType(ptr) == MotionEvent.TOOL_TYPE_STYLUS)) {
					//Log.i("XorgTablet", String.format("Generic motion event logged: %f|%f, pressure %f", event.getX(ptr), event.getY(ptr), event.getPressure(ptr)));
					if (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE)
						xorgClient.getQueue().add(new XMotionEvent((int)event.getX(ptr), (int)event.getY(ptr), (int)event.getPressure(ptr)*PRESSURE_RESOLUTION));
				}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isEnabled()) {
			for (int ptr = 0; ptr < event.getPointerCount(); ptr++)
				if (!acceptStylusOnly || (event.getToolType(ptr) == MotionEvent.TOOL_TYPE_STYLUS)) {
					//Log.i("XorgTablet", String.format("Touch event logged: %f|%f, pressure %f", event.getX(ptr), event.getY(ptr), event.getPressure(ptr)));
					switch (event.getActionMasked()) {
					case MotionEvent.ACTION_MOVE:
						xorgClient.getQueue().add(new XMotionEvent((int)event.getX(ptr), (int)event.getY(ptr), (int)event.getPressure(ptr)*PRESSURE_RESOLUTION));
						break;
					case MotionEvent.ACTION_DOWN:
						xorgClient.getQueue().add(new XButtonEvent((int)event.getX(ptr), (int)event.getY(ptr), (int)event.getPressure(ptr)*PRESSURE_RESOLUTION, true));
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						xorgClient.getQueue().add(new XButtonEvent((int)event.getX(ptr), (int)event.getY(ptr), (int)event.getPressure(ptr)*PRESSURE_RESOLUTION, false));
						break;
					}
						
				}
			return true;
		}
		return false;
	}
	
	
	private class ConfigureNetworkingTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return xorgClient.configureNetworking();
		}
		
		protected void onPostExecute(Boolean success) {
			if (success)
				setEnabled(true);
			else {
				setEnabled(false);
				Toast.makeText(getContext(), "Unknown host name, network tablet disabled!", Toast.LENGTH_LONG).show();
			}
		}
	}
}
