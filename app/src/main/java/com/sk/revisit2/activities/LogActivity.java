package com.sk.revisit2.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sk.revisit2.R;
import com.sk.revisit2.databinding.ActivityLogBinding;
import com.sk.revisit2.databinding.ItemLogBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {

	private static final String TAG = "LogActivity";
	private ActivityLogBinding binding;
	private RecyclerView logcatView;
	private List<LogItem> logcatItems;
	private LogItemAdapter adapter;
	private Process logcatProcess;
	private Handler mainHandler;
	private String currentPriority = "V";
	private static final int MAX_LOGS = 1000;
	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityLogBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		mainHandler = new Handler(Looper.getMainLooper());
		initUi();
		initLogCatObserver();
	}

	private void initUi() {
		logcatView = binding.logRecyclerView;

		String[] priorities = {"Verbose", "Debug", "Info", "Warning", "Error"};
		ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
				this,
				android.R.layout.simple_dropdown_item_1line,
				priorities
		);

		binding.prioritySpinner.setAdapter(priorityAdapter);
		binding.prioritySpinner.setText(priorities[0], false);
		binding.prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				currentPriority = priorities[i];
				Toast.makeText(LogActivity.this, "selected: " + currentPriority, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		binding.clearButton.setOnClickListener(v -> clearLogs());

		swipeRefreshLayout = binding.swipeRefreshLayout;
		swipeRefreshLayout.setOnRefreshListener(() -> {
			clearLogs();
			swipeRefreshLayout.setRefreshing(false);
		});

		logcatView.setItemAnimator(null);
		logcatView.setHasFixedSize(true);
	}

	private void initLogCatObserver() {
		logcatItems = new ArrayList<>();
		adapter = new LogItemAdapter(logcatItems);
		logcatView.setLayoutManager(new LinearLayoutManager(this));
		logcatView.setAdapter(adapter);

		new Thread(() -> {
			try {
				Runtime.getRuntime().exec("logcat -c");

				ProcessBuilder processBuilder = new ProcessBuilder(
						"logcat",
						"-v", "threadtime"
				);

				processBuilder.redirectErrorStream(true);
				logcatProcess = processBuilder.start();

				if (logcatProcess == null) {
					mainHandler.post(() -> Toast.makeText(this, "Err: Failed to start logcat process", Toast.LENGTH_LONG).show());
					throw new IOException("Failed to start logcat process");
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) continue;

                    /*
                    logcat -v threadtime
                    mm-dd hh-mm-ss.mmm pid tid priority tag: msg
                     */
					String[] parts = line.split(" ", 6);

					LogItem item = new LogItem(
							parts[0] + parts[1], // timestamp
							parts[2], // pid
							parts[3], // tid
							parts[4], // priority
							parts[5], // tag
							parts[6]  // message
					);

					mainHandler.post(() -> {
						if (logcatItems.size() >= MAX_LOGS) {
							logcatItems.remove(0);
							adapter.notifyItemRemoved(0);
						}
						logcatItems.add(item);
						adapter.notifyItemInserted(MAX_LOGS-1);
					});
				}
			} catch (IOException e) {
				Log.e(TAG, "Error reading logcat: " + e);
				mainHandler.post(() -> {
					Toast.makeText(this, "Error reading logs: " + e.getMessage(), Toast.LENGTH_LONG).show();
					finish();
				});
			}
		}).start();
	}

	private void clearLogs() {
		logcatItems.clear();
		adapter.notifyDataSetChanged();
		try {
			Runtime.getRuntime().exec("logcat -c");
		} catch (IOException e) {
			Log.e(TAG, "Error clearing logs: " + e);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (logcatProcess != null) {
			logcatProcess.destroy();
		}
	}

	class LogItem {
		private final String timestamp;
		private final String pid;
		private final String tid;
		private final String priority;
		private final String tag;
		private final String message;

		public LogItem(String timestamp, String pid, String tid, String priority, String tag, String message) {
			this.timestamp = timestamp;
			this.pid = pid;
			this.tid = tid;
			this.priority = priority;
			this.tag = tag;
			this.message = message;
		}

		@NonNull
		@Override
		public String toString() {
			return String.format("%s %s %s %s %s: %s",
					timestamp, pid, tid, priority, tag, message);
		}
	}

	private class LogItemAdapter extends RecyclerView.Adapter<LogItemAdapter.ViewHolder> {
		private final List<LogItem> items;

		public LogItemAdapter(List<LogItem> items) {
			this.items = items;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ItemLogBinding binding = ItemLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
			return new ViewHolder(binding);
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			LogItem item = items.get(position);
			holder.bind(item);
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			ItemLogBinding binding;

			ViewHolder(ItemLogBinding binding) {
				super(binding.getRoot());
				this.binding = binding;
			}

			void bind(LogItem item) {
				binding.timestampText.setText(item.timestamp);
				binding.priorityText.setText(item.priority);
				binding.tagText.setText(item.tag);
				binding.messageText.setText(item.message);
				binding.pidText.setText(item.pid);
				binding.tidText.setText(item.tid);

				// Set text color based on priority
				int colorRes;
				switch (item.priority) {
					case "E":
						colorRes = R.color.error;
						break;
					case "W":
						colorRes = R.color.warning;
						break;
					case "I":
						colorRes = R.color.info;
						break;
					case "D":
						colorRes = R.color.debug;
						break;
					default:
						colorRes = R.color.verbose;
				}
				binding.priorityText.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), colorRes));
			}
		}
	}
}