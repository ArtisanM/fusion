package com.gtbluesky.fusion.container

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.gtbluesky.fusion.Fusion
import com.gtbluesky.fusion.constant.FusionConstant
import com.gtbluesky.fusion.handler.FusionMessengerHandler
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterActivityLaunchConfigs.BackgroundMode
import io.flutter.embedding.android.FlutterImageView
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.android.RenderMode
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.platform.PlatformPlugin
import java.io.Serializable
import java.util.UUID

open class FusionActivity : FlutterActivity(), FusionContainer {
    private val history = mutableListOf<Map<String, Any?>>()
    private var flutterView: FlutterView? = null
    private var isAttached = false
    private var uniqueId = "container_${UUID.randomUUID()}"
    private var engineBinding = Fusion.engineBinding
    private var platformPlugin: PlatformPlugin? = null

    override fun uniqueId() = uniqueId

    override fun history() = history

    override fun isTransparent() = backgroundMode.name == BackgroundMode.transparent.name

    override fun isAttached() = isAttached

    private fun attachToContainer() {
        if (isAttached) return
        isAttached = true
        val engine = engineBinding?.engine ?: return
        // Attach plugins to the activity.
        engine.activityControlSurface.attachToActivity(
            exclusiveAppComponent,
            lifecycle
        )
        // Attach rendering pipeline.
        flutterView?.attachToFlutterEngine(engine)
        // 配置PlatformChannel和CustomChannel是因为其和Activity相关联
        // 而三方插件和Activity无关，一个Engine配置一次即可
        // Configure platform channel
        if (platformPlugin == null) {
            val platformChannel = engineBinding?.engine?.platformChannel ?: return
            platformPlugin = PlatformPlugin(this, platformChannel, this)
        }
        // Configure custom channel
        (this as? FusionMessengerHandler)?.configureFlutterChannel(engine.dartExecutor.binaryMessenger)
    }

    override fun detachFromContainer() {
        if (!isAttached) return
        isAttached = false
        val engine = engineBinding?.engine ?: return
        // Plugins are no longer attached to the activity.
        engine.activityControlSurface.detachFromActivity()
        // Detach rendering pipeline.
        flutterView?.detachFromFlutterEngine()
        // Fixed since 3.0 stable
        flutterView?.forEach {
            if (it is FlutterImageView) {
                flutterView?.removeView(it)
            }
        }
        // Release platform channel
        platformPlugin?.destroy()
        platformPlugin = null
        // Release custom channel
        (this as? FusionMessengerHandler)?.releaseFlutterChannel()
    }

    private fun onContainerCreate() {
        if (!isTransparent()) {
            val backgroundColor =
                intent.getIntExtra(FusionConstant.EXTRA_BACKGROUND_COLOR, Color.WHITE)
            window.setBackgroundDrawable(ColorDrawable(backgroundColor))
        }
        if (FusionStackManager.isEmpty()) {
            engineBinding?.engine?.lifecycleChannel?.appIsResumed()
        }
        FusionStackManager.add(this)
    }

    private fun onContainerVisible() {
        val top = FusionStackManager.getTopContainer()
        if (top != this) {
            top?.detachFromContainer()
        }
        FusionStackManager.add(this)
        engineBinding?.switchTop(uniqueId) {
            this.attachToContainer()
            this.updateSystemOverlayStyle()
        }
        engineBinding?.notifyPageVisible(uniqueId)
    }

    private fun updateSystemOverlayStyle() {
        engineBinding?.checkStyle { systemChromeStyle ->
            try {
                val field = platformPlugin?.javaClass?.getDeclaredField("currentTheme")
                field?.isAccessible = true
                field?.set(platformPlugin, systemChromeStyle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            platformPlugin?.updateSystemUiOverlays()
        }
    }

    private fun onContainerInvisible() {
        engineBinding?.notifyPageInvisible(uniqueId)
    }

    private fun onContainerDestroy() {
        detachFromContainer()
        FusionStackManager.remove(this)
        engineBinding?.destroy(uniqueId)
    }

    private fun skipUnexpectedLifecycle(): Boolean {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
            return false
        }
        val top = FusionStackManager.getTopActivityContainer()
        val result =
            top != null && top != activity && top.isTransparent() && !(top as Activity).isFinishing
        if (result) {
            Log.w("Fusion", "Skip the unexpected activity lifecycle on Android Q.")
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Detach
        val top = FusionStackManager.getTopContainer()
        if (top != this) {
            top?.detachFromContainer()
        }
        super.onCreate(savedInstanceState)
        val routeName =
            intent.getStringExtra(FusionConstant.ROUTE_NAME) ?: FusionConstant.INITIAL_ROUTE
        val routeArgs = intent.getSerializableExtra(FusionConstant.ROUTE_ARGS) as? Map<String, Any>
        savedInstanceState?.getString(FusionConstant.FUSION_RESTORATION_UNIQUE_ID_KEY)?.let {
            uniqueId = it
        }
        val restoredHistory =
            savedInstanceState?.getSerializable(FusionConstant.FUSION_RESTORATION_HISTORY_KEY) as? List<Map<String, Any?>>
        if (restoredHistory.isNullOrEmpty()) {
            engineBinding?.create(uniqueId, routeName, routeArgs)
        } else {
            engineBinding?.restore(uniqueId, restoredHistory)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
        flutterView = findFlutterView(window.decorView)
        flutterView?.detachFromFlutterEngine()
        onContainerCreate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            FusionConstant.FUSION_RESTORATION_UNIQUE_ID_KEY, uniqueId
        )
        outState.putSerializable(
            FusionConstant.FUSION_RESTORATION_HISTORY_KEY, history as? Serializable
        )
    }

    override fun onResume() {
        super.onResume()
        if (skipUnexpectedLifecycle()) {
            return
        }
        onContainerVisible()
    }

    override fun onPause() {
        super.onPause()
        if (skipUnexpectedLifecycle()) {
            return
        }
        onContainerInvisible()
    }

    override fun onDestroy() {
        super.onDestroy()
        onContainerDestroy()
        if (FusionStackManager.isEmpty()) {
            engineBinding?.engine?.lifecycleChannel?.appIsPaused()
        }
        engineBinding = null
    }

    override fun setTaskDescription(taskDescription: ActivityManager.TaskDescription?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (taskDescription?.label.isNullOrEmpty()) {
                return
            }
        }
        super.setTaskDescription(taskDescription)
    }

    override fun onBackPressed() {
        engineBinding?.maybePop(null)
    }

    override fun getRenderMode() = RenderMode.texture

    override fun shouldAttachEngineToActivity() = false

    override fun shouldDispatchAppLifecycleState() = false

    override fun attachToEngineAutomatically() = false

    override fun detachFromFlutterEngine() {}

    override fun provideFlutterEngine(context: Context) = engineBinding?.engine

    override fun providePlatformPlugin(
        activity: Activity?, flutterEngine: FlutterEngine
    ): PlatformPlugin? = null
}