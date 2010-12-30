package kao.app.okusama;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kao.app.okusama.db.DBHelper;
import kao.app.okusama.util.DefaultDrawableCollection;

import org.kao.okusama.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

public class IconsDialogActivity extends Activity {
	private static final String TAG = "IconsDialogActivity";
	private DBHelper helper;
	private GridView iconGrid;
	private IconsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "call onCreate!!!!!!!!!!!!!!!!!!!");
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String value = sp.getString("orient_key", "");
		if (value.equals(getResources().getStringArray(
				R.array.prelist_enttryvalues)[0])) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (value.equals(getResources().getStringArray(
				R.array.prelist_enttryvalues)[1])) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.icon_dialog);
		
		iconGrid = (GridView) findViewById(R.id.icon_grid);
		if (adapter == null) {
			adapter = new IconsAdapter();
			iconGrid.setAdapter(adapter);
			iconGrid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					List<Integer> ids = adapter.getList();
					Integer selectIconId = (Integer) (ids.get(position));
					DefaultDrawableCollection dc = DefaultDrawableCollection
							.getDrawableCollection();
					String iconName = dc.getResourceName(selectIconId);
					Intent intent = getIntent();
					String itemName = intent.getCharSequenceExtra("itemname")
							.toString();
					String tableName = intent.getCharSequenceExtra("tablename")
							.toString();
					helper.addItem(tableName, iconName, itemName);
					setResult(RESULT_OK);
					finish();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (helper == null) {
			helper = new DBHelper(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "call onPause!!!!!!!!!!!!!!!!!!!");
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "call onDestroy!!!!!!!!!!!!!!!!!!!");
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
	}

	public class IconsAdapter extends BaseAdapter {
		private List<Integer> ids = new ArrayList<Integer>();

		public IconsAdapter() {
			DefaultDrawableCollection dc = DefaultDrawableCollection
					.getDrawableCollection();
			Iterator<Integer> ite = dc.getResourceIds();

			while (ite.hasNext()) {
				Integer tmp = (Integer) ite.next();
				ids.add(tmp);
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
		
			if (convertView == null) { // if it's not recycled, initialize some
				// attributes
				imageView = new ImageView(getApplicationContext());
				imageView.setLayoutParams(new GridView.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource((Integer) (ids.get(position)));
			return imageView;
		}

		public final int getCount() {
			return ids.size();
		}

		public final Object getItem(int position) {
			return ids.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public final List<Integer> getList() {
			return ids;
		}
	}

}
