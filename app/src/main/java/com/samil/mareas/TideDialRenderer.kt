package com.samil.mareas

import android.graphics.*
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

object TideDialRenderer {

    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun render(size: Int, state: TideState): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        // Fondo gris muy claro y semitransparente (mas claro que el aro gris y un poco traslucido)
        canvas.drawColor(0xE0F2F2F2.toInt())

        val cx = size / 2f
        val cy = size / 2f
        val radius = size * 0.30f
        val ringStroke = size * 0.045f

        val ringRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        val grayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = ringStroke
            color = Color.parseColor("#D9D9D9")
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawArc(ringRect, 0f, 360f, false, grayPaint)

        val needleAngle: Float
        val arcStart: Float
        val arcSweep: Float
        val arcColorStart: Int
        val arcColorEnd: Int

        if (state.rising) {
            needleAngle = (180f + 180f * state.progressFraction).toFloat()
            arcStart = 180f
            arcSweep = (needleAngle - 180f)
            arcColorStart = Color.parseColor("#4FA9E8")
            arcColorEnd = Color.parseColor("#2C4FD6")
        } else {
            needleAngle = (180f * state.progressFraction).toFloat()
            arcStart = 0f
            arcSweep = needleAngle
            arcColorStart = Color.parseColor("#7A3FD1")
            arcColorEnd = Color.parseColor("#E5344A")
        }

        val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = ringStroke
            strokeCap = Paint.Cap.ROUND
            shader = SweepGradient(cx, cy, intArrayOf(arcColorStart, arcColorEnd), null).also {
                val matrix = Matrix()
                matrix.postRotate(arcStart - 90f, cx, cy)
                it.setLocalMatrix(matrix)
            }
        }
        if (arcSweep > 0.5f) {
            canvas.drawArc(ringRect, arcStart - 90f, arcSweep, false, sweepPaint)
        }

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = ringStroke * 0.6f
            strokeCap = Paint.Cap.ROUND
        }
        tickPaint.color = Color.parseColor("#3D7FD6")
        drawTick(canvas, cx, cy, radius, ringStroke, -90f, tickPaint)
        tickPaint.color = Color.parseColor("#D6303D")
        drawTick(canvas, cx, cy, radius, ringStroke, 90f, tickPaint)

        drawNeedle(canvas, cx, cy, radius * 0.72f, needleAngle - 90f)

        drawLabels(canvas, cx, cy, size, state)

        return bmp
    }

    private fun drawTick(
        canvas: Canvas, cx: Float, cy: Float, radius: Float, ringStroke: Float,
        angleDeg: Float, paint: Paint
    ) {
        val rad = Math.toRadians(angleDeg.toDouble())
        val outer = radius + ringStroke * 0.9f
        val inner = radius - ringStroke * 0.9f
        val x1 = cx + inner * cos(rad).toFloat()
        val y1 = cy + inner * sin(rad).toFloat()
        val x2 = cx + outer * cos(rad).toFloat()
        val y2 = cy + outer * sin(rad).toFloat()
        canvas.drawLine(x1, y1, x2, y2, paint)
    }

    private fun drawNeedle(canvas: Canvas, cx: Float, cy: Float, length: Float, angleDeg: Float) {
        val rad = Math.toRadians(angleDeg.toDouble())
        val tipX = cx + length * cos(rad).toFloat()
        val tipY = cy + length * sin(rad).toFloat()

        val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E24A2B")
            style = Paint.Style.FILL
        }

        val perpRad = rad + Math.PI / 2
        val baseHalfWidth = length * 0.10f
        val bx = cx + baseHalfWidth * cos(perpRad).toFloat()
        val by = cy + baseHalfWidth * sin(perpRad).toFloat()
        val bx2 = cx - baseHalfWidth * cos(perpRad).toFloat()
        val by2 = cy - baseHalfWidth * sin(perpRad).toFloat()

        val path = Path().apply {
            moveTo(tipX, tipY)
            lineTo(bx, by)
            lineTo(bx2, by2)
            close()
        }
        canvas.drawPath(path, needlePaint)

        val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E24A2B")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, length * 0.16f, hubPaint)
        val hubHole = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, length * 0.075f, hubHole)
    }

    private fun drawLabels(canvas: Canvas, cx: Float, cy: Float, size: Int, state: TideState) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.062f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.058f
            textAlign = Paint.Align.CENTER
        }
        val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.11f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val blue = Color.parseColor("#3D7FD6")
        val red = Color.parseColor("#D6303D")

        val highLabel = state.nextHighTide?.dateTime?.format(timeFmt) ?: "--:--"
        val lowLabel = state.nextLowTide?.dateTime?.format(timeFmt) ?: "--:--"

        titlePaint.color = blue
        canvas.drawText("pleamar", cx, size * 0.055f, titlePaint)
        timePaint.color = blue
        canvas.drawText("${highLabel}h", cx, size * 0.115f, timePaint)

        titlePaint.color = red
        canvas.drawText("bajamar", cx, size * 0.900f, titlePaint)
        timePaint.color = red
        canvas.drawText("${lowLabel}h", cx, size * 0.960f, timePaint)

        arrowPaint.color = blue
        canvas.drawText("↑", size * 0.10f, cy + size * 0.04f, arrowPaint)
        arrowPaint.color = red
        canvas.drawText("↓", size * 0.90f, cy + size * 0.04f, arrowPaint)
    }
}
