package com.IfeoluwaAdewoyin.inventorymanagementapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Database helper class for managing inventory and user data
 * Provides CRUD operations for both users and inventory items
 */
public class InventoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "InventoryDatabase";
    private static final String DATABASE_NAME = "inventory_management.db";
    private static final int DATABASE_VERSION = 1;

    // User table constants
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_CREATED_AT = "created_at";

    // Inventory table constants
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_ITEM_DESCRIPTION = "item_description";
    public static final String COLUMN_ITEM_CATEGORY = "item_category";
    public static final String COLUMN_ITEM_QUANTITY = "item_quantity";
    public static final String COLUMN_LOW_STOCK_THRESHOLD = "low_stock_threshold";
    public static final String COLUMN_BARCODE = "barcode";
    public static final String COLUMN_ITEM_CREATED_AT = "item_created_at";
    public static final String COLUMN_ITEM_UPDATED_AT = "item_updated_at";

    // SQL statements for table creation
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    private static final String CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + TABLE_INVENTORY + " (" +
                    COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                    COLUMN_ITEM_DESCRIPTION + " TEXT, " +
                    COLUMN_ITEM_CATEGORY + " TEXT, " +
                    COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_LOW_STOCK_THRESHOLD + " INTEGER DEFAULT 10, " +
                    COLUMN_BARCODE + " TEXT, " +
                    COLUMN_ITEM_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    COLUMN_ITEM_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public InventoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_INVENTORY_TABLE);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Insert sample data for demonstration purposes
     */
    private void insertSampleData(SQLiteDatabase db) {
        Log.d(TAG, "Inserting sample inventory data");

        // Create demo user
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_EMAIL, "demo@inventory.com");
        userValues.put(COLUMN_PASSWORD, "demo123");
        db.insert(TABLE_USERS, null, userValues);

        // Sample inventory items
        String[] sampleItems = {
                "INSERT INTO " + TABLE_INVENTORY + " (item_name, item_description, item_category, item_quantity, low_stock_threshold) VALUES " +
                        "('Laptop Computer', 'Dell XPS 13 Laptop', 'Electronics', 25, 5)",

                "INSERT INTO " + TABLE_INVENTORY + " (item_name, item_description, item_category, item_quantity, low_stock_threshold) VALUES " +
                        "('Office Chair', 'Ergonomic office chair with lumbar support', 'Office Supplies', 8, 3)",

                "INSERT INTO " + TABLE_INVENTORY + " (item_name, item_description, item_category, item_quantity, low_stock_threshold) VALUES " +
                        "('Wireless Mouse', 'Logitech MX Master 3 Wireless Mouse', 'Electronics', 2, 5)",

                "INSERT INTO " + TABLE_INVENTORY + " (item_name, item_description, item_category, item_quantity, low_stock_threshold) VALUES " +
                        "('Coffee Beans', 'Premium Arabica Coffee Beans - 1kg', 'Food & Beverages', 0, 10)",

                "INSERT INTO " + TABLE_INVENTORY + " (item_name, item_description, item_category, item_quantity, low_stock_threshold) VALUES " +
                        "('Notebook', 'A4 Spiral Notebook - 200 pages', 'Office Supplies', 50, 15)"
        };

        for (String sql : sampleItems) {
            db.execSQL(sql);
        }
    }

    // ======================== USER OPERATIONS ========================

    /**
     * Create a new user account
     * @param email User's email address
     * @param password User's password (should be hashed in production)
     * @return User ID if successful, -1 if failed
     */
    public long createUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password); // Note: In production, hash this password

        long userId = db.insert(TABLE_USERS, null, values);
        db.close();

        Log.d(TAG, "User created with ID: " + userId);
        return userId;
    }

    /**
     * Verify user login credentials
     * @param email User's email
     * @param password User's password
     * @return true if credentials are valid, false otherwise
     */
    public boolean verifyUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        Log.d(TAG, "User verification for " + email + ": " + userExists);
        return userExists;
    }

    /**
     * Check if user already exists
     * @param email User's email to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return exists;
    }

    // ======================== INVENTORY OPERATIONS (CRUD) ========================

    /**
     * CREATE: Add a new inventory item
     * @param item The inventory item to add
     * @return Item ID if successful, -1 if failed
     */
    public long addInventoryItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, item.getName());
        values.put(COLUMN_ITEM_DESCRIPTION, item.getDescription());
        values.put(COLUMN_ITEM_CATEGORY, item.getCategory());
        values.put(COLUMN_ITEM_QUANTITY, item.getQuantity());
        values.put(COLUMN_LOW_STOCK_THRESHOLD, item.getLowStockThreshold());
        values.put(COLUMN_BARCODE, item.getBarcode());

        long itemId = db.insert(TABLE_INVENTORY, null, values);
        db.close();

        Log.d(TAG, "Inventory item added with ID: " + itemId);
        return itemId;
    }

    /**
     * READ: Get all inventory items
     * @return List of all inventory items
     */
    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> itemList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_INVENTORY + " ORDER BY " + COLUMN_ITEM_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                InventoryItem item = cursorToInventoryItem(cursor);
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d(TAG, "Retrieved " + itemList.size() + " inventory items");
        return itemList;
    }

    /**
     * READ: Get inventory item by ID
     * @param itemId The ID of the item to retrieve
     * @return InventoryItem if found, null otherwise
     */
    public InventoryItem getInventoryItem(long itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ITEM_ID, COLUMN_ITEM_NAME, COLUMN_ITEM_DESCRIPTION,
                COLUMN_ITEM_CATEGORY, COLUMN_ITEM_QUANTITY, COLUMN_LOW_STOCK_THRESHOLD,
                COLUMN_BARCODE, COLUMN_ITEM_CREATED_AT, COLUMN_ITEM_UPDATED_AT};
        String selection = COLUMN_ITEM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(itemId)};

        Cursor cursor = db.query(TABLE_INVENTORY, columns, selection, selectionArgs, null, null, null);

        InventoryItem item = null;
        if (cursor.moveToFirst()) {
            item = cursorToInventoryItem(cursor);
        }

        cursor.close();
        db.close();

        return item;
    }

    /**
     * UPDATE: Update an existing inventory item
     * @param item The updated inventory item
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateInventoryItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, item.getName());
        values.put(COLUMN_ITEM_DESCRIPTION, item.getDescription());
        values.put(COLUMN_ITEM_CATEGORY, item.getCategory());
        values.put(COLUMN_ITEM_QUANTITY, item.getQuantity());
        values.put(COLUMN_LOW_STOCK_THRESHOLD, item.getLowStockThreshold());
        values.put(COLUMN_BARCODE, item.getBarcode());

        String whereClause = COLUMN_ITEM_ID + " = ?";
        String[] whereArgs = {String.valueOf(item.getId())};

        int rowsAffected = db.update(TABLE_INVENTORY, values, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Updated inventory item ID " + item.getId() + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * UPDATE: Update only the quantity of an inventory item
     * @param itemId The ID of the item to update
     * @param newQuantity The new quantity value
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateInventoryQuantity(long itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_QUANTITY, newQuantity);

        String whereClause = COLUMN_ITEM_ID + " = ?";
        String[] whereArgs = {String.valueOf(itemId)};

        int rowsAffected = db.update(TABLE_INVENTORY, values, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Updated quantity for item ID " + itemId + " to " + newQuantity);
        return rowsAffected;
    }

    /**
     * DELETE: Remove an inventory item
     * @param itemId The ID of the item to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    public int deleteInventoryItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ITEM_ID + " = ?";
        String[] whereArgs = {String.valueOf(itemId)};

        int rowsAffected = db.delete(TABLE_INVENTORY, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Deleted inventory item ID " + itemId + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    /**
     * Get items that are low in stock (quantity <= low stock threshold)
     * @return List of low stock items
     */
    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> lowStockItems = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_INVENTORY +
                " WHERE " + COLUMN_ITEM_QUANTITY + " <= " + COLUMN_LOW_STOCK_THRESHOLD +
                " ORDER BY " + COLUMN_ITEM_QUANTITY;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                InventoryItem item = cursorToInventoryItem(cursor);
                lowStockItems.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d(TAG, "Found " + lowStockItems.size() + " low stock items");
        return lowStockItems;
    }

    /**
     * Get inventory statistics for dashboard
     * @return Array containing [totalItems, lowStockCount, criticalStockCount]
     */
    public int[] getInventoryStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] stats = new int[3]; // [total, low stock, critical stock]

        // Total items
        Cursor totalCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_INVENTORY, null);
        if (totalCursor.moveToFirst()) {
            stats[0] = totalCursor.getInt(0);
        }
        totalCursor.close();

        // Low stock items (quantity <= threshold but > 0)
        Cursor lowStockCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_INVENTORY +
                        " WHERE " + COLUMN_ITEM_QUANTITY + " <= " + COLUMN_LOW_STOCK_THRESHOLD +
                        " AND " + COLUMN_ITEM_QUANTITY + " > 0", null);
        if (lowStockCursor.moveToFirst()) {
            stats[1] = lowStockCursor.getInt(0);
        }
        lowStockCursor.close();

        // Critical stock items (quantity = 0)
        Cursor criticalCursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_INVENTORY +
                        " WHERE " + COLUMN_ITEM_QUANTITY + " = 0", null);
        if (criticalCursor.moveToFirst()) {
            stats[2] = criticalCursor.getInt(0);
        }
        criticalCursor.close();

        db.close();

        Log.d(TAG, "Inventory stats - Total: " + stats[0] + ", Low: " + stats[1] + ", Critical: " + stats[2]);
        return stats;
    }

    /**
     * Helper method to convert cursor data to InventoryItem object
     * @param cursor Database cursor positioned at a valid row
     * @return InventoryItem object
     */
    private InventoryItem cursorToInventoryItem(Cursor cursor) {
        InventoryItem item = new InventoryItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME)));
        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_DESCRIPTION)));
        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_CATEGORY)));
        item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY)));
        item.setLowStockThreshold(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOW_STOCK_THRESHOLD)));
        item.setBarcode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE)));
        return item;
    }
}
