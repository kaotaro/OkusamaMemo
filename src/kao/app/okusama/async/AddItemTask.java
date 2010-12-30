package kao.app.okusama.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kao.app.okusama.DeleteItemActivity;
import kao.app.okusama.StockCheckActivity;
import kao.app.okusama.db.DBHelper;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class AddItemTask extends
		AsyncTask<String, Integer, List<HashMap<String, Object>>> {
	public static final String ADD_ACTION = "add";
	public static final String DELETE_ACTION = "delete";
	public static final String GET_ITEMS_ACTION = "get_item";

	private final static String TAG = "StockCheckDbTask";
	private DeleteItemActivity mActivity;
	private SQLiteDatabase db;
	static public AddItemTask addItemTask;
	
	public AddItemTask(DeleteItemActivity activity, SQLiteDatabase db) {
		try {
			this.mActivity = activity;
			this.db = db;
		} catch (Exception e) {
			Log.e(TAG, "error", e);
		}
	}
	
	static public AddItemTask getStockCheckDbTask(StockCheckActivity activity, SQLiteDatabase db){
//		if(deleteItemTask == null){
//			deleteItemTask = new AddItemTask(activity, db);
//		}
		return addItemTask;
	}
	
	public List<HashMap<String, Object>> deleteItem(int id) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();

		db.beginTransaction();
		try {
			db.delete(DBHelper.T7_SENDDATA,
					DBHelper.Clumes_LIST2[0] + "=" + id, null);
			db.setTransactionSuccessful();
			map.put("result", "ok");
			list.add(map);

		} catch (SQLException e) {
			Log.e(TAG, "deleteSendedData Error", e);
			map.put("result", "error");
			list.add(map);

		} finally {
			db.endTransaction();
		}
		return list;
	}

	public List<HashMap<String, Object>> addItem(int tabId, String imageName,
			String name) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		db.beginTransaction();
		try {
			String table = "";
			if (tabId == 0) {
				table = DBHelper.CTEGOLY_LIST[0];
			} else if (tabId == 1) {
				table = DBHelper.CTEGOLY_LIST[1];
			} else if (tabId == 2) {
				table = DBHelper.CTEGOLY_LIST[2];
			} else if (tabId == 3) {
				table = DBHelper.CTEGOLY_LIST[3];
			} else if (tabId == 4) {
				table = DBHelper.CTEGOLY_LIST[4];
			} else if (tabId == 5) {
				table = DBHelper.CTEGOLY_LIST[5];
			}
			// アイテムの追加
			ContentValues values = new ContentValues();
			values.put(DBHelper.Clumes_LIST[1], imageName);
			values.put(DBHelper.Clumes_LIST[2], name);
			values.put(DBHelper.Clumes_LIST[3], new Integer(0));
			db.insert(table, null, values);
			db.setTransactionSuccessful();
			map.put("result", "ok");
			list.add(map);
		} catch (SQLException e) {
			Log.e(TAG, "addItem Error", e);
			map.put("result", "error");
			list.add(map);
		} finally {
			db.endTransaction();
		}
		return list;
	}

	// バックグラウンドで実行
	// 第一引数は必ずACTION名
	// 第二引数以降が必要な要素
	@Override
	protected List<HashMap<String, Object>> doInBackground(String... params) {
		String actionName = params[0];
		List<HashMap<String, Object>> list = null;
		if (actionName.equals(ADD_ACTION)) {
			int tabId = Integer.valueOf((params[1]));
			String imageName = params[2];
			String name = params[3];
			list = addItem(tabId, imageName,name);
		} else if (actionName.equals(DELETE_ACTION)) {
			int id = Integer.valueOf((params[1]));
			list = deleteItem(id);
		}
		return list;
	}

	// 結果をUIにセット
	@Override
	protected void onPostExecute(List<HashMap<String, Object>> result) {
		mActivity.setResultInfo(result);
	}
}
