package com.IfeoluwaAdewoyin.inventorymanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AddItemActivity handles adding new inventory items and editing existing ones
 * Supports full CRUD operations with database integration
 */
public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "AddItemActivity";

    // UI Components
    private TextInputEditText productNameInput;
    private TextInputEditText productDescriptionInput;
    private AutoCompleteTextView categoryDropdown;
    private TextInputEditText quantityInput;
    private TextInputEditText lowStockThresholdInput;
    private TextInputEditText barcodeInput;
    private MaterialButton cancelButton;
    private MaterialButton saveButton;

    // Data and database
    private InventoryDatabaseHelper databaseHelper;
    private boolean isEditMode = false;
    private long editItemId = -1;
    private InventoryItem currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize database helper
        databaseHelper = new InventoryDatabaseHelper(this);

        // Check if we're in edit mode
        checkEditMode();

        setupToolbar();
        initializeViews();
        setupCategoryDropdown();
        setupClickListeners();

        // Load item data if in edit mode
        if (isEditMode) {
            loadItemData();
        }

        Log.d(TAG, "AddItemActivity created - Edit mode: " + isEditMode);
    }

    /**
     * Check if activity was started in edit mode
     */
    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false);
        editItemId = intent.getLongExtra("ITEM_ID", -1);
    }

    /**
     * Set up the toolbar with appropriate title and navigation
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title based on mode
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Item" : "Add New Item");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        productNameInput = findViewById(R.id.product_name_input);
        productDescriptionInput = findViewById(R.id.product_description_input);
        categoryDropdown = findViewById(R.id.category_dropdown);
        quantityInput = findViewById(R.id.quantity_input);
        lowStockThresholdInput = findViewById(R.id.low_stock_threshold_input);
        barcodeInput = findViewById(R.id.barcode_input);
        cancelButton = findViewById(R.id.btn_cancel);
        saveButton = findViewById(R.id.btn_save);

        // Update button text for edit mode
        if (isEditMode) {
            saveButton.setText("Update Item");
        }
    }

    /**
     * Set up category dropdown with predefined options
     */
    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.product_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        categoryDropdown.setAdapter(adapter);
    }

    /**
     * Set up click listeners for interactive elements
     */
    private void setupClickListeners() {
        cancelButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> handleSaveItem());
    }

    /**
     * Load item data for editing
     */
    private void loadItemData() {
        if (editItemId == -1) {
            Log.e(TAG, "Invalid item ID for edit mode");
            finish();
            return;
        }

        try {
            currentItem = databaseHelper.getInventoryItem(editItemId);
            if (currentItem != null) {
                populateFormWithItemData(currentItem);
            } else {
                Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading item data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading item data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Populate form fields with existing item data
     * @param item Item data to populate form with
     */
    private void populateFormWithItemData(InventoryItem item) {
        productNameInput.setText(item.getName());
        productDescriptionInput.setText(item.getDescription());
        categoryDropdown.setText(item.getCategory(), false);
        quantityInput.setText(String.valueOf(item.getQuantity()));
        lowStockThresholdInput.setText(String.valueOf(item.getLowStockThreshold()));
        barcodeInput.setText(item.getBarcode());
    }

    /**
     * Handle save/update item action
     */
    private void handleSaveItem() {
        if (!validateForm()) {
            return;
        }

        // Disable button to prevent multiple clicks
        saveButton.setEnabled(false);

        try {
            InventoryItem item = createItemFromForm();

            if (isEditMode) {
                updateExistingItem(item);
            } else {
                createNewItem(item);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving item: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
        }
    }

    /**
     * Create new inventory item in database
     * @param item Item to create
     */
    private void createNewItem(InventoryItem item) {
        long itemId = databaseHelper.addInventoryItem(item);

        if (itemId != -1) {
            Log.d(TAG, "Item created successfully with ID: " + itemId);
            Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();

            // Return success result
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
        }
    }

    /**
     * Update existing inventory item in database
     * @param item Updated item data
     */
    private void updateExistingItem(InventoryItem item) {
        item.setId(editItemId); // Ensure we're updating the correct item

        int rowsAffected = databaseHelper.updateInventoryItem(item);

        if (rowsAffected > 0) {
            Log.d(TAG, "Item updated successfully");
            Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();

            // Return success result
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
        }
    }

    /**
     * Create InventoryItem object from form data
     * @return InventoryItem with form data
     */
    private InventoryItem createItemFromForm() {
        InventoryItem item = new InventoryItem();

        item.setName(getTextFromInput(productNameInput));
        item.setDescription(getTextFromInput(productDescriptionInput));
        item.setCategory(categoryDropdown.getText().toString().trim());
        item.setQuantity(getIntFromInput(quantityInput, 0));
        item.setLowStockThreshold(getIntFromInput(lowStockThresholdInput, 10));
        item.setBarcode(getTextFromInput(barcodeInput));

        return item;
    }

    /**
     * Validate all form inputs
     * @return true if form is valid, false otherwise
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Validate product name
        String productName = getTextFromInput(productNameInput);
        if (productName.isEmpty()) {
            productNameInput.setError("Product name is required");
            productNameInput.requestFocus();
            isValid = false;
        } else if (productName.length() > 100) {
            productNameInput.setError("Product name is too long (max 100 characters)");
            productNameInput.requestFocus();
            isValid = false;
        }

        // Validate quantity
        String quantityStr = getTextFromInput(quantityInput);
        if (quantityStr.isEmpty()) {
            quantityInput.setError("Quantity is required");
            if (isValid) quantityInput.requestFocus();
            isValid = false;
        } else {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    quantityInput.setError("Quantity cannot be negative");
                    if (isValid) quantityInput.requestFocus();
                    isValid = false;
                } else if (quantity > 999999) {
                    quantityInput.setError("Quantity is too large");
                    if (isValid) quantityInput.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                quantityInput.setError("Please enter a valid number");
                if (isValid) quantityInput.requestFocus();
                isValid = false;
            }
        }

        // Validate low stock threshold
        String thresholdStr = getTextFromInput(lowStockThresholdInput);
        if (!thresholdStr.isEmpty()) {
            try {
                int threshold = Integer.parseInt(thresholdStr);
                if (threshold < 0) {
                    lowStockThresholdInput.setError("Threshold cannot be negative");
                    if (isValid) lowStockThresholdInput.requestFocus();
                    isValid = false;
                } else if (threshold > 9999) {
                    lowStockThresholdInput.setError("Threshold is too large");
                    if (isValid) lowStockThresholdInput.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lowStockThresholdInput.setError("Please enter a valid number");
                if (isValid) lowStockThresholdInput.requestFocus();
                isValid = false;
            }
        }

        // Validate category
        String category = categoryDropdown.getText().toString().trim();
        if (category.isEmpty()) {
            categoryDropdown.setError("Please select a category");
            if (isValid) categoryDropdown.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Safely get text from TextInputEditText
     * @param input Input field to get text from
     * @return Trimmed text or empty string if null
     */
    private String getTextFromInput(TextInputEditText input) {
        if (input.getText() != null) {
            return input.getText().toString().trim();
        }
        return "";
    }

    /**
     * Safely get integer from TextInputEditText
     * @param input Input field to get integer from
     * @param defaultValue Default value if parsing fails
     * @return Parsed integer or default value
     */
    private int getIntFromInput(TextInputEditText input, int defaultValue) {
        try {
            String text = getTextFromInput(input);
            if (!text.isEmpty()) {
                return Integer.parseInt(text);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse integer from input: " + e.getMessage());
        }
        return defaultValue;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}