package com.sk.revisit2.activities;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
	String TAG = this.getClass().getSimpleName();
	void alert(String msg){
		Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
	}
}
