package com.jim.pdudebugger;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.ajwcc.pduUtils.gsm3040.*;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    SharedPreferences storage;
    ArrayList<String> smsPdusList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    private static MainActivity inst;

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = (ListView) findViewById(R.id.messages);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsPdusList);
        messages.setAdapter(arrayAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        }
        for (String savedPduData: loadSavedPdus()) {
            arrayAdapter.insert(renderSavedSms(savedPduData), 0);
        }
        if (SmsDebugBroadcastReceiver.isExternalStorageWritable()){
            String logPath = SmsDebugBroadcastReceiver.getLogFile(this.getApplicationContext()).toString();
            Toast.makeText(this, "Log is written to " + logPath, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "! Log is disabled !", Toast.LENGTH_LONG).show();
        }
    }

    protected static String getCurrentTimestamp(){
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return df.format(cal.getTime());
    }

    private String renderSavedSms(String pduData){

        String output = "";

        String[] data = pduData.split(";");
        String timestamp = data[0];
        String pduHex = data[1];

        output += "Received at: " + timestamp + "\n";
        output += inspectPdu(pduHex);
        return output;
    }

    private String inspectPdu(String pduHex){
        String output = "";

        PduParser parser = new PduParser();
        Pdu pduObject = parser.parsePdu(pduHex);
        output += "from: " + pduObject.getAddress() + "\n";
        output += "text: " + pduObject.getDecodedText() + "\n";
        output += pduObject.toString();
        return output;
    }

    public void updateInbox(final String hexPdu) {

        String now = getCurrentTimestamp();
        String smsMessageStr = "Received at: " + now + "\n";
        smsMessageStr += inspectPdu(hexPdu);

        arrayAdapter.insert(smsMessageStr, 0);
        arrayAdapter.notifyDataSetChanged();

    }

    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                for (String savedPduData: loadSavedPdus()) {
                    arrayAdapter.insert(renderSavedSms(savedPduData), 0);
                }
            } else {
                     Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
                    }

            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

        }


    private String[] loadSavedPdus(){
        storage = getSharedPreferences("pdus", MODE_PRIVATE);
        Map<String, ?> unsortedPdus = storage.getAll();
        Map<String, ?> sortedPdus = new TreeMap<String, Object>(unsortedPdus); //first in array => the oldest pdu
        return sortedPdus.values().toArray(new String[0]);
    }

}
