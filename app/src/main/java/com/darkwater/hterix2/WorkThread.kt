package com.darkwater.hterix2

import android.app.Activity
import android.widget.TextView
import android.widget.Toast
import com.darkwater.hterix2.http.AminoRequest
import com.darkwater.hterix2.objects.User
import com.darkwater.hterix2.utils.MailingCallback
import com.darkwater.hterix2.utils.Serialization
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.lang.Exception

class WorkThread(private val activity: Activity, private val ndcId: String, private val isReadOnlyRequired: Boolean, private val area: String, private val type: String, private val usersInPrivate: Int, private val text: String, private val callback: MailingCallback) : Thread() {

    private var isFinished = false
    private var isStopRequested = false

    override fun run() {
        super.run()
        var start = 0
        val size = 30
        while (!isFinished && !isStopRequested) {
            AminoRequest.initRequest("GET", "/x$ndcId/s/live-layer?topic=ndtopic:x$ndcId:online-members&start=$start&size=$size", activity)
                .sync()
                .onAminoError {
                    activity.runOnUiThread { Toast.makeText(activity, "${it.code}\n${it.message}", Toast.LENGTH_SHORT).show()  }
                }
                .send {
                    val users = Serialization.extractUserList(it, ndcId)
                    if (!isStopRequested)
                        activity.runOnUiThread { callback.onPrepared() }
                    if (users.isEmpty()) {
                        isFinished = true
                    } else {
                        if (type == "messages")
                            send(users)
                        else
                            comment(users)
                    }
                        start += 30
                    }
                }
    }

    fun comment(users: MutableList<User>) {
        val ids = mutableListOf<String>()
        users.forEach {
            ids.add(it.uid)
        }
        for (i in 0 until ids.size - 1) {
            if (isStopRequested) break
            val endpoint = if (area == "global") {
                "/g/s/user-profile/${ids[i]}/comment"
            } else {
                "/x$ndcId/s/user-profile/${ids[i]}/comment"
            }
            AminoRequest.initRequest("POST", endpoint, activity)
                .sync()
                .addBody(Serialization.createCommentBody(text, ids[i]))
                .onAminoError {
                    activity.runOnUiThread { Toast.makeText(activity, "${it.code}\n${it.message}", Toast.LENGTH_SHORT).show() }
                }
                .send {
                    if (!isStopRequested)
                        activity.runOnUiThread { callback.onPartPassed(1) }
                }
        }
    }

    fun send(users: MutableList<User>) {
        val ids = mutableListOf<String>()
        users.forEach {
            ids.add(it.uid)
        }
        for (i in 0 until ids.size - 1 step(usersInPrivate)) {
            if (isStopRequested) break
            val inviteList = try {
                ids.subList(i, usersInPrivate + i)
            } catch (e: Exception) {
                break
            }
            val endpoint = if (area == "global") {
                "/g/s/chat/thread"
            } else {
                "/x$ndcId/s/chat/thread"
            }
            AminoRequest.initRequest("POST", endpoint, activity)
                .sync()
                .addBody(Serialization.createStartChatBody(text, inviteList))
                .onAminoError {
                    activity.runOnUiThread { Toast.makeText(activity, "${it.code}\n${it.message}", Toast.LENGTH_SHORT).show()  }
                }
                .send {
                    val chatId = JsonParser.parseString(it).asJsonObject["messageList"].asJsonArray[0].asJsonObject["threadId"].asString
                    if (!isStopRequested)
                        activity.runOnUiThread { callback.onMessageSent(chatId) }
                    if (isReadOnlyRequired) {
                        readOnly(chatId)
                    }
                    if (!isStopRequested)
                        activity.runOnUiThread { callback.onPartPassed(inviteList.size) }
                }
        }
    }

    fun readOnly(chatId: String) {
        val endpoint = if (area == "global") {
            "/g/s/chat/thread/$chatId/view-only/enable"
        } else {
            "/x$ndcId/s/chat/thread/$chatId/view-only/enable"
        }
        val json = JsonObject()
        json.addProperty("timestamp", System.currentTimeMillis())
        AminoRequest.initRequest("POST", endpoint, activity)
            .sync()
            .addBody(json.toString())
            .send {
                if (!isStopRequested)
                    activity.runOnUiThread { callback.onReadOnlyEnabled(chatId) }
            }
    }

    fun kill() {
        isStopRequested = true
    }
}