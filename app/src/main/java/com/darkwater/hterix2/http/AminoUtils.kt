package com.darkwater.hterix2.http

import android.util.Base64
import com.darkwater.hterix2.LibConstants
import com.darkwater.hterix2.decodeHex
import com.darkwater.hterix2.toHexString
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object AminoUtils {

    fun getSignature(data: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(LibConstants.NDC_MSG_SIG_KEY.decodeHex(), LibConstants.HMAC_SHA_1))
        return Base64.encodeToString("32".decodeHex() + mac.doFinal(data.toByteArray()), Base64.NO_WRAP)
    }

    fun getDeviceId(): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(LibConstants.DEVICE_ID_KEY.decodeHex(), LibConstants.HMAC_SHA_1))
        val bytes = ByteArray(20)
        Random().nextBytes(bytes)
        val id = "32" + bytes.toHexString()
        return (id + mac.doFinal("2".toByteArray(Charsets.UTF_8) + bytes).toHexString()).uppercase()
    }

}