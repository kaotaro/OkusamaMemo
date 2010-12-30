package kao.app.okusama;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kao.app.okusama.async.DeleteItemTask;
import kao.app.okusama.async.StockCheckTask;
import kao.app.okusama.db.DBHelper;
import kao.app.okusama.util.DefaultDrawableCollection;

import org.kao.okusama.R;

import android.app.Activity;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

public class StockCheckActivity extends TabActivity implements OnClickListener{

	private static final String TAG = "StockCheckActivity";
	private static final int DELETE_ITEM = 0;
	private static final int ADD_ITEM = 1;

	private int nowTabIndex = 0;
	private int beforeTabIndex = 0;
	private int upDateTabIndex = -1;
	private DBHelper helper;
	private int tabIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "call onCreate!!!!!!!!!!!!!!!!!!!");
		setTitle("");
		
		//helper = new DBHelper(this);

		TabHost tabHost = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.stockcheck_list,
				tabHost.getTabContentView(), true);
		for (int i = 0; i < 6; i++) {
			createTab(i, tabHost);
		}

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				TabHost tabHost = getTabHost();
				nowTabIndex = tabHost.getCurrentTab();
				if (nowTabIndex != beforeTabIndex) {
					upDateTabIndex = beforeTabIndex;
					updateDBCurrentList(upDateTabIndex);
					beforeTabIndex = tabHost.getCurrentTab();
				} else {
					Log.d(TAG, "updateDBCurrentList NON UPDATE!!!!");
				}
				getItemList(tabHost.getCurrentTab());
			}
		});
		
		for (int i = 0; i < 6; i++) {
			ListView listView = getListView(i);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listView.setItemsCanFocus(false);

			AdapterEx adapter = new AdapterEx(this,
					new ArrayList<DBHelper.ItemData>());
			createListView(adapter, listView);
		}

		ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_b);
		ImageButton addButton = (ImageButton) findViewById(R.id.add_b);
		ImageButton memoButton = (ImageButton) findViewById(R.id.to_memo_b);
		ImageButton toTopButton = (ImageButton) findViewById(R.id.to_top_b);

		deleteButton.setOnClickListener(this);
		addButton.setOnClickListener(this);
		memoButton.setOnClickListener(this);
		toTopButton.setOnClickListener(this);


		getTabHost().setFocusableInTouchMode(false);
		
	}

	public void createTab(int tabIndex, TabHost tabHost) {
		ImageView tabView = new ImageView(this);
		tabView.setBackgroundColor(getResources().getColor(R.color.beige));
		tabView.setAdjustViewBounds(true);
		if (tabIndex == 0) {
			tabView.setImageResource(R.drawable.tab1_stateful);
			tabHost.addTab(tabHost
					.newTabSpec(getText(R.string.food).toString())
					.setIndicator(tabView).setContent(R.id.LinearLayout01));
		} else if (tabIndex == 1) {
			tabView.setImageResource(R.drawable.tab2_stateful);
			tabHost.addTab(tabHost.newTabSpec(
					getText(R.string.kitchen).toString()).setIndicator(tabView)
					.setContent(R.id.LinearLayout02));
		} else if (tabIndex == 2) {
			tabView.setImageResource(R.drawable.tab3_stateful);
			tabHost.addTab(tabHost.newTabSpec(
					getText(R.string.washroom).toString())
					.setIndicator(tabView).setContent(R.id.LinearLayout03));
		} else if (tabIndex == 3) {
			tabView.setImageResource(R.drawable.tab4_stateful);
			tabHost.addTab(tabHost.newTabSpec(
					getText(R.string.bathroom).toString())
					.setIndicator(tabView).setContent(R.id.LinearLayout04));
		} else if (tabIndex == 4) {
			tabView.setImageResource(R.drawable.tab5_stateful);
			tabHost.addTab(tabHost.newTabSpec(
					getText(R.string.living).toString()).setIndicator(tabView)
					.setContent(R.id.LinearLayout05));
		} else if (tabIndex == 5) {
			tabView.setImageResource(R.drawable.tab6_stateful);
			tabHost.addTab(tabHost.newTabSpec(getText(R.string.etc).toString())
					.setIndicator(tabView).setContent(R.id.LinearLayout06));
		}
	}

	public void createListView(AdapterEx adapter, final ListView listView) {
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView,
					View selectedView, int position, long id) {
				CheckedTextView ct = (CheckedTextView) selectedView
						.findViewById(R.id.CheckedItemName);
				List<DBHelper.ItemData> itemList = ((AdapterEx) ((ArrayAdapter<DBHelper.ItemData>) listView
						.getAdapter())).getAdapterList();
				for (DBHelper.ItemData data : itemList) {
					if (data.name.equals(ct.getText().toString())) {
						if (data.flag == 0) {
							data.flag = 1;
						} else {
							data.flag = 0;
						}
						break;
					}
				}
			}
		});
	}

	public ListView getListView(int tabIndex) {
		ListView listView = null;
		if (tabIndex == 0) {
			listView = (ListView) findViewById(R.id.ListView01);
		} else if (tabIndex == 1) {
			listView = (ListView) findViewById(R.id.ListView02);
		} else if (tabIndex == 2) {
			listView = (ListView) findViewById(R.id.ListView03);
		} else if (tabIndex == 3) {
			listView = (ListView) findViewById(R.id.ListView04);
		} else if (tabIndex == 4) {
			listView = (ListView) findViewById(R.id.ListView05);
		} else if (tabIndex == 5) {
			listView = (ListView) findViewById(R.id.ListView06);
		}
		return listView;
	}

	public TextView getTextView(int tabIndex) {
		TextView emptyText = null;
		if (tabIndex == 0) {
			emptyText = (TextView) findViewById(R.id.empty1);
		} else if (tabIndex == 1) {
			emptyText = (TextView) findViewById(R.id.empty2);
		} else if (tabIndex == 2) {
			emptyText = (TextView) findViewById(R.id.empty3);
		} else if (tabIndex == 3) {
			emptyText = (TextView) findViewById(R.id.empty4);
		} else if (tabIndex == 4) {
			emptyText = (TextView) findViewById(R.id.empty5);
		} else if (tabIndex == 5) {
			emptyText = (TextView) findViewById(R.id.empty6);
		}
		return emptyText;
	}

	public List<DBHelper.ItemData> getItemList(int tabIndex,
			DefaultDrawableCollection dc) {
		List<DBHelper.ItemData> itemList = null;
		if (tabIndex == 0) {
			itemList = helper.fetchAllRowsListData(DBHelper.T1_FOOD, dc);
		} else if (tabIndex == 1) {
			itemList = helper.fetchAllRowsListData(DBHelper.T2_KITCHEN, dc);
		} else if (tabIndex == 2) {
			itemList = helper.fetchAllRowsListData(DBHelper.T3_WASHROOM, dc);
		} else if (tabIndex == 3) {
			itemList = helper.fetchAllRowsListData(DBHelper.T4_BATHROOM, dc);
		} else if (tabIndex == 4) {
			itemList = helper.fetchAllRowsListData(DBHelper.T5_LIVING, dc);
		} else if (tabIndex == 5) {
			itemList = helper.fetchAllRowsListData(DBHelper.T6_ETC, dc);
		}
		return itemList;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "call onResume!!!!!!!!!!!!!!!!!!!");
		if(helper == null){
			helper = new DBHelper(this);
		}
		// 前の画面から受け取ったタブインデックスのタブのリストを描画
		Intent i = getIntent();
		int index = i.getIntExtra("tabindex", 0);
		TabHost tabHost = getTabHost();
		tabHost.setCurrentTab(index);
		nowTabIndex = tabHost.getCurrentTab();
		beforeTabIndex = tabHost.getCurrentTab();
		upDateTabIndex = -1;
		getItemList(index);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "call onPause!!!!!!!!!!!!!!!!!!!");
		// 今表示されているのタブのリストの内容をDBに上書きする
		updateDBCurrentList(nowTabIndex);	
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
	}	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "call onDestroy!!!!!!!!!!!!!!!!!!!");
		//helper.cleanup();
		
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
	}
	
	/**
	 * 戻るボタンが押されたらアプリ終了
	 */
	@Override 
	public boolean dispatchKeyEvent(KeyEvent event) { 
		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
			finish();
	        return true; 
	        } 
	        return super.dispatchKeyEvent(event); 
	} 

	/**
	 * 指定のタブにあるListのデータをDBにアップデートする
	 * @param tabIndex 0-5:タブのID
	 */
	public void updateDBCurrentList(int tabIndex) {
		Log.d(TAG, "call updateDBCurrentList   tabIndex =   " + tabIndex);
		ListView listView = getListView(tabIndex);
		List<DBHelper.ItemData> itemList = ((AdapterEx) ((ArrayAdapter) listView
				.getAdapter())).getAdapterList();
		helper.updateFlag(itemList);
	}

	/**
	 * ビューの表示／非表示
	 * @param flag　true:list表示/false:list非表示
	 */
	public void setViewVisible(boolean flag, ListView listView,
			TextView textView) {
		if (flag) {
			listView.setVisibility(View.VISIBLE);
			textView.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.GONE);
			textView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * リストの描画のためのアイテムをDBから取得
	 * @param tabIndex
	 */
	public void getItemList(int tabIndex) {
		Log.d(TAG, "call getItemList!!!!!!!!!!!!!!!!!!!   tabIndex = " + tabIndex);
		StockCheckTask stockCheckTask = new StockCheckTask(this, helper);
		stockCheckTask.execute(DeleteItemTask.GET_ITEMS_ACTION, new String("" + tabIndex));	
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
				Log.d(TAG, "call getItemList!!!!!!!!!!!!!!!!!!!   tabIndex = " + tabIndex);
//				DefaultDrawableCollection dc = DefaultDrawableCollection
//				.getDrawableCollection();
				ListView listView = getListView(tabIndex);
				TextView emptyText = getTextView(tabIndex);
				AdapterEx adapter = ((AdapterEx) ((ArrayAdapter<DBHelper.ItemData>) listView.getAdapter()));
				
				List<DBHelper.ItemData> list = (List<DBHelper.ItemData>)obj;
				if (list.size() == 0) {
					//空っぽ表示
					setViewVisible(false, listView, emptyText);
					return;
				}
				
				setViewVisible(true, listView, emptyText);
				for (DBHelper.ItemData data : list) {
					adapter.add(data);
					adapter.notifyDataSetChanged();// アダプタを更新
				}
				for (int i = 0; i < adapter.getCount(); i++) {
					DBHelper.ItemData data = (DBHelper.ItemData) adapter.getItem(i);
					if (data.flag == 1) {
						listView.setItemChecked(i, true);
					} else {
						listView.setItemChecked(i, false);
					}
				}
				Log.d(TAG, "ItemList " + tabIndex + 1 + " fill Done!");
			}
			
		}
	
	}


//	/**
//	 * リストの描画
//	 * @param listView
//	 * @param emptyText
//	 * @param itemList
//	 */
//	public void fillData(ListView listView, TextView emptyText,
//			List<DBHelper.ItemData> itemList) {
//		AdapterEx adpter = ((AdapterEx) ((ArrayAdapter<DBHelper.ItemData>) listView.getAdapter()));
//		List<DBHelper.ItemData> aList = adpter.getAdapterList();
//		aList.clear();
//
//		if (itemList.size() == 0) {
//			setViewVisible(false, listView, emptyText);
//		} else {
//			setViewVisible(true, listView, emptyText);
//			for (DBHelper.ItemData data : itemList) {
//				aList.add(data);
//			}
//			for (int i = 0; i < adpter.getCount(); i++) {
//				DBHelper.ItemData data = (DBHelper.ItemData) adpter.getItem(i);
//				if (data.flag == 1) {
//					listView.setItemChecked(i, true);
//				} else {
//					listView.setItemChecked(i, false);
//				}
//			}
//		}
//	}
	
	@Override
	public void onClick(View v) {
		ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_b);
		ImageButton addButton = (ImageButton) findViewById(R.id.add_b);
		ImageButton memoButton = (ImageButton) findViewById(R.id.to_memo_b);
		ImageButton toTopButton = (ImageButton) findViewById(R.id.to_top_b);
		
		if (v == deleteButton) {
			ListView listView = getListView(getTabHost().getCurrentTab());
			List<DBHelper.ItemData> itemList = getItemList(listView);

			if (itemList == null || itemList.size() == 0) {
				showErrorCustomToast(getText(R.string.deleate_empty).toString());
				return;
			}

			Intent intent = new Intent(getApplicationContext(),
					kao.app.okusama.DeleteItemActivity.class);
			intent.putExtra("tabindex", getTabHost().getCurrentTab());
			startActivityForResult(intent, DELETE_ITEM);
		} else if (v == addButton) {		
			//start
			//v1.0.1 HT03Aでトラックボール使ったあとソフトウェアキーボードを戻るで非表示にするとTabHostの
			//onTouchModeChangedでヌルポになるバグの対処
			this.getTabHost().getTabContentView().setVisibility(View.GONE);
			//end
			Intent intent = new Intent(getApplicationContext(),
					kao.app.okusama.AddItemActivity.class);
			intent.putExtra("tabindex",getTabHost().getCurrentTab());
			startActivityForResult(intent, ADD_ITEM);
		} else if (v == memoButton) {
			Intent intent = new Intent(getApplicationContext(),
					kao.app.okusama.MemoToBuyActivity.class);
			startActivity(intent);
			finish();
		} else if (v == toTopButton) {
			Intent intent = new Intent(getApplicationContext(),
					kao.app.okusama.TopActivity.class);
			startActivity(intent);
			finish();
		}
	}

	/**
	 * 削除ダイアログ、または追加ダイアログから戻ってきたときに呼ばれる
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == DELETE_ITEM || requestCode == ADD_ITEM) {
			if (resultCode == Activity.RESULT_OK) {
				showStockCheckActivity(data);
			}
		}
	}
	
	
	/**
	 * ダイアログから戻ったあとにこのActivityを再表示
	 * @param data
	 */
	private void showStockCheckActivity(Intent data){
		tabIndex = data.getIntExtra("tabindex", 0);
		TabHost tabHost = getTabHost();
		tabHost.setCurrentTab(tabIndex);
		getItemList(tabIndex);
	}

	/**
	 * トラックボールを無効にする
	 */
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// ここでトラックボール動かすと落ちるので無効にする。
	
		return true;
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

	/**
	 * アダプターにセットされているアイテムリストを取得する
	 * @param listView
	 * @return
	 */
	public List<DBHelper.ItemData> getItemList(ListView listView) {
		AdapterEx adpter = ((AdapterEx) ((ArrayAdapter<DBHelper.ItemData>) listView.getAdapter()));
		return adpter.getAdapterList();
	}	
	
	/**
	 * リストのアダプタークラス
	 * @author kaorin
	 */
	private class AdapterEx extends ArrayAdapter<DBHelper.ItemData> {
		private final List<DBHelper.ItemData> adapterItemList;

		public AdapterEx(Context context, List<DBHelper.ItemData> itemList) {
			super(context, R.layout.stockcheck_list_row, itemList);
			this.adapterItemList = itemList;
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

		public List<DBHelper.ItemData> getAdapterList() {
			return adapterItemList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			DBHelper.ItemData data = this.adapterItemList.get(position);

			View row = convertView;
			if (row == null) {
				row = new CheckableLinearLayout(getApplicationContext());
			}
			CheckedTextView ct = (CheckedTextView) row
					.findViewById(R.id.CheckedItemName);
			ct.setText(data.name);

			ImageView icon = (ImageView) row.findViewById(R.id.ImageView01);
			if (data.imageId == -1) {
				// drawableでアプリが持っている既存のアイコン以外の画像がアイテムにセットされている
				// カメラ撮影画像、ギャラリーの画像
				String imageUri = data.image;
				if (imageUri.startsWith("content://media/")) {

					InputStream imgis;
					BitmapFactory.Options bm_opt;
					int zoomx, zoomy;
					Uri uri = Uri.parse(imageUri);
					ContentResolver _cont_reslv = getApplicationContext()
							.getContentResolver();
					bm_opt = new BitmapFactory.Options();
					try {
						imgis = _cont_reslv.openInputStream(uri);
						bm_opt.inJustDecodeBounds = true;
						Bitmap bitmap = BitmapFactory.decodeStream(imgis, null,
								bm_opt);
						imgis.close();
						zoomx = (int) Math
								.floor((double) bm_opt.outWidth / 100);
						zoomy = (int) Math
								.floor((double) bm_opt.outHeight / 100);
						bm_opt.inJustDecodeBounds = false;
						bm_opt.inPurgeable = true;
						bm_opt.inSampleSize = Math.max(zoomx, zoomy);
						bm_opt.inPreferredConfig = Bitmap.Config.RGB_565;
						imgis = _cont_reslv.openInputStream(uri);
						bitmap = BitmapFactory
								.decodeStream(imgis, null, bm_opt);
						imgis.close();

						if (bitmap == null) {
							// 代替えの画像表示
							icon.setImageResource(R.drawable.ic_no_image);
						} else {
							icon.setImageBitmap(bitmap);
							bitmap = null;
						}
					} catch (FileNotFoundException e) {
						Log.e(TAG, "Bitmap error...", e);
					} catch (IOException e) {
						Log.e(TAG, "Bitmap error...", e);
					}

				} else {
					// イメージ画像がない
					icon.setImageResource(R.drawable.ic_no_image);
				}
			} else {
				// 既存のアイコン表示
				icon.setImageResource(data.imageId);
			}
			return row;
		}
	}
	
	/**
	 * チェック付きテキストビューのカスタムレイアウト
	 * @author kaorin
	 */
	public class CheckableLinearLayout extends LinearLayout implements
			Checkable {
		private CheckedTextView _checkedTextView;

		public CheckableLinearLayout(Context context) {
			super(context);

			View view = ((LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.stockcheck_list_row, this, false);
			_checkedTextView = (CheckedTextView) view
					.findViewById(R.id.CheckedItemName);

			addView(view);
		}

		@Override
		public boolean isChecked() {
			return _checkedTextView.isChecked();
		}

		@Override
		public void setChecked(boolean checked) {
			_checkedTextView.setChecked(checked);
		}

		@Override
		public void toggle() {
			setChecked(!isChecked());
		}
	}
	
	//Activityのコンフィギュレーション状態変化時のイベント
	//ただしmanifestに対象を記載すること
//	@Override
//	public void  onConfigurationChanged (Configuration newConfig){
//		super.onConfigurationChanged(newConfig);
//		if(newConfig.equals(Configuration.KEYBOARDHIDDEN_NO)){
//			Log.i(TAG, "onConfigurationChanged+++++++++++++++");
//		}else if(newConfig.equals(Configuration.KEYBOARDHIDDEN_YES)){
//			Log.i(TAG, "onConfigurationChanged+++++++++++++++");
//		}
//	}

}
