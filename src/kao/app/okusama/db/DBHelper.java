package kao.app.okusama.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kao.app.okusama.util.DefaultDrawableCollection;

import org.kao.okusama.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBHelper {
	static final String TAG = "DBHelper";
	public static final String DATABASE_NAME = "okusama.db";
	public static final int DB_VERSION = 1;

	// ///テーブル 名
	public static final String T1_FOOD = "food";
	public static final String T2_KITCHEN = "kitchen";
	public static final String T3_WASHROOM = "washroom";
	public static final String T4_BATHROOM = "bathroom";
	public static final String T5_LIVING = "living";
	public static final String T6_ETC = "etc";
	public static final String T7_SENDDATA = "senddata";

	public static final String[] CTEGOLY_LIST = new String[] { "food",
			"kitchen", "washroom", "bathroom", "living", "etc" };

	static final public String[] Clumes_LIST = new String[] { "_id", "image",
			"name", "flag" };

	static final public String[] Clumes_LIST2 = new String[] { "_id",
			"sendname", "sendaddress" };
	
	private SQLiteDatabase db;
	private final DBOpenHelper dbOpenHelper;
	private final Context context;


	public DBHelper(Context context) {
		this.dbOpenHelper = new DBOpenHelper(context, DATABASE_NAME, DB_VERSION);
		this.establishDb();
		this.context = context;
	}

	private void establishDb() {
		if (this.db == null) {
			this.db = this.dbOpenHelper.getWritableDatabase();
		}
	}

	public void cleanup() {
		if (this.db != null) {
			this.db.close();
			this.db = null;
		}
	}

	public SQLiteDatabase getDb(){
		return db;
	}
	
	public ItemData getItemDataInstance(Integer id, Integer flag,
			String tableName) {
		return new ItemData(id, flag, tableName);
	}

	public ItemData getItemDataInstance(Integer id, String imageName, String name,Integer flag,
			String tableName) {
		return new ItemData(id, imageName, name, flag, tableName);
	}

	
	public SendedData getSendedDataInstance(String name, String address) {
		return new SendedData(name, address);
	}

	public void deleteItem(int id, String tableName) {
		db.beginTransaction();
		try {
			// アイテムの削除
			db.delete(tableName, Clumes_LIST[0] + "=" + id, null);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "deleteItem Error", e);
		} finally {
			db.endTransaction();
		}
	}

	public void addItem(String tableName, String imageName, String name) {
		db.beginTransaction();
		try {
			String table = "";
			if(tableName == null){
				throw new NullPointerException();
			}
			if (tableName.equals(context.getText(R.string.food).toString())) {
				table = CTEGOLY_LIST[0];
			} else if (tableName.equals(context.getText(R.string.kitchen)
					.toString())) {
				table = CTEGOLY_LIST[1];
			} else if (tableName.equals(context.getText(R.string.washroom)
					.toString())) {
				table = CTEGOLY_LIST[2];
			} else if (tableName.equals(context.getText(R.string.bathroom)
					.toString())) {
				table = CTEGOLY_LIST[3];
			} else if (tableName.equals(context.getText(R.string.living)
					.toString())) {
				table = CTEGOLY_LIST[4];
			} else if (tableName.equals(context.getText(R.string.etc)
					.toString())) {
				table = CTEGOLY_LIST[5];
			}
			// アイテムの追加
			ContentValues values = new ContentValues();
			values.put(Clumes_LIST[1], imageName);
			values.put(Clumes_LIST[2], name);
			values.put(Clumes_LIST[3], new Integer(0));
			db.insert(table, null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "addItem Error", e);
		} finally {
			db.endTransaction();
		}
	}

	public boolean checkDuplicationName(String tableName, String item) {
		Cursor c = null;
		String table = "";
		if (tableName.equals(context.getText(R.string.food).toString())) {
			table = CTEGOLY_LIST[0];
		} else if (tableName.equals(context.getText(R.string.kitchen)
				.toString())) {
			table = CTEGOLY_LIST[1];
		} else if (tableName.equals(context.getText(R.string.washroom)
				.toString())) {
			table = CTEGOLY_LIST[2];
		} else if (tableName.equals(context.getText(R.string.bathroom)
				.toString())) {
			table = CTEGOLY_LIST[3];
		} else if (tableName
				.equals(context.getText(R.string.living).toString())) {
			table = CTEGOLY_LIST[4];
		} else if (tableName.equals(context.getText(R.string.etc).toString())) {
			table = CTEGOLY_LIST[5];
		}
		try {
			c = db.query(table, Clumes_LIST, table + "." + Clumes_LIST[2]
					+ " like ?", new String[] { item }, null, null, null, null);
			if (c.getCount() > 0) {
				return false;
			}
		} catch (SQLException e) {
			Log.e(TAG, "checkDuplicationName Error", e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return true;
	}

	public List<ItemData> fetchAllRowsListData(String tableName,
			DefaultDrawableCollection dc) {
		ArrayList<ItemData> ret = new ArrayList<ItemData>();
		Cursor c = null;
		try {
			c = db.query(tableName, Clumes_LIST, null, null, null, null,
					Clumes_LIST[2] + " ASC");
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				int imageId = dc.getResourceId(c.getString(1));
				ItemData data = new ItemData(c.getInt(0), c.getString(1),
						imageId, c.getString(2), c.getInt(3), tableName);
				ret.add(data);
				c.moveToNext();
			}
		} catch (SQLException e) {
			Log.e(TAG, "fetchAllRowsListData Error", e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return ret;
	}

	public List<ItemData> fetchDeleteListData(String tableName) {
		ArrayList<ItemData> ret = new ArrayList<ItemData>();
		Cursor c = null;
		try {
			c = db.query(tableName, Clumes_LIST, null, null, null, null,
					Clumes_LIST[2] + " ASC");
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				ItemData data = new ItemData(c.getInt(0), c.getString(1), c
						.getString(2), c.getInt(3), tableName);
				ret.add(data);
				c.moveToNext();
			}
		} catch (SQLException e) {
			Log.e(TAG, "fetchAllRowsListData Error", e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return ret;
	}

	public List<ItemData> getItemData(String tableName, String item) {
		List<ItemData> ret = new ArrayList<ItemData>();
		Cursor c = null;
		try {
			c = db.query(tableName, Clumes_LIST, tableName + "."
					+ Clumes_LIST[2] + " = ?", new String[] { item }, null,
					null, null, null);
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				ItemData data = new ItemData(c.getInt(0), c.getString(1), c
						.getString(2), c.getInt(3), tableName);
				ret.add(data);
				c.moveToNext();
			}
		} catch (SQLException e) {
			Log.e(TAG, "getItemData Error", e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return ret;
	}

	public List<String> getItemList(String tableName, int flag) {
		ArrayList<String> ret = new ArrayList<String>();
		Cursor c = null;
		try {
			c = db.query(tableName, Clumes_LIST, tableName + "."
					+ Clumes_LIST[3] + " = " + flag, null, null, null,
					Clumes_LIST[2] + " ASC", null);
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				ret.add(c.getString(2));
				c.moveToNext();
			}
		} catch (SQLException e) {
			Log.e(TAG, "getItemList Error", e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return ret;
	}

	public void updateFlag(List<ItemData> list) {
		db.beginTransaction();
		try {
			for (ItemData data : list) {
				ContentValues cv = new ContentValues();
				cv.put(Clumes_LIST[3], data.flag);
				String whereClause = Clumes_LIST[0] + " = " + data.id;
				db.update(data.tableName, cv, whereClause, null);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "modFlag Error", e);
		} finally {
			db.endTransaction();
		}
	}

	public void updateFlag(List<ItemData> list, Integer flag) {
		db.beginTransaction();
		try {
			for (ItemData data : list) {
				ContentValues cv = new ContentValues();
				cv.put(Clumes_LIST[3], flag);
				String whereClause = Clumes_LIST[0] + " = " + data.id;
				db.update(data.tableName, cv, whereClause, null);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "modFlag Error", e);
		} finally {
			db.endTransaction();
		}
	}

	public List<SendedData> getSendedData() {
		List<SendedData> ret = new ArrayList<SendedData>();
		Cursor c = null;
		try {
			c = db.query(T7_SENDDATA, new String[] { Clumes_LIST2[0],
					Clumes_LIST2[1], Clumes_LIST2[2] }, null, null, null, null,
					null, null);
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				SendedData data = new SendedData(c.getInt(0), c.getString(1), c
						.getString(2));
				ret.add(data);
				c.moveToNext();
			}
		} catch (SQLException e) {
			Log.e(TAG, "getSendedDat Error", e);
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
		return ret;
	}

	public void addSendedData(SendedData data) {
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(Clumes_LIST2[1], data.name);
			values.put(Clumes_LIST2[2], data.address);
			db.insert(T7_SENDDATA, null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "addSendedData Error", e);
		} finally {
			db.endTransaction();
		}
	}

	public void deleteSendedData(int id) {
		db.beginTransaction();
		try {
			db.delete(T7_SENDDATA, Clumes_LIST2[0] + "=" + id, null);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "deleteSendedData Error", e);
		} finally {
			db.endTransaction();
		}
	}

	private static class DBOpenHelper extends SQLiteOpenHelper {

		// テーブル１ 生成／削除
		private static final String CREATE_T1 = "create table "
				+ DBHelper.T1_FOOD + " (" + Clumes_LIST[0]
				+ " integer primary key autoincrement, " + Clumes_LIST[1]
				+ " text not null, " + Clumes_LIST[2] + " text not null, "
				+ Clumes_LIST[3] + " integer not null )";
		private static final String DROP_T1 = "drop table if exists "
				+ DBHelper.T1_FOOD;

		// テーブル２ 生成／削除
		private static final String CREATE_T2 = "create table "
				+ DBHelper.T2_KITCHEN + " (" + Clumes_LIST[0]
				+ " integer primary key autoincrement, " + Clumes_LIST[1]
				+ " text not null, " + Clumes_LIST[2] + " text not null, "
				+ Clumes_LIST[3] + " integer not null )";
		private static final String DROP_T2 = "drop table if exists "
				+ DBHelper.T2_KITCHEN;

		// テーブル3 生成／削除
		private static final String CREATE_T3 = "create table "
				+ DBHelper.T3_WASHROOM + " (" + Clumes_LIST[0]
				+ " integer primary key autoincrement, " + Clumes_LIST[1]
				+ " text not null, " + Clumes_LIST[2] + " text not null, "
				+ Clumes_LIST[3] + " integer not null )";
		private static final String DROP_T3 = "drop table if exists "
				+ DBHelper.T3_WASHROOM;

		// テーブル4 生成／削除
		private static final String CREATE_T4 = "create table "
				+ DBHelper.T4_BATHROOM + " (" + Clumes_LIST[0]
				+ " integer primary key autoincrement, " + Clumes_LIST[1]
				+ " text not null, " + Clumes_LIST[2] + " text not null, "
				+ Clumes_LIST[3] + " integer not null )";
		private static final String DROP_T4 = "drop table if exists "
				+ DBHelper.T4_BATHROOM;

		// テーブル5 生成／削除
		private static final String CREATE_T5 = "create table "
				+ DBHelper.T5_LIVING + " (" + Clumes_LIST[0]
				+ " integer primary key autoincrement, " + Clumes_LIST[1]
				+ " text not null, " + Clumes_LIST[2] + " text not null, "
				+ Clumes_LIST[3] + " integer not null )";
		private static final String DROP_T5 = "drop table if exists "
				+ DBHelper.T5_LIVING;

		// テーブル6 生成／削除
		private static final String CREATE_T6 = "create table "
				+ DBHelper.T6_ETC + " (" + Clumes_LIST[0]
				+ " integer primary key autoincrement, " + Clumes_LIST[1]
				+ " text not null, " + Clumes_LIST[2] + " text not null, "
				+ Clumes_LIST[3] + " integer not null )";
		private static final String DROP_T6 = "drop table if exists "
				+ DBHelper.T6_ETC;

		// テーブル7 生成／削除
		private static final String CREATE_T7 = "create table "
				+ DBHelper.T7_SENDDATA + " (" + Clumes_LIST2[0]
				+ " integer primary key autoincrement, " + Clumes_LIST2[1]
				+ " text not null, " + Clumes_LIST2[2] + " text not null )";
		private static final String DROP_T7 = "drop table if exists "
				+ DBHelper.T7_SENDDATA;
		private Context context;

		public DBOpenHelper(Context context, String name, int version) {
			super(context, DBHelper.DATABASE_NAME, null, DBHelper.DB_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(CREATE_T1);
				db.execSQL(CREATE_T2);
				db.execSQL(CREATE_T3);
				db.execSQL(CREATE_T4);
				db.execSQL(CREATE_T5);
				db.execSQL(CREATE_T6);
				db.execSQL(CREATE_T7);
				Map<String, String> map = new HashMap<String, String>();
				addDefaltValueExecute(
						getDefaultData(context, map, 1), T1_FOOD, db);
				addDefaltValueExecute(
						getDefaultData(context, map, 2), T2_KITCHEN, db);
				addDefaltValueExecute(
						getDefaultData(context, map, 3), T3_WASHROOM, db);
				addDefaltValueExecute(
						getDefaultData(context, map, 4), T4_BATHROOM, db);
				addDefaltValueExecute(
						getDefaultData(context, map, 5), T5_LIVING, db);
				map = null;
			} catch (SQLException e) {
				Log.e(TAG, "SQL Error in onCreate()", e);
			}
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			// TODO アップデートで中のデータが保持されるか確認
			// ALTER TABLEなどのSQLを使って対応することになります。
			// oldVersionとnewVersionが渡ってくるので、
			// バージョンが1から2になった場合、
			// 2から3になった時などで分岐させられます。
		}

		public void addDefaltValueExecute(Map<String, String> defaultMap,
				String table, SQLiteDatabase db) {
			Set<String> set = defaultMap.keySet();
			Iterator<String> ite = set.iterator();
			db.beginTransaction();
			try {
				while (ite.hasNext()) {
					String key = (String) ite.next();
					String value = defaultMap.get(key);
					ContentValues values = new ContentValues();
					values.put(Clumes_LIST[1], key);// アイコン名（DrawableCollectionのイメージ名）
					values.put(Clumes_LIST[2], value);// アイテム名
					values.put(Clumes_LIST[3], new Integer(0));
					db.insert(table, null, values);
				}
				db.setTransactionSuccessful();
			} catch (SQLException e) {
				Log.e(TAG, "addDefaltValueExecute Error", e);
			} finally {
				db.endTransaction();
			}
		}



		// DBの各テーブルのデフォルト値
		// Mapのキーはそのままアイコンのリソース名(.pngは除く)
		// Mapのバリューは実際に表示するテキスト。国際化できるようにString.xmlで定義する。
		private Map<String, String> getDefaultData(Context context, Map<String, String> map, int tableId) {
			map.clear();
			if(tableId == 1){
				// 常備食品カテゴリのデフォルト項目 food
				map.put("miso", context.getString(R.string.miso));// 味噌
				map.put("salt", context.getString(R.string.salt));// 塩
				map.put("soy", context.getString(R.string.soy));// 醤油
				map.put("sake", context.getString(R.string.sake));// 料理酒
			}else if(tableId == 2){
				// 台所カテゴリのデフォルト項目 kitchen
				map.put("dish_liquid", context.getString(R.string.dish_liquid));// 食器用洗剤
				map.put("small_wrap", context.getString(R.string.small_wrap));// 小サランラップ
				map.put("large_wrap", context.getString(R.string.large_wrap));// 大サランラップ
				map.put("aluminum_foil", context
						.getString(R.string.aluminum_foil));// アルミホイル
			}else if(tableId == 3){
				// 洗面所カテゴリのデフォルト項目 washroom
				map.put("hand_soap", context.getString(R.string.hand_soap));// ハンドソープ
				map.put("toothpaste", context.getString(R.string.toothpaste));// 歯磨き粉
				map.put("landry_detergent", context
						.getString(R.string.landry_detergent));// 洗濯用洗剤
				map.put("softener", context.getString(R.string.softener));// 柔軟剤
			}else if(tableId == 4){
				// トイレ／風呂カテゴリのデフォルト項目 bathroom
				map.put("body_wash", context.getString(R.string.body_wash));// ボディシャンプー
				map.put("shampoo", context.getString(R.string.shampoo));// シャンプー
				map.put("rinse", context.getString(R.string.rinse));// リンス
				map.put("toilet_paper", context.getString(R.string.toilet_paper));// トイレットペーパー
			}else if(tableId == 5){
				// リビングカテゴリのデフォルト項目 living
				map.put("tissue", context.getString(R.string.tissue));// 箱ティッシュ
				map.put("sticky_roller", context
						.getString(R.string.sticky_roller));// コロコロの替え
				map.put("flooring_wiper", context
						.getString(R.string.flooring_wiper));// フローリングワイパーの替え
				map.put("cleaner_bag", context.getString(R.string.cleaner_bag));// 掃除機紙パック
			}
			return map;
		}

	}

	public class ItemData {
		public int id;
		public String image;
		public int imageId;
		public String name;
		public Integer flag;
		public String tableName;

		public ItemData(int id, String image, int imageId, String name,
				Integer flag, String tableName) {
			this.id = id;
			this.image = image;
			this.imageId = imageId;
			this.name = name;
			this.flag = flag;
			this.tableName = tableName;
		}

		public ItemData(Integer id, Integer flag, String tableName) {
			this.id = id;
			this.flag = flag;
			this.tableName = tableName;
		}

		public ItemData(Integer id, String image, String name, Integer flag,
				String tableName) {
			this.id = id;
			this.flag = flag;
			this.tableName = tableName;
			this.image = image;
			this.name = name;
		}
	}

	public class SendedData {
		public int id;
		public String name;
		public String address;

		public SendedData(int id, String name, String address) {
			this.id = id;
			this.name = name;
			this.address = address;
		}

		public SendedData(String name, String address) {
			this.name = name;
			this.address = address;
		}
	}
	
}
