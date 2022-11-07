package com.mjohnsullivan.flutterwear.wear

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientController
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


class WearPlugin : FlutterPlugin, ActivityAware, MethodCallHandler, LifecycleObserver {
    private var mAmbientCallback = WearableAmbientCallback()
    private var mMethodChannel: MethodChannel? = null
    private var mActivityBinding: ActivityPluginBinding? = null
    private var mAmbientController: AmbientController? = null

    companion object {
        const val BURN_IN_PROTECTION = AmbientModeSupport.EXTRA_BURN_IN_PROTECTION
        const val LOW_BIT_AMBIENT = AmbientModeSupport.EXTRA_LOWBIT_AMBIENT
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel = MethodChannel(binding.binaryMessenger, "wear")
        mMethodChannel!!.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel?.setMethodCallHandler(this)
        mMethodChannel = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        attachAmbientController(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        detachAmbientController()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        attachAmbientController(binding)
    }

    override fun onDetachedFromActivity() {
        detachAmbientController()
    }

    private fun attachAmbientController(binding: ActivityPluginBinding) {
        (binding.activity as WearProvider).registerWearBinding(this)
        mAmbientController = AmbientModeSupport.attach(binding.activity as FragmentActivity)
        mActivityBinding = binding
        val reference = (binding.lifecycle as HiddenLifecycleReference)
        reference.lifecycle.addObserver(this)
    }

    private fun detachAmbientController() {
        mActivityBinding?.let {
            val reference = (it.lifecycle as HiddenLifecycleReference)
            reference.lifecycle.removeObserver(this)
        }
        mActivityBinding = null

    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getShape" -> {
                val activity = mActivityBinding?.activity
                when {
                    activity == null -> {
                        result.error("no-activity", "No android activity available.", null)
                    }
                    activity.resources.configuration.isScreenRound -> {
                        result.success("round")
                    }
                    else -> {
                        result.success("square")
                    }
                }
            }
            "isAmbient" -> {
                result.success(mAmbientController?.isAmbient ?: false)
            }
            "setAutoResumeEnabled" -> {
                val enabled = call.argument<Boolean>("enabled")
                if (mAmbientController == null || enabled == null) {
                    result.error("not-ready", "Ambient mode controller not ready", null)
                } else {
                    mAmbientController!!.setAutoResumeEnabled(enabled)
                    result.success(null)
                }
            }
            "setAmbientOffloadEnabled" -> {
                val enabled = call.argument<Boolean>("enabled")
                if (mAmbientController == null || enabled == null) {
                    result.error("not-ready", "Ambient mode controller not ready", null)
                } else {
                    mAmbientController!!.setAmbientOffloadEnabled(enabled)
                    result.success(null)
                }
            }
            else -> result.notImplemented()
        }
    }

    inner class WearableAmbientCallback : AmbientModeSupport.AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            val burnInProtection = ambientDetails.getBoolean(BURN_IN_PROTECTION, false)
            val lowBitAmbient = ambientDetails.getBoolean(LOW_BIT_AMBIENT, false)
            mMethodChannel?.invokeMethod("onEnterAmbient", mapOf(
                    "burnInProtection" to burnInProtection,
                    "lowBitAmbient" to lowBitAmbient
            ))
        }

        override fun onExitAmbient() {
            mMethodChannel?.invokeMethod("onExitAmbient", null)
        }

        override fun onUpdateAmbient() {
            mMethodChannel?.invokeMethod("onUpdateAmbient", null)
        }

        override fun onAmbientOffloadInvalidated() {
            mMethodChannel?.invokeMethod("onInvalidateAmbientOffload", null)
        }
    }
}
