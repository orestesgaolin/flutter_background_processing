import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MainApp());
}

class MainApp extends StatefulWidget {
  const MainApp({super.key});

  @override
  State<MainApp> createState() => _MainAppState();
}

class _MainAppState extends State<MainApp> {
  /// Creates a [MethodChannel] with the specified name to invoke platform method.
  /// In order to communicate across platforms, the name of MethodChannel
  /// should be same on native and dart side.
  static MethodChannel mainMethodChannel = const MethodChannel('service.main');

  static Future<void> start() async {
    await mainMethodChannel.invokeMethod<void>('start');
  }

  static Future<void> stop() async {
    await mainMethodChannel.invokeMethod<void>('stop');
  }

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: Scaffold(
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            ElevatedButton(
              onPressed: start,
              child: Text('Start or connect'),
            ),
            ElevatedButton(
              onPressed: stop,
              child: Text('Stop'),
            ),
          ],
        ),
      ),
    );
  }
}

Future<void> updateInTheBackground(
    MethodChannel channel, String message) async {
  await channel.invokeMethod<void>(
    'update',
    {'message': message},
  );
}

@pragma('vm:entry-point')
void backgroundServiceMain() {
  const channel = MethodChannel('service.background');
  WidgetsFlutterBinding.ensureInitialized();

  print('Starting background service');

  const allTicks = 100;
  var remainingTicks = 100;

  Timer.periodic(const Duration(seconds: 1), (timer) {
    print('Updating service at tick: $remainingTicks');

    remainingTicks--;

    final progress = (allTicks - remainingTicks) / allTicks;

    updateInTheBackground(
      channel,
      'Progress $progress from background service',
    );

    if (remainingTicks == 0) {
      timer.cancel();
    }
  });
}
