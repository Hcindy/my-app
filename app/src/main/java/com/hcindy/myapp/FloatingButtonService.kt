package com.hcindy.myapp

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageView

class FloatingButtonService : Service() {
    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var button: Button? = null

    override fun onCreate() {
        super.onCreate()
        isStarted = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams!!.format = PixelFormat.RGBA_8888
        layoutParams!!.gravity = Gravity.LEFT or Gravity.TOP
        layoutParams!!.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams!!.width = 500
        layoutParams!!.height = 100
        layoutParams!!.x = 300
        layoutParams!!.y = 300
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingWindow()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showFloatingWindow() {
//        button = Button(getApplicationContext())
//        button!!.setText("Floating Window")
//        button!!.setBackgroundColor(Color.BLUE)
//        windowManager!!.addView(button, layoutParams)
//        button!!.setOnTouchListener(FloatingOnTouchListener())

        val layoutInflater = LayoutInflater.from(this)
        val displayView = layoutInflater.inflate(R.layout.image_display, null) as View
        val imageView = displayView.findViewById<ImageView>(R.id.image_display_imageview)
//        imageView.setImageResource(R.drawable.image_01);
        windowManager!!.addView(displayView, layoutParams)
    }

    private inner class FloatingOnTouchListener : View.OnTouchListener {
        private var x = 0
        private var y = 0
        override fun onTouch(view: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    layoutParams!!.x = layoutParams!!.x + movedX
                    layoutParams!!.y = layoutParams!!.y + movedY
                    windowManager!!.updateViewLayout(view, layoutParams)
                }
                else -> {}
            }
            return false
        }
    }

    companion object {
        var isStarted = false
    }
}