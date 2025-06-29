package com.IfeoluwaAdewoyin.inventorymanagementapp;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch smsNotificationsSwitch;
    private MaterialSwitch pushNotificationsSwitch;
    private Slider lowStockSlider;
    private Slider criticalStockSlider;
    private TextView lowStockValue;
    private TextView criticalStockValue;
    private LinearLayout exportDataOption;
    private LinearLayout aboutOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        initializeViews();
        setupSliders();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        smsNotificationsSwitch = findViewById(R.id.sms_notifications_switch);
        pushNotificationsSwitch = findViewById(R.id.push_notifications_switch);
        lowStockSlider = findViewById(R.id.low_stock_slider);
        criticalStockSlider = findViewById(R.id.critical_stock_slider);
        lowStockValue = findViewById(R.id.low_stock_value);
        criticalStockValue = findViewById(R.id.critical_stock_value);
        exportDataOption = findViewById(R.id.export_data_option);
        aboutOption = findViewById(R.id.about_option);
    }

    private void setupSliders() {
        lowStockSlider.addOnChangeListener((slider, value, fromUser) -> {
            lowStockValue.setText(String.format("%.0f units", value));
        });

        criticalStockSlider.addOnChangeListener((slider, value, fromUser) -> {
            criticalStockValue.setText(String.format("%.0f units", value));
        });
    }

    private void setupClickListeners() {
        exportDataOption.setOnClickListener(v -> {
            // TODO: Implement data export functionality in Project 3
            Toast.makeText(this, "Export functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        aboutOption.setOnClickListener(v -> {
            // TODO: Show about dialog in Project 3
            Toast.makeText(this, "Inventory Management App v1.0.0", Toast.LENGTH_SHORT).show();
        });
    }
}
