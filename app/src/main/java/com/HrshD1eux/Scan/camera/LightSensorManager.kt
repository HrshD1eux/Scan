package com.HrshD1eux.Scan.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class LightSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    // Threshold for low light (lux)
    private val LOW_LIGHT_THRESHOLD = 5.0f 
    
    private var isLowLight = false
    private var listener: ((Boolean) -> Unit)? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val lux = event.values[0]
                val currentIsLow = lux < LOW_LIGHT_THRESHOLD
                if (currentIsLow != isLowLight) {
                    isLowLight = currentIsLow
                    listener?.invoke(isLowLight)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startListening(onLowLightChanged: (Boolean) -> Unit) {
        listener = onLowLightChanged
        lightSensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
        listener = null
    }
}
