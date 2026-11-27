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

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFFFFFFFF.toInt())
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )
            setBackgroundColor(0xFF1E1E1E.toInt())
            setPadding(32, 16, 32, 16)
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setBackgroundColor(0x00000000)
            setColorFilter(0xFFFFFFFF.toInt())
            setOnClickListener { finish() }
        }
        topBar.addView(backButton)
        rootLayout.addView(topBar)

        val canvasContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        drawingView = DrawingView(this)
        canvasContainer.addView(drawingView)
        rootLayout.addView(canvasContainer)

        val bottomToolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = 48
            }
            setPadding(24, 16, 24, 16)
            setBackgroundColor(0xEE2D2D2D.toInt())
        }

        val btnPen = createToolbarButton("Pen") { drawingView.setEraser(false) }
        val btnEraser = createToolbarButton("Eraser") { drawingView.setEraser(true) }
        val btnClear = createToolbarButton("Clear") { drawingView.clearCanvas() }

        bottomToolbar.addView(btnPen)
        bottomToolbar.addView(btnEraser)
        bottomToolbar.addView(btnClear)

        canvasContainer.addView(bottomToolbar)
        setContentView(rootLayout)
    }

    private fun createToolbarButton(title: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = title
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x00000000)
            setOnClickListener { onClick() }
        }
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
                currentPath.quadTo(x1, y1, controlX, controlY)
                x1 = x
                y1 = y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(x, y)
                paths.add(Pair(currentPath, currentPaint))
                currentPath = Path()
                invalidate()
            }
        }
        return true
    }
}
