package com.IfeoluwaAdewoyin.inventorymanagementapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import java.util.List;

/**
 * Manages SMS notifications for inventory alerts
 * Handles low stock notifications and critical stock alerts
 */
public class SMSNotificationManager {

    private static final String TAG = "SMSNotificationManager";
    private static final String DEFAULT_PHONE_NUMBER = "5551234567"; // Demo number for testing

    private Context context;
    private SmsManager smsManager;

    public SMSNotificationManager(Context context) {
        this.context = context;
        this.smsManager = SmsManager.getDefault();
    }

    /**
     * Send low stock notifications for multiple items
     * @param lowStockItems List of items that are low in stock
     */
    public void sendLowStockNotifications(List<InventoryItem> lowStockItems) {
        if (lowStockItems.isEmpty()) {
            return;
        }

        // Check if SMS permission is granted
        if (!SMSPermissionHelper.hasSMSPermission(context)) {
            Log.w(TAG, "SMS permission not granted, cannot send notifications");
            return;
        }

        try {
            StringBuilder message = new StringBuilder();
            message.append("INVENTORY ALERT: ").append(lowStockItems.size())
                    .append(" item(s) are running low:\n");

            for (InventoryItem item : lowStockItems) {
                message.append("• ").append(item.getName())
                        .append(" (").append(item.getQuantity())
                        .append(" left)\n");
            }

            sendSMSMessage(message.toString());

        } catch (Exception e) {
            Log.e(TAG, "Error sending low stock notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Send low stock alert for a single item
     * @param item Item that is low in stock
     */
    public void sendLowStockAlert(InventoryItem item) {
        if (!SMSPermissionHelper.hasSMSPermission(context)) {
            Log.w(TAG, "SMS permission not granted, cannot send alert");
            return;
        }

        try {
            String message;
            if (item.isCriticalStock()) {
                message = "CRITICAL ALERT: " + item.getName() + " is OUT OF STOCK!";
            } else {
                message = "LOW STOCK ALERT: " + item.getName() + " only has " +
                        item.getQuantity() + " items remaining.";
            }

            sendSMSMessage(message);

        } catch (Exception e) {
            Log.e(TAG, "Error sending low stock alert: " + e.getMessage(), e);
        }
    }

    /**
     * Send SMS message using Android SMS API
     * @param message Message content to send
     */
    private void sendSMSMessage(String message) {
        try {
            // Create pending intents for delivery reports
            PendingIntent sentIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent("SMS_SENT"),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            PendingIntent deliveredIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent("SMS_DELIVERED"),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // For demo purposes, we'll use a default number
            // In production, this would come from user settings
            String phoneNumber = getNotificationPhoneNumber();

            // Split message if it's too long
            if (message.length() > 160) {
                // Send as multipart SMS
                smsManager.sendMultipartTextMessage(
                        phoneNumber,
                        null,
                        smsManager.divideMessage(message),
                        null,
                        null
                );
            } else {
                // Send as single SMS
                smsManager.sendTextMessage(
                        phoneNumber,
                        null,
                        message,
                        sentIntent,
                        deliveredIntent
                );
            }

            Log.d(TAG, "SMS notification sent successfully to " + phoneNumber);

            // Show user feedback (for demo purposes)
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "SMS notification sent to " + phoneNumber, Toast.LENGTH_SHORT).show()
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage(), e);

            // Show error feedback (for demo purposes)
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Failed to send SMS notification", Toast.LENGTH_SHORT).show()
                );
            }
        }
    }

    /**
     * Get phone number for notifications from preferences
     * @return Phone number string
     */
    private String getNotificationPhoneNumber() {
        // In a real app, this would be retrieved from user settings/preferences
        // For demo purposes, we return a default number
        android.content.SharedPreferences prefs = context.getSharedPreferences("InventoryAppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("notification_phone", DEFAULT_PHONE_NUMBER);
    }

    /**
     * Set phone number for notifications
     * @param phoneNumber Phone number to set
     */
    public void setNotificationPhoneNumber(String phoneNumber) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("InventoryAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("notification_phone", phoneNumber).apply();
        Log.d(TAG, "Notification phone number updated");
    }
}
