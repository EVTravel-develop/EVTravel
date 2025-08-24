// app/src/main/java/com/jeju/evtravel/ui/map/HeadingSensor.kt
package com.jeju.evtravel.ui.map

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

fun headingFlow(context: Context): Flow<Float> = callbackFlow {
    val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: run {
            close(IllegalStateException("Rotation Vector sensor unavailable"))
            return@callbackFlow
        }

    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    val listener = object : SensorEventListener {
        private val R = FloatArray(9)
        private val Rm = FloatArray(9)
        private val ori = FloatArray(3)

        override fun onSensorChanged(e: SensorEvent) {
            if (e.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
            SensorManager.getRotationMatrixFromVector(R, e.values)

            // 화면 회전 보정
            val rotation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.display?.rotation ?: Surface.ROTATION_0
            } else {
                @Suppress("DEPRECATION")
                wm.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }

            when (rotation) {
                Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                    R, SensorManager.AXIS_X, SensorManager.AXIS_Y, Rm
                )
                Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                    R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, Rm
                )
                Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                    R, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, Rm
                )
                Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                    R, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, Rm
                )
                else -> System.arraycopy(R, 0, Rm, 0, 9)
            }

            SensorManager.getOrientation(Rm, ori)
            var deg = Math.toDegrees(ori[0].toDouble()).toFloat() // -180~180
            if (deg < 0f) deg += 360f                               // 0~360

            trySend(deg)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
    awaitClose { sm.unregisterListener(listener) }
}
