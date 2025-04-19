package com.sk.revisit2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.sk.revisit2.MyUtils;
import com.sk.revisit2.R;
import com.sk.revisit2.databinding.ActivityMainBinding;
import com.sk.revisit2.databinding.MainNavBinding;
import com.sk.revisit2.log.Log;
import com.sk.revisit2.webview.MyWebView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

	final String TAG = MainActivity.class.getSimpleName();
	MyUtils myUtils;
	ExecutorService executorService;
	String rootPathString;
	String LogFilePath;
	MyWebView webViewMain;
	MainNavBinding navBinding;
	DrawerLayout drawerLayout;
	NavigationView navigationView;
	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

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
		//binding views
		navigationView = binding.navigationView;
		drawerLayout = binding.dlm;
		navBinding = MainNavBinding.bind(navigationView.getHeaderView(0));

		//init nav menu
		navigationView.setNavigationItemSelectedListener(item -> {
			if (item.getItemId() == R.id.nav_settings) {
				startMyActivity(PreferenceActivity.class);
				return true;
			} else if (item.getItemId() == R.id.nav_about) {
				startMyActivity(AboutActivity.class);
				return true;
			}
			return false;
		});

		//init webview
		webViewMain = binding.mainWebView;
		webViewMain.setMyUtils(myUtils);
		webViewMain.setProgressBar(binding.webProgressBar);

		navBinding.urlEditText.setOnEditorActionListener((v, actionId, event) -> {
			try {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					webViewMain.loadUrl(navBinding.urlEditText.getText().toString());
					executorService.execute(() -> runOnUiThread(() ->
							drawerLayout.closeDrawer(navigationView)));
				}
			} catch (Exception e) {
				alert(e.toString());
				Log.e(TAG, " ok ", e);
			}
			return true;
		});

		navBinding.useInternet.setOnCheckedChangeListener((v, b) -> {
			MyUtils.isNetWorkAvailable = b;
			navBinding.shouldUpdate.setEnabled(b);
		});

		navBinding.shouldUpdate.setOnCheckedChangeListener((v, b) -> MyUtils.shouldUpdate = b);

		navBinding.urlEditText.setText("https://www.google.com");
		navBinding.refreshButton.setOnClickListener(v -> {
			webViewMain.loadUrl(navBinding.urlEditText.getText().toString());
		});
	}

	void initBackPress() {
		OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (drawerLayout.isDrawerOpen(navigationView)) {
					drawerLayout.closeDrawer(navigationView);
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
			Log.e(TAG, e);
			alert(e.toString());
		}
		myUtils.close();
		super.onDestroy();
	}

	void alert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	void startMyActivity(Class<?> activityClass) {
		Intent intent = new Intent(this, activityClass);
		drawerLayout.closeDrawer(navigationView);
		startActivity(intent);
	}
}