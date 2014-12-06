package at.bitfire.gfxtablet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
public class CanvasActivity extends Activity implements OnSharedPreferenceChangeListener {
	CanvasView canvas;
	SharedPreferences settings;
	NetworkClient netClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.network_preferences, false);
		PreferenceManager.setDefaultValues(this, R.xml.drawing_preferences, false);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(this);
		if (settings.getBoolean(SettingsActivity.KEY_PREF_FULLSCREEN, false)) {
			if (ViewConfiguration.get(this).hasPermanentMenuKey())
				requestWindowFeature(Window.FEATURE_NO_TITLE);
			else
				Toast.makeText(this, "Limited full-screen due to missing hardware menu button", Toast.LENGTH_LONG).show();
			
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		setContentView(R.layout.activity_canvas);
		LinearLayout layout = (LinearLayout)findViewById(R.id.canvas_layout);
		
		new Thread(netClient = new NetworkClient(PreferenceManager.getDefaultSharedPreferences(this))).start();

		canvas = new CanvasView(this, netClient);
		layout.addView(canvas);
		this.reconfigureLayout();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (key.equals(SettingsActivity.KEY_PREF_PADDING))
			this.reconfigureLayout();
	}
	
	void reconfigureLayout()
	{
		String padding = settings.getString(SettingsActivity.KEY_PREF_PADDING, "0");
		int p = Integer.parseInt(padding);
		RelativeLayout l = (RelativeLayout)findViewById(R.id.relative_layout);
		l.setPadding(p, p, p, p);
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
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(("http://rfc2822.github.io/GfxTablet/"))));
	}
	
	public void showSettings(MenuItem item) {
		startActivityForResult(new Intent(this, SettingsActivity.class), 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == SettingsActivity.RESULT_RESTART) {
	        finish();
			Intent i = new Intent(this, CanvasActivity.class);
	        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(i);
		}
	}
}
