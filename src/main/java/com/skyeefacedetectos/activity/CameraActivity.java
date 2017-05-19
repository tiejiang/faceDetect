package com.skyeefacedetectos.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skyeefacedetectos.R;
import com.skyeefacedetectos.camera.CameraInterface;
import com.skyeefacedetectos.camera.preview.CameraSurfaceView;
import com.skyeefacedetectos.mode.GoogleFaceDetect;
import com.skyeefacedetectos.ui.FaceView;
import com.skyeefacedetectos.util.Base64Helper.ImgHelper;
import com.skyeefacedetectos.util.DisplayUtil;
import com.skyeefacedetectos.util.EventUtil;
import com.skyeefacedetectos.util.FileUtil;
import com.skyeefacedetectos.util.okhttp.HttpConnect;

public class CameraActivity extends XunFeiActivity {
	CameraSurfaceView surfaceView = null;
	ImageButton shutterBtn;
	ImageButton switchBtn;
	FaceView faceView;
	float previewRate = -1f;
	private MainHandler mMainHandler = null;
	GoogleFaceDetect googleFaceDetect = null;
	BroadcastReceiver mBroadcastReceiver;
	/**
	 * 请求码
	 */
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;

	private ImageView dis_image;
	private boolean isVerify;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		Intent mIntent = getIntent();
		isVerify = mIntent.getBooleanExtra("isVerify", false);
		Log.d("TIEJIANG", "isVerify = " + isVerify);

		mBroadcastReceiver = new MyBroadcastReceiver();
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("com.take.pich");
		registerReceiver(mBroadcastReceiver, mIntentFilter);
		mMainHandler = new MainHandler();
		googleFaceDetect = new GoogleFaceDetect(getApplicationContext(), mMainHandler);
		shutterBtn.setOnClickListener(new BtnListeners());
		switchBtn.setOnClickListener(new BtnListeners());
		mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);

//		int code = mTts.startSpeaking("语音识别模式，添加会员图像", mTtsListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("com.take.pich")) {

				if (intent.getExtras().getString("message").equals("take")) {
					// takePicture();
					Log.d("TIEJIANG", "CameraActivity-onReceive if takePicture");
					int i = 0;
					//if (i < 1) {
					//i++;
					takePicture();
					//}

				} else {  // take picture succeed begin to send to server side
					String imageurl = intent.getStringExtra("message").split("&")[1];
					startPhotoZoom(Uri.parse(imageurl));
				}
			}
		}

	}

	/**
	 * 保存裁剪之后的图片数据
	 *
	 * @param
	 */
	private void getImageToView(Intent data) {

		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(this.getResources(), photo);
			dis_image.setImageDrawable(drawable);
			String imgUrl = FileUtil.saveBitmap(photo, true).getPath();
			if (imgUrl != null){
				showWhetherUpload(photo);
				Log.d("TIEJIANG", "imageurl= " + imgUrl);
			}
		}
	}

	public void showWhetherUpload(final Bitmap bitmap) {

		View mDialogView = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_view, null);
		final LinearLayout edit_member_view = (LinearLayout)mDialogView.findViewById(R.id.edit_member_view);
		final ImageView imageView = (ImageView)mDialogView.findViewById(R.id.wait_for_send_img);
		final EditText editText = (EditText)mDialogView.findViewById(R.id.member_id);
		if (isVerify){
			edit_member_view.setVisibility(View.INVISIBLE);
		}else {
			mTts.startSpeaking("请  输入会员ID", mTtsListener);
			edit_member_view.setVisibility(View.VISIBLE);
		}


		final AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
 		builder.setMessage("是否上传图像？");
		builder.setTitle("提示");
		builder.setView(mDialogView);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					imageView.setImageBitmap(bitmap);
					String base64String = ImgHelper.bitmaptoString(bitmap);
					Log.d("TIEJIANG", "base64String= " + base64String);
					if (isVerify){  // if verifying member, do not display the edit text view
						mTts.startSpeaking("上传中，请稍等", mTtsListener);
						new HttpConnect().httpPostJson(isVerify, base64String, "0");
					}else {
						String ID = editText.getText().toString();
						Log.d("TIEJIANG", "ID= " + ID);
						if (!ID.equals("")){
							mTts.startSpeaking("上传中，请稍等", mTtsListener);
							new HttpConnect().httpPostJson(isVerify, base64String, ID);
						}else{
							mTts.startSpeaking("请输入会员编号", mTtsListener);
							Toast.makeText(CameraActivity.this, "请输入会员编号", Toast.LENGTH_SHORT).show();
						}
					}

				}catch (Exception e){
					e.printStackTrace();
				}

				/**
				 *  received response from Http post
				 **/
				HttpConnect.mHandler=new Handler (){
					@Override
					public void handleMessage(Message msg)
					{
						String receStr = msg.obj.toString ();
						Log.d("TIEJIANG", "RESPONCE FORM HTTP POST= " + receStr);
						if (receStr != null){
							if (isVerify){
								if (receStr.contains("200")){
									mTts.startSpeaking("比对成功", mTtsListener);
								}else {
									mTts.startSpeaking("系统不存在此会员信息", mTtsListener);
								}
							}else {
								if (receStr.contains("200")){
									mTts.startSpeaking("会员信息上传成功", mTtsListener);
								}else if (receStr.contains("600")){
									mTts.startSpeaking("会员信息已存在，请确认是否重复录入", mTtsListener);
								}else if (receStr.contains("201")){
									mTts.startSpeaking("会员信息录入失败，请重新录入", mTtsListener);
								}
							}
						}


					}
				};
