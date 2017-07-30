package com.jim.pdudebugger;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.telephony.SmsMessage;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;


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
            int pduNum = sms.length;
            appendLog("New message received, " + pduNum + " pdu(s) total", context);
            for (int i = 0; i < pduNum; ++i) {
                appendLog("Working on pdu number " + (i+1) + " out of " + pduNum, context);
    ////
    //                String format = intentExtras.getString("format");
    //                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);
    //// if we needed message class or actual android internal text/sender/... — we would use SmsMessage android class^^^

                try {
                    String pduHex = byteArrayToHexString((byte[]) sms[i]);
                    String now = MainActivity.getCurrentTimestamp();
                    saveTextFromReceiver(now + ";" + pduHex, context);
                } catch(Exception e) {
                    appendLog("Saving text failed to load with exception: " + e.getMessage(), context);

                }

                try {
                    String pduHex = byteArrayToHexString((byte[]) sms[i]);
                    MainActivity inst = MainActivity.instance();
                    inst.updateInbox(pduHex);
                } catch(Exception e) {
                    appendLog("MainActivity failed to load with exception: " + e.getMessage(), context);

                }
            }
        }
    }

    private void saveTextFromReceiver(final String text, Context context) {
        String key;
        SharedPreferences storage = context.getSharedPreferences("pdus", Context.MODE_PRIVATE);
        int keyTrial = 0;
        do {
            ++keyTrial;
            key = Long.toString(System.currentTimeMillis());
//            //this shit doesn't work. fuck java.
//            appendLog("Current keyset in sharedPrefs: " +
//                    storage.getAll().keySet().toArray(new String[storage.getAll().size()]), context);
            appendLog("Trying key (attempt number " + keyTrial + "): " + key, context);
        } while (storage.contains(key));
        SharedPreferences.Editor ed = storage.edit();
        ed.putString(key, text);
        ed.apply();
        appendLog("Data saved to shared prefs: {" +
                "key: \"" + key + "\", " +
                "value:\"" + text + "\"}",
                context);
    }

    // Too lazy to make a singleton for logging
    // https://stackoverflow.com/a/6209739/5823260
    //
    private void appendLog(String text, Context context)
    {
        if(!isExternalStorageWritable()){
            return;
        }
        File logFile = getLogFile(context);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(MainActivity.getCurrentTimestamp());
            buf.append(" > ");
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static File getLogFile(Context context) {
        return new File(context.getExternalFilesDir(null), "logfile.log");
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}