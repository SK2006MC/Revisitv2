package com.sk.revisit2.components;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit2.adapters.UrlAdapter;
import com.sk.revisit2.models.UrlItem;

public class UrlMonitor extends Component {
	private final RecyclerView urlRecyclerView;
	private final Button clearButton;
	UrlAdapter urlAdapter;

	public UrlMonitor(Context context, RecyclerView recyclerView, Button clear) {
		super(context);
		this.urlRecyclerView = recyclerView;
		this.clearButton = clear;
		
		urlAdapter = new UrlAdapter();
		
		urlRecyclerView.setLayoutManager(new LinearLayoutManager(context));
		urlRecyclerView.setAdapter(urlAdapter);
		clearButton.setOnClickListener((v) -> {
			urlAdapter.clearAll();
		});
	}

	public void addUrlToMonitor(String url, String method) {
		runOnUiThread(() -> urlAdapter.addUrl(new UrlItem(url, method)));
	}

	public void updateUrlStatus(String url, UrlItem.Status status, long size, int progress) {
		runOnUiThread(() -> urlAdapter.updateUrl(url, status, size, progress));
	}

	private void runOnUiThread(Runnable e) {
		urlRecyclerView.post(e);
	}
}
