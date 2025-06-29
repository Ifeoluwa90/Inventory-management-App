package com.IfeoluwaAdewoyin.inventorymanagementapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * MainActivity - Main dashboard showing inventory items and statistics
 * Handles CRUD operations, SMS permissions, and navigation
 */
public class MainActivity extends AppCompatActivity implements
        InventoryAdapter.OnItemClickListener,
        InventoryAdapter.OnItemDeleteListener,
        InventoryAdapter.OnQuantityChangeListener {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "InventoryAppPrefs";
    private static final int ADD_ITEM_REQUEST_CODE = 100;
    private static final int EDIT_ITEM_REQUEST_CODE = 101;

    // UI Components
    private RecyclerView inventoryRecyclerView;
    private FloatingActionButton fabAddItem;
    private TextView totalItemsCount;
    private TextView lowStockCount;
    private TextView criticalStockCount;

    // Data and adapters
    private InventoryAdapter inventoryAdapter;
    private InventoryDatabaseHelper databaseHelper;
    private SMSNotificationManager smsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database and SMS manager
        databaseHelper = new InventoryDatabaseHelper(this);
        smsManager = new SMSNotificationManager(this);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        // Load data and check SMS permissions
        loadInventoryData();
        checkSMSPermissions();

        Log.d(TAG, "MainActivity created successfully");
    }

    /**
     * Set up the toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        inventoryRecyclerView = findViewById(R.id.inventory_recycler_view);
        fabAddItem = findViewById(R.id.fab_add_item);
        totalItemsCount = findViewById(R.id.total_items_count);
        lowStockCount = findViewById(R.id.low_stock_count);
        criticalStockCount = findViewById(R.id.critical_stock_count);
    }

    /**
     * Set up RecyclerView with adapter
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        inventoryRecyclerView.setLayoutManager(layoutManager);

        inventoryAdapter = new InventoryAdapter(this);
        inventoryAdapter.setOnItemClickListener(this);
        inventoryAdapter.setOnItemDeleteListener(this);
        inventoryAdapter.setOnQuantityChangeListener(this);

        inventoryRecyclerView.setAdapter(inventoryAdapter);
    }

    /**
     * Set up click listeners for interactive elements
     */
    private void setupClickListeners() {
        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            startActivityForResult(intent, ADD_ITEM_REQUEST_CODE);
        });
    }

    /**
     * Load inventory data from database and update UI
     */
    private void loadInventoryData() {
        try {
            // Get all inventory items
            List<InventoryItem> items = databaseHelper.getAllInventoryItems();
            inventoryAdapter.updateItems(items);

            // Update statistics
            updateInventoryStatistics();

            // Check for low stock items and send notifications if needed
            checkLowStockAndNotify();

            Log.d(TAG, "Loaded " + items.size() + " inventory items");

        } catch (Exception e) {
            Log.e(TAG, "Error loading inventory data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading inventory data", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update inventory statistics display
     */
    private void updateInventoryStatistics() {
        int[] stats = databaseHelper.getInventoryStats();

        totalItemsCount.setText(String.valueOf(stats[0]));
        lowStockCount.setText(String.valueOf(stats[1]));
        criticalStockCount.setText(String.valueOf(stats[2]));
    }

    /**
     * Check for low stock items and send SMS notifications if permissions granted
     */
    private void checkLowStockAndNotify() {
        if (SMSPermissionHelper.hasSMSPermission(this)) {
            List<InventoryItem> lowStockItems = databaseHelper.getLowStockItems();
            if (!lowStockItems.isEmpty()) {
                smsManager.sendLowStockNotifications(lowStockItems);
            }
        }
    }

    /**
     * Check and request SMS permissions if needed
     */
    private void checkSMSPermissions() {
        if (!SMSPermissionHelper.hasTelephonyFeature(this)) {
            Log.d(TAG, "Device does not support telephony features");
            return;
        }

        if (!SMSPermissionHelper.hasSMSPermission(this)) {
            if (SMSPermissionHelper.shouldShowRationale(this)) {
                showSMSPermissionDialog();
            } else {
                requestSMSPermission();
            }
        } else {
            Log.d(TAG, "SMS permission already granted");
        }
    }

    /**
     * Show dialog explaining why SMS permission is needed
     */
    private void showSMSPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("SMS Notifications")
                .setMessage("This app can send SMS notifications when inventory items are running low. " +
                        "Would you like to enable SMS notifications?")
                .setPositiveButton("Enable", (dialog, which) -> requestSMSPermission())
                .setNegativeButton("Skip", (dialog, which) -> {
                    Log.d(TAG, "User declined SMS permissions");
                    Toast.makeText(this, "SMS notifications disabled. You can enable them later in Settings.",
                            Toast.LENGTH_LONG).show();
                })
                .show();
    }

    /**
     * Request SMS permission from user
     */
    private void requestSMSPermission() {
        SMSPermissionHelper.requestSMSPermission(this);
    }

    // ======================== ADAPTER INTERFACE IMPLEMENTATIONS ========================

    @Override
    public void onItemClick(InventoryItem item, int position) {
        // Navigate to edit item activity
        Intent intent = new Intent(this, AddItemActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("ITEM_ID", item.getId());
        startActivityForResult(intent, EDIT_ITEM_REQUEST_CODE);
    }

    @Override
    public void onItemDelete(InventoryItem item, int position) {
        // Show confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        int rowsAffected = databaseHelper.deleteInventoryItem(item.getId());
                        if (rowsAffected > 0) {
                            inventoryAdapter.removeItem(position);
                            updateInventoryStatistics();
                            Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error deleting item: " + e.getMessage(), e);
                        Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onQuantityChanged(InventoryItem item, int newQuantity, int position) {
        try {
            // Update quantity in database
            int rowsAffected = databaseHelper.updateInventoryQuantity(item.getId(), newQuantity);
            if (rowsAffected > 0) {
                // Update item object and refresh display
                item.setQuantity(newQuantity);
                inventoryAdapter.updateItem(position, item);
                updateInventoryStatistics();

                Toast.makeText(this, "Quantity updated to " + newQuantity, Toast.LENGTH_SHORT).show();

                // Check if item is now low stock and send notification
                if (item.isLowStock() && SMSPermissionHelper.hasSMSPermission(this)) {
                    smsManager.sendLowStockAlert(item);
                }
            } else {
                Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating quantity: " + e.getMessage(), e);
            Toast.makeText(this, "Error updating quantity", Toast.LENGTH_SHORT).show();
        }
    }

    // ======================== ACTIVITY LIFECYCLE AND RESULTS ========================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_ITEM_REQUEST_CODE || requestCode == EDIT_ITEM_REQUEST_CODE) {
                // Refresh data when returning from add/edit activity
                loadInventoryData();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMSPermissionHelper.SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "SMS permission granted");
                Toast.makeText(this, "SMS notifications enabled", Toast.LENGTH_SHORT).show();

                // Check for low stock items now that we have permission
                checkLowStockAndNotify();
            } else {
                Log.d(TAG, "SMS permission denied");
                Toast.makeText(this, "SMS notifications disabled. You can enable them later in Settings.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadInventoryData();
    }

    // ======================== MENU HANDLING ========================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_export) {
            exportInventoryData();
            return true;
        } else if (id == R.id.action_logout) {
            handleLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Export inventory data (placeholder implementation)
     */
    private void exportInventoryData() {
        try {
            List<InventoryItem> items = databaseHelper.getAllInventoryItems();

            // Simple CSV export to demonstrate functionality
            StringBuilder csvData = new StringBuilder();
            csvData.append("Name,Description,Category,Quantity,Low Stock Threshold,Barcode\n");

            for (InventoryItem item : items) {
                csvData.append(item.getName()).append(",")
                        .append(item.getDescription()).append(",")
                        .append(item.getCategory()).append(",")
                        .append(item.getQuantity()).append(",")
                        .append(item.getLowStockThreshold()).append(",")
                        .append(item.getBarcode()).append("\n");
            }

            Log.d(TAG, "CSV Export Data:\n" + csvData.toString());
            Toast.makeText(this, "Export functionality would save " + items.size() +
                    " items to CSV file", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Error exporting data: " + e.getMessage(), e);
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle user logout
     */
    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear login state
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    // Navigate to login screen
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}