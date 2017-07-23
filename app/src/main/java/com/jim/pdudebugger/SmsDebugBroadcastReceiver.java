package com.jim.pdudebugger;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.telephony.SmsMessage;
import android.content.SharedPreferences;


public class SmsDebugBroadcastReceiver extends BroadcastReceiver {

    public static String byteArrayToHexString(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

            if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            for (int i = 0; i < sms.length; ++i) {
////
//                String format = intentExtras.getString("format");
//                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);
//// if we needed message class or actual android internal text/sender/... — we would use SmsMessage android class^^^

                String pduHex = byteArrayToHexString((byte[]) sms[i]);
                String now = MainActivity.getCurrentTimestamp();
                saveTextFromReceiver(now + ";" + pduHex, context);

                MainActivity inst = MainActivity.instance();
                inst.updateInbox(pduHex);
            }
        }
    }

    private void saveTextFromReceiver(final String text, Context context) {
        String key;
        SharedPreferences storage = context.getSharedPreferences("pdus", Context.MODE_PRIVATE);
        do {
            key = Long.toString(System.currentTimeMillis());
        } while (storage.contains(key));
        SharedPreferences.Editor ed = storage.edit();
        ed.putString(key, text);
        ed.apply();
    }
}