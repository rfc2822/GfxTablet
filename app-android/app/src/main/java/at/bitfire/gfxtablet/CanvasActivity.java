package at.bitfire.gfxtablet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class CanvasActivity extends ActionBarActivity {
    NetworkClient netClient;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        netClient = new NetworkClient(PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(SettingsActivity.KEY_KEEP_DISPLAY_ACTIVE, false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new ConfigureNetworkingTask().execute();
    }

	@Override
	protected void onDestroy() {
        super.onDestroy();
		netClient.getQueue().add(new NetEvent(NetEvent.Type.TYPE_DISCONNECT));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_canvas, menu);
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_fullscreen).setVisible(Build.VERSION.SDK_INT >= 19);
        return true;
    }

	public void showAbout(MenuItem item) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(("https://rfc2822.github.io/GfxTablet/"))));
	}
	
	public void showSettings(MenuItem item) {
		startActivityForResult(new Intent(this, SettingsActivity.class), 0);
	}

    public void switchFullScreen(MenuItem item) {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |    // hide nav bar
                View.SYSTEM_UI_FLAG_FULLSCREEN |         // hide status bar
                View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }


    private class ConfigureNetworkingTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return netClient.configureNetworking();
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                setContentView(new CanvasView(CanvasActivity.this, netClient));
                new Thread(netClient).start();

                Toast.makeText(CanvasActivity.this, "Touch events will be sent to " + netClient.destAddress.getHostAddress() + ":" + NetworkClient.GFXTABLET_PORT, Toast.LENGTH_LONG).show();
            } else
                setContentView(R.layout.activity_no_host);
        }
    }

}
