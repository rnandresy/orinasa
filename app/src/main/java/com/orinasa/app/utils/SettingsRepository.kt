package com.orinasa.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore("orinasa_settings")

class SettingsRepository(private val context: Context) {

    private val KEY_COMPANY_ID   = stringPreferencesKey("company_id")
    private val KEY_USER_ID      = stringPreferencesKey("user_id")
    private val KEY_IS_ADMIN     = booleanPreferencesKey("is_admin")
    private val KEY_NOTIFY_LEAVE = booleanPreferencesKey("notify_leave")
    private val KEY_NOTIFY_PAY   = booleanPreferencesKey("notify_pay")
    private val KEY_NOTIFY_ANN   = booleanPreferencesKey("notify_announcement")

    val companyId: Flow<String>  = context.dataStore.data.map { it[KEY_COMPANY_ID] ?: "" }
    val userId: Flow<String>     = context.dataStore.data.map { it[KEY_USER_ID] ?: "" }
    val isAdmin: Flow<Boolean>   = context.dataStore.data.map { it[KEY_IS_ADMIN] ?: false }
    val notifyLeave: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFY_LEAVE] ?: true }
    val notifyPay: Flow<Boolean>   = context.dataStore.data.map { it[KEY_NOTIFY_PAY] ?: true }
    val notifyAnn: Flow<Boolean>   = context.dataStore.data.map { it[KEY_NOTIFY_ANN] ?: true }

    suspend fun saveSession(companyId: String, userId: String, isAdmin: Boolean) =
        context.dataStore.edit {
            it[KEY_COMPANY_ID] = companyId
            it[KEY_USER_ID]    = userId
            it[KEY_IS_ADMIN]   = isAdmin
        }

    suspend fun clearSession() =
        context.dataStore.edit {
            it.remove(KEY_COMPANY_ID)
            it.remove(KEY_USER_ID)
            it.remove(KEY_IS_ADMIN)
        }

    suspend fun setNotifyLeave(v: Boolean) = context.dataStore.edit { it[KEY_NOTIFY_LEAVE] = v }
    suspend fun setNotifyPay(v: Boolean)   = context.dataStore.edit { it[KEY_NOTIFY_PAY] = v }
    suspend fun setNotifyAnn(v: Boolean)   = context.dataStore.edit { it[KEY_NOTIFY_ANN] = v }
}