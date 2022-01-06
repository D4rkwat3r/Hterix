package com.darkwater.hterix2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.darkwater.hterix2.activity.AuthActivity
import com.darkwater.hterix2.activity.MainActivity
import com.darkwater.hterix2.http.AminoRequest
import com.darkwater.hterix2.http.Telemetry

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        val prefs = getSharedPreferences("account", Context.MODE_PRIVATE)
        if (prefs.getBoolean("authorized", false)) {
            AminoRequest.sid = prefs.getString("sid", null)!!
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()
    }
}