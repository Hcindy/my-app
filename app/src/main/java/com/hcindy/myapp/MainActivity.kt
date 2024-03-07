package com.hcindy.myapp

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.color_counter)
            .setOnClickListener {
                startActivity(Intent(this, ColorCounterActivity::class.java))
            }
        findViewById<Button>(R.id.text_search)
            .setOnClickListener {
                startActivity(Intent(this, TextSearchActivity::class.java))
                /*
                // 用于直接测试悬浮窗
                if (isServiceRunning()) {
                    stopService(Intent(this@MainActivity, TextSearchWindow::class.java))
                }
                startService(Intent(this@MainActivity, TextSearchWindow::class.java))
                */
                finish()
            }
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (TextSearchWindow::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
