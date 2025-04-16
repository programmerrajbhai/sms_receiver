package com.example.smsread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.content.SharedPreferences;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            StringBuilder fullMessage = new StringBuilder();
            String sender = null;
            long timestampMillis = 0;

            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    if (sender == null) {
                        sender = sms.getDisplayOriginatingAddress();
                        timestampMillis = sms.getTimestampMillis();
                    }
                    fullMessage.append(sms.getMessageBody());
                }

                String time = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(new Date(timestampMillis));

                String fullSms = "Sender: " + sender + "\nTime: " + time + "\nMessage:\n" + fullMessage;

                // Log & Save
                Log.d(TAG, fullSms);
                SharedPreferences sharedPreferences = context.getSharedPreferences("SMSData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("smsFull", fullSms);
                editor.apply();
            }
        }
    }
}
