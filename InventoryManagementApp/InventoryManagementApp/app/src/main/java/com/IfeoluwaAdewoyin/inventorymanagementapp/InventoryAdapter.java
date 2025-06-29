package com.IfeoluwaAdewoyin.inventorymanagementapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying inventory items in a grid layout
 * Handles item display, click events, and quantity updates
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private Context context;
    private List<InventoryItem> inventoryItems;
    private OnItemClickListener itemClickListener;
    private OnItemDeleteListener itemDeleteListener;
    private OnQuantityChangeListener quantityChangeListener;

    // Interface definitions for callback events
    public interface OnItemClickListener {
        void onItemClick(InventoryItem item, int position);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(InventoryItem item, int position);
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged(InventoryItem item, int newQuantity, int position);
    }

    // Constructor
    public InventoryAdapter(Context context) {
        this.context = context;
        this.inventoryItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory_card, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }

    // Public methods for data manipulation

    /**
     * Update the entire dataset
     * @param newItems New list of inventory items
     */
    public void updateItems(List<InventoryItem> newItems) {
        this.inventoryItems.clear();
        this.inventoryItems.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Add a single item to the list
     * @param item Item to add
     */
    public void addItem(InventoryItem item) {
        inventoryItems.add(item);
        notifyItemInserted(inventoryItems.size() - 1);
    }

    /**
     * Remove an item from the list
     * @param position Position of item to remove
     */
    public void removeItem(int position) {
        if (position >= 0 && position < inventoryItems.size()) {
            inventoryItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Update a specific item in the list
     * @param position Position of item to update
     * @param updatedItem Updated item data
     */
    public void updateItem(int position, InventoryItem updatedItem) {
        if (position >= 0 && position < inventoryItems.size()) {
            inventoryItems.set(position, updatedItem);
            notifyItemChanged(position);
        }
    }

    // Setter methods for event listeners
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.itemDeleteListener = listener;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.quantityChangeListener = listener;
    }

    /**
     * ViewHolder class for inventory items
     * Handles individual item layout and user interactions
     */
    public class InventoryViewHolder extends RecyclerView.ViewHolder {

        private MaterialCardView itemCard;
        private ImageView itemImage;
        private TextView itemName;
        private TextView itemDescription;
        private TextView itemQuantity;
        private View stockStatusIndicator;
        private MaterialButton deleteButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            itemCard = itemView.findViewById(R.id.item_card);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemDescription = itemView.findViewById(R.id.item_description);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            stockStatusIndicator = itemView.findViewById(R.id.stock_status_indicator);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        /**
         * Bind data to views for a specific inventory item
         * @param item The inventory item to display
         * @param position Position in the adapter
         */
        public void bind(InventoryItem item, int position) {
            // Set basic item information
            itemName.setText(item.getName());
            itemDescription.setText(item.getDescription());
            itemQuantity.setText("Qty: " + item.getQuantity());

            // Set stock status indicator color based on inventory level
            updateStockStatusIndicator(item);

            // Set up click listeners
            setupClickListeners(item, position);

            // Update card appearance based on stock status
            updateCardAppearance(item);
        }

        /**
         * Update the stock status indicator color
         * @param item Inventory item to check status for
         */
        private void updateStockStatusIndicator(InventoryItem item) {
            int colorResource;

            if (item.isCriticalStock()) {
                colorResource = R.color.inventory_critical;
            } else if (item.isLowStock()) {
                colorResource = R.color.inventory_low;
            } else {
                colorResource = R.color.inventory_good;
            }

            int color = ContextCompat.getColor(context, colorResource);
            stockStatusIndicator.setBackgroundColor(color);
        }

        /**
         * Set up click listeners for user interactions
         * @param item Current inventory item
         * @param position Position in adapter
         */
        private void setupClickListeners(InventoryItem item, int position) {
            // Card click for item details/editing
            itemCard.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item, position);
                }
            });

            // Delete button click
            deleteButton.setOnClickListener(v -> {
                if (itemDeleteListener != null) {
                    itemDeleteListener.onItemDelete(item, position);
                }
            });

            // Long press for quantity adjustment options
            itemCard.setOnLongClickListener(v -> {
                showQuantityAdjustmentOptions(item, position);
                return true;
            });
        }

        /**
         * Update card appearance based on stock status
         * @param item Inventory item to check
         */
        private void updateCardAppearance(InventoryItem item) {
            if (item.isCriticalStock()) {
                itemCard.setStrokeColor(ContextCompat.getColor(context, R.color.inventory_critical));
                itemCard.setStrokeWidth(4);
            } else if (item.isLowStock()) {
                itemCard.setStrokeColor(ContextCompat.getColor(context, R.color.inventory_low));
                itemCard.setStrokeWidth(3);
            } else {
                itemCard.setStrokeColor(ContextCompat.getColor(context, R.color.md_theme_outline));
                itemCard.setStrokeWidth(1);
            }
        }

        /**
         * Show options for adjusting item quantity
         * @param item Item to adjust
         * @param position Position in adapter
         */
        private void showQuantityAdjustmentOptions(InventoryItem item, int position) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Adjust Quantity for " + item.getName());
            builder.setMessage("Current quantity: " + item.getQuantity());

            // Add options to increase/decrease quantity
            String[] options = {"Increase by 1", "Decrease by 1", "Set Custom Amount"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Increase by 1
                        adjustQuantity(item, position, 1);
                        break;
                    case 1: // Decrease by 1
                        adjustQuantity(item, position, -1);
                        break;
                    case 2: // Custom amount
                        showCustomQuantityDialog(item, position);
                        break;
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        /**
         * Show dialog for custom quantity input
         * @param item Item to adjust
         * @param position Position in adapter
         */
        private void showCustomQuantityDialog(InventoryItem item, int position) {
            android.widget.EditText input = new android.widget.EditText(context);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            input.setText(String.valueOf(item.getQuantity()));

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Set Quantity");
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                try {
                    int newQuantity = Integer.parseInt(input.getText().toString());
                    if (newQuantity >= 0) {
                        if (quantityChangeListener != null) {
                            quantityChangeListener.onQuantityChanged(item, newQuantity, position);
                        }
                    } else {
                        Toast.makeText(context, "Quantity cannot be negative",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Please enter a valid number",
                            Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        /**
         * Adjust quantity by a specific amount
         * @param item Item to adjust
         * @param position Position in adapter
         * @param adjustment Amount to adjust (can be positive or negative)
         */
        private void adjustQuantity(InventoryItem item, int position, int adjustment) {
            int newQuantity = item.getQuantity() + adjustment;
            if (newQuantity >= 0) {
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged(item, newQuantity, position);
                }
            } else {
                Toast.makeText(context, "Quantity cannot be negative",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
