
package com.noteflow.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var toolbar: LinearLayout
    private lateinit var showToolbarButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(35, 35, 35)
        window.navigationBarColor = Color.rgb(25, 25, 25)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(35, 35, 35))

        drawingView = DrawingView(this)

        val paperParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        paperParams.setMargins(dp(18), dp(24), dp(18), dp(24))
        root.addView(drawingView, paperParams)

        // Back button
        val backButton = ImageButton(this)

        backButton.setImageResource(android.R.drawable.ic_menu_revert)
        backButton.setColorFilter(Color.WHITE)
        backButton.setBackgroundColor(Color.TRANSPARENT)

        backButton.setOnClickListener {
            finish()
        }

        val backParams = FrameLayout.LayoutParams(
            dp(58),
            dp(58),
            Gravity.TOP or Gravity.START
        )

        backParams.leftMargin = dp(20)
        backParams.topMargin = dp(28)

        root.addView(backButton, backParams)

        // Drawing toolbar
        toolbar = LinearLayout(this)

        toolbar.orientation = LinearLayout.HORIZONTAL
        toolbar.gravity = Gravity.CENTER_VERTICAL
        toolbar.setPadding(dp(8), dp(8), dp(8), dp(8))
        toolbar.setBackgroundColor(Color.rgb(48, 58, 72))

        val toolbarParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            dp(72),
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        )

        toolbarParams.bottomMargin = dp(24)
        root.addView(toolbar, toolbarParams)

        // Pen
        addToolButton("✎") {
            drawingView.setTool(DrawingView.Tool.PEN)
        }

        // Eraser
        addToolButton("◇") {
            drawingView.setTool(DrawingView.Tool.ERASER)
        }

        // Undo
        addToolButton("↶") {
            drawingView.undo()
        }

        // Redo
        addToolButton("↷") {
            drawingView.redo()
        }

        // Grid
        addToolButton("▦") {
            drawingView.toggleGrid()
        }

        // Colour
        addToolButton("●") {
            drawingView.changeColor()
        }

        // Clear
        addToolButton("⌫") {
            drawingView.clearCanvas()
        }

        // Hide toolbar
        addToolButton("⌄") {
            toolbar.visibility = View.GONE
            showToolbarButton.visibility = View.VISIBLE
        }

        // Show toolbar button
        showToolbarButton = ImageButton(this)

        showToolbarButton.setImageResource(
            android.R.drawable.arrow_down_float
        )

        showToolbarButton.setColorFilter(Color.BLACK)
        showToolbarButton.setBackgroundColor(Color.WHITE)
        showToolbarButton.visibility = View.GONE

        val showButtonParams = FrameLayout.LayoutParams(
            dp(58),
            dp(58),
            Gravity.BOTTOM or Gravity.END
        )

        showButtonParams.rightMargin = dp(24)
        showButtonParams.bottomMargin = dp(28)

        showToolbarButton.setOnClickListener {
            toolbar.visibility = View.VISIBLE
            showToolbarButton.visibility = View.GONE
        }

        root.addView(showToolbarButton, showButtonParams)

        setContentView(root)
    }

    private fun addToolButton(
        symbol: String,
        action: () -> Unit
    ) {
        val button = Button(this)

        button.text = symbol
        button.textSize = 24f
        button.setTextColor(Color.WHITE)
        button.setBackgroundColor(Color.TRANSPARENT)
        button.setPadding(dp(8), 0, dp(8), 0)

        button.minWidth = 0
        button.minimumWidth = 0

        button.setOnClickListener {
            action()
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


// ======================================
// DRAWING VIEW
// ======================================

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
    private var currentTool = Tool.PEN

    private var previousX = 0f
    private var previousY = 0f

    private var gridVisible = false
    private var colorIndex = 0

    private val colors = listOf(
        Color.BLACK,
        Color.BLUE,
        Color.RED,
        Color.rgb(0, 150, 70),
        Color.rgb(130, 60, 180)
    )

    private val gridPaint = Paint().apply {
        color = Color.rgb(225, 225, 225)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private var currentPaint = createPenPaint()

    private fun createPenPaint(): Paint {
        return Paint().apply {
            color = colors[colorIndex]
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
    }

    init {
        setBackgroundColor(Color.WHITE)
        isFocusable = true
    }

    // Select pen or eraser
    fun setTool(tool: Tool) {
        currentTool = tool

        currentPaint = createPenPaint()

        if (tool == Tool.ERASER) {
            currentPaint.color = Color.WHITE
            currentPaint.strokeWidth = 30f
        }

        invalidate()
    }

    // Change pen colour
    fun changeColor() {
        colorIndex = (colorIndex + 1) % colors.size

        if (currentTool == Tool.PEN) {
            currentPaint = createPenPaint()
        }

        invalidate()
    }

    // Toggle grid
    fun toggleGrid() {
        gridVisible = !gridVisible
        invalidate()
    }

    // Undo
    fun undo() {
        if (strokes.isNotEmpty()) {
            undoneStrokes.add(strokes.removeAt(strokes.lastIndex))
            invalidate()
        }
    }

    // Redo
    fun redo() {
        if (undoneStrokes.isNotEmpty()) {
            strokes.add(undoneStrokes.removeAt(undoneStrokes.lastIndex))
            invalidate()
        }
    }

    // Clear entire page
    fun clearCanvas() {
        strokes.clear()
        undoneStrokes.clear()
        currentPath = null
        invalidate()
    }

    // Draw everything
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)

        // Draw grid
        if (gridVisible) {
            val spacing = 60f

            var x = 0f

            while (x < width) {
                canvas.drawLine(
                    x, 0f, x, height.toFloat(), gridPaint
                )
                x += spacing
            }

            var y = 0f

            while (y < height) {
                canvas.drawLine(
                    0f, y, width.toFloat(), y, gridPaint
                )
                y += spacing
            }
        }

        // Draw saved strokes
        for (stroke in strokes) {
            canvas.drawPath(stroke.path, stroke.paint)
        }

        // Draw current stroke
        currentPath?.let {
            canvas.drawPath(it, currentPaint)
        }
    }

    // Handle finger and stylus input
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x
        val y = event.y

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                currentPath = Path()

                currentPath?.moveTo(x, y)

                previousX = x
                previousY = y

                invalidate()

                return true
            }

            MotionEvent.ACTION_MOVE -> {

                currentPath?.quadTo(
                    previousX,
                    previousY,
                    (x + previousX) / 2f,
                    (y + previousY) / 2f
                )

                previousX = x
                previousY = y

                invalidate()

                return true
            }

            MotionEvent.ACTION_UP -> {

                currentPath?.lineTo(x, y)

                currentPath?.let {
                    strokes.add(
                        Stroke(
                            Path(it),
                            Paint(currentPaint)
                        )
                    )
                }

                currentPath = null

                // New drawing clears redo history
                undoneStrokes.clear()

                invalidate()

                performClick()

                return true
            }

            MotionEvent.ACTION_CANCEL -> {

                currentPath = null
                invalidate()

                return true
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
