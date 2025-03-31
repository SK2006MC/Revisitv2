package com.sk.revisit2;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.sk.revisit2.log.Log;
import com.sk.revisit2.webview.MyWebView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

	final String TAG = MainActivity.class.getSimpleName();
	EditText urlEditText;
	DrawerLayout drawerLayoutMain;
	LinearLayout linearLayoutMain;
	MyWebView webViewMain;
	MyUtils myUtils;
	ExecutorService executorService;
	String rootPathString;
	String LogFilePath;
	SwitchCompat useInternet, shouldUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initVars();
		initUi();
		initBackPress();
	}

	private void initVars() {
		rootPathString = getObbDir().getAbsolutePath();
		LogFilePath = rootPathString + File.separator + "Log2.txt";
		myUtils = new MyUtils(this, rootPathString);
		executorService = Executors.newSingleThreadExecutor();
	}

	private void initUi() {
		drawerLayoutMain = findViewById(R.id.dlm);
		linearLayoutMain = findViewById(R.id.llm);

		webViewMain = findViewById(R.id.mainWebView);
		webViewMain.setMyUtils(myUtils);

		urlEditText = findViewById(R.id.urlEditText);
		urlEditText.setOnEditorActionListener((v, actionId, event) -> {
			try {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					webViewMain.loadUrl(urlEditText.getText().toString());
					executorService.execute(() -> runOnUiThread(() -> drawerLayoutMain.closeDrawer(linearLayoutMain)));
				}
			} catch (Exception e) {
				alert(e.toString());
				Log.e(TAG, " ok ", e);
			}
			return true;
		});

		useInternet = findViewById(R.id.useInternet);
		shouldUpdate = findViewById(R.id.shouldUpdate);

		useInternet.setChecked(false);
		useInternet.setOnCheckedChangeListener((v, b) -> {
			MyUtils.isNetWorkAvailable = b;
			shouldUpdate.setEnabled(b);
		});

		shouldUpdate.setChecked(false);
		shouldUpdate.setOnCheckedChangeListener((v, b) -> MyUtils.shouldUpdate = b);
	}

	void initBackPress(){
		OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (drawerLayoutMain.isDrawerOpen(linearLayoutMain)) {
					drawerLayoutMain.closeDrawer(linearLayoutMain);
				} else if (webViewMain.canGoBack()) {
					webViewMain.goBack();
					alert("gone back");
				} else {
					finish();
				}
			}
		};

		getOnBackPressedDispatcher().addCallback(backPressedCallback);
	}

	@Override
	protected void onDestroy() {
		webViewMain.destroy();
		try {
			Log.saveLog(new File(LogFilePath));
		} catch (Exception e) {
			Log.e(TAG,e);
			alert(e.toString());
		}
		myUtils.close();
		super.onDestroy();
	}

	void alert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}