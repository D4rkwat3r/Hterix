package com.darkwater.hterix2.http

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.darkwater.hterix2.LibConstants
import com.darkwater.hterix2.clear
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AminoRequest private constructor(private val requestMethod: String, private val endpoint: String, private val activity: Activity) {

    private val client = OkHttpClient()
    private val JSON_CONTENT_TYPE = "application/json; charset=utf-8"
    private val BASE_API_URL = "http://service.narvii.com/api/v1"
    private var REQUEST_BODY: String? = null
    private var errorHandler: ((Throwable) -> Unit)? = null
    private var aminoErrorHandler: ((AminoError) -> Unit)? = null
    private var async = false

    fun addBody(body: String): AminoRequest {
        REQUEST_BODY = body
        return this
    }

    fun onError(handler: (Throwable) -> Unit): AminoRequest {
        errorHandler = handler
        return this
    }

    fun onAminoError(handler: (AminoError) -> Unit): AminoRequest {
        aminoErrorHandler = handler
        return this
    }

    fun async(): AminoRequest {
        async = true
        return this
    }

    fun sync(): AminoRequest {
        async = false
        return this
    }

    fun send(onSuccess: (String) -> Unit) {
        val builder = Request.Builder()
        builder.url(BASE_API_URL + endpoint)
        builder.addHeader(AminoHeaders.DeviceId, AminoUtils.getDeviceId())
        builder.addHeader(AminoHeaders.Lang, LibConstants.RU_NDC_LANG)
        if (sid != null)
            builder.addHeader(AminoHeaders.Authorization, sid!!)
        if (REQUEST_BODY != null) {
            builder.method(requestMethod, REQUEST_BODY!!.toRequestBody(JSON_CONTENT_TYPE.toMediaType()))
            builder.addHeader(AminoHeaders.Signature, AminoUtils.getSignature(REQUEST_BODY!!))
        }
        builder.addHeader("Accept-Language", LibConstants.RU_LANG)
        builder.addHeader("User-Agent", LibConstants.AMINO_USER_AGENT)
        builder.addHeader("Connection", LibConstants.CONNECTION_KEEP_ALIVE)
        val request = builder.build()
        if (async) {
            client.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (errorHandler != null) {
                        activity.runOnUiThread { errorHandler!!.invoke(e) }
                    } else {
                        activity.runOnUiThread { Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show() }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body!!.string()
                    val json = JsonParser.parseString(body).asJsonObject
                    val apiStatus = json["api:statuscode"].asInt
                    val apiMessage = json["api:message"].asString
                    if (apiStatus != 0) {
                        if (aminoErrorHandler != null) {
                            val error = AminoError(apiStatus, apiMessage)
                            activity.runOnUiThread { aminoErrorHandler!!.invoke(error) }
                        } else {
                            val alert = AlertDialog.Builder(activity)
                            alert.setTitle("Ошибка $apiStatus")
                            alert.setMessage(apiMessage)
                            alert.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            activity.runOnUiThread { alert.show() }
                        }
                    } else {
                        activity.runOnUiThread { onSuccess(body) }
                    }
                    response.close()
                }

            })
        } else {
            val response = try {
                client.newCall(request).execute()
            } catch (e: IOException) {
                if (errorHandler != null) {
                    errorHandler!!.invoke(e)
                } else {
                    Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                return
            }
            val body = response.body!!.string()
            val json = JsonParser.parseString(body).asJsonObject
            val apiStatus = json["api:statuscode"].asInt
            val apiMessage = json["api:message"].asString
            if (apiStatus != 0) {
                if (aminoErrorHandler != null) {
                    val error = AminoError(apiStatus, apiMessage)
                    aminoErrorHandler!!.invoke(error)
                } else {
                    val alert = AlertDialog.Builder(activity)
                    alert.setTitle("Ошибка $apiStatus")
                    alert.setMessage(apiMessage)
                    alert.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
            } else {
                onSuccess(body)
            }
            response.close()
        }
    }

    companion object {
        @JvmStatic
        fun initRequest(requestMethod: String, endpoint: String, activity: Activity): AminoRequest {
            return AminoRequest(requestMethod, endpoint, activity)
        }
        var sid: String? = null
    }
}