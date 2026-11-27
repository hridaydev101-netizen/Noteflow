package com.noteflow.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this)
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        rootLayout.setBackgroundColor(0xFFFFFFFF.toInt())

        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            120
        )
        topBar.setBackgroundColor(0xFF1E1E1E.toInt())
        topBar.setPadding(32, 16, 32, 16)
        topBar.gravity = Gravity.CENTER_VERTICAL

        val backButton = ImageButton(this)
        backButton.setImageResource(android.R.drawable.ic_menu_revert)
        backButton.setBackgroundColor(0x00000000)
        backButton.setColorFilter(0xFFFFFFFF.toInt())
        backButton.setOnClickListener { finish() }
        topBar.addView(backButton)
        rootLayout.addView(topBar)

        val canvasContainer = FrameLayout(this)
        canvasContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        )

        drawingView = DrawingView(this)
        canvasContainer.addView(drawingView)
        rootLayout.addView(canvasContainer)

        val bottomToolbar = LinearLayout(this)
        bottomToolbar.orientation = LinearLayout.HORIZONTAL
        val toolbarParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        )
        toolbarParams.bottomMargin = 48
        bottomToolbar.layoutParams = toolbarParams
        bottomToolbar.setPadding(24, 16, 24, 16)
        bottomToolbar.setBackgroundColor(0xEE2D2D2D.toInt())

        val btnPen = Button(this)
        btnPen.text = "Pen"
        btnPen.setTextColor(0xFFFFFFFF.toInt())
        btnPen.setBackgroundColor(0x00000000)
        btnPen.setOnClickListener { drawingView.setEraser(false) }

        val btnEraser = Button(this)
        btnEraser.text = "Eraser"
        btnEraser.setTextColor(0xFFFFFFFF.toInt())
        btnEraser.setBackgroundColor(0x00000000)
        btnEraser.setOnClickListener { drawingView.setEraser(true) }

        val btnClear = Button(this)
        btnClear.text = "Clear"
        btnClear.setTextColor(0xFFFFFFFF.toInt())
        btnClear.setBackgroundColor(0x00000000)
        btnClear.setOnClickListener { drawingView.clearCanvas() }

        bottomToolbar.addView(btnPen)
        bottomToolbar.addView(btnEraser)
        bottomToolbar.addView(btnClear)

        canvasContainer.addView(bottomToolbar)
        
        setContentView(rootLayout)
    }
}

class DrawingView(context: Context) : View(context) {

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath = Path()
    private var currentPaint = createPenPaint()

    private val gridPaint = Paint().apply {
        color = 0xFFE0E0E0.toInt()
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private var x1 = 0f
    private var y1 = 0f

    private fun createPenPaint(): Paint {
        return Paint().apply {
            color = 0xFF111111.toInt()
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 6f
        }
    }

    fun setEraser(isEraser: Boolean) {
        currentPaint = if (isEraser) {
            Paint().apply {
                color = 0xFFFFFFFF.toInt()
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 40f
            }
        } else {
            createPenPaint()
        }
    }

    fun clearCanvas() {
        paths.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val gridSize = 60f
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += gridSize
        }
        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += gridSize
        }

        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }
        canvas.drawPath(currentPath, currentPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath.moveTo(x, y)
                x1 = x
                y1 = y
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val controlX = (x + x1) / 2
                val controlY = (y + y1) / 2
                currentPath.quadTo(x1, y1, controlX, control
            
