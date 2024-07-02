package com.example.isolates

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity(), ActivityAware {
    companion object {
        private const val LOG_TAG = "MainActivity"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        // Creates a MethodChannel as soon as the FlutterEngine is attached to
        // the Activity, and registers a MethodCallHandler. The Method.setMethodCallHandler
        // is responsible to register a MethodCallHandler to handle the incoming calls.

        // The call parameter of MethodCallHandler has information about the incoming call,
        // like method name, and arguments. The result parameter of MethodCallHandler is
        // responsible to send the results of the call.
        MethodChannel(flutterEngine.dartExecutor, "service.main")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "start" -> {
                        startService()
                        result.success(null)
                    }

                    "stop" -> {
                        stopService()
                        result.success(null)
                    }

                    else -> result.notImplemented()
                }
            }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private var computationService: ComputationForegroundService? = null
    private var serviceConnection: ServiceConnection? = null

    private fun startService() {
        Log.d(LOG_TAG, "Starting computation service")
        FlutterUtils.initialize(context);

        val serviceIntent = Intent(context, ComputationForegroundService::class.java)
        val message = "Service started";
        ComputationServiceNotification.createNotification(context, serviceIntent, message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        serviceConnection = createServiceConnection()
        context.bindService(serviceIntent, serviceConnection!!, Context.BIND_AUTO_CREATE)

        Log.d(LOG_TAG, "Computation service started")
    }

    private fun stopService() {
        Log.d(LOG_TAG, "Stopping computation service")

        computationService?.stopService()
        computationService = null
        serviceConnection?.let {
            context.unbindService(it)
            serviceConnection = null
        }

        Log.d(LOG_TAG, "Computation service stopped")
    }

    private fun createServiceConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                computationService =
                    (service as ComputationForegroundService.LocalBinder).getInstance()
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                computationService = null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "Destroying MainActivity")
        computationService?.let {
            serviceConnection?.let { connection ->
                context.unbindService(connection)
                serviceConnection = null
            }
            computationService = null
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {

    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }

    override fun onDetachedFromActivity() {

    }
}
