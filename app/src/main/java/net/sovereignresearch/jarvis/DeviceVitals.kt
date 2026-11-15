package net.sovereignresearch.jarvis

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager

/** On-device readings only; no persistent identifier or sensor permission. */
internal object DeviceVitals {
    data class Snapshot(
        val batteryPercent: Int?,
        val charging: Boolean,
        val powerSaver: Boolean,
        val thermalStatus: Int,
        val lowMemory: Boolean,
        val availableRamMb: Long,
        val totalRamMb: Long
    )

    fun read(context: Context): Snapshot {
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) ((level * 100L) / scale).toInt() else null
        val chargingStatus = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = chargingStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
            chargingStatus == BatteryManager.BATTERY_STATUS_FULL
        val power = context.getSystemService(PowerManager::class.java)
        val memory = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java).getMemoryInfo(memory)
        return Snapshot(
            batteryPercent = percent,
            charging = charging,
            powerSaver = power.isPowerSaveMode,
            thermalStatus = power.currentThermalStatus,
            lowMemory = memory.lowMemory,
            availableRamMb = memory.availMem / (1024 * 1024),
            totalRamMb = memory.totalMem / (1024 * 1024)
        )
    }
}
