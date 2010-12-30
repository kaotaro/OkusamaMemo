package kao.app.okusama;

import kao.app.okusama.db.DBHelper;

import org.kao.okusama.R;

import android.app.Activity;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddItemActivity extends Activity {
	private static final String TAG = "AddItemActivity";
	private DBHelper helper;
	private String selectedCategoly;
	private String addItemName;
	private static final int SELECT_IMAGE = 0;
	private static final int DEFAULT_ICON = 1;
	private static final int TAKE_PIC = 2;

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
		setContentView(R.layout.add_dialog);
		//helper = new DBHelper(this);
		
		Spinner categolySp = (Spinner) findViewById(R.id.categoly_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.select_categoly, R.layout.my_simple_spinner_item);
		adapter
				.setDropDownViewResource(R.layout.my_simple_spinner_dropdown_item);

		categolySp.setAdapter(adapter);
		categolySp
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						Spinner spinner = (Spinner) parent;
						selectedCategoly = (String) spinner.getSelectedItem();
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

		Intent i = getIntent();
		int tabIndex = i.getIntExtra("tabindex", 0);
		categolySp.setSelection(tabIndex);
		final RadioGroup iconsRadio = (RadioGroup) findViewById(R.id.icons_RadioGroup);
		final EditText itemName = (EditText) findViewById(R.id.edit_item_name);
		ImageButton nextButton = (ImageButton) findViewById(R.id.nextbutton);
		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addItemName = itemName.getText().toString();
				// 未入力チェック
				if (addItemName.equals("")) {
					showErrorCustomToast(getText(R.string.input_error1)
							.toString());
					return;
				}
				// DB重複チェック
				if (!helper.checkDuplicationName(selectedCategoly, addItemName)) {
					showErrorCustomToast(getText(R.string.input_error2)
							.toString());
					itemName.setText("");
					return;
				}

				int selectRadioId = iconsRadio.getCheckedRadioButtonId();
				if (selectRadioId == R.id.icon01) {
					// 既存アイコンの一覧ダイアログ表示
					Intent intent = new Intent(getApplicationContext(),
							IconsDialogActivity.class);
					intent.putExtra("itemname", addItemName);
					intent.putExtra("tablename", selectedCategoly);
					intent.putExtra("tabindex", getTabIndex(selectedCategoly));
					startActivityForResult(intent, DEFAULT_ICON);
				} else if (selectRadioId == R.id.icon02) {
					// ギャラリーからイメージを取得
					startActivityForResult(
							new Intent(
									Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
							SELECT_IMAGE);
				} else if (selectRadioId == R.id.icon03) {
					// カメラ起動
					Intent intent = new Intent(getApplicationContext(),
							IconCameraActivity.class);
					intent.putExtra("itemname", addItemName);
					intent.putExtra("tablename", selectedCategoly);
					intent.putExtra("tabindex", getTabIndex(selectedCategoly));
					startActivityForResult(intent, TAKE_PIC);
				}
			}
		});

		ImageButton cancelButton = (ImageButton) findViewById(R.id.cancelbutton);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backStockCheckActivity();
			}
		});

		final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		itemName.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// // ここではEditTextに改行が入らないようにしている。
				// if (event.getAction() == KeyEvent.ACTION_DOWN) {
				// return true;
				// }
				// Enterを離したときに検索処理を実行
				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					inputMethodManager.hideSoftInputFromWindow(v
							.getWindowToken(), 0); // vはViewクラス派生のインスタンス
					return true;
				}
				return false;
			}
		});

	}

	public int getTabIndex(String name) {
		String[] arrayStr = getResources().getStringArray(
				R.array.select_categoly);
		for (int i = 0; i < arrayStr.length; i++) {
			if (arrayStr[i].equals(name)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_IMAGE) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				// DB登録実行
				if(helper == null){
					helper = new DBHelper(this);
				}
				try{
				helper.addItem(selectedCategoly, selectedImage.toString(),
						addItemName);
				}catch(Exception e){
					Toast.makeText(getApplicationContext(), "アイテムの追加が正常にできませんでした",Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}
		if (requestCode == SELECT_IMAGE || requestCode == TAKE_PIC
				|| requestCode == DEFAULT_ICON) {
			if (resultCode == Activity.RESULT_OK) {
				backStockCheckActivity();
			}
		}
	}

	private void backStockCheckActivity() {
		int inowIndex = getTabIndex(selectedCategoly);
		Intent intent = new Intent(getApplicationContext(),
				kao.app.okusama.StockCheckActivity.class);
		intent.putExtra("tabindex", inowIndex);
		setResult(RESULT_OK, intent);
		finish();
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
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "call onResume!!!!!!!!!!!!!!!!!!!");
		if(helper == null){
			helper = new DBHelper(this);
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

	// 追加ダイアログの表示時は戻るボタンを無効
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

}
