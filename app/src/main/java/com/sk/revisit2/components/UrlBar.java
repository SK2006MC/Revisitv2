package com.sk.revisit2.components;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;

public class UrlBar extends Component {

	private final EditText urlBar;
	private final WebView webView;

	public UrlBar(Context context, EditText urlBar, WebView webView) {
		super(context);
		this.urlBar = urlBar;
		this.webView = webView;

		urlBar.setOnEditorActionListener((v, actionId, event) -> {
			try {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					webView.loadUrl(urlBar.getText().toString());
				}
			} catch (Exception e) {
				alert(e.toString());
				Log.e(TAG, "err: ", e);
			}
			return true;
		});
		urlBar.setText("https://www.google.com");

	}
}
