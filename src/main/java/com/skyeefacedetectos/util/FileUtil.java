package com.skyeefacedetectos.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.skyeefacedetectos.ui.FaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
	private static final  String TAG = "FileUtil";
	private static final File parentPath = Environment.getExternalStorageDirectory();
	private static   String storagePath = "";
	private static final String DST_FOLDER_NAME = "PlayCamera";

	/**?????????��??
	 * @return
	 */
	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;
			File f = new File(storagePath);
			if(!f.exists()){
				f.mkdir();
			}
		}
		return storagePath;
	}

	/**????Bitmap??sdcard
	 * @param b
	 */
	public static Uri saveBitmap(Bitmap b, boolean cut_ok){

		String path = initPath();
		long dataTake = System.currentTimeMillis();
		String jpegName = path + "/" + "img" +".png";
//		String jpegName = path + "/" + "detectimg" +".png";
//		Log.i("TIEJIANG", "saveBitmap:jpegName = " + jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.PNG, 100, bos);
			bos.flush();
			bos.close();
			File temp = new File(jpegName);
			Uri imageFileUri = Uri.fromFile(temp);
			Log.d("TIEJIANG", "saveBitmap:jpegName = " + jpegName + " ??url=" + imageFileUri);
			if (!cut_ok){
				Intent intent = new Intent();
				intent.putExtra("message", "finsh"+"&"+imageFileUri);
				intent.setAction("com.take.pich");
				FaceView.mContext.getApplicationContext().sendBroadcast(intent);
			}
			return imageFileUri;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i("TIEJIANG", "saveBitmap: failed! ");
			Intent intent=new Intent();
			intent.putExtra("message", "finsh");
			intent.setAction("com.take.pich");
			FaceView.mContext.getApplicationContext().sendBroadcast(intent);
			e.printStackTrace();
		}
		return null;

	}


}
