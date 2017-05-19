package com.skyeefacedetectos.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.skyeefacedetectos.R;

/**
 * Created by tiejiang on 17-4-26.
 */

public class MenuPageActivity extends Activity implements View.OnClickListener {

    private Button faceAdd, faceDetect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        faceAdd = (Button)findViewById(R.id.face_add);
        faceDetect = (Button)findViewById(R.id.face_detect);

        faceAdd.setOnClickListener(this);
        faceDetect.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.face_add:
                Intent faceAddIntent = new Intent(MenuPageActivity.this, CameraActivity.class);
                faceAddIntent.putExtra("isVerify", false);
                startActivity(faceAddIntent);
                break;
            case R.id.face_detect:
                Intent faceVerifyIntent = new Intent(MenuPageActivity.this, CameraActivity.class);
                faceVerifyIntent.putExtra("isVerify", true);
                startActivity(faceVerifyIntent);
                break;

        }
    }
}
