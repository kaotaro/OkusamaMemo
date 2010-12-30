package kao.app.okusama;

import org.kao.okusama.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TopActivity extends Activity {
	private static final String Tag = "TopActivity";
	private static final int MENU_ID1 = (Menu.FIRST + 1);
	private static final int MENU_ID2 = (Menu.FIRST + 2);
	
	private SharedPreferences pref;
	
    public boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if( ni != null ){
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ID1, Menu.NONE, getResources().getText(R.string.menu_setting));
		menu.add(Menu.NONE, MENU_ID2, Menu.NONE, getResources().getText(R.string.menu_exit));
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		boolean ret = true;
		switch (item.getItemId()) {
		default:
			ret = super.onMenuItemSelected(featureId, item);
			break;
		case MENU_ID1:
			Intent intent = new Intent(getApplicationContext(),OkusamaPreferenceActivity.class);
			startActivity(intent);
			ret = true;
			break;
		case MENU_ID2:
			finish();
			ret = true;
			break;	
			
		}
		return ret;
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(Tag, "onCreate()++++++");
		pref = getSharedPreferences("info_pref", MODE_WORLD_READABLE
				| MODE_WORLD_WRITEABLE);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String value = sp.getString("orient_key", "");
		if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[0])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[1])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		boolean showed = pref.getBoolean("infotoast", false);
		if (!showed) {
			showInfoCustomToast(getResources().getText(R.string.first_info).toString());
		}	
		ImageView aboutButton = (ImageView) findViewById(R.id.aboutButton);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//ネットワークが使用可能かチェックしてOKだったらIntent
				//NGだったらエラーToast
				boolean net = isConnected(getApplicationContext());
				if(!net){
					Context c = getApplicationContext();
					showErrorCustomToast(c.getResources().getText(R.string.no_network).toString());
					return;
				}else{
					Intent i = new Intent(getApplicationContext(), AboutOkusamaDialog.class);
					startActivity(i);
				}
				//add v1.0.1
				//finish();
			}
		});
		ImageView stockCheckButton = (ImageView) findViewById(R.id.listButton);
		stockCheckButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						kao.app.okusama.StockCheckActivity.class);
				startActivity(intent);
				//add v1.0.1
				finish();
			}
		});
		ImageView memoButton = (ImageView) findViewById(R.id.mamoButton);
		memoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	        	Intent intent = new Intent(getApplicationContext(),
						kao.app.okusama.MemoToBuyActivity.class);
				startActivity(intent);
				//add v1.0.1
				finish();
			}
		});
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.i(Tag, "onResume()");
		pref = getSharedPreferences("info_pref", MODE_WORLD_READABLE
				| MODE_WORLD_WRITEABLE);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String value = sp.getString("orient_key", "");
		if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[0])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[1])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
	}
	
	public void showInfoCustomToast(String msg1) {
		Editor e = pref.edit();
		e.putBoolean("infotoast", true);
		e.commit();		
		
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		TextView text1 = (TextView) layout.findViewById(R.id.text1);
		text1.setText(msg1);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	public void showErrorCustomToast(String msg1) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.error);

		TextView text1 = (TextView) layout.findViewById(R.id.text1);
		text1.setText(msg1);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
}