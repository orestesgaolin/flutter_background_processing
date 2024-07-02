// based on https://github.com/leancodepl/flutter-add2app-background-services/blob/main/android/app/src/main/java/co/leancode/add2appbackgroundservice/computation/ComputationForegroundService.kt
package com.example.isolates

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class ComputationForegroundService : Service() {
    companion object {
        private const val LOG_TAG = "ComputationService"
        private const val SERVICE_ID = 1
    }

    private val binder = LocalBinder()
    private var engine: FlutterEngine? = null

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "Starting service")

        if (engine == null) {
            Log.d(LOG_TAG, "Initializing Flutter engine")
            ComputationServiceNotification.createNotificationChannel(applicationContext)
            val notification =
                ComputationServiceNotification.createNotification(applicationContext, intent!!, "Service started")
            startForeground(SERVICE_ID, notification)
            startDartService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun stopService() {
        Log.d(LOG_TAG, "Stopping service")

        stopForeground(true)
        stopSelf()
        FlutterUtils.destroyEngine(AppFlutterEngine.computationService)
        engine = null

        Log.d(LOG_TAG, "Service stopped")
    }

    private fun startDartService() {
        engine = FlutterUtils.createOrGetEngine(this, AppFlutterEngine.computationService)
        Log.d(LOG_TAG, "Started Dart service with engine ${engine!!}")

        engine!!.let {
            MethodChannel(it.dartExecutor, "service.background")
                .setMethodCallHandler { call, result ->
                    when (call.method) {
                        "update" -> {
                            val message: String? = call.argument<String>("message")
                            message?.let {
                                ComputationServiceNotification.updateNotification(applicationContext, it)
                            }
                            result.success(null)
                        }
                        else -> result.notImplemented()
                    }
                }

        }
    }

    inner class LocalBinder : Binder() {
        fun getInstance(): ComputationForegroundService = this@ComputationForegroundService
    }
}