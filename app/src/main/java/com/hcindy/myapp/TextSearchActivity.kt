package com.hcindy.myapp

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.hcindy.myapp.adapter.TextSearchItemAdapter
import java.util.*

class TextSearchActivity : AppCompatActivity() {
    private lateinit var imm: InputMethodManager

    private lateinit var selectTextFile: Button
    private lateinit var searchGroup: Group
    private lateinit var searchContentInput: EditText
    private lateinit var searchContentInputClear: Button
    private lateinit var selectTextFileRest: Button
    private lateinit var searchResult: RecyclerView

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_search)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val selectTextFileLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent(), ::readTextFile)
        selectTextFile = findViewById(R.id.select_text_file)
        selectTextFile.setOnClickListener { selectTextFileLauncher.launch("*/*") }
        searchGroup = findViewById(R.id.search_group)
        searchContentInput = findViewById(R.id.search_content_input)
        searchContentInput.doOnTextChanged(::filterAndFill)
        searchContentInputClear = findViewById(R.id.select_text_input_clear)
        searchContentInputClear.setOnClickListener {
            searchContentInput.setText("")
            searchContentInput.requestFocus()
            imm.showSoftInput(searchContentInput, InputMethodManager.SHOW_FORCED)
        }
        selectTextFileRest = findViewById(R.id.select_text_file_reset)
        selectTextFileRest.setOnClickListener {
            searchContentInput.setText("")
            selectTextFileLauncher.launch("*/*")
        }
        searchResult = findViewById(R.id.search_result)
        searchResult.adapter = TextSearchItemAdapter(this, false, TextSearchStore.searchLines)

        if (TextSearchStore.textLines.size > 0) {
            selectTextFile.visibility = View.INVISIBLE
            searchGroup.visibility = View.VISIBLE
        }

        if (isServiceRunning()) {
            stopService(Intent(this@TextSearchActivity, TextSearchWindow::class.java))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterAndFill(text: CharSequence?, start: Int, before: Int, count: Int) {
        TextSearchStore.filterAndFill(text)
        searchResult.adapter!!.notifyDataSetChanged()
    }

    private fun readTextFile(result: Uri) {
        try {
            TextSearchStore.textLines.clear()
            val scanner = Scanner(contentResolver.openInputStream(result))
            while (scanner.hasNextLine()) {
                val nextLine = scanner.nextLine().trim()
                if (nextLine.isNotEmpty()) TextSearchStore.textLines.add(nextLine)
            }
            scanner.close()
            if (TextSearchStore.textLines.size == 0)
                Toast.makeText(this, "注意！文件内容为空", Toast.LENGTH_SHORT).show()
            selectTextFile.visibility = View.INVISIBLE
            searchGroup.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "选择的文件有误，读取内容失败", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_text_search, menu)
        return true
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

    private fun requestFloatingWindowPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("需要覆盖显示的权限")
        builder.setMessage("请允许覆盖显示")
        builder.setPositiveButton(
            "打开设定",
            DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, RESULT_OK)
            })

        dialog = builder.create()
        dialog.show()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this) // will return true when had permission
        } else true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.float_window -> {
                if (TextSearchStore.textLines.size == 0)
                    Toast.makeText(this, "先选择文本文件", Toast.LENGTH_SHORT).show()
                else if (checkOverlayPermission()) {
                    startService(Intent(this@TextSearchActivity, TextSearchWindow::class.java))
                    finish()
                } else {
                    requestFloatingWindowPermission()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    /*
    // 声明屏幕的宽高
    private var x: Float = 0f
    private var y: Float = 0f
    private var top: Int = 0
    // 创建悬浮窗体
    @SuppressLint("ClickableViewAccessibility")
    private fun createDesktopLayout() {
        mTextSearchWindowLayout = TextSearchWindow(this)
        mTextSearchWindowLayout.setOnTouchListener(object : OnTouchListener {
            var mTouchStartX = 0f
            var mTouchStartY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                x = event.rawX
                y = event.rawY - top // 25是系统状态栏的高度
//                Log.i(
//                    "startP", "startX" + mTouchStartX + "====startY"
//                            + mTouchStartY
//                )
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 获取相对View的坐标，即以此View左上角为原点
                        mTouchStartX = event.x
                        mTouchStartY = event.y
//                        Log.i(
//                            "startP", "startX" + mTouchStartX + "====startY"
//                                    + mTouchStartY
//                        )
                        val end: Long = System.currentTimeMillis() - startTime
                        // 双击的间隔在 300ms以下
                        if (end < 300) {
                            mWindowManager.removeView(mTextSearchWindowLayout)
                            finish()
                        }
                        startTime = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 更新浮动窗口位置参数
                        mLayout.x = (x - mTouchStartX).toInt()
                        mLayout.y = (y - mTouchStartY).toInt()
                        mWindowManager.updateViewLayout(v, mLayout)
                    }
                    MotionEvent.ACTION_UP -> {
                        // 更新浮动窗口位置参数
                        mLayout.x = (x - mTouchStartX).toInt()
                        mLayout.y = (y - mTouchStartY).toInt()
                        mWindowManager.updateViewLayout(v, mLayout)
                        // 可以在此记录最后一次的位置
                        mTouchStartX = 0f
                        mTouchStartX = 0f
                    }
                }
                return true
            }
        })
    }
    */
}