//				finish();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.create().show();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 结果码不等于取消时候
		if (resultCode != RESULT_CANCELED) {
			switch (requestCode) {
				case IMAGE_REQUEST_CODE :
//					startPhotoZoom(data.getData());
					break;
				case CAMERA_REQUEST_CODE :
					// 判断存储卡是否可以用，可用进行存储
//					String state = Environment.getExternalStorageState();
//					if (state.equals(Environment.MEDIA_MOUNTED)) {
//						File path = Environment
//								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//						File tempFile = new File(path, IMAGE_FILE_NAME);
//						startPhotoZoom(Uri.fromFile(tempFile));
//					} else {
//						Toast.makeText(getApplicationContext(),
//								"未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show();
//					}
					break;
				case RESULT_REQUEST_CODE : // 图片缩放完成后
					if (data != null) {
						getImageToView(data);

					}
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);

	}


	/**
	 * 裁剪图片方法实现
	 *
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 340);
		intent.putExtra("outputY", 340);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, RESULT_REQUEST_CODE);
		mTts.startSpeaking("请  框选人脸轮廓", mTtsListener);
	}

	private void initUI() {
		surfaceView = (CameraSurfaceView) findViewById(R.id.camera_surfaceview);
		shutterBtn = (ImageButton) findViewById(R.id.btn_shutter);
		switchBtn = (ImageButton) findViewById(R.id.btn_switch);
		faceView = (FaceView) findViewById(R.id.face_view);
		dis_image = (ImageView)findViewById(R.id.dis_image);

	}

	private void initViewParams() {
		LayoutParams params = surfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		previewRate = DisplayUtil.getScreenRate(this); // 默认全屏的比例预览
		surfaceView.setLayoutParams(params);

	}

	private class BtnListeners implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_shutter:
				takePicture();
				break;
			case R.id.btn_switch:
				switchCamera();
				break;
			default:
				break;
			}
		}

	}

	private class MainHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case EventUtil.UPDATE_FACE_RECT:
				Face[] faces = (Face[]) msg.obj;
				faceView.setFaces(faces);
				break;
			case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
				startGoogleFaceDetect();
				break;
			}
			super.handleMessage(msg);
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mBroadcastReceiver != null) {
			unregisterReceiver(mBroadcastReceiver);
		}
		CameraInterface.getInstance().doStopCamera();
		isVerify = false;
	}

	private void takePicture() {
		CameraInterface.getInstance().doTakePicture();
		// mMainHandler.sendEmptyMessageDelayed(
		// EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
	}

	private void switchCamera() {
		stopGoogleFaceDetect();
		int newId = (CameraInterface.getInstance().getCameraId() + 1) % 2;
		CameraInterface.getInstance().doStopCamera();
		CameraInterface.getInstance().doOpenCamera(null, newId);
		CameraInterface.getInstance().doStartPreview(
				surfaceView.getSurfaceHolder(), previewRate);
		mMainHandler.sendEmptyMessageDelayed(
				EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
		// startGoogleFaceDetect();

	}

	private void startGoogleFaceDetect() {
		Camera.Parameters params = CameraInterface.getInstance()
				.getCameraParams();
		if (params.getMaxNumDetectedFaces() > 0) {
			if (faceView != null) {
				faceView.clearFaces();
				faceView.setVisibility(View.VISIBLE);
			}
			CameraInterface.getInstance().getCameraDevice()
					.setFaceDetectionListener(googleFaceDetect);
			CameraInterface.getInstance().getCameraDevice()
					.startFaceDetection();
		}
	}

	private void stopGoogleFaceDetect() {
		Camera.Parameters params = CameraInterface.getInstance()
				.getCameraParams();
		if (params.getMaxNumDetectedFaces() > 0) {
			CameraInterface.getInstance().getCameraDevice()
					.setFaceDetectionListener(null);
			CameraInterface.getInstance().getCameraDevice().stopFaceDetection();
			faceView.clearFaces();
		}
	}

}
