package com.example.smsread;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String SMS_PREFS = "SMS_STORE";
    private static final String SMS_KEY = "latest_sms";
    private TextView smsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsText = findViewById(R.id.smsText);

        // Show the last saved SMS from SharedPreferences
        showLastSms();

        // Start listening for new SMS notifications (Long Polling)
        startListeningForNewSms();
    }

    private void showLastSms() {
        SharedPreferences sp = getSharedPreferences(SMS_PREFS, MODE_PRIVATE);
        String msg = sp.getString(SMS_KEY, "No SMS yet.");
        smsText.setText(msg);
    }

    private void startListeningForNewSms() {
        // Handler to run the polling task periodically
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Make an HTTP request to check for new SMS data from server
                checkForNewSms();

                // Repeat the task every 10 seconds (or any interval you prefer)
                handler.postDelayed(this, 10000);
            }
        };

        handler.post(runnable); // Start the polling task
    }

    private void checkForNewSms() {
        // This method is used to make a HTTP request to your server to check for new SMS.
        new Thread(() -> {
            try {
                URL url = new URL("http://your_android_app_server_address/notify"); // Replace with your server URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "sender=JohnDoe&message=Hello, how are you?&time=2025-04-17 10:30AM"; // Example data, update as needed
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Successfully sent data to server, now show the response or handle UI update
                    SharedPreferences sp = getSharedPreferences(SMS_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(SMS_KEY, "New SMS: Hello, how are you?"); // Update with actual data
                    editor.apply();

                    // Run this on the main thread to update the UI
                    runOnUiThread(() -> smsText.setText("New SMS received: Hello, how are you?"));
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
