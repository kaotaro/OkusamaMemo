package kao.app.okusama;

import java.util.Locale;

import org.kao.okusama.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AboutOkusamaDialog extends Activity {
	static final String Tag = "AboutOkusamaDialog";
	int count = 0;
	ImageButton rightButton;
	ImageButton leftButton;
	static final int PAGEMAX = 6;
	static final int PAGEMIN = 0;
	WebView webview;

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
		setContentView(R.layout.about);
		LinearLayout layout = (LinearLayout) findViewById(R.id.AboutTopLayout);
		layout.setVisibility(View.INVISIBLE);
		count = 0;
		webview = (WebView) findViewById(R.id.webview);
		webview.setWebViewClient(new CustomWebViewClient(this));
		webview.getSettings().setDefaultZoom(ZoomDensity.FAR);
		webview.getSettings().setBuiltInZoomControls(true);

		ImageButton closeButton = (ImageButton) findViewById(R.id.about_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		ImageButton devButton = (ImageButton) findViewById(R.id.dev_info);
		devButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						DevInfoDialog.class);
				startActivity(intent);
				finish();
			}
		});

		rightButton = (ImageButton) findViewById(R.id.right);
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				count++;
				loadAboutInfo(count + 1);
				if (count == PAGEMAX) {
					// add start v1.0.1
					if (rightButton == null) {
						rightButton = (ImageButton) findViewById(R.id.right);
					}
					// end
					rightButton.setVisibility(View.INVISIBLE);
				}
				if (count >= PAGEMIN) {
					// add start v1.0.1
					if (leftButton == null) {
						leftButton = (ImageButton) findViewById(R.id.left);
					}
					// end
					leftButton.setVisibility(View.VISIBLE);
				}
			}
		});

		leftButton = (ImageButton) findViewById(R.id.left);
		leftButton.setVisibility(View.INVISIBLE);
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				count--;
				loadAboutInfo(count + 1);
				if (count == PAGEMIN) {
					// add start v1.0.1
					if (leftButton == null) {
						leftButton = (ImageButton) findViewById(R.id.left);
					}
					// end
					leftButton.setVisibility(View.INVISIBLE);
				}
				if (count <= PAGEMAX) {
					// add start v1.0.1
					if (rightButton == null) {
						rightButton = (ImageButton) findViewById(R.id.right);
					}
					// end
					rightButton.setVisibility(View.VISIBLE);
				}
			}
		});
		loadAboutInfo(count + 1);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		count = 0;
		rightButton = null;
		leftButton = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		rightButton = null;
		leftButton = null;

	}

	private void loadAboutInfo(int page) {
		String url;
		Locale locale = Locale.getDefault();
		if (locale.equals(Locale.JAPAN)) {
			url = "https://sites.google.com/site/howtookusama/ja" + page;
		} else {
			url = "https://sites.google.com/site/howtookusama/" + page;
		}
		webview.loadUrl(url);
	}

	private class CustomWebViewClient extends WebViewClient {
		private ProgressDialog progressDialog;
		private Context context;
		// ページ読み込み開始時の動作
		public CustomWebViewClient(Context c) {
			context = c;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			showErrorCustomToast(context.getResources().getText(
					R.string.error_url).toString());

			// ダイアログを削除
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			finish();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.AboutTopLayout);
			layout.setVisibility(View.INVISIBLE);
			progressDialog = new ProgressDialog(context);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage(context.getResources().getText(
					R.string.load));
			progressDialog.show();
		}

		// ページ読み込み終了時の動作
		@Override
		public void onPageFinished(WebView view, String url) {
			// ダイアログを削除
			if (progressDialog != null) {	
				progressDialog.dismiss();
				progressDialog = null;
			}
			
			//webview.zoomOut()がtrueになるまで繰り返す。
			int cnt = 0;
			while(webview.zoomOut() && cnt != 10){
				webview.getSettings().setDefaultZoom(ZoomDensity.FAR);
				cnt++;
			}
			LinearLayout layout = (LinearLayout) findViewById(R.id.AboutTopLayout);
			layout.setVisibility(View.VISIBLE);
		}
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