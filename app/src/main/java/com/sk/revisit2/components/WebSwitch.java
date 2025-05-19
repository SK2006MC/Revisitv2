package com.sk.revisit2.components;

import android.content.Context;

import androidx.appcompat.widget.SwitchCompat;

import com.sk.revisit2.MyUtils;

public class WebSwitch extends Component {
	private final SwitchCompat useInternet;
	private final SwitchCompat shouldUpdate;

	public WebSwitch(Context context, SwitchCompat useInternet, SwitchCompat update) {
		super(context);
		this.useInternet = useInternet;
		this.shouldUpdate = update;
		init();
	}

	public void init() {
		useInternet.setOnCheckedChangeListener((v, b) -> {
			MyUtils.isNetWorkAvailable = b;
			shouldUpdate.setEnabled(b);
		});

		shouldUpdate.setOnCheckedChangeListener((v, b) -> MyUtils.shouldUpdate = b);

		MyUtils.isNetWorkAvailable = false;
		MyUtils.shouldUpdate = false;
	}
}
