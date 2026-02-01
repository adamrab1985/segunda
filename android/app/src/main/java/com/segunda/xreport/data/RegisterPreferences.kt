package com.segunda.xreport.data
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFS_NAME = "segunda_prefs"
private const val KEY_HIDDEN_REGISTERS = "hidden_registers"
private const val KEY_LAST_STORE_NAMES = "last_store_names"
private const val SEP = "|||"

class RegisterPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHiddenRegisters(): Set<String> = prefs.getStringSet(KEY_HIDDEN_REGISTERS, null) ?: emptySet()

    fun setHiddenRegisters(names: Set<String>) { prefs.edit { putStringSet(KEY_HIDDEN_REGISTERS, names) } }

    fun setVisible(name: String, visible: Boolean) {
        val current = getHiddenRegisters().toMutableSet()
        if (visible) current.remove(name) else current.add(name)
        setHiddenRegisters(current)
    }

    fun saveLastStoreNames(names: List<String>) { prefs.edit { putString(KEY_LAST_STORE_NAMES, names.joinToString(SEP)) } }

    fun getLastStoreNames(): List<String> {
        val s = prefs.getString(KEY_LAST_STORE_NAMES, null) ?: return emptyList()
        return s.split(SEP).map { it.trim() }.filter { it.isNotEmpty() }
    }
}
