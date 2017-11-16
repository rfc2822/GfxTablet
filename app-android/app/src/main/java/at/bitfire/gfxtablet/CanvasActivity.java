package at.bitfire.gfxtablet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import at.bitfire.gfxtablet.NetEvent.Type;

public class CanvasActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "GfxTablet.Canvas";
    public static String SCREEN_PATH;
    private static CanvasActivity instance;
    private Handler autoRefreshHandler;
    private Runnable autoRefreshBackground;
    public static CanvasActivity get() { return instance; }

    NetworkClient netClient;
    NetworkServer netServer;

    SharedPreferences preferences;
    boolean fullScreen = false;


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
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
        netClient.getQueue().add(new NetEvent(NetEvent.Type.TYPE_DISCONNECT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_canvas, menu);
        return true;
    }

    public void sendMotionStopSignal(){
        netClient.getQueue().add(new NetEvent(Type.TYPE_MOTION, (short) 0, (short) 0, (short) 0));
    }

    @Override
    public void onBackPressed() {
        if (fullScreen)
            switchFullScreen(null);
        else
            super.onBackPressed();
    }

    public void showSettings(MenuItem item) {
        startActivityForResult(new Intent(this, SettingsActivity.class), 0);
    }


    // preferences were changed

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SettingsActivity.KEY_PREF_HOST:
                Log.i(TAG, "Recipient host changed, reconfiguring network client");
                new ConfigureNetworkingTask().execute();
                break;
        }
    }

    // refresh methods
    public void refreshBackground() {
        netClient.getQueue().add(new NetEvent(Type.TYPE_MOTION, (short) 0, (short) 0, (short) 0, -1, false));
    }

    public void refreshBackground(MenuItem item) {
        refreshBackground();
    }


    // full-screen methods
    public void switchFullScreen(MenuItem item) {
        final View decorView = getWindow().getDecorView();
        int uiFlags = decorView.getSystemUiVisibility();

        if (Build.VERSION.SDK_INT >= 14)
            uiFlags ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= 16)
            uiFlags ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19)
            uiFlags ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setOnSystemUiVisibilityChangeListener(this);
        decorView.setSystemUiVisibility(uiFlags);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        Log.i("GfxTablet", "System UI changed " + visibility);

        fullScreen = (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;

        // show/hide action bar according to full-screen mode
        if (fullScreen) {
            CanvasActivity.this.getSupportActionBar().hide();
            Toast.makeText(CanvasActivity.this, "Press Back button to leave full-screen mode.", Toast.LENGTH_LONG).show();
        } else
            CanvasActivity.this.getSupportActionBar().show();
    }


    /**
     * Fits chosen image to screen size.
     */
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
                Toast.makeText(CanvasActivity.this, "Touch events will be sent to " + netClient.destAddress.getHostAddress() + ":" + NetworkClient.GFXTABLET_PORT, Toast.LENGTH_LONG).show();

            findViewById(R.id.canvas_template).setVisibility(success ? View.VISIBLE : View.GONE);
            findViewById(R.id.canvas).setVisibility(success ? View.VISIBLE : View.GONE);
        }
    }

}
