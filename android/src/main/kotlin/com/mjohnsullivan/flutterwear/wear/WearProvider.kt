package com.mjohnsullivan.flutterwear.wear

import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallbackProvider
import io.flutter.embedding.android.FlutterFragmentActivity

interface WearProvider : AmbientCallbackProvider {
    class Delegate : WearProvider {
        companion object {
            val instance = Delegate()
        };
        private lateinit var plugin: WearPlugin
        private var ambientCallback: AmbientModeSupport.AmbientCallback? = null

        /**
         * @return the [AmbientCallback] to be used by this class to communicate with the
         * entity interested in ambient events.
         */
        override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
            if (ambientCallback != null) {
                return ambientCallback!!
            }
            ambientCallback = plugin.WearableAmbientCallback()
            return ambientCallback!!
        }
        override fun registerWearBinding(plugin: WearPlugin) {
            this.plugin = plugin
            ambientCallback = null
        }
    }
    fun registerWearBinding(plugin: WearPlugin);
}