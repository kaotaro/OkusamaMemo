package kao.app.okusama;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import kao.app.okusama.db.DBHelper;

import org.kao.okusama.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class IconCameraActivity extends Activity {

	private static final String TAG = "IconCameraActivity";
	private Camera mCamera;
	private boolean mInProgress;
	private DBHelper helper;
	private byte[] previewData;

	private SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// カメラ取得
			mCamera = Camera.open();

			Camera.Parameters params = mCamera.getParameters();
			// List<Size> supportedSizes = Reflect
			// .getSupportedPreviewSizes(params);
			// Log.d(TAG, "supportedSizes.size()  = " + supportedSizes.size());
			// if (supportedSizes != null && supportedSizes.size() > 0) {
			// size = supportedSizes.get(0);
			// Log.i(TAG, "in surfaceCreated() size.width = " + size.width
			// + " size.height = " + size.height);
			// params.setPreviewSize(size.width, size.height);
			// mCamera.setParameters(params);
			// }
			mCamera.setParameters(params);

			// プレビュー設定
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mCamera.release();
			mCamera = null;
			Log.i(TAG, "Camera released");
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters params = mCamera.getParameters();
			// params.setPreviewSize(size.width, size.height);
			params.setPreviewSize(720, 480);
			// Log.i(TAG, "in surfaceChanged() size.width = " + size.width
			// + " size.height = " + size.height);
			mCamera.setParameters(params);
			mCamera.startPreview();
		}
	};

	private Camera.AutoFocusCallback mAutoFocusListener = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {

			if (camera != null && success) {
				Log.i(TAG, "AutoFocus : " + success);
				// camera.autoFocus(null);
				camera.takePicture(mShutterListener, null, mPictureListener);
				// mInProgress = true;
			} else {
				mInProgress = false; // ここ
			}
		}
	};

	private Camera.ShutterCallback mShutterListener = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {
			Log.d(TAG, "onShutter");
		}
	};

	private Camera.PictureCallback mPictureListener = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "Picture taken");
			if (data != null && camera != null) {
				Log.d(TAG, "JPEG Picture Taken");
				LinearLayout shutterButtonLayout = (LinearLayout) findViewById(R.id.shutterLayout);
				LinearLayout viewButtonLayout = (LinearLayout) findViewById(R.id.buttonViewLayout);

				shutterButtonLayout.setVisibility(View.GONE);
				viewButtonLayout.setVisibility(View.VISIBLE);
				ImageButton yesButton = (ImageButton) findViewById(R.id.camera_ok);
				yesButton.requestFocus();
				previewData = data;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate called !!!");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera);

		SurfaceView surface = (SurfaceView) findViewById(R.id.SurfaceView01);
		SurfaceHolder holder = surface.getHolder();

		holder.addCallback(mSurfaceListener);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		final ImageButton shutterButton = (ImageButton) findViewById(R.id.shutter);
		final ImageButton yesButton = (ImageButton) findViewById(R.id.camera_ok);
		final ImageButton cancelButton = (ImageButton) findViewById(R.id.camera_cancel);
		final ImageButton retakeButton = (ImageButton) findViewById(R.id.camera_retake);

		LinearLayout shutterButtonLayout = (LinearLayout) findViewById(R.id.shutterLayout);
		shutterButtonLayout.setVisibility(View.VISIBLE);
		LinearLayout viewButtonLayout = (LinearLayout) findViewById(R.id.buttonViewLayout);
		viewButtonLayout.setVisibility(View.GONE);

		View.OnClickListener mButtonListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == shutterButton) {
					if (mCamera != null && mInProgress == false) {
						Log
								.d(TAG,
										"click shutterButton!!!      mButtonListener#onClick");
						mCamera.autoFocus(mAutoFocusListener);
						mInProgress = true; // ここ！
					}
				} else if (v == yesButton) {

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 8;// 1/16に縮小
					options.inPreferredConfig = Bitmap.Config.RGB_565;
					Bitmap bitmap = BitmapFactory.decodeByteArray(previewData,
							0, previewData.length, options);

					// SDカード格納
					ContentResolver cr = getApplicationContext()
							.getContentResolver();
					Uri uri = addImageAsApplication(cr, bitmap,
							getApplicationContext());
					Log.i(TAG, "add picture done ! uri = " + uri.toString());

					Intent intent = getIntent();
					String itemName = intent.getCharSequenceExtra("itemname")
							.toString();
					String tableName = intent.getCharSequenceExtra("tablename")
							.toString();

					// DB登録実行
					// helper = new DBHelper(getApplicationContext());
					helper.addItem(tableName, uri.toString(), itemName);

					// if (helper != null) {
					// helper.cleanup();
					// helper = null;
					// }

					// ビットマップリソースを解放
					previewData = null;
					bitmap.recycle();
					bitmap = null;

					mCamera.startPreview();
					mInProgress = false;

					LinearLayout shutterButtonLayout = (LinearLayout) findViewById(R.id.shutterLayout);
					LinearLayout viewButtonLayout = (LinearLayout) findViewById(R.id.buttonViewLayout);
					shutterButtonLayout.setVisibility(View.VISIBLE);
					viewButtonLayout.setVisibility(View.GONE);

					// 追加ダイアログに戻る
					setResult(RESULT_OK);
					finish();

				} else if (v == cancelButton) {
					mCamera.startPreview();
					mInProgress = false;
					previewData = null;
					// 追加ダイアログに戻る
					finish();
				} else if (v == retakeButton) {
					mCamera.startPreview();
					mInProgress = false;
					previewData = null;
					LinearLayout shutterButtonLayout = (LinearLayout) findViewById(R.id.shutterLayout);
					LinearLayout viewButtonLayout = (LinearLayout) findViewById(R.id.buttonViewLayout);
					shutterButtonLayout.setVisibility(View.VISIBLE);
					viewButtonLayout.setVisibility(View.GONE);
				}
			}
		};
		shutterButton.setOnClickListener(mButtonListener);
		yesButton.setOnClickListener(mButtonListener);
		cancelButton.setOnClickListener(mButtonListener);
		retakeButton.setOnClickListener(mButtonListener);
	}

	// トラックボールイベントの処理
	// @Override
	// public boolean onTrackballEvent(MotionEvent event) {
	// // ここでトラックボール動かすと落ちるので無効にする。
	// return true;
	// }

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "call onResume!!!!!!!!!!!!!!!!!!!");
		if (helper == null) {
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
	public void onDestroy() {
		Log.i(TAG, "onDestroy called !!!");
		super.onDestroy();
		if (helper != null) {
			helper.cleanup();
			helper = null;
		}
	}

	public static Uri addImageAsApplication(ContentResolver cr, Bitmap bitmap,
			Context context) {
		long dateTaken = System.currentTimeMillis();
		String name = createName(dateTaken) + ".jpg";
		return addImageAsApplication(cr, name, dateTaken, Environment
				.getExternalStorageDirectory().toString()
				+ "/" + context.getText(R.string.app_name).toString(), name,
				bitmap, null);
	}

	public static Uri addImageAsApplication(ContentResolver cr, String name,
			long dateTaken, String directory, String filename, Bitmap source,
			byte[] jpegData) {

		OutputStream outputStream = null;
		String filePath = directory + "/" + filename;
		Log.i(TAG, "file path  = " + filePath);
		try {
			File dir = new File(directory);
			if (!dir.exists()) {
				dir.mkdirs();
				Log.d(TAG, dir.toString() + " create");
			}
			File file = new File(directory, filename);
			if (file.createNewFile()) {
				outputStream = new FileOutputStream(file);
				if (source != null) {
					Matrix aMatrix = new Matrix();
					aMatrix.setRotate(90.0f);
					Bitmap newBmp = Bitmap.createBitmap(source, 0, 0, source
							.getWidth(), source.getHeight(), aMatrix, false);
					newBmp.compress(CompressFormat.JPEG, 50, outputStream);

					// ビットマップリソースを解放
					source.recycle();
					newBmp.recycle();

					source = null;
					newBmp = null;
					System.gc();
				} else {
					// jpegData は null
					outputStream.write(jpegData);
				}
			}

		} catch (FileNotFoundException ex) {
			Log.w(TAG, ex);
			return null;
		} catch (IOException ex) {
			Log.w(TAG, ex);
			return null;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Throwable t) {
				}
			}
		}

		ContentValues values = new ContentValues(7);
		values.put(Images.Media.TITLE, name);
		values.put(Images.Media.DISPLAY_NAME, filename);
		values.put(Images.Media.DATE_TAKEN, dateTaken);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.DATA, filePath);

		Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		return cr.insert(IMAGE_URI, values);
	}

	private static String createName(long dateTaken) {
		return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString();
	}

}
