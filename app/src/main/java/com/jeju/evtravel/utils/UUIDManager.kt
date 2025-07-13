package com.jeju.evtravel.utils

import android.content.Context
import java.util.UUID

object UUIDManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_UUID = "guest_uuid"

    /**
     * UUID 존재 여부 확인
     */
    fun exists(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_UUID)
    }

    /**
     * UUID 생성 및 저장 (이미 존재하면 새로 생성하지 않음)
     */
    fun create(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_UUID, null)
        return existing ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_UUID, it).apply()
        }
    }

    /**
     * UUID 강제 업데이트 (기존 UUID 덮어쓰기)
     */
    fun update(context: Context, newUUID: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_UUID, newUUID).apply()
    }

    /**
     * 현재 UUID 가져오기 (없으면 null 반환)
     */
    fun get(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_UUID, null)
    }
}
