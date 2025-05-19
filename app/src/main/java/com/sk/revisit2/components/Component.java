package com.sk.revisit2.components;

import android.content.Context;
import android.widget.Toast;

public class Component {

	final Context context;
	String TAG = this.getClass().getSimpleName();

	Component(Context context) {
		this.context = context;
	}

	public void alert(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
