package at.bitfire.gfxtablet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import at.bitfire.gfxtablet.NetEvent.Type;

public class CanvasActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "GfxTablet.Canvas";
    public static String SCREEN_PATH;
    private static CanvasActivity instance;
    private Handler autoRefreshHandler;
    private Runnable autoRefreshBackground;
    public static CanvasActivity get() { return instance; }
    private NetworkClient netClient;
    private NetworkServer netServer;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        autoRefreshHandler = new Handler();
        SCREEN_PATH = CanvasActivity.get().getCacheDir() + "/screen.png";

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        setContentView(R.layout.activity_canvas);

        // create network client in a separate thread
        netClient = new NetworkClient(PreferenceManager.getDefaultSharedPreferences(this));
        new Thread(netClient).start();
        // create network server in a separate thread
        netServer = new NetworkServer(PreferenceManager.getDefaultSharedPreferences(this));
        new Thread(netServer).start();

        new ConfigureNetworkingTask().execute();

        // notify CanvasView of the network client
        CanvasView canvas = (CanvasView)findViewById(R.id.canvas);
        canvas.setNetworkClient(netClient);
        setShowTouches(true);
    }

    private void setShowTouches(boolean b){
        Settings.System.putInt(CanvasActivity.get().getContentResolver(),
                "show_touches", b ? 1 : 0);
        Settings.System.putInt(CanvasActivity.get().getContentResolver(),
                "pointer_location", b ? 1 : 0);
    }

    private void startAutoRefresh() {
        autoRefreshHandler.postDelayed(new Runnable() {
            public void run() {
                refreshBackground();
                autoRefreshBackground=this;
                autoRefreshHandler.postDelayed(autoRefreshBackground, 5000);
            }
        }, 5000);
    }

    private void stopAutoRefresh() {
        autoRefreshHandler.removeCallbacks(autoRefreshBackground);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (preferences.getBoolean(SettingsActivity.KEY_KEEP_DISPLAY_ACTIVE, true))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (preferences.getBoolean(SettingsActivity.KEY_AUTO_REFRESH, true))
            startAutoRefresh();
        else
            stopAutoRefresh();

        showTemplateImage();
        setShowTouches(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
        setShowTouches(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
        netClient.getQueue().add(new NetEvent(NetEvent.Type.TYPE_DISCONNECT));
        setShowTouches(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_canvas, menu);
        return true;
    }

    public void sendMotionStopSignal(){
        netClient.getQueue().add(new NetEvent(Type.TYPE_MOTION, (short) 0, (short) 0, (short) 0));
    }

    public void showSettings(MenuItem item) {
        startActivityForResult(new Intent(this, SettingsActivity.class), 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SettingsActivity.KEY_PREF_HOST:
                Log.i(TAG, "Recipient host changed, reconfiguring network client");
                new ConfigureNetworkingTask().execute();
                break;
        }
    }

    public void refreshBackground() {
        netClient.getQueue().add(new NetEvent(Type.TYPE_MOTION, (short) 0, (short) 0, (short) 0, -1, false));
    }

    public void refreshBackground(MenuItem item) {
        refreshBackground();
    }

    public void showTemplateImage() {
        ImageView template = (ImageView)findViewById(R.id.canvas_template);
        template.setVisibility(View.VISIBLE);
        try {
            Drawable d = Drawable.createFromPath(SCREEN_PATH);
            template.setImageDrawable(d);
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class ConfigureNetworkingTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return netClient.reconfigureNetworking();
        }

        protected void onPostExecute(Boolean success) {
            if (success)
                Toast.makeText(CanvasActivity.this,
                        "Touch events will be sent to " +
                                netClient.destAddress.getHostAddress() + ":" +
                                NetworkClient.GFXTABLET_PORT, Toast.LENGTH_LONG).show();

            findViewById(R.id.canvas_template).setVisibility(success ? View.VISIBLE : View.GONE);
            findViewById(R.id.canvas).setVisibility(success ? View.VISIBLE : View.GONE);
        }
    }

}
