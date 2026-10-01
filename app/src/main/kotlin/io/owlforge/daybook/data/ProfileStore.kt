package io.owlforge.daybook.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.profileDs by preferencesDataStore("profile")

data class Profile(
    val name: String = "",
    val planHour: Int = 21,
    val planMinute: Int = 0,
    val onboarded: Boolean = false,
    /** "preset:<index>" or "photo:<timestamp>" (the photo lives in filesDir/avatar.jpg). */
    val avatar: String = "preset:0",
)

class ProfileStore(private val ctx: Context) {
    private object K {
        val NAME = stringPreferencesKey("name")
        val HOUR = intPreferencesKey("plan_hour")
        val MIN = intPreferencesKey("plan_minute")
        val DONE = booleanPreferencesKey("onboarded")
        val AVATAR = stringPreferencesKey("avatar")
    }

    val flow: Flow<Profile> = ctx.profileDs.data.map { p ->
        Profile(
            p[K.NAME] ?: "",
            p[K.HOUR] ?: 21,
            p[K.MIN] ?: 0,
            p[K.DONE] ?: false,
            p[K.AVATAR] ?: "preset:0",
        )
    }

    suspend fun current(): Profile = flow.first()

    suspend fun save(name: String, hour: Int, minute: Int) {
        ctx.profileDs.edit {
            it[K.NAME] = name
            it[K.HOUR] = hour
            it[K.MIN] = minute
            it[K.DONE] = true
        }
    }

    suspend fun setAvatar(value: String) {
        ctx.profileDs.edit { it[K.AVATAR] = value }
    }
}
