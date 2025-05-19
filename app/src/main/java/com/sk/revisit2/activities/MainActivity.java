package com.sk.revisit2.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.sk.revisit2.MyUtils;
import com.sk.revisit2.R;
import com.sk.revisit2.components.UrlBar;
import com.sk.revisit2.components.UrlMonitor;
import com.sk.revisit2.components.WebSwitch;
import com.sk.revisit2.databinding.ActivityMainBinding;
import com.sk.revisit2.databinding.NavHeaderMainBinding;
import com.sk.revisit2.databinding.NavHeaderUrlMonitorBinding;
import com.sk.revisit2.webview.MyWebView;

public class MainActivity extends BaseActivity {

	public String rootPathString;
	public UrlBar urlBar;
	public WebSwitch webSwitch;
	public UrlMonitor urlMonitor;
	private MyUtils myUtils;
	private MyWebView webViewMain;
	private NavHeaderMainBinding navBinding;
	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private NavigationView urlMonitorView;
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
		myUtils = new MyUtils(this, rootPathString);
	}

	private void initUi() {
		//binding views
		navigationView = binding.navigationView;
		urlMonitorView = binding.urlMonitorView;
		drawerLayout = binding.dlm;
		navBinding = NavHeaderMainBinding.bind(navigationView.getHeaderView(0));
		NavHeaderUrlMonitorBinding urlMonitorViewBinding = NavHeaderUrlMonitorBinding.bind(urlMonitorView.getHeaderView(0));

		initNavMenu();

		//init webview
		webViewMain = binding.mainWebView;
		webViewMain.setMyUtils(myUtils);
		webViewMain.setProgressBar(binding.webProgressBar);
		webViewMain.loadUrl("google.com");

		//component initialization
		urlBar = new UrlBar(this, navBinding.urlEditText, webViewMain);
		webSwitch = new WebSwitch(this, navBinding.useInternet, navBinding.shouldUpdate);
		urlMonitor = new UrlMonitor(this, urlMonitorViewBinding.urlRecyclerView, urlMonitorViewBinding.clearButton);
		webViewMain.setUrlMonitor(urlMonitor);

		navBinding.refreshButton.setOnClickListener(v -> webViewMain.loadUrl(navBinding.urlEditText.getText().toString()));
	}

	private void initNavMenu() {
		navigationView.setNavigationItemSelectedListener(item -> {
			if (item.getItemId() == R.id.nav_settings) {
				startMyActivity(PreferenceActivity.class);
				return true;
			} else if (item.getItemId() == R.id.nav_about) {
				startMyActivity(AboutActivity.class);
				return true;
			} else if (item.getItemId() == R.id.nav_test) {
				startMyActivity(TestActivity.class);
				return true;
			} else if (item.getItemId() == R.id.nav_log) {
				startMyActivity(LogActivity.class);
				return true;
			} else if (item.getItemId() == R.id.nav_url_monitor) {
				drawerLayout.openDrawer(urlMonitorView);
				return true;
			}
			return false;
		});
	}

	void initBackPress() {
		OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (drawerLayout.isDrawerOpen(navigationView)) {
					drawerLayout.closeDrawer(navigationView);
				} else if (drawerLayout.isDrawerOpen(urlMonitorView)) {
					drawerLayout.closeDrawer(urlMonitorView);
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
		myUtils.close();
		super.onDestroy();
	}

	void startMyActivity(Class<?> activityClass) {
		Intent intent = new Intent(this, activityClass);
		drawerLayout.closeDrawer(navigationView);
		alert("launching activity: " + activityClass.getSimpleName());
		startActivity(intent);
	}
}