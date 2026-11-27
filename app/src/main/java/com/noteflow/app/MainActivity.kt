class DrawingView(context: Context) : View(context) {

    private val paths = mutableListOf<Pair<Path, Paint>>()

    private var currentPath = Path()
    private var currentPaint = createPenPaint()

    private var previousX = 0f
    private var previousY = 0f

    private val gridPaint = Paint().apply {
        color = 0xFFE0E0E0.toInt()
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

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

        canvas.drawColor(0xFFFFFFFF.toInt())

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
                val controlX = (previousX + x) / 2f
                val controlY = (previousY + y) / 2f

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

                paths.add(
                    Pair(
                        Path(currentPath),
                        Paint(currentPaint)
                    )
                )

                currentPath.reset()
                invalidate()

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
}
