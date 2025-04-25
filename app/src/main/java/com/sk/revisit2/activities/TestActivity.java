package com.sk.revisit2.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sk.revisit2.databinding.ActivityTestBinding;

public class TestActivity extends AppCompatActivity {

	ActivityTestBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityTestBinding.inflate(getLayoutInflater());
		EdgeToEdge.enable(this);
		setContentView(binding.getRoot());
		ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		//binding.urlEditText;
	}
}