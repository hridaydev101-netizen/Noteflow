package com.noteflow.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var toolbar: LinearLayout
    private lateinit var showToolbarButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(35, 35, 35))

        drawingView = DrawingView(this)

        val paperParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        paperParams.setMargins(dp(18), dp(24), dp(18), dp(24))
        root.addView(drawingView, paperParams)

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
        backParams.topMargin = dp(30)
        root.addView(backButton, backParams)

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

        toolbarParams.bottomMargin = dp(26)
        root.addView(toolbar, toolbarParams)

        addToolButton("✎") {
            drawingView.setTool(DrawingView.Tool.PEN)
        }

        addToolButton("◇") {
            drawingView.setTool(DrawingView.Tool.ERASER)
        }

        addToolButton("↶") {
            drawingView.undo()
        }

        addToolButton("↷") {
            drawingView.redo()
        }

        addToolButton("▦") {
            drawingView.toggleGrid()
        }

        addToolButton("●") {
            drawingView.changeColor()
        }

        addToolButton("⌫") {
            drawingView.clearCanvas()
        }

        addToolButton("⌄") {
            toolbar.visibility = View.GONE
            showToolbarButton.visibility = View.VISIBLE
        }

        showToolbarButton = ImageButton(this)
        showToolbarButton.setImageResource(android.R.drawable.arrow_down_float)
        showToolbarButton.setColorFilter(Color.BLACK)
        showToolbarButton.setBackgroundColor(Color.TRANSPARENT)
        showToolbarButton.visibility = View.GONE

        val showButtonParams = FrameLayout.LayoutParams(
            dp(58),
            dp(58),
            Gravity.BOTTOM or Gravity.END
        )

        showButtonParams.rightMargin = dp(24)
        showButtonParams.bottomMargin = dp(30)

        showToolbarButton.setOnClickListener {
            toolbar.visibility = View.VISIBLE
            showToolbarButton.visibility = View.GONE
        }

        root.addView(showToolbarButton, showButtonParams)

        setContentView(root)
    }

    private fun addToolButton(
        textValue: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = textValue
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

    init {
        setBackgroundColor(Color.WHITE)
    }

    private fun createPenPaint(): Paint {
        return Paint().apply {
            color = colors[colorIndex]
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
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
        }
    }

    fun setTool(tool: Tool) {
        currentTool = tool

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

        if (currentTool == Tool.PEN) {
            currentPaint = createPenPaint()
        }

        invalidate()
    }

    fun toggleGrid() {
        gridVisible = !gridVisible
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
            undoneStrokes.add(strokes.removeAt(strokes.lastIndex))
            invalidate()
        }
    }

    fun redo() {
        if (undoneStrokes.isNotEmpty()) {
            strokes.add(undoneStrokes.removeAt(undoneStrokes.lastIndex))
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)

        if (gridVisible) {
            drawGrid(canvas)
        }

        for (stroke in strokes) {
            canvas.drawPath(stroke.path, stroke.paint)
        }

        val path = currentPath
        if (path != null) {
            canvas.drawPath(path, currentPaint)
        }
    }

    private fun drawGrid(canvas: Canvas) {
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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath?.moveTo(x, y)

                previousX = x
                previousY = y
                undoneStrokes.clear()

                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val path = currentPath ?: return true

                if (abs(x - previousX) > 1f || abs(y - previousY) > 1f) {
                    val middleX = (previousX + x) / 2f
                    val middleY = (previousY + y) / 2f

                    path.quadTo(
                        previousX,
                        previousY,
                        middleX,
                        middleY
                    )

                    previousX = x
                    previousY = y

                    invalidate()
                }

                return true
            }

            MotionEvent.ACTION_UP -> {
                val path = currentPath

                if (path != null) {
                    path.lineTo(x, y)

                    strokes.add(
                        Stroke(
                            Path(path),
                            Paint(currentPaint)
                        )
                    )
                }

                currentPath = null
                invalidate()
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
}
