package kao.app.okusama.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kao.app.okusama.StockCheckActivity;
import kao.app.okusama.db.DBHelper;
import kao.app.okusama.db.DBHelper.ItemData;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class StockCheckTask extends
		AsyncTask<String, Integer, List<HashMap<String, Object>>> {
	public static final String GET_ITEMS_ACTION = "get_item";

	private final static String TAG = "StockCheckTask";
	private StockCheckActivity mActivity;
	private DBHelper helper;
	static public StockCheckTask deleteItemTask;

	public StockCheckTask(StockCheckActivity activity, DBHelper helper) {
		try {
			this.mActivity = activity;
			this.helper = helper;
		} catch (Exception e) {
			Log.e(TAG, "error", e);
		}
	}


	public List<HashMap<String, Object>> getItems(int tabId) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("action", GET_ITEMS_ACTION);
		
		ArrayList<ItemData> ret = new ArrayList<ItemData>();
		Cursor c = null;
		try {
			String tableName = getTableName(tabId);
			SQLiteDatabase db = helper.getDb();
			c = db.query(tableName, DBHelper.Clumes_LIST, null, null, null,
					null, DBHelper.Clumes_LIST[2] + " ASC");
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				DBHelper.ItemData data = helper.getItemDataInstance(c.getInt(0), c.getString(1), c
						.getString(2), c.getInt(3), tableName);
				ret.add(data);
				c.moveToNext();
			}
			map.put("result", ret);
			list.add(map);
		} catch (SQLException e) {
			Log.e(TAG, "fetchAllRowsListData Error", e);
			map.put("result", "error");
			list.add(map);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
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
		if (actionName.equals(GET_ITEMS_ACTION)) {
			int tabId = Integer.valueOf((params[1]));
			list = getItems(tabId);
		}
		return list;
	}

	// 結果をUIにセット
	@Override
	protected void onPostExecute(List<HashMap<String, Object>> result) {
		mActivity.setResultInfo(result);
	}

	private String getTableName(int tabIndex) {
		String tableName = null;
		if (tabIndex == 0) {
			tableName = DBHelper.T1_FOOD;
		} else if (tabIndex == 1) {
			tableName = DBHelper.T2_KITCHEN;
		} else if (tabIndex == 2) {
			tableName = DBHelper.T3_WASHROOM;
		} else if (tabIndex == 3) {
			tableName = DBHelper.T4_BATHROOM;
		} else if (tabIndex == 4) {
			tableName = DBHelper.T5_LIVING;
		} else if (tabIndex == 5) {
			tableName = DBHelper.T6_ETC;
		}
		return tableName;
	}

}
