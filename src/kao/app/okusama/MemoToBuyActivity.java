package kao.app.okusama;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kao.app.okusama.db.DBHelper;

import org.kao.okusama.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MemoToBuyActivity extends Activity implements OnClickListener {

	private static final String TAG = "MemoToBuyActivity";
	private static final int PICK_CONTACT_REQUEST = 0;
	//コンタクトピッカーの1.x系、2.x系コンパチのためのリフレクション
	private final ContactAccessor mContactAccessor = ContactAccessor
			.getInstance();
	
	
	private DBHelper helper;;
	private MemoToBuyAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String value = sp.getString("orient_key", "");
		if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[0])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else if(value.equals(getResources().getStringArray(R.array.prelist_enttryvalues)[1])){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.memo_list);

		//helper = new DBHelper(this);
		ListView listView = (ListView) findViewById(R.id.MemoListView);
		listView.setItemsCanFocus(false);

		ImageButton toTopButton = (ImageButton) findViewById(R.id.to_top2_b);
		toTopButton.setOnClickListener(this);
		ImageButton mailButton = (ImageButton) findViewById(R.id.mail_b);
		mailButton.setOnClickListener(this);
		ImageButton updateButton = (ImageButton) findViewById(R.id.update_b);
		updateButton.setOnClickListener(this);

		// 検索結果のリストをListViewにAdaptする
		List<String> listToBuy = new ArrayList<String>();
		adapter = new MemoToBuyAdapter(this, listToBuy);
		listView.setAdapter(adapter);
		
		//TODO
		MemoAppWidget.updateAppWidget(this);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(helper == null){
			helper = new DBHelper(this);
		}
		
		
		List<String> listToBuy = adapter.getAdapterItemList();
		// adapterから取得したlistToBuyはadapterにセットされたリストそのもの
		// なのでlistToBuyをクリアしたりaddしたりするのはadapterにそれをするのと同じ
		listToBuy.clear();
		// DBのアイテムでflagが0のアイテムを検索、listToBuyに保存
		listToBuy = addListToBuy(listToBuy, DBHelper.T1_FOOD);
		listToBuy = addListToBuy(listToBuy, DBHelper.T2_KITCHEN);
		listToBuy = addListToBuy(listToBuy, DBHelper.T3_WASHROOM);
		listToBuy = addListToBuy(listToBuy, DBHelper.T4_BATHROOM);
		listToBuy = addListToBuy(listToBuy, DBHelper.T5_LIVING);
		listToBuy = addListToBuy(listToBuy, DBHelper.T6_ETC);

		ListView listView = (ListView) findViewById(R.id.MemoListView);
		TextView emptyText = (TextView) findViewById(R.id.memo_empty);
		if (listToBuy.size() == 0) {
			listView.setVisibility(View.GONE);
			emptyText.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			emptyText.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "call onPause!!!!!!!!!!!!!!!!!!!");
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
		// finish();
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

	private List<String> addListToBuy(List<String> listToBuy, String tableName) {
		List<String> memoItemList = helper.getItemList(tableName, 0);
		for (String memoItem : memoItemList) {
			listToBuy.add(memoItem);
		}
		return listToBuy;
	}

	/**
	 * メール送信ダイアログでコンタクトリストから選択だった場合 コンタクトリストのActivityから帰ってきたときに呼ばれる
	 * RecestCode:PICK_CONTACT_REQUEST
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == PICK_CONTACT_REQUEST) && (resultCode == RESULT_OK)) {
			Map<String, String> result = mContactAccessor.getEmailData(this,data);
			String name = result.get("name");
			String email = result.get("email");
			sendMail(name, email);
		}
	}

	/**
	 * メール送信機能 チェックの入っていないアイテムを買い物メモとしてメール送信
	 * 
	 * @param name
	 * @param email
	 */
	private void sendMail(String name, String email) {

		String msg1 = (getText(R.string.send_name).toString() + name);
		String msg2 = (getText(R.string.send_address).toString() + email);

		showSendDataCustomToast(msg1, msg2);

		final Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setData(Uri.parse("mailto:" + email));
		it.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.mail_title)
				.toString());

		StringBuffer sb = new StringBuffer();
		sb.append(getText(R.string.mail_msg1).toString());
		sb.append("\n");
		sb.append("\n");
		sb.append(getText(R.string.mail_msg2).toString());
		sb.append("\n");

		ListView listView = (ListView) findViewById(R.id.MemoListView);
		List<String> listToBuy = adapter.getAdapterItemList();
		for (String item : listToBuy) {
			int pos = adapter.getPosition(item);
			if (!(listView.isItemChecked(pos))) {
				sb.append(item);
				sb.append("\n");
			}
		}
		it.putExtra(Intent.EXTRA_TEXT, sb.toString());
		
		// メール送信した人情報をDBに保持
		if(helper == null){
			helper = new DBHelper(this);
		}
		List<DBHelper.SendedData> list = helper.getSendedData();
		if (list.size() > 0) {
			for (DBHelper.SendedData data : list) {
				helper.deleteSendedData(data.id);
			}
		}
		helper.addSendedData(helper.getSendedDataInstance(name, email));
		startActivity(it);

	}

	/**
	 * メール送信時のダイアログビルダー作成
	 * 
	 * @param builder
	 * @return
	 */

	private void showMailDialog() {

		// ダイアログのタイトルのセット
		LayoutInflater inflater_t = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater_t.inflate(R.layout.mail_dialog_title,
				(ViewGroup) findViewById(R.id.FrameLayout01));
		layout.setBackgroundColor(R.color.beige);

		// ダイアログの中身のセット
		LayoutInflater inf = this.getLayoutInflater();
		View v = inf.inflate(R.layout.mail_dialog, null);

		final RadioGroup mailGroup = (RadioGroup) v
				.findViewById(R.id.mail_radio_group);

		final RadioButton histryButton = (RadioButton) v
				.findViewById(R.id.histryradio);
		final RadioButton contactButton = (RadioButton) v
				.findViewById(R.id.contactradio);
		final RadioButton directButton = (RadioButton) v
				.findViewById(R.id.directinput);
		final EditText adressEdit = (EditText) v
				.findViewById(R.id.input_address);

		mailGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == (R.id.directinput)) {
					adressEdit.setVisibility(View.VISIBLE);
					adressEdit.requestFocus();
				} else {
					adressEdit.setText("");
					adressEdit.setVisibility(View.GONE);
				}
			}
		});

		final AlertDialog dialog = new AlertDialog.Builder(this).setView(v)
				.setInverseBackgroundForced(true).setCustomTitle(layout)
				.create();

		ImageButton yesButton = (ImageButton) v.findViewById(R.id.yesbutton);
		yesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 前回送信した人に送信
				if (histryButton.isChecked()) {
					// DBから前回送信した人を取得
					List<DBHelper.SendedData> list = helper.getSendedData();
					if (list.size() == 0) {
						showErrorCustomToast(getText(R.string.no_history)
								.toString());
					} else {
						for (DBHelper.SendedData data : list) {
							sendMail(data.name, data.address);
						}
					}
				} else if (contactButton.isChecked()) {
					// コンタクトリストから選択
					startActivityForResult(mContactAccessor.getContactPickerIntent(),
							PICK_CONTACT_REQUEST);

				} else if (directButton.isChecked()) {
					String mailAddress = adressEdit.getText().toString();
					if (mailAddress.equals("")) {
						showErrorCustomToast(getText(R.string.no_input_address)
								.toString());
					} else {
						sendMail("Direct Input", mailAddress);
					}
				}

				dialog.dismiss();
			}
		});

		ImageButton cancelButton = (ImageButton) v
				.findViewById(R.id.cancelbutton);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();

	}

	@Override
	public void onClick(View v) {

		ImageButton toTopButton = (ImageButton) findViewById(R.id.to_top2_b);
		ImageButton mailButton = (ImageButton) findViewById(R.id.mail_b);
		ImageButton updateButton = (ImageButton) findViewById(R.id.update_b);
		List<String> listToBuy = adapter.getAdapterItemList();
		if (v == toTopButton) {
			Intent i1 = new Intent(getApplicationContext(),
					kao.app.okusama.TopActivity.class);
			startActivity(i1);
			finish();
		} else if (v == mailButton) {
			if (listToBuy.size() == 0) {
				showErrorCustomToast(getText(R.string.memo_empty).toString());
				return;
			}

			showMailDialog();

		} else if (v == updateButton) {
			ListView listView = (ListView) findViewById(R.id.MemoListView);
			// 買い物リストのチェック状態をDBに更新する
			for (String checkedItem : listToBuy) {
				int pos = adapter.getPosition(checkedItem);
				if (listView.isItemChecked(pos)) {
					for (String tableName : DBHelper.CTEGOLY_LIST) {
						List<DBHelper.ItemData> list = helper.getItemData(
								tableName, checkedItem);
						helper.updateFlag(list, 1);
					}
				}
			}
			// この画面は終了して在庫チェックの画面へ遷移
			Intent i = new Intent(this, StockCheckActivity.class);
			startActivity(i);
			finish();
		}
	}

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
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	private void showSendDataCustomToast(String msg1, String msg2) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setVisibility(View.GONE);

		TextView text1 = (TextView) layout.findViewById(R.id.text1);
		text1.setText(msg1);
		TextView text2 = (TextView) layout.findViewById(R.id.text2);
		text2.setVisibility(View.VISIBLE);
		text2.setText(msg2);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	private class MemoToBuyAdapter extends ArrayAdapter<String> {
		private final List<String> adapterItemList;

		public MemoToBuyAdapter(Context context, List<String> itemList) {
			super(context, R.layout.memo_list_row, itemList);
			this.adapterItemList = itemList;
		}

		@Override
		public int getCount() {
			return this.adapterItemList.size();
		}

		@Override
		public String getItem(int position) {
			return this.adapterItemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public List<String> getAdapterItemList() {
			return adapterItemList;
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}


	/**
	 * トラックボールを無効にする
	 */
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		return true;
	}
	
}
