import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:headlesswifi/headlesswifi.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Headless Wifi',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a purple toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MyHomePage(title: 'Headless Wifi'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Map<String, dynamic> credentials = {};
  bool isConnected = false;
  bool hasInternet = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) async {
      Map<Permission, PermissionStatus> statuses =
          await [
            Permission.location,
            Permission.nearbyWifiDevices,
            Permission.ignoreBatteryOptimizations,
            Permission.notification,
          ].request();

      if (statuses[Permission.location]!.isGranted &&
          statuses[Permission.nearbyWifiDevices]!.isGranted &&
          statuses[Permission.ignoreBatteryOptimizations]!.isGranted) {
        /// Listens for connection event from native code via MethodChannel
        Headlesswifi().listenForWifiEvent((isConnected, hasInternet) {
          log('wifi event: $isConnected, $hasInternet');
          setState(() {
            this.isConnected = isConnected;
            this.hasInternet = hasInternet;
          });
          if (isConnected) {
            ScaffoldMessenger.of(
              context,
            ).showSnackBar(SnackBar(content: Text('✅ Wifi Connected')));
          }
        });
        final res = await Headlesswifi().startWifi();
        log('res: $res');
        if (res != null && res['ssid'] != null && res['password'] != null) {
          setState(() {
            credentials = res;
          });
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        // TRY THIS: Try changing the color here to a specific color (to
        // Colors.amber, perhaps?) and trigger a hot reload to see the AppBar
        // change color while the other colors stay the same.
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Center(
        // Center is a layout widget. It takes a single child and positions it
        // in the middle of the parent.
        child: Builder(
          builder: (context) {
            if (credentials.isEmpty) {
              return Text(
                'Please wait',
                style: Theme.of(context).textTheme.headlineMedium,
              );
            }
            return Column(
              children: [
                Text(
                  'To connect to the wifi, Open your wifi settings and connect to the following network:',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                SizedBox(height: 10),
                Text(
                  'SSID: ${credentials['ssid']}',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                SizedBox(height: 10),
                Text(
                  'Password: ${credentials['password']}',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                SizedBox(height: 10),
                Text(
                  'once connected, open browser and open the following url:',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                SizedBox(height: 10),
                Text(
                  'http://${credentials['ip']}:8080',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                SizedBox(height: 10),
                if (isConnected)
                  Text(
                    '✅ Wifi Connected',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
              ],
            );
          },
        ),
      ),
    );
  }
}
