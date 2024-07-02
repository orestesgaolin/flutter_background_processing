As mobile, and specifically Flutter developers we face a lot of various challenges and topic every day. From building UIs, animations, through in-app purchases, navigation, system permissions, to feature flags and dealing with the app stores.

And as you grow as a mobile developer, you eventually reach the point, where you have to carry out some process when the app is not running. And as you probably see already, this slide misses one particular topic.

Whether it's backend driven data update, or on-device cleanup, you need to step into the background processing. As Flutter developers we can coast for several years without ever writing a single line of native code, but in this particular case understanding how the underlying native implementation works is crucial. I'd argue that learning how to do background processing can have bigger impact on your development than any of the topics from the previous slide.

My journey with background processing spans from the times when I was building backend systems with .NET. Having workers or job schedulers was just a part of day to day backlog. Then when I worked with Xamarin at LeanCode we also have done a bit of background processing on mobile, but it used to be a bit detached from the native platform. Only when I joined Visible and had to migrate our app to offline-first paradigm, I realized how naive my approach has been for all these years.

This may be actually a good thing with Flutter that in certain way it constrains developers to consider mostly the active section of the app lifecycle, the one when app is in the foreground and user is actively interacting with it. For most of the users in the wild the app will just not operate when not on the screen - whether it's due to the power saving limitations, or people automatically swiping away the app once they're done with whatever was happening in the app.

Our app - Visible - is a health tracker for people with chronic illnesses. It's like a fitbit for ill. For instance we have a wearable armband integrated via Bluetooth.

At Visible we had to embrace background processing with Flutter as primary framework. It took some time but currently we have a quite significant portion of the app written natively, mostly all the BLE integrations. All of that needs to work when app is not active, for instance it needs to react to Bluetooth armband coming back in range. Over the last 2 years we also made some serious mistakes when trying to solve it, so today I'd like to share with you some of the lessons I learned on the way.

Let's see what's our agenda for today. First let's just explore couple of use cases. Then we'll get into the interesting details, and at the end we may discuss whether using Dart for background processing is reasonable option in the long run.

# Use-case #1

Our first use case, that at Visible we had to add aside from Bluetooth integration is out-of-lifecycle data synchronization. I guess that's quite obvious in offline-first app where we keep most of the user data in the local database. It may happen that they used the app in poor network conditions, so overnight we want to sync the data up and down to the backend.

Wondering if anybody in the room had to solve something similar?

In our case we have a set of background processes written in Dart that are run at a specified cadence, and here you can see an example of it in action.

# Use-case #2

Another quite popular use-case is downloading or uploading some significant amount of data to the cloud. It makes me sad sometimes how fragmented and unknown this is among Flutter developers, but I'd argue that in the many cases going with native, out-of-lifecycle upload is the way to go.

Here you can see a simple example of OneDrive synchronizing new photos using a foreground service on Android. iOS is a bit trickier but in some circumstances it's possible to achieve similar experience there as well. If big apps do it, why wouldn't you as well?

# Use-case #3

Another example, that also works well with offline-first apps is refreshing the app content on backend trigger e.g. by receiving a silent push notification. This in turn triggers a short sync process to fetch the data to be shown to the user when they come back to the app.

# Use-case #4

And finally the use case that's similar to what we do at Visible, but this can be applied to any BLE accessory. For instance in case of Tesla mobile app, I think it gets woken up when the device BLE radio is detected in range to unlock the door. In case of Visible we get these triggers when user turns on the armband.

# Isolates

All right, having all of these use cases in mind, some of which you might have implemented already, what are our options?

When you look for background processing keyword online, the most common answer you'll get is "isolates".

And in fact in some sense to do a background work in Flutter you have to use isolates. In reality this is just part of the answer and to do it correctly you may need to look deeper.

First of all isolates work within Dart VM, so in case of a mobile app it means you probably need to run Flutter engine. They can be invoked from your main engine and let you run Dart code asynchronously without causing UI jank. And that's the main way we leverage isolates at Visible. Sometimes there may be several of them running at the same time but they're mostly imperceptible for the users.

For instance our graphql client uses `compute` function which spins up a new isolate to decode string response to json. With big responses it would cause a significant jank if not for the isolate doing the job.

# Isolates from native

However, for me the more interesting part is running the Dart code from a background job triggered natively. There may be several reasons for that like push notification callback, user action on foreground service or iOS live activity, or perhaps user tapping on a home screen widget.

In some way all of these leverage similar mechanism of invoking a new flutter engine from the Flutter Engine cache pool. For a simple one-off case that I'm going to show today you can do it in 3 major steps:

First create a method channel to receive information on the native side.

Then start a foreground service (or any other background job) that will launch a new Flutter engine. This new engine needs to point to a top-level function marked with VM entry-point. The function that you specify will be called as if it was you main function of the app.

In that function you can freely create new method channel that will communicate back and forth with the service.

Once done, you can just stop the service or cancel the job.

# Videos

Let's see how it can be done with Android foreground service.

Explain video...

# Plugins

There are some great community plugins doing almost exactly this. Whether you just need to run a quick background process or spin up a foreground service that will use Dart, you can leverage these. However, I think it's quite important to know how it all works under the hood, and in fact it's not that mysterious.
