package com.caisse.pos.util

import android.content.Context
import java.util.UUID

// id appareil + préfixe pour les n° de ticket (ex: A3F2)
object DeviceIdProvider {
    private const val PREFS = "caisse_device"
    private const val KEY_ID = "device_id"
    private const val KEY_PREFIX = "device_prefix"

    fun getOrCreate(context: Context): DeviceIdentity {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existingId = prefs.getString(KEY_ID, null)
        val existingPrefix = prefs.getString(KEY_PREFIX, null)
        if (existingId != null && existingPrefix != null) {
            return DeviceIdentity(existingId, existingPrefix)
        }
        val id = UUID.randomUUID().toString()
        val prefix = id.replace("-", "").take(4).uppercase()
        prefs.edit()
            .putString(KEY_ID, id)
            .putString(KEY_PREFIX, prefix)
            .apply()
        return DeviceIdentity(id, prefix)
    }

    fun formatTicket(prefix: String, sequence: Long): String {
        return "$prefix-${sequence.toString().padStart(6, '0')}"
    }
}

data class DeviceIdentity(
    val deviceId: String,
    val prefix: String
)
