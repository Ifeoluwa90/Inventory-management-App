package com.IfeoluwaAdewoyin.inventorymanagementapp;

/**
 * Model class representing an inventory item
 * Contains all necessary properties and methods for inventory management
 */
public class InventoryItem {

    private long id;
    private String name;
    private String description;
    private String category;
    private int quantity;
    private int lowStockThreshold;
    private String barcode;

    // Default constructor
    public InventoryItem() {
        this.id = -1;
        this.name = "";
        this.description = "";
        this.category = "";
        this.quantity = 0;
        this.lowStockThreshold = 10;
        this.barcode = "";
    }

    // Constructor with parameters
    public InventoryItem(String name, String description, String category,
                         int quantity, int lowStockThreshold, String barcode) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.barcode = barcode;
    }

    // Getters and setters with proper validation
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category != null ? category.trim() : "";
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity); // Ensure quantity is not negative
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = Math.max(0, lowStockThreshold); // Ensure threshold is not negative
    }

    public String getBarcode() {
        return barcode != null ? barcode : "";
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode != null ? barcode.trim() : "";
    }

    // Helper methods for business logic

    /**
     * Check if item is low in stock
     * @return true if quantity is at or below threshold, false otherwise
     */
    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    /**
     * Check if item is critically low (out of stock)
     * @return true if quantity is 0, false otherwise
     */
    public boolean isCriticalStock() {
        return quantity == 0;
    }

    /**
     * Get stock status as a string
     * @return "Critical", "Low", or "Good"
     */
    public String getStockStatus() {
        if (isCriticalStock()) {
            return "Critical";
        } else if (isLowStock()) {
            return "Low";
        } else {
            return "Good";
        }
    }

    /**
     * Adjust quantity by a given amount (can be positive or negative)
     * @param adjustment Amount to add or subtract from current quantity
     * @return true if adjustment was successful, false if it would result in negative quantity
     */
    public boolean adjustQuantity(int adjustment) {
        int newQuantity = quantity + adjustment;
        if (newQuantity < 0) {
            return false; // Cannot have negative quantity
        }
        quantity = newQuantity;
        return true;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", lowStockThreshold=" + lowStockThreshold +
                ", barcode='" + barcode + '\'' +
                ", stockStatus='" + getStockStatus() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InventoryItem that = (InventoryItem) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
