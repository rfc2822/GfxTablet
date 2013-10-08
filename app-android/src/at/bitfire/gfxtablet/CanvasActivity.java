package at.bitfire.gfxtablet;

import at.bitfire.gfxtablet.R;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class CanvasActivity extends Activity {
	CanvasView canvas;
	SharedPreferences settings;
	NetworkClient netClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.network_preferences, false);
		PreferenceManager.setDefaultValues(this, R.xml.drawing_preferences, false);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (settings.getBoolean(SettingsActivity.KEY_PREF_FULLSCREEN, true)) {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		setContentView(R.layout.activity_canvas);
		LinearLayout layout = (LinearLayout)findViewById(R.id.canvas_layout);
		
		new Thread(netClient = new NetworkClient(PreferenceManager.getDefaultSharedPreferences(this))).start();

		canvas = new CanvasView(this, netClient);
		layout.addView(canvas);
	}

	@Override
	protected void onDestroy() {
		netClient.getQueue().add(new NetEvent(NetEvent.Type.TYPE_DISCONNECT));
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_canvas, menu);
		return true;
	}

	public void showAbout(MenuItem item) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(("http://rfc2822.github.com/GfxTablet"))));
	}
	
	public void showSettings(MenuItem item) {
		startActivity(new Intent(CanvasActivity.this, SettingsActivity.class));
	}
}
