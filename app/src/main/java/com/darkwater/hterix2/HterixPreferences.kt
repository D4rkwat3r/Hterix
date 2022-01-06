package com.darkwater.hterix2

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.darkwater.hterix2.objects.Account

class HterixPreferences private constructor(private val activity: Activity) {

    private val accountPreferences = activity.getSharedPreferences("account", Context.MODE_PRIVATE)

    fun saveAccount(account: Account) {
        accountPreferences.edit {
            putString("email", account.nickname)
            putString("nickname", account.nickname)
            putString("password", account.password)
            putString("sid", account.sid)
            putString("uid", account.uid)
            putString("auid", account.auid)
            putString("aminoId", account.aminoId)
            putBoolean("isAminoIdEditable", account.isAminoIdEditable)
            putString("createdTime", account.createdTime)
            if (account.phoneNumber != null)
                putString("phoneNumber", account.phoneNumber)
            putString("secret", account.secret)
            putString("authorizedDeviceId", account.authorizedDeviceId)
            putBoolean("isAuthorized", true)
        }
    }

    fun getAccount(): Account {
        return Account(
            accountPreferences.getString("email", "unknown")!!,
            accountPreferences.getString("nickname", "unknown")!!,
            accountPreferences.getString("password", "unknown")!!,
            accountPreferences.getString("sid", "unknown")!!,
            accountPreferences.getString("uid", "unknown")!!,
            accountPreferences.getString("auid", "unknown")!!,
            accountPreferences.getString("aminoId", "unknown")!!,
            accountPreferences.getBoolean("isAminoIdEditable", false),
            accountPreferences.getString("createdTime", "unknown")!!,
            accountPreferences.getString("phoneNumber", null)!!,
            accountPreferences.getString("secret", "unknown")!!,
            accountPreferences.getString("authorizedDeviceId", "unknown")!!
        )
    }

    companion object {
        @JvmStatic
        fun getPreferences(activity: Activity): HterixPreferences {
            return HterixPreferences(activity)
        }
    }
}