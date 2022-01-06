package com.darkwater.hterix2.utils

interface MailingCallback {
    fun onPrepared()
    fun onMessageSent(chatId: String)
    fun onReadOnlyEnabled(chatId: String)
    fun onPartPassed(invitedCount: Int)
}