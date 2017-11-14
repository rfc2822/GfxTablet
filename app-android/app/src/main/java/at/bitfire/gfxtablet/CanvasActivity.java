package at.bitfire.gfxtablet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import at.bitfire.gfxtablet.NetEvent.Type;

public class CanvasActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String TAG = "GfxTablet.Canvas";
    private static CanvasActivity instance;
    public static CanvasActivity get() { return instance; }

    final Uri homepageUri = Uri.parse(("https://gfxtablet.bitfire.at"));

    NetworkClient netClient;
    NetworkServer netServer;

    SharedPreferences preferences;
    boolean fullScreen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

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

    @Override
    protected void onResume() {
        super.onResume();

        if (preferences.getBoolean(SettingsActivity.KEY_KEEP_DISPLAY_ACTIVE, true))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        showTemplateImage();
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

    public void showAbout(MenuItem item) {
        startActivity(new Intent(Intent.ACTION_VIEW, homepageUri));
    }

    public void showDonate(MenuItem item) {
        startActivity(new Intent(Intent.ACTION_VIEW, homepageUri.buildUpon().appendPath("donate").build()));
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


    // template image logic

    private String getTemplateImagePath() {
        return preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, null);
    }

    public void selectTemplateImage(MenuItem item) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void clearTemplateImage(MenuItem item) {
        preferences.edit().remove(SettingsActivity.KEY_TEMPLATE_IMAGE).apply();
        showTemplateImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            try {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);

                preferences.edit().putString(SettingsActivity.KEY_TEMPLATE_IMAGE, picturePath).apply();
                showTemplateImage();
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Fits chosen image to screen size.
     */
    public void showTemplateImage() {
        ImageView template = (ImageView)findViewById(R.id.canvas_template);
        template.setImageDrawable(null);

        if (template.getVisibility() == View.VISIBLE) {
            String picturePath = preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, null);
            if (picturePath != null)
                try {
                    final Drawable drawable = new BitmapDrawable(getResources(), picturePath);
                    template.setScaleType(ImageView.ScaleType.FIT_XY);
                    template.setImageDrawable(drawable);
                } catch (Exception e) {
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
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
            findViewById(R.id.canvas_message).setVisibility(success ? View.GONE : View.VISIBLE);
        }
    }

}
