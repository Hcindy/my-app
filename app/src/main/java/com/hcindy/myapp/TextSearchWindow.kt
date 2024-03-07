package com.hcindy.myapp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.hcindy.myapp.adapter.TextSearchItemAdapter

class TextSearchWindow : Service() {
    private lateinit var floatView: ViewGroup
    private lateinit var floatWindowLayoutParams: WindowManager.LayoutParams
    private lateinit var windowManager: WindowManager
    private lateinit var searchInput: EditText
    private lateinit var searchInputClear: Button
    private lateinit var backToActivity: Button
    private lateinit var searchResult: RecyclerView

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 按屏幕尺寸设定宽高
        val metrics = applicationContext.resources.displayMetrics
        /*
        val width = (metrics.widthPixels * 0.5f).toInt()
        val height = (metrics.heightPixels * 0.5f).toInt()
        */
        // 取得系统窗体
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 6.0之前manifest中申请权限即可，但6.0开始还要动态权限
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else { // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
            WindowManager.LayoutParams.TYPE_TOAST
        }
        floatWindowLayoutParams = WindowManager.LayoutParams(
            // 设置窗口的宽为自动
            WindowManager.LayoutParams.WRAP_CONTENT,
            // 设置窗口的高为自动
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or // 不需要获取焦点，这样用户点击悬浮窗口以外的区域，就不需要关闭悬浮窗口
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // // 当前window区域以外的点击事件传递给下层window,当前window区域以内的点击事件自己处理,如果不设置这个值,则window消费掉所有点击事件,不管这些点击事件是不是在window的范围之内
            //使用 RGBA_8888 像素格式的窗口可以在保持高质量图像的同时实现透明度效果。
            PixelFormat.RGBA_8888 // TRANSLUCENT // System chooses a format that supports translucency (many alpha bits)
        )
        floatWindowLayoutParams.gravity = Gravity.TOP or Gravity.START
        floatWindowLayoutParams.x = 0
        floatWindowLayoutParams.y = (metrics.heightPixels * 0.1).toInt()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        floatView = inflater.inflate(R.layout.window_text_search, null) as ViewGroup
        floatView.setOnTouchListener(object : View.OnTouchListener {
            val updatedFloatWindowLayoutParam = floatWindowLayoutParams
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = updatedFloatWindowLayoutParam.x.toDouble()
                        y = updatedFloatWindowLayoutParam.y.toDouble()

                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        updatedFloatWindowLayoutParam.x = (x + event.rawX - px).toInt()
                        updatedFloatWindowLayoutParam.y = (y + event.rawY - py).toInt()

                        windowManager.updateViewLayout(floatView, updatedFloatWindowLayoutParam)
                    }
                }
                return false
            }
        })

        searchInput = floatView.findViewById(R.id.search_input)
        searchInput.isCursorVisible = false // 取消闪烁的输入光标
        searchInput.setOnTouchListener { v, event ->
            onlyMineInput()
            false
        }
        searchInput.setOnEditorActionListener { textView, i, keyEvent ->
            resumeOtherInput()
            false
        }
        searchInput.doOnTextChanged(::filterAndFill)

        searchInputClear = floatView.findViewById(R.id.search_input_clear)
        searchInputClear.setOnClickListener {
            searchInput.setText("")
            backToActivity.visibility = View.VISIBLE
            searchResult.visibility = View.INVISIBLE
        }

        backToActivity = floatView.findViewById(R.id.back_to_activity)
        backToActivity.setOnClickListener {
            stopSelf()
            val back = Intent(this@TextSearchWindow, TextSearchActivity::class.java)
            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(back)
        }

        searchResult = floatView.findViewById(R.id.search_result)
        searchResult.adapter = TextSearchItemAdapter(this, true, TextSearchStore.searchLines)

        windowManager.addView(floatView, floatWindowLayoutParams)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onlyMineInput() {
        val updatedFloatParamsFlag = floatWindowLayoutParams
        updatedFloatParamsFlag.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or // 当前window区域以外的点击事件传递给下层window,当前window区域以内的点击事件自己处理,如果不设置这个值,则window消费掉所有点击事件,不管这些点击事件是不是在window的范围之内
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 将window放置在整个屏幕之内,无视其他的装饰(比如状态栏)
        windowManager.updateViewLayout(floatView, updatedFloatParamsFlag)
    }

    private fun resumeOtherInput() {
        val updatedFloatParamsFlag = floatWindowLayoutParams
        updatedFloatParamsFlag.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or // 当前window区域以外的点击事件传递给下层window,当前window区域以内的点击事件自己处理,如果不设置这个值,则window消费掉所有点击事件,不管这些点击事件是不是在window的范围之内
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL // 将window放置在整个屏幕之内,无视其他的装饰(比如状态栏)
        windowManager.updateViewLayout(floatView, updatedFloatParamsFlag)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterAndFill(text: CharSequence?, start: Int, before: Int, count: Int) {
        if (!text.isNullOrEmpty()) {
            backToActivity.visibility = View.INVISIBLE
            searchResult.visibility = View.VISIBLE
        }
        TextSearchStore.filterAndFill(text)
        searchResult.adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        windowManager.removeView(floatView)
    }
}
