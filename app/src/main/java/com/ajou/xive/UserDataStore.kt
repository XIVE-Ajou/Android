package com.ajou.xive

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "userData")

class UserDataStore() {
    private val context = App.context()
    private val dataStore : DataStore<Preferences> = context.dataStore

    private object PreferencesKeys {
        val FIRST_FLAG = booleanPreferencesKey("first_flag")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val LOGIN_TYPE = stringPreferencesKey("login_type")
        val NICKNAME = stringPreferencesKey("nickname")
    }

    suspend fun saveAccessToken(token : String) {
        withContext(Dispatchers.IO){
            dataStore.edit { pref ->
                pref[PreferencesKeys.ACCESS_TOKEN] = token
            }
        }
    }

    suspend fun getAccessToken():String? {
        return withContext(Dispatchers.IO) {
            dataStore.data.first()[PreferencesKeys.ACCESS_TOKEN]
        }
    }

    suspend fun saveRefreshToken(token : String) {
        withContext(Dispatchers.IO){
            dataStore.edit { pref ->
                pref[PreferencesKeys.REFRESH_TOKEN] = token
            }
        }
    }

    suspend fun getRefreshToken():String? {
        return withContext(Dispatchers.IO) {
            dataStore.data.first()[PreferencesKeys.REFRESH_TOKEN]
        }
    }

    suspend fun saveLoginType(type:String) {
        withContext(Dispatchers.IO) {
            dataStore.edit { pref ->
                pref[PreferencesKeys.LOGIN_TYPE] = type
            }
        }
    }

    suspend fun getLoginType(): String? {
        return withContext(Dispatchers.IO) {
            dataStore.data.first()[PreferencesKeys.LOGIN_TYPE]
        }
    }

    suspend fun saveNickname(data: String) {
        withContext(Dispatchers.IO) {
            dataStore.edit { pref ->
                pref[PreferencesKeys.NICKNAME] = data
            }
        }
    }

    suspend fun getNickname(): String? {
        return withContext(Dispatchers.IO) {
            dataStore.data.first()[PreferencesKeys.NICKNAME]
        }
    }
    suspend fun saveFirstFlag(flag : Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.edit { pref ->
                pref[PreferencesKeys.FIRST_FLAG] = flag
            }
        }
    }

    suspend fun getFirstFlag():Boolean {
        var flag = false
        withContext(Dispatchers.IO){
            dataStore.edit { pref ->
                flag = pref[PreferencesKeys.FIRST_FLAG] ?: false
            }
        }
        return flag
    }
    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dataStore.edit { pref ->
                pref.clear()
            }
        }
    }

}