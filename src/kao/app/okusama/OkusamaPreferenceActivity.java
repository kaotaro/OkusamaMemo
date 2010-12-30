package kao.app.okusama;

import org.kao.okusama.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;

public class OkusamaPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	   private ListPreference mListPreference;
	@Override
	public void onCreate(Bundle bundle){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(bundle);
		addPreferencesFromResource(R.xml.pref);
		mListPreference = (ListPreference)getPreferenceScreen().findPreference("orient_key");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String value = sp.getString("orient_key", "");
		String summary = "";
		if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[0])){
			summary = getResources().getStringArray(R.array.prelist_entries)[0];
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[1])){
			summary = getResources().getStringArray(R.array.prelist_entries)[1];		
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		mListPreference.setSummary(summary); 
	 // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	  @Override
	    protected void onPause() {
	        super.onPause();

	        // Unregister the listener whenever a key changes            
	        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	    }
		@Override
	    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	        // Let's do something a preference value changes
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String value = sp.getString("orient_key", "");
			String summary = "";
			if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[0])){
				summary = getResources().getStringArray(R.array.prelist_entries)[0];
			}else if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[1])){
				summary = getResources().getStringArray(R.array.prelist_entries)[1];		
			}
			mListPreference.setSummary(summary); 
			
	    }

}
