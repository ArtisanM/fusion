package com.gtbluesky.fusion

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Looper
import androidx.annotation.UiThread
import com.gtbluesky.fusion.constant.FusionConstant
import com.gtbluesky.fusion.engine.FusionEngineBinding
import com.gtbluesky.fusion.container.FusionStackManager
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.embedding.engine.dart.DartExecutor

object Fusion {
    var engineGroup: FlutterEngineGroup? = null
        private set
    var defaultEngine: FlutterEngine? = null
        private set
    internal var engineBinding: FusionEngineBinding? = null
        private set
    internal lateinit var delegate: FusionRouteDelegate
        private set
    private lateinit var context: Application
    private var lifecycleCallback: Application.ActivityLifecycleCallbacks? = null
    private var isRunning = false

    @UiThread
    fun install(context: Application, delegate: FusionRouteDelegate) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(
                "Methods marked with @UiThread must be executed on the main thread. Current thread: ${Thread.currentThread().name}"
            )
        }
        if (isRunning) {
            return
        }
        isRunning = true
        this.context = context
        this.delegate = delegate
        engineGroup = FlutterEngineGroup(context)
        defaultEngine = createAndRunEngine()
        engineBinding = FusionEngineBinding(defaultEngine)
        engineBinding?.attach()
        lifecycleCallback = FusionLifecycleCallbacks()
        context.registerActivityLifecycleCallbacks(lifecycleCallback)
    }

    @UiThread
    fun uninstall() {
        context.unregisterActivityLifecycleCallbacks(lifecycleCallback)
        lifecycleCallback = null
        engineBinding?.detach()
        engineBinding = null
        engineGroup = null
        defaultEngine = null
        isRunning = false
    }

    @UiThread
    private fun createAndRunEngine(initialRoute: String = FusionConstant.INITIAL_ROUTE): FlutterEngine? {
        /// GeneratedPluginRegister里会通过反射调用GeneratedPluginRegistrant来注册插件
        return engineGroup?.createAndRunEngine(
            context,
            DartExecutor.DartEntrypoint.createDefault(),
            initialRoute
        )
    }
}

internal class FusionLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    private var visibleActivityCount = 0
    private var isActivityChangingConfigurations = false
    private var isLaunching = true

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        // 首次启动不触发进入前台回调，与iOS保持一致
        ++visibleActivityCount
        if (isLaunching) {
            isLaunching = false
            return
        }
        if (visibleActivityCount == 1 && !isActivityChangingConfigurations) {
            FusionStackManager.notifyEnterForeground()
        }
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--visibleActivityCount == 0 && !isActivityChangingConfigurations) {
            FusionStackManager.notifyEnterBackground()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

}