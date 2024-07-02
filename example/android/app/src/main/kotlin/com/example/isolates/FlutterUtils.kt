// https://github.com/leancodepl/flutter-add2app-background-services/blob/main/android/app/src/main/java/co/leancode/add2appbackgroundservice/FlutterUtils.kt
package com.example.isolates

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.android.TransparencyMode
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor

enum class AppFlutterEngine(val id: String, val entrypoint: String) {
    computationService("COMPUTATION_SERVICE", "backgroundServiceMain"),
}

class FlutterUtils {
    companion object {
        private const val LOG_TAG = "FlutterUtils"

        private lateinit var engineGroup: FlutterEngineGroup

        fun initialize(context: Context) {
            engineGroup = FlutterEngineGroup(context)
        }

        fun createOrGetEngine(
            context: Context,
            engine: AppFlutterEngine,
        ): FlutterEngine {
            var cachedEngine = FlutterEngineCache.getInstance().get(engine.id)
            if (cachedEngine == null) {
                Log.d(LOG_TAG, "Engine ${engine.id} does not exist, creating engine")

                val entrypoint = DartExecutor.DartEntrypoint("lib/main.dart", engine.entrypoint)
                cachedEngine = engineGroup.createAndRunEngine(context, entrypoint)
                FlutterEngineCache.getInstance().put(engine.id, cachedEngine)
            }
            return cachedEngine!!
        }

        fun getEngine(engine: AppFlutterEngine): FlutterEngine? {
            return FlutterEngineCache.getInstance().get(engine.id)
        }

        fun destroyEngine(engine: AppFlutterEngine) {
            val cachedEngine = getEngine(engine)
            cachedEngine?.let {
                FlutterEngineCache.getInstance().remove(engine.id)
                it.destroy()
                Log.d(LOG_TAG, "Destroyed engine ${engine.id}")
            }
        }

    }
}