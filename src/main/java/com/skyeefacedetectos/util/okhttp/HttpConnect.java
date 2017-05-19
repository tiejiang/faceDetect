package com.skyeefacedetectos.util.okhttp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by tiejiang on 17-4-27.
 */

public class HttpConnect {


    /**
     * note: \" 表示一个双引号字符
     *
      "{\n" +
        "    \"company_id\": \"张三 \"\"age\": \"23 \"\n" +
        "}";
      */
    public static final String faceAddURL = "http://119.29.150.245:8190/api_add_member_face";
    public static final String faceDetectURL = "http://119.29.150.245:8190/api_verify_member_face";
    public static Handler mHandler;
//    public String mPostJson = "{\n" + "    \"com/id\": \"100 \" " + "," +
//                "\"member_id\": \"1 \" " + "," +
//                "\"face_image_type\": \"jpg \" " + "," +
//                "\"face_image_data\": \" \" \n   " +
//                "}";

    /**
     *send the base64 encode picture to server side
     * this function contains two state this add member or verify member
     *
     * @param verify the value is truth the function choose the verify json string to execute
     *               others to choose the add json to execute
     *@param image_data it includes add member base64 encode or verify member base64 encode
     *                  whether to execute depends on the former param(boolean verify)
     *@param member_id when add member, it should be set a number to the current member
     */
    public void httpPostJson(boolean verify, String image_data, String member_id){

        RequestBody mRequestBody;
        String data = replaceBlank(image_data);
        String mPostAddMemberJson = "{\n" + "    \"company_id\": \"100 \" " + "," +
                "\"member_id\": " + "\"" + member_id + "\"" + "," +
                "\"face_image_type\": \"jpg \" " + "," +
                "\"face_image_data\": " + "\"" + data + " \"" +
                " \n}";
        String mPostVerifyMemberJson = "{\n" + "    \"company_id\": \"100 \" " + "," +
                "\"face_image_type\": \"jpg \" " + "," +
                "\"face_image_data\": " + "\"" + data + " \"" +
                " \n}";
        String postJsonString;
        String postUrl;
        if (verify){
            postJsonString = mPostVerifyMemberJson;
            postUrl = faceDetectURL;
        }else {
            postJsonString = mPostAddMemberJson;
            postUrl = faceAddURL;
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient mOkHttpClient = new OkHttpClient();
        mRequestBody = RequestBody.create(JSON, postJsonString);
        Request mRequest = new Request.Builder()
                .url(postUrl)
                .post(mRequestBody)
                .build();
        Call mCall = mOkHttpClient.newCall(mRequest);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("TIEJIANG", "POST FAILED");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resStr = response.body().string();
//                    Log.d("TIEJIANG", "onResponse: " + response.body().string());
                    returnMessage(resStr);
                }
            }
        });
    }

    public void returnMessage(String str){
//        if (str!=null) {
        Message msg = new Message();
        msg.obj = str;
        mHandler.sendMessage(msg);
//        }
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
