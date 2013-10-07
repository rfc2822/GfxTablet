package at.bitfire.gfxtablet;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import java.util.List;

import at.bitfire.gfxtablet.R;

public class SettingsActivity extends PreferenceActivity {
    public static final String
		KEY_PREF_HOST = "host_preference",
		KEY_PREF_STYLUS_ONLY = "stylus_only_preference",
		KEY_PREF_FULLSCREEN = "fullscreen_preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if (item.getItemId() == android.R.id.home)
    	   finish();
       return false;
    }
    
    
    public static class NetworkPrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.network_preferences);
        }
    }
        
    public static class DrawingPrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.drawing_preferences);
        }
    }
}
