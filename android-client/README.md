Sofia4Cities Android Client API
============================

## Android API documentation

OnesatPlatformAndroidClient folder contains a full-working Android Studio project designed to showcase inherent IoT capabilities within onesait Platform. 
There is also an APK file ready to be installed on any Android device (KitKat version or higher).

This is a REST Client interface API allowing Android devices to perform these operations on Sofia4Cities IoTBroker: JOIN, INSERT & LEAVE.
The enclosed App features a GPS and Accelerometer logger to Sofia4Cities whenever the user press the Start button located in the menu on top. The loggin ends when the Stop button is pressed or when the selected BLE beacon is detected nearby the device.

## APK Testing
In order to test the example application on your phone, it is needed to allow installation of apps from unknown sources. This setting is located under Settings->Security->Unknown sources, and MUST BE enabled to install the APK.

## Download
To add the REST Client interface api just add this lines to the build.gradle file in your application
```
repositories {
    mavenCentral()
    maven{ url "http://sofia2.org/nexus/content/repositories/releases/"}
}
dependencies {
	compile 'com.minsait.onesait.platform:onesaitplatform-android-client:1.0.0'

}
```

## Build instructions

The Android API is distributed now distributed as an Android library module. To build your own version fork this repository to a new Android module in Android Studio.

