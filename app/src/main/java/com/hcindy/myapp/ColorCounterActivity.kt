package com.hcindy.myapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.timerTask

class ColorCounterActivity : AppCompatActivity() {
    private var time = AtomicInteger(0)
    private var maxNumber = 0
    private lateinit var maxButton: Button
    private val res = mutableMapOf<Button, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_counter)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addButton(findViewById(R.id.red))
        addButton(findViewById(R.id.yellow))
        addButton(findViewById(R.id.white))
        addButton(findViewById(R.id.blue))
        maxButton = findViewById(R.id.max)
        findViewById<Button>(R.id.reset)
            .setOnClickListener(::resetAll)

        // FIXME 添加一个自动重置的开关，并且可以设置倒计时
//        Timer().schedule(timerTask {
//            if (time.addAndGet(1) > 10) {
//                time.set(0)
//                // Create an executor that executes tasks in the main thread.
//                val mainExecutor = ContextCompat.getMainExecutor(this)
//                // Execute a task in the main thread
//                mainExecutor.execute {
//                    // You code logic goes here.
//                    resetAll(null)
//                }
//            }
//        }, 1000, 1000)
    }

    private fun resetAll(view: View?) {
        maxNumber = 0
        maxButton.text = "最多"
        res.keys.forEach {
            it.text = "0"
            res[it] = 0
        }
    }

    private fun addButton(button: Button) {
        button.setOnClickListener(::count)
        res[button] = 0
    }

    private fun count(view: View) {
        time.set(0)
        val button = view as Button
        val n = res[button]!! + 1
        res[button] = n
        button.text = n.toString()
        if (n > maxNumber) {
            maxNumber = n
            maxButton.text = "最多 $n"
            // TODO 这个if里面的东西会不会导致不兼容LOLLIPOP以下的版本？
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (maxButton.backgroundTintList != button.backgroundTintList) {
                    maxButton.backgroundTintList = button.backgroundTintList
                }
            }
        }
    }
}
