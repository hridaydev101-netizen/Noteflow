class DrawingView(context: Context) : View(context) {

    private val paths = mutableListOf<Pair<Path, Paint>>()

    private var currentPath = Path()
    private var currentPaint = createPenPaint()

    private val gridPaint = Paint().apply {
        color = 0xFFE0E0E0.toInt()
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var previousX = 0f
    private var previousY = 0f

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
                isDither = true
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
        currentPath.reset()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // White background
        canvas.drawColor(0xFFFFFFFF.toInt())

        // Draw grid
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

        // Draw completed paths
        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }

        // Draw the active path
        canvas.drawPath(currentPath, currentPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path()
                currentPath.moveTo(x, y)

                previousX = x
                previousY = y

                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val controlX = (x + previousX) / 2f
                val controlY = (y + previousY) / 2f

                currentPath.quadTo(
                    previousX,
                    previousY,
                    controlX,
                    controlY
                )

                previousX = x
                previousY = y

                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(x, y)

                // Store a copy of the paint so later changes do not affect
                // paths that have already been drawn.
                paths.add(
                    Pair(
                        Path(currentPath),
                        Paint(currentPaint)
                    )
                )

                currentPath.reset()
                invalidate()
                performClick()

                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                currentPath.reset()
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
