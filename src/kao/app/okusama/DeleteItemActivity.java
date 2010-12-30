package kao.app.okusama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kao.app.okusama.async.DeleteItemTask;
import kao.app.okusama.db.DBHelper;

import org.kao.okusama.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeleteItemActivity extends Activity {
	private static final String TAG = "DeleteItemActivity";
	private DeleteItemActivity mActivity;
	private DeleteListAdapter adapter;
	private DBHelper helper;
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String value = sp.getString("orient_key", "");
		if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[0])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[1])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.delete_item_list);

		ListView listView = (ListView) findViewById(R.id.DeleteListView);
		ImageButton backButton = (ImageButton) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				backStockCheckActivity();
			}
		});
		mActivity = this;
		List<DBHelper.ItemData> itemList = new ArrayList<DBHelper.ItemData>();
		adapter = new DeleteListAdapter(this, itemList);
		listView.setAdapter(adapter);
	}

	private void createListView(){
		DeleteItemTask deleteItemTask = new DeleteItemTask(this, helper);
		deleteItemTask.execute(DeleteItemTask.GET_ITEMS_ACTION, new String("" + index));	
	}
	
	public void setResultInfo(List<HashMap<String, Object>> result) {
		HashMap<String,Object> map = (HashMap<String,Object>)result.get(0);
		String action = (String)map.get("action");
		if(action.equals(DeleteItemTask.GET_ITEMS_ACTION)){
			Object obj = map.get("result");
			if(obj instanceof String){
				//TODO エラー
				//showErrorCustomToast(getText(R.string.deleate_empty).toString());
				showErrorCustomToast("DBエラー");
				
			}else {
				List<DBHelper.ItemData> list = (List<DBHelper.ItemData>)obj;
				if (list.size() == 0) {
					showErrorCustomToast(getText(R.string.deleate_empty).toString());
					finish();
				}
				for (DBHelper.ItemData data : list) {
					adapter.add(data);
					adapter.notifyDataSetChanged();// アダプタを更新
				}	
			}
			
		}else if(action.equals(DeleteItemTask.DELETE_ACTION)){
			String obj = (String)map.get("result");
			if(obj.equals("error")){
				//TODO エラー
				//showErrorCustomToast(getText(R.string.deleate_empty).toString());
				showErrorCustomToast("DBエラー");
			}	
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "call onResume!!!!!!!!!!!!!!!!!!!");
		if (helper == null) {
			helper = new DBHelper(this);
		}
		Intent i = getIntent();
		index = i.getIntExtra("tabindex", 0);
		createListView();
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "call onPause!!!!!!!!!!!!!!!!!!!");
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "call onDestroy!!!!!!!!!!!!!!!!!!!");
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
	}

	private void backStockCheckActivity() {
		Intent i = getIntent();
		int inowIndex = i.getIntExtra("tabindex", 0);
		Intent intent = new Intent(getApplicationContext(),
				kao.app.okusama.StockCheckActivity.class);
		intent.putExtra("tabindex", inowIndex);
		setResult(RESULT_OK, intent);
		finish();
	}


	// 削除ダイアログの表示時は戻るボタンを無効
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private class DeleteListAdapter extends ArrayAdapter<DBHelper.ItemData> {
		private final List<DBHelper.ItemData> adapterItemList;
		private final LayoutInflater inflater;

		public DeleteListAdapter(Context context,
				List<DBHelper.ItemData> itemList) {
			super(context, R.layout.delete_list_row, itemList);
			this.adapterItemList = itemList;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return this.adapterItemList.size();
		}

		@Override
		public DBHelper.ItemData getItem(int position) {
			return this.adapterItemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// 画面に表示される毎に呼び出される
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				row = this.inflater.inflate(R.layout.delete_list_row, null);
			}
			final DBHelper.ItemData data = this.adapterItemList.get(position);
			ImageButton dButton = (ImageButton) row
					.findViewById(R.id.DeleteButton);
			dButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showAlertDialog(data);
				}
			});
			TextView text = (TextView) row.findViewById(R.id.TextItem);
			text.setText(data.name);
			return row;
		}
	}

	private void showAlertDialog(final DBHelper.ItemData data) {

		// ダイアログのタイトルのセット
		LayoutInflater inflater_t = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater_t.inflate(R.layout.delete_dialog_title,
				(ViewGroup) findViewById(R.id.FrameLayout01));
		layout.setBackgroundColor(R.color.beige);

		// ダイアログの中身のセット
		LayoutInflater inf = this.getLayoutInflater();
		View v = inf.inflate(R.layout.delete_dialog, null);
		TextView msg = (TextView) v.findViewById(R.id.dialogmsg);
		msg.setText(data.name + " "
				+ getText(R.string.deleate_dialog_msg).toString());

		final AlertDialog dialog = new AlertDialog.Builder(this).setView(v)
				.setInverseBackgroundForced(true).setCustomTitle(layout)
				.create();

		ImageButton yesButton = (ImageButton) v
				.findViewById(R.id.delete_yesbutton);
		yesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int deleteId = data.id;
				DeleteItemTask deleteItemTask = new DeleteItemTask(mActivity, helper);
				deleteItemTask.execute(DeleteItemTask.DELETE_ACTION, new String("" + deleteId));
				adapter.remove(data);
				adapter.notifyDataSetChanged();// アダプタを更新
				dialog.dismiss();
			}

		});

		ImageButton cancelButton = (ImageButton) v
				.findViewById(R.id.delete_cancelbutton);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	/**
	 * エラー用トースト表示
	 * @param msg1
	 */
	private void showErrorCustomToast(String msg1) {
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
