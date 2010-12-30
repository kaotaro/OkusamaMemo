package kao.app.okusama.util;

import java.util.HashMap;
import java.util.Iterator;

import org.kao.okusama.R;

public class DefaultDrawableCollection {
	private HashMap<String, Integer> drawableMap;
	private static DefaultDrawableCollection dc;
	public DefaultDrawableCollection() {
		setDc(this);
		createDrawbleCollection();
	}

	private void createDrawbleCollection() {
		drawableMap = new HashMap<String, Integer>();
		drawableMap.put("miso", R.drawable.miso);// 味噌
		drawableMap.put("salt", R.drawable.salt);// 塩
		drawableMap.put("soy", R.drawable.soy);// 醤油
		drawableMap.put("sake", R.drawable.sake);// 料理酒
		drawableMap.put("dish_liquid", R.drawable.dish_liquid);// 食器用洗剤
		drawableMap.put("small_wrap", R.drawable.small_wrap);// 小サランラップ
		drawableMap.put("large_wrap", R.drawable.large_wrap);// 大サランラップ
		drawableMap.put("aluminum_foil", R.drawable.aluminum_foil);// アルミホイル
		drawableMap.put("hand_soap", R.drawable.hand_soap);// ハンドソープ
		drawableMap.put("toothpaste", R.drawable.toothpaste);// 歯磨き粉
		drawableMap.put("landry_detergent", R.drawable.landry_detergent);// 洗濯用洗剤
		drawableMap.put("softener", R.drawable.softener);// 柔軟剤
		drawableMap.put("body_wash", R.drawable.body_wash);// ボディシャンプー
		drawableMap.put("shampoo", R.drawable.shampoo);// シャンプー
		drawableMap.put("rinse", R.drawable.rinse);// リンス
		drawableMap.put("toilet_paper", R.drawable.toilet_paper);// トイレットペーパー
		drawableMap.put("tissue", R.drawable.tissue);// 箱ティッシュ
		drawableMap.put("sticky_roller", R.drawable.sticky_roller);// コロコロの替え
		drawableMap.put("flooring_wiper", R.drawable.flooring_wiper);// フローリングワイパーの替え
		drawableMap.put("cleaner_bag", R.drawable.cleaner_bag);// 掃除機紙パック
		drawableMap.put("noimage", R.drawable.noimage);
		
	}

	public int getResourceId(String resourceName) {
		Integer id = (Integer) drawableMap.get(resourceName);
		if (id == null) {
			id = -1;
		}
		return id;
	}

	public String getResourceName(int id) {
		Iterator<String> ite = drawableMap.keySet().iterator();
		while (ite.hasNext()) {
			String iconName = (String) ite.next();
			Integer mapId = (Integer) (drawableMap.get(iconName));
			if (mapId == id) {
				return iconName;
			}
		}
		return null;
	}

	public Iterator<Integer> getResourceIds() {
		Iterator<Integer> ite = (drawableMap.values()).iterator();
		return ite;
	}

	public static DefaultDrawableCollection getDrawableCollection() {
		if (getDc() == null) {
			return new DefaultDrawableCollection();
		} else {
			return getDc();
		}
	}

	public static void setDc(DefaultDrawableCollection dc) {
		DefaultDrawableCollection.dc = dc;
	}

	public static DefaultDrawableCollection getDc() {
		return dc;
	}

}
