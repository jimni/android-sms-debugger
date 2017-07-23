# Android SMS debugger
An Android app that shows raw PDU bytes (hex dump) and their meaning for every incoming SMS message. 

An [app-debug.apk](https://github.com/jimni/android-sms-debugger/blob/master/app-debug.apk) is prebuilt for your convenience.

Stores every incoming SMS PDU that comes through BroadcastReceiver. When the app is open, [smslib](https://github.com/tdelenikas/smslib/) is utilised to parse every PDU in storage as well as any new-coming one. The `smslib` is included as a jar.

Storage is a private SharedPreferences instance, thus deleting the app empties the storage.

This app is unable to parse messages that were received prior to it being granted permission to read SMS.

The theoretical minimum Android version is 4.0, however, the app was only tested on 6+.

The PDUs are decoded according to [3GPP TS 23.040](https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=747) and I honestly have no idea how this will work in a non-GSM environment (e. g. CDMA).

This app systematically crashes by design and has no tests whatsoever :)

Your questions, requests, etc. are more than welcome in GitHub issues.
