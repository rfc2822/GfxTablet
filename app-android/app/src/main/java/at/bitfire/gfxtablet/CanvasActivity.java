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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class CanvasActivity extends ActionBarActivity implements View.OnSystemUiVisibilityChangeListener {
    private static int RESULT_LOAD_IMAGE = 1;

    NetworkClient netClient;
    CanvasView canvas;

    SharedPreferences preferences;
    boolean fullScreen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        netClient = new NetworkClient(PreferenceManager.getDefaultSharedPreferences(this));
        new Thread(netClient).start();

        canvas = new CanvasView(CanvasActivity.this, netClient);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new ConfigureNetworkingTask().execute();
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
    protected void onStop() {
        netClient.getQueue().add(new NetEvent(NetEvent.Type.TYPE_DISCONNECT));
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_canvas, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (fullScreen)
            switchFullScreen(null);
        else
            super.onBackPressed();
    }

    public void showAbout(MenuItem item) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(("https://rfc2822.github.io/GfxTablet/"))));
    }

    public void showSettings(MenuItem item) {
        startActivityForResult(new Intent(this, SettingsActivity.class), 0);
    }


    public void switchFullScreen(MenuItem item) {
        final View decorView = getWindow().getDecorView();
        int uiFlags = decorView.getSystemUiVisibility();

        if (Build.VERSION.SDK_INT >= 14)
            uiFlags ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= 16)
            uiFlags ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 18)
            uiFlags ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setOnSystemUiVisibilityChangeListener(this);
        decorView.setSystemUiVisibility(uiFlags);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        // show/hide action bar according to full-screen mode
        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0) {
            CanvasActivity.this.getSupportActionBar().hide();
            fullScreen = true;
            Toast.makeText(CanvasActivity.this, "Press Back button to leave full-screen mode.", Toast.LENGTH_LONG).show();
        } else
            CanvasActivity.this.getSupportActionBar().show();
    }


    private String getTemplateImagePath() {
        return preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, null);
    }

    public void setTemplateImage(MenuItem item) {
        if (getTemplateImagePath() == null)
            selectTemplateImage(item);
        else {
            // template image already set, show popup
            PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu_set_template_image));
            popup.getMenuInflater().inflate(R.menu.set_template_image, popup.getMenu());
            popup.show();
        }
    }

    public void selectTemplateImage(MenuItem item) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void clearTemplateImage(MenuItem item) {
        preferences.edit().remove(SettingsActivity.KEY_TEMPLATE_IMAGE).commit();
        showTemplateImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            try {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);

                preferences.edit().putString(SettingsActivity.KEY_TEMPLATE_IMAGE, picturePath).commit();
                showTemplateImage();
            } finally {
                cursor.close();
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void showTemplateImage() {
        String picturePath = preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, null);
        if (picturePath != null) {
            Drawable drawable = BitmapDrawable.createFromPath(picturePath);
            getWindow().setBackgroundDrawable(drawable);
        } else
            getWindow().setBackgroundDrawableResource(android.R.drawable.screen_background_light);
    }


    private class ConfigureNetworkingTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return netClient.configureNetworking();
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                setContentView(canvas);
                Toast.makeText(CanvasActivity.this, "Touch events will be sent to " + netClient.destAddress.getHostAddress() + ":" + NetworkClient.GFXTABLET_PORT, Toast.LENGTH_LONG).show();
            } else
                setContentView(R.layout.activity_no_host);
        }
    }

}
