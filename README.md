# Android SMS debugger
An Android app that shows raw pdu bytes and their meaning for every incoming SMS message.

Stores every incoming SMS PDU that comes through BroadcastReceiver. When the app is open, [smslib](https://github.com/tdelenikas/smslib/) is utilized to parse every PDU in storage as well as any newcoming one. The `smslib` is included as a jar.

Storage is a private SharedPreferences instance, thus deleting the app empties the storage.

This app is unable to parse messages that were receveid prior to it being granted permisson to read SMS.

Theoretical minimum android version is 4.0, however the app was only tested on 6.0+.