footer: Fluttercon 2024 | @OrestesGaolin | roszkowski.dev
background-color: #171A42
build-lists: true
slide-transition: fadeThroughColor(#171A42, 1.0)
autoscale: true

## Native Background Processing with Flutter

### Dominik Roszkowski

---

<!-- [.column] -->

<!-- ![inline](img/book.png) -->

<!-- [.column] -->

[.footer: false]

![original](./img/background-about-me.jpg)

---

# What kind of apps do you build?

![inline](./img/slido_july_24.png)

👉 Slido.com with **#1924827**

---

# What does it take to build a mobile app?

UI/UX, state management, a11y, deeplinks, notifications, **offline support**, CI/CD, in-app purchases, navigation, force upgrade, l10n, events, modularization, code push, OS permissions, testing, crashes, fragmentation, dependencies, migrations, stores, feature flags, widgets, tracking, attribution...

and?

---

# Background Processing

---

![](img/droidcon2023.png)

# My take on background processing

![inline 100%](img/logo.png)

---

# Agenda

- Use cases for background processing
- Ways to achieve it in Flutter apps
- Dart or...

---

[.build-lists: false]

# Agenda

- **Use cases for background processing**
- Ways to achieve it in Flutter apps
- Dart or...?

---

# Use-case #1

Offline-first app:

You should be able to
synchronize it out-of-lifecycle

---

![autoplay 50%](img/android.mp4)

---

# Use-case #2

Need to fetch or upload for
longer than 10-20 seconds?

You should to be able
to run it outside of the main process

---

![autoplay 50%](img/android-1.mp4)

---

# Use-case #3

Refreshing app data

When content becomes
available on the backend

---

# Use-case #4

![](img/tesla.jpeg)

Using BLE accessory

Waking up the app when in range

---

"In Flutter you can just use isolates"

~ first answer in Google

🤔

---

# 🤔

---

# Agenda

[.build-lists: false]

- Use cases for background processing
- **Ways to achieve it in Flutter apps**
- Dart or...?

---

# Isolates

- they require Flutter engine to run
- can be invoked from main engine
- or new engine can be spun from native code

---

# Isolates: `compute()` with graphql

multiple responses decoded without jank

[.code-highlight: 4]

```dart
Future<Map<String, dynamic>?> isolateHttpResponseDecoder(
  http.Response httpResponse,
) async =>
    await compute(jsonDecode, httpResponse.body)
        as Map<String, dynamic>?;
```

---

## Invoking isolates from native platform

- when push notification received
- when foreground service/live activity is running
- when user taps on widget

---

[.text: alignment(left), size(0.8)]

## Invoking isolates from native platform

1. Create _main_ method channel and register it on both sides
2. Start foreground service (FS) via _main_ method channel
   1. Within the FS create new Flutter engine
   2. New Flutter engine entry-point is a top-level function
   3. Create _service_ method channel for the new engine
   4. Communicate between FS and top-level function
3. Stop the service

---

[.hide-footer]

![autoplay inline](img/android_1.mp4)

---

[.hide-footer]

![autoplay inline](img/android_2.mp4)

---

[.hide-footer]

![autoplay inline](img/android_3.mp4)

---

[.hide-footer]

![autoplay inline](img/android_4.mp4)

---

[.hide-footer]

![autoplay inline](img/android_5.mp4)

---

[.column]

![inline](./img/razvan.png)

[.column]

![inline](./img/leancode.png)

---

[.build-lists: false]

[.text: alignment(left)]

Some production-ready plugins:

- `flutter_background_fetch` by Transistor Software
- `workmanager` by fluttercommunity
- `flutter_background_service` by ekasetiawans
- `flutter_downloader` / `background_downloader` / `flutter_uploader`

---

[.build-lists: false]

# Agenda

- Use cases for background processing
- Ways to achieve it in Flutter apps
- **Dart or native code?**

---

## Hot take 🔥

## Maybe write your code natively?

---

## What options do we have?

[.text: alignment(left)]

[.column]

## Android

- Foreground Service
- Background Work: WorkManager/Worker/JobScheduler...
- User-initiated data transfers

[.column]

## iOS

# 🦗

---

## Running a Foreground Service

At Visible we decided it's the most reliable way for our main use case.

With the scale of BLE integration, SQLite database, notifications, widgets we decided to just maintain it in Kotlin/Swift.

---

All right - Android is obvious - but what about iOS?

---

# iOS makes background processing tricky

[.text: text-scale(0.8)]

|                     | Short Task                           | Long Task                      |
| ------------------- | ------------------------------------ | ------------------------------ |
| **BGTaskScheduler** | `BGAppRefreshTask`                   | `BGProcessingTask`             |
| **Hacky way** 😉    | Push Notification Background Updates | State Restoration App Relaunch |

---

[.code-highlight: all]
[.code-highlight: 1-6]
[.code-highlight: 7-10]
[.code-highlight: 11-19]

```swift
// didFinishLaunchingWithOptions
BGTaskScheduler.shared.register(
  forTaskWithIdentifier: "fluttercon.hello.db_cleaning", using: nil //Info.plist
  ) { task in
    self.handleDatabaseCleaning(task: task as! BGProcessingTask)
}

func applicationDidEnterBackground(_ application: UIApplication) {
    scheduleDatabaseCleaningIfNeeded()
}

func scheduleDatabaseCleaningIfNeeded() {
    let request = BGProcessingTaskRequest(identifier: "fluttercon.hello.db_cleaning")
    request.requiresNetworkConnectivity = false
    request.requiresExternalPower = true

    try BGTaskScheduler.shared.submit(request)
    // run Flutter engine if needed
}
```

---

Trick we use at Visible (iOS)

# [fit] State Preservation and Restoration

# [fit] Bluetooth State Restoration app relaunch

---

# Final remarks

- Do you see a use-case for background processing in your app?
- Issues with bg processing and new Android policy
- Building with Flutter ➡️ understand all platforms

---

# Learn more

[.text: alignment(left)]

- Lucas Goldner: _Saving data before the app getting killed! Easy state restoration with Flutter_
  - Wednesday 3:45 PM / Widget Way
- Domen Lanišnik: _Guide to Foreground Services_
  - Friday 11:55 AM / Things
- Links at [**roszkowski.dev/background**](https://roszkowski.dev/background)

---

This was "Native Background Processing with Flutter"

Reference materials and slides
[**roszkowski.dev/background**](https://roszkowski.dev/background)

![inline](img/tally_july_2024.png)

Find me online using: **@OrestesGaolin** / **Dominik Roszkowski**

<!-- - https://www.droidcon.com/2021/11/22/executing-dart-code-in-background-with-flutter/?unapproved=23281&moderation-hash=a01970f2cffdb436b12dccdacffc6867#comment-23281
- https://www.dhiwise.com/post/work-wonders-with-flutter-workmanager-ultimate-guide
- https://www.xavor.com/blog/the-significance-of-background-tasks-in-flutter-app-development/
- https://leancode.co/blog/background-services-in-flutter-add-to-app
- https://www.droidcon.com/2020/10/10/workmanager-clever-delegate-for-deferrable-background-tasks/
- https://developer.apple.com/videos/play/wwdc2019/707/
- https://developer.android.com/about/versions/14/changes/fgs-types-required
- https://www.droidcon.com/2023/11/15/a-guide-to-using-foreground-services-and-background-work-in-android-14/
- https://developer.android.com/about/versions/14/changes/user-initiated-data-transfers
- https://docs.flutter.dev/packages-and-plugins/background-processes -->

```

```
