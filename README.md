# Headless Wifi

This app creates a Wi-Fi hotspot and hosts a web portal where users can enter the credentials of the Wi-Fi network they want the device to connect to.

## Prerequisites

**IDE -- Either of these, your preference**
 - Android Studio Meerkat (2024.3.1) or latest
 - VSCode
 - Cursor

**Flutter**
- 3.29.3 on stable channel or latest

**Dart**
- 3.7.2

**Pre-Built APK**

I have gone ahead building the apk. You can install and test the app.


**Secrets and Keystore**

This project includes `*.jks`, `key.properties` in the repository since this is just an exercise.

**Running the App**

Run the following command to run the app

```sh
    # install dependencies
    flutter pub get

    # run the app
    flutter run
```

**Challenges**

Starting from Android 8.0 (API level 26), the ability to programmatically configure and enable a hotspot using `setWifiApEnabled` has been completely removed for security reasons. Attempting to access this method, even via reflection, results in a `NoSuchMethodException`. Only apps that are signed with the vendor/manufacturer's key, system app, can access such method.

The only supported way to start a hotspot is through `startLocalOnlyHotspot`, which creates a hotspot with a randomly generated SSID and password. These credentials cannot be customized. Once the hotspot is active, the app can display the connection details along with instructions for another device to submit the desired Wi-Fi credentials.

Connecting the app to a new Wi-Fi network for the first time also requires user approval due to Android's current security model. On devices like the Samsung A52 running Android 13, the user is prompted to allow the connection once. After granting permission, subsequent connections to the same network do not require further user interaction. 


**How the app works**

When the app launches, it creates a Wi-Fi hotspot with a randomly generated SSID and password. Once the hotspot is active, the app starts a local web portal and displays instructions on how to connect. These instructions include the hotspot’s SSID, password, and the URL of the local web portal.

After connecting to the hotspot, the tester must open the web portal in a browser and enter the SSID and password of the target Wi-Fi network. If the target network is hidden, the tester must also check the corresponding option. Upon successful authentication, the app prompts the user to grant one-time permission to connect to the network. Once granted, future connections to the same network will not require user interaction.

If the app is swiped away, the service restarts automatically, reconnects to the last known Wi-Fi network, and continues running in the background. It also updates the foreground notification with the current hotspot’s SSID and password. Since the notification text may be truncated, the user should tap the notification to reopen the app and view the full instructions.

**Appreciation**
Thanks to online resources such as StackOverflow, GitHub Issues, and AI -- ChatGPT, ClaudeAI. Having my queries answered blazing fast is one of the greatest feeling of development.
