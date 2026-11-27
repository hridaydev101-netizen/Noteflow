package com.noteflow.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var toolbar: LinearLayout
    private lateinit var showToolbarButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(35, 35, 35)
        window.navigationBarColor = Color.rgb(25, 25, 25)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(35, 35, 35))
        }

        drawingView = DrawingView(this)

        val paperParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(dp(18), dp(24), dp(18), dp(24))
        }

        root.addView(drawingView, paperParams)

        // Back button
        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(12), dp(12), dp(12), dp(12))

            setOnClickListener {
                finish()
            }
        }

        val backParams = FrameLayout.LayoutParams(
            dp(58),
            dp(58),
            Gravity.TOP or Gravity.START
        ).apply {
            leftMargin = dp(20)
            topMargin = dp(30)
        }

        root.addView(backButton, backParams)

        // Hide/show toolbar button
        showToolbarButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.arrow_down_float)
            setColorFilter(Color.BLACK)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            visibility = View.GONE
        }

        val showButtonParams = FrameLayout.LayoutParams(
            dp(56),
            dp(56),
            Gravity.BOTTOM or Gravity.END
        ).apply {
            rightMargin = dp(26)
            bottomMargin = dp(32)
        }

        showToolbarButton.setOnClickListener {
            toolbar.visibility = View.VISIBLE
            showToolbarButton.visibility = View.GONE
        }

        root.addView(showToolbarButton, showButtonParams)

        // Bottom toolbar
        toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.rgb(48, 58, 72))
        }

        val toolbarParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            dp(76),
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            bottomMargin = dp(26)
        }

        root.addView(toolbar, toolbarParams)

        // Pen
        addToolButton("✎", "Pen") {
            drawingView.setTool(DrawingView.Tool.PEN)
        }

        // Eraser
        addToolButton("◇", "Eraser") {
            drawingView.setTool(DrawingView.Tool.ERASER)
        }

        // Undo
        addToolButton("↶", "Undo") {
            drawingView.undo()
        }

        // Redo
        addToolButton("↷", "Redo") {
            drawingView.redo()
        }

        // Grid
        addToolButton("▦", "Grid") {
            drawingView.toggleGrid()
        }

        // Clear
        addToolButton("⌫", "Clear") {
            drawingView.clearCanvas()
        }

        // Color
        addToolButton("●", "Color") {
            drawingView.changeColor()
        }

        // Hide toolbar
        addToolButton("⌄", "Hide") {
            toolbar.visibility = View.GONE
            showToolbarButton.visibility = View.VISIBLE
        }

        setContentView(root)
    }

    private fun addToolButton(
        icon: String,
        description: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = icon
            textSize = 25f
            contentDescription = description
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(10), 0, dp(10), 0)
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0

            setOnClickListener {
                action()
            }
        }

        toolbar.addView(
            button,
            LinearLayout.LayoutParams(dp(58), dp(58))
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}


class DrawingView(context: Context) : View(context) {

    enum class Tool {
        PEN,
        ERASER
    }

    private data class Stroke(
        val path: Path,
        val paint: Paint
    )

    private val strokes = mutableListOf<Stroke>()
    private val undoneStrokes = mutableListOf<Stroke>()

    private var currentPath: Path? = null
    private var currentPaint = createPenPaint()
    private var selectedTool = Tool.PEN

    private var previousX = 0f
    private var previousY = 0f

    private var showGrid = false
    private var colorIndex = 0

    private val colors = listOf(
        Color.BLACK,
        Color.rgb(40, 100, 220),
        Color.rgb(220, 60, 60),
        Color.rgb(30, 150, 80),
        Color.rgb(130, 70, 190)
    )

    private val gridPaint = Paint().apply {
        color = Color.rgb(225, 225, 225)
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    init {
        setBackgroundColor(Color.WHITE)
        isFocusable = true
    }

    private fun createPenPaint(): Paint {
        return Paint().apply {
            color = colors[colorIndex]
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
            isDither = true
        }
    }

    private fun createEraserPaint(): Paint {
        return Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 42f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
            isDither = true
        }
    }

    fun setTool(tool: Tool) {
        selectedTool = tool

        currentPaint = when (tool) {
            Tool.PEN -> createPenPaint()
            Tool.ERASER -> createEraserPaint()
        }

        invalidate()
    }

    fun changeColor() {
        colorIndex++

        if (colorIndex >= colors.size) {
            colorIndex = 0
        }

        if (selectedTool == Tool.PEN) {
            currentPaint = createPenPaint()
        }

        invalidate()
    }

    fun toggleGrid() {
        showGrid = !showGrid
        invalidate()
    }

    fun clearCanvas() {
        strokes.clear()
        undoneStrokes.clear()
        currentPath = null
        invalidate()
    }

    fun undo() {
        if (strokes.isNotEmpty()) {
            undoneStrokes.add(strokes
