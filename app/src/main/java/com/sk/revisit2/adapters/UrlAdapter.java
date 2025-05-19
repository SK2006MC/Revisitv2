package com.sk.revisit2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.revisit2.databinding.ItemUrlBinding;
import com.sk.revisit2.models.UrlItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlViewHolder> {
	private final List<UrlItem> items = new ArrayList<>();

	@NonNull
	@Override
	public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemUrlBinding binding = ItemUrlBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new UrlViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
		holder.bind(items.get(position));
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void addUrl(UrlItem item) {
		items.add(0, item);
		notifyItemInserted(0);
	}

	public void updateUrl(String url, UrlItem.Status status, long size, int progress) {
		for (int i = 0; i < items.size(); i++) {
			UrlItem item = items.get(i);
			if (item.getUrl().equals(url)) {
				item.setStatus(status);
				item.setSize(size);
				item.setProgress(progress);
				notifyItemChanged(i);
				break;
			}
		}
	}

	public void clearAll() {
		items.clear();
		notifyDataSetChanged();
	}

	public class UrlViewHolder extends RecyclerView.ViewHolder {
		private final ItemUrlBinding binding;

		UrlViewHolder(ItemUrlBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(UrlItem item) {
			binding.urlText.setText(item.getUrl());
			binding.methodText.setText(item.getMethod());
			long size = item.getSize();
			android.util.Log.d("UrlAdapter", "Size for " + item.getUrl() + ": " + size);
			binding.sizeText.setText(formatSize(size));
		}

		private String formatSize(long size) {
			if (size == -1) return "err";
			if (size == 0) return "0 B";
			if (size < 1024) return size + " B";
			if (size < 1024 * 1024) return String.format(Locale.ENGLISH, "%.1f KB", size / 1024.0);
			return String.format(Locale.ENGLISH, "%.1f MB", size / (1024.0 * 1024));
		}
	}
} 