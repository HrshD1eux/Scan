package com.HrshD1eux.Scan.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class LightSensorManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    private var isLowLight = false
    private var listener: ((Boolean) -> Unit)? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val lux = event?.values?.getOrNull(0) ?: return
            val lowLight = lux < LOW_LIGHT_THRESHOLD
            if (lowLight != isLowLight) {
                isLowLight = lowLight
                listener?.invoke(isLowLight)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startListening(onLowLightChanged: (Boolean) -> Unit) {
        listener = onLowLightChanged
        lightSensor?.let {
            sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(sensorEventListener)
        listener = null
    }

    companion object {
        private const val LOW_LIGHT_THRESHOLD = 5.0f
    }
}
