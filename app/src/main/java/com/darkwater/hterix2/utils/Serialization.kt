package com.darkwater.hterix2.utils

import com.darkwater.hterix2.LibConstants
import com.darkwater.hterix2.objects.Account
import com.darkwater.hterix2.objects.Community
import com.darkwater.hterix2.objects.User
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.*

object Serialization {
    fun createAuthBody(
        email: String,
        password: String
    ): String {
        val jsonData = JsonObject()
        jsonData.addProperty("email", email)
        jsonData.addProperty("v", 2)
        jsonData.addProperty("secret", "0 $password")
        jsonData.addProperty("deviceID", LibConstants.DEFAULT_DEVICE_ID)
        jsonData.addProperty("clientType", 100)
        jsonData.addProperty("action", "normal")
        jsonData.addProperty("timestamp", System.currentTimeMillis())
        return jsonData.toString()
    }

    fun createStartChatBody(text: String, userIds: MutableList<String>): String {
        val jsonData = JsonObject()
        jsonData.add("title", null)
        jsonData.add("inviteeUids", JsonParser.parseString(userIds.toString()).asJsonArray)
        jsonData.addProperty(
            "initialMessageContent",
            "$text\n\nЭто сообщение было отправлено из рекламного приложения Hterix! Автор: t.me/D4rkwat3r\nЕго канал: t.me/DWReaction"
        )
        jsonData.add("content", null)
        jsonData.addProperty("type", 0)
        jsonData.addProperty("timestamp", System.currentTimeMillis())
        return jsonData.toString()
    }

    fun createCommentBody(text: String, userId: String): String {
        val jsonData = JsonObject()
        jsonData.addProperty(
            "content",
            "$text\n\nЭтот комментарий был оставлен с использованием рекламного приложения Hterix! Автор: t.me/D4rkwat3r\nЕго канал: t.me/DWReaction")
        jsonData.add("stickerId", null)
        jsonData.addProperty("type", 0)
        jsonData.addProperty("timestamp", System.currentTimeMillis())
        return jsonData.toString()
    }

    fun extractAccount(response: String, email: String, password: String, authorizedDeviceId: String): Account {
        val responseJson = JsonParser.parseString(response).asJsonObject
        val accountJson = responseJson["account"].asJsonObject
        val phone = if (accountJson["phoneNumber"].isJsonNull) {
            null
        } else {
            accountJson["phoneNumber"].asString
        }
        return Account(
            email,
            accountJson["nickname"].asString,
            password,
            responseJson["sid"].asString,
            accountJson["uid"].asString,
            responseJson["auid"].asString,
            accountJson["aminoId"].asString,
            accountJson["aminoIdEditable"].asBoolean,
            accountJson["createdTime"].asString,
            phone,
            responseJson["secret"].asString,
            authorizedDeviceId
        )
    }
    fun extractCommunityList(response: String): MutableList<Community> {
        val json = JsonParser.parseString(response).asJsonObject
        val array = json["communityList"].asJsonArray
        val list = mutableListOf<Community>()
        array.forEach {
            val currentJson = it.asJsonObject
            if (currentJson["status"].asInt == 0) {
                list.add(
                    Community(
                        currentJson["name"].asString,
                        currentJson["ndcId"].asString,
                        if (currentJson["icon"].isJsonNull) {
                            "https://i.ytimg.com/vi/DTTHyOJi2gQ/maxresdefault.jpg"
                        } else {
                            currentJson["icon"].asString
                        }
                    )
                )
            }
        }
        return list
    }
    fun extractUserList(response: String, ndcId: String): MutableList<User> {
        val json = JsonParser.parseString(response).asJsonObject
        val array = json["userProfileList"].asJsonArray
        val list = mutableListOf<User>()
        array.forEach {
            val currentJson = it.asJsonObject
            list.add(
                User(
                    currentJson["nickname"].asString,
                    currentJson["uid"].asString,
                    ndcId
                )
            )
        }
        return list
    }
}