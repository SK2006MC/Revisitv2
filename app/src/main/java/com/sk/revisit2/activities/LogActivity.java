package com.sk.revisit2.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import com.sk.revisit2.R;
import com.sk.revisit2.databinding.ActivityLogBinding;
import com.sk.revisit2.databinding.ItemLogBinding;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStreamReader;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.LayoutInflater;
import java.util.concurrent.TimeUnit;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Intent;
import android.widget.ArrayAdapter;

public class LogActivity extends AppCompatActivity {

    private static final String TAG = "LogActivity";
    private ActivityLogBinding binding;
    private RecyclerView logcatView;
    private EditText searchEditText;
    private List<LogItem> logcatItems;
    private LogItemAdapter adapter;
    private Process logcatProcess;
    private Handler mainHandler;
    private String currentFilter = "";
    private String currentPriority = "V"; // Default to Verbose
    private static final int MAX_LOGS = 1000;
    private static final long SEARCH_DELAY = 300; // milliseconds
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
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

    private void initUi(){
        searchEditText = binding.searchEditText;
        logcatView = binding.logRecyclerView;
        
        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentFilter = s.toString();
                    filterLogs();
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Setup priority spinner
        String[] priorities = {"Verbose", "Debug", "Info", "Warning", "Error"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            priorities
        );
        binding.prioritySpinner.setAdapter(priorityAdapter);
        binding.prioritySpinner.setText(priorities[0], false);

        // Setup clear button
        binding.clearButton.setOnClickListener(v -> clearLogs());

        // Setup export button
        binding.exportButton.setOnClickListener(v -> exportLogs());

        swipeRefreshLayout = binding.swipeRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearLogs();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Add smooth scrolling behavior
        logcatView.setItemAnimator(null); // Disable animations for better performance
        logcatView.setHasFixedSize(true); // Optimize for fixed size items
    }

    private void initLogCatObserver(){
        logcatItems = new ArrayList<>();
        adapter = new LogItemAdapter(logcatItems);
        logcatView.setLayoutManager(new LinearLayoutManager(this));
        logcatView.setAdapter(adapter);

        new Thread(() -> {
            try {
                // Clear existing logs first
                //Runtime.getRuntime().exec("logcat -c");
                
                // Start logcat process with buffer size limit and threadtime format
                ProcessBuilder processBuilder = new ProcessBuilder(
                    "logcat",
                    "-v", "threadtime",
                    "-b", "main",  // main buffer
                    //"-b", "system", // system buffer
                    "-b", "crash",  // crash buffer
                    "-n", "1000"    // limit buffer size
                );
                processBuilder.redirectErrorStream(true);
                logcatProcess = processBuilder.start();
                
                // Check if process started successfully
                if (logcatProcess == null) {
                    throw new IOException("Failed to start logcat process");
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    
                    // Parse logcat line
                    String[] parts = line.split("\\s+", 6);
                    if (parts.length >= 6) {
                        final LogItem item = new LogItem(
                            parts[0], // timestamp
                            parts[1], // pid
                            parts[2], // tid
                            parts[3], // priority
                            parts[4], // tag
                            parts[5]  // message
                        );
                        
                        mainHandler.post(() -> {
                            if (logcatItems.size() >= MAX_LOGS) {
                                logcatItems.remove(0);
                                adapter.notifyItemRemoved(0);
                            }
                            logcatItems.add(item);
                            if (shouldShowLog(item)) {
                                adapter.notifyItemInserted(logcatItems.size() - 1);
                                logcatView.smoothScrollToPosition(logcatItems.size() - 1);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading logcat: " + e.toString());
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error reading logs: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish(); // Close activity on critical error
                });
            }
        }).start();
    }

    private boolean shouldShowLog(LogItem item) {
        boolean matchesFilter = currentFilter.isEmpty() || 
            item.getMessage().toLowerCase().contains(currentFilter.toLowerCase()) ||
            item.getTag().toLowerCase().contains(currentFilter.toLowerCase());
            
        boolean matchesPriority = getPriorityLevel(item.getPriority()) >= 
            getPriorityLevel(currentPriority);
            
        return matchesFilter && matchesPriority;
    }

    private int getPriorityLevel(String priority) {
        switch (priority) {
            case "V": return 0; // Verbose
            case "D": return 1; // Debug
            case "I": return 2; // Info
            case "W": return 3; // Warning
            case "E": return 4; // Error
            default: return 0;
        }
    }

    private void filterLogs() {
        adapter.notifyDataSetChanged();
    }

    private void clearLogs() {
        logcatItems.clear();
        adapter.notifyDataSetChanged();
        try {
            Runtime.getRuntime().exec("logcat -c");
        } catch (IOException e) {
            Log.e(TAG, "Error clearing logs: " + e.toString());
        }
    }

    private void exportLogs() {
        StringBuilder logs = new StringBuilder();
        for (LogItem item : logcatItems) {
            logs.append(item.toString()).append("\n");
        }
        
        // Save to file or share
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, logs.toString());
        startActivity(Intent.createChooser(shareIntent, "Export Logs"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logcatProcess != null) {
            logcatProcess.destroy();
        }
    }

    class LogItem {
        private String timestamp;
        private String pid;
        private String tid;
        private String priority;
        private String tag;
        private String message;

        public LogItem(String timestamp, String pid, String tid, String priority, String tag, String message) {
            this.timestamp = timestamp;
            this.pid = pid;
            this.tid = tid;
            this.priority = priority;
            this.tag = tag;
            this.message = message;
        }

        public String getTimestamp() { return timestamp; }
        public String getPid() { return pid; }
        public String getTid() { return tid; }
        public String getPriority() { return priority; }
        public String getTag() { return tag; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return String.format("%s %s %s %s %s: %s", 
                timestamp, pid, tid, priority, tag, message);
        }
    }

    private class LogItemAdapter extends RecyclerView.Adapter<LogItemAdapter.ViewHolder> {
        private List<LogItem> items;

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

			void bind(LogItem item){
				binding.timestampText.setText(item.timestamp);
				binding.priorityText .setText(item.priority);
				binding.tagText.setText(item.tag);
				binding.messageText.setText(item.message);
				binding.pidText.setText(item.pid);
				binding.tidText.setText(item.tid);

                // Set text color based on priority
                int colorRes;
                switch (item.priority) {
                    case "E": colorRes = R.color.error; break;
                    case "W": colorRes = R.color.warning; break;
                    case "I": colorRes = R.color.info; break;
                    case "D": colorRes = R.color.debug; break;
                    default: colorRes = R.color.verbose;
                }
                binding.priorityText.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
			}
        }
    }
}