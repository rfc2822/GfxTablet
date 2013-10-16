package at.bitfire.gfxtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import at.bitfire.gfxtablet.NetEvent.Type;

@SuppressLint("ViewConstructor")
public class CanvasView extends View implements OnSharedPreferenceChangeListener {
	NetworkClient netClient;
	SharedPreferences settings;
	boolean acceptStylusOnly;
	
	Bitmap bitmap;
	Canvas canvas;
	Path path;
	Paint paint_bitmap, paint;

	int maxX, maxY;
	
	public CanvasView(Context context, NetworkClient netClient) {
		super(context);
		
		// disable until networking has been configured
		setEnabled(false);

		this.path = new Path();
		this.paint_bitmap = new Paint(Paint.DITHER_FLAG);
		this.paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		this.paint.setColor(0xFF000000);
		this.paint.setStyle(Paint.Style.STROKE);
		
		settings = PreferenceManager.getDefaultSharedPreferences(context);
		settings.registerOnSharedPreferenceChangeListener(this);
		reconfigureAcceptedInputDevices();
		
		this.netClient = netClient;
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
		maxX = w;
		maxY = h;
		this.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		this.canvas = new Canvas(this.bitmap);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0xFFD0D0D0);
		canvas.drawBitmap(this.bitmap, 0, 0, this.paint_bitmap);
		canvas.drawPath(this.path, this.paint);
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (isEnabled()) {
			for (int ptr = 0; ptr < event.getPointerCount(); ptr++)
				if (!acceptStylusOnly || (event.getToolType(ptr) == MotionEvent.TOOL_TYPE_STYLUS)) {
					//Log.i("XorgTablet", String.format("Generic motion event logged: %f|%f, pressure %f", event.getX(ptr), event.getY(ptr), event.getPressure(ptr)));
					if (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE)
						netClient.getQueue().add(new NetEvent(Type.TYPE_MOTION,
							normalizeX(event.getX(ptr)),
							normalizeY(event.getY(ptr)),
							normalizePressure(event.getPressure(ptr))
						));
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
					short nx = normalizeX(event.getX(ptr)),
						  ny = normalizeY(event.getY(ptr)),
						  npressure = normalizePressure(event.getPressure(ptr));
					float x = event.getX();
					float y = event.getY();
					//Log.i("XorgTablet", String.format("Touch event logged: %f|%f, pressure %f", event.getX(ptr), event.getY(ptr), event.getPressure(ptr)));
					switch (event.getActionMasked()) {
					case MotionEvent.ACTION_MOVE:
						netClient.getQueue().add(new NetEvent(Type.TYPE_MOTION, nx, ny, npressure));
						this.path.lineTo(x, y);
						break;
					case MotionEvent.ACTION_DOWN:
						netClient.getQueue().add(new NetEvent(Type.TYPE_BUTTON, nx, ny, npressure, 0, true));
						this.path.reset();
						this.path.moveTo(x, y);
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						netClient.getQueue().add(new NetEvent(Type.TYPE_BUTTON, nx, ny, npressure, 0, false));
						this.path.lineTo(x, y);
						this.canvas.drawPath(this.path, this.paint);
						this.path.reset();
						break;
					}
					invalidate();
				}
			return true;
		}
		return false;
	}
	
	
	short normalizeX(float x) {
		return (short)(Math.min(Math.max(0, x), maxX) * Short.MAX_VALUE/maxX);
	}
	
	short normalizeY(float x) {
		return (short)(Math.min(Math.max(0, x), maxY) * Short.MAX_VALUE/maxY);
	}
	
	short normalizePressure(float x) {
		return (short)(Math.min(Math.max(0, x), 2.0) * Short.MAX_VALUE/2.0);
	}
	
	void clearDraw() {
		this.canvas.drawColor(0, Mode.CLEAR);
		invalidate();
	}
	
	private class ConfigureNetworkingTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return netClient.configureNetworking();
		}
		
		protected void onPostExecute(Boolean success) {
			if (success)
				setEnabled(true);
			else {
				setEnabled(false);
				Toast.makeText(getContext(), "Unknown host name, please configure", Toast.LENGTH_LONG).show();
				getContext().startActivity(new Intent(getContext(), SettingsActivity.class));
			}
		}
	}
}
