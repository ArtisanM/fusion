package com.gtbluesky.fusion.container

import android.app.Activity
import com.gtbluesky.fusion.Fusion
import java.lang.ref.WeakReference

internal object FusionStackManager {
    val containerStack = mutableListOf<WeakReference<FusionContainer>>()

    fun isEmpty() = containerStack.isEmpty()

    fun add(container: FusionContainer) {
        remove(container)
        containerStack.add(WeakReference(container))
    }

    fun remove(container: FusionContainer) {
        containerStack.removeAll {
            it.get() == container || it.get() == null
        }
    }

    fun getTopContainer(): FusionContainer? {
        if (containerStack.isEmpty()) return null
        return containerStack.last().get()
    }

    fun getTopActivityContainer(): FusionContainer? {
        if (containerStack.isEmpty()) return null
        return containerStack.findLast { it.get() is Activity }?.get()
    }

    fun findContainer(uniqueId: String): FusionContainer? {
        if (uniqueId.isEmpty()) return null
        for (containerRef in containerStack) {
            if (containerRef.get()?.uniqueId() == uniqueId) {
                return containerRef.get()
            }
        }
        return null
    }

    fun closeContainer(container: FusionContainer) {
        if (container is Activity) {
            container.finish()
            // 透明容器则关闭退出动画
            if (container.isTransparent()) {
                container.overridePendingTransition(0, 0)
            }
        }
    }

    fun isAttached(): Boolean {
        containerStack.forEach {
            if (it.get()?.isAttached() == true) {
                return true
            }
        }
        return false
    }

    fun notifyEnterForeground() {
        if (!isEmpty()) {
            Fusion.engineBinding?.engine?.lifecycleChannel?.appIsResumed()
        }
        Fusion.engineBinding?.notifyEnterForeground()
    }

    fun notifyEnterBackground() {
        if (!isEmpty()) {
            Fusion.engineBinding?.engine?.lifecycleChannel?.appIsPaused()
        }
        Fusion.engineBinding?.notifyEnterBackground()
    }
}