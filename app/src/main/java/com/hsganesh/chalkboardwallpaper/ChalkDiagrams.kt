package com.hsganesh.chalkboardwallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws simple hand-sketched physics/math diagrams directly with Canvas
 * primitives, in the same thin chalk-line style as the reference image.
 * Every diagram is positioned by fractional (x,y) coordinates of the
 * board so it scales cleanly to any screen size.
 */
object ChalkDiagrams {

    private fun linePaint(strokeWidth: Float, alpha: Int, color: Int): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            this.color = color
            this.alpha = alpha
            strokeCap = Paint.Cap.ROUND
        }

    fun drawSineWave(canvas: Canvas, cx: Float, cy: Float, w: Float, h: Float, color: Int) {
        val paint = linePaint(w * 0.006f, 200, color)
        // axes
        canvas.drawLine(cx - w / 2, cy, cx + w / 2, cy, paint)
        canvas.drawLine(cx - w / 2, cy + h / 2, cx - w / 2, cy - h / 2, paint)
        val path = Path()
        val amp = h * 0.38f
        var first = true
        var x = -w / 2
        while (x <= w / 2) {
            val theta = (x / w) * 4 * Math.PI
            val y = -amp * sin(theta).toFloat()
            if (first) {
                path.moveTo(cx + x, cy + y); first = false
            } else path.lineTo(cx + x, cy + y)
            x += w / 60f
        }
        val wavePaint = linePaint(w * 0.01f, 220, color)
        canvas.drawPath(path, wavePaint)
    }

    fun drawAtom(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = linePaint(r * 0.03f, 210, color)
        // nucleus
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = r * 0.05f
            this.color = color
            alpha = 230
        }
        canvas.drawCircle(cx, cy, r * 0.10f, fillPaint)
        val orbitRects = listOf(0f, 60f, 120f)
        for (angle in orbitRects) {
            canvas.save()
            canvas.rotate(angle, cx, cy)
            val oval = RectF(cx - r, cy - r * 0.38f, cx + r, cy + r * 0.38f)
            canvas.drawOval(oval, paint)
            canvas.restore()
        }
        // electrons
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = color
            alpha = 230
        }
        val positions = listOf(
            Pair(0.0, 0f), Pair(120.0, 60f), Pair(240.0, 120f)
        )
        for ((deg, rot) in positions) {
            val rad = Math.toRadians(deg)
            val ex = (cx + r * cos(rad)).toFloat()
            val ey = (cy + r * 0.38f * sin(rad)).toFloat()
            canvas.save()
            canvas.rotate(rot, cx, cy)
            canvas.drawCircle(ex, ey, r * 0.045f, dotPaint)
            canvas.restore()
        }
    }

    fun drawMagneticField(canvas: Canvas, cx: Float, cy: Float, w: Float, h: Float, color: Int) {
        val paint = linePaint(w * 0.01f, 190, color)
        val loops = 3
        for (i in 0 until loops) {
            val dx = (i - loops / 2) * w * 0.22f
            canvas.drawOval(RectF(cx + dx - w * 0.14f, cy - h / 2, cx + dx + w * 0.14f, cy + h / 2), paint)
        }
        // arrowheads roughly at top
        val arrow = linePaint(w * 0.008f, 190, color)
        canvas.drawLine(cx - w * 0.35f, cy - h * 0.05f, cx - w * 0.30f, cy - h * 0.14f, arrow)
        canvas.drawLine(cx + w * 0.35f, cy - h * 0.05f, cx + w * 0.30f, cy - h * 0.14f, arrow)
    }

    fun drawCircuit(canvas: Canvas, left: Float, top: Float, w: Float, h: Float, color: Int) {
        val paint = linePaint(w * 0.018f, 200, color)
        val right = left + w
        val bottom = top + h
        // rectangle loop
        canvas.drawLine(left, top, right, top, paint)
        canvas.drawLine(right, top, right, bottom, paint)
        canvas.drawLine(right, bottom, left, bottom, paint)
        canvas.drawLine(left, bottom, left, top, paint)
        // AC source circle on the left mid
        val midY = (top + bottom) / 2
        canvas.drawCircle(left, midY, w * 0.10f, paint)
        val sinePath = Path()
        sinePath.moveTo(left - w * 0.06f, midY)
        sinePath.cubicTo(
            left - w * 0.03f, midY - w * 0.08f,
            left + w * 0.0f, midY + w * 0.08f,
            left + w * 0.06f, midY
        )
        canvas.drawPath(sinePath, paint)
        // resistor zigzag on the right side
        val zigTop = top + h * 0.15f
        val zigBottom = bottom - h * 0.15f
        val zig = Path()
        zig.moveTo(right, zigTop)
        val steps = 6
        for (i in 1..steps) {
            val yy = zigTop + (zigBottom - zigTop) * i / steps
            val xx = right + if (i % 2 == 0) w * 0.05f else -w * 0.05f
            zig.lineTo(xx, yy)
        }
        canvas.drawPath(zig, paint)
    }

    fun drawSphereAngles(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = linePaint(r * 0.02f, 190, color)
        canvas.drawCircle(cx, cy, r, paint)
        canvas.drawOval(RectF(cx - r, cy - r * 0.32f, cx + r, cy + r * 0.32f), paint)
        canvas.drawLine(cx - r, cy, cx + r, cy, paint)
        canvas.drawLine(cx, cy - r, cx, cy + r, paint)
        // slanted radius line for theta/phi
        canvas.drawLine(cx, cy, cx + r * 0.75f, cy - r * 0.55f, paint)
        canvas.drawLine(cx, cy, cx - r * 0.6f, cy + r * 0.25f, paint)
    }

    /** A coordinate system with a parabola-like curve plotted on it. */
    fun drawCoordCurve(canvas: Canvas, cx: Float, cy: Float, w: Float, h: Float, color: Int) {
        val paint = linePaint(w * 0.008f, 190, color)
        canvas.drawLine(cx - w / 2, cy, cx + w / 2, cy, paint)
        canvas.drawLine(cx, cy + h / 2, cx, cy - h / 2, paint)
        val path = Path()
        var first = true
        var x = -w / 2
        while (x <= w / 2) {
            val t = x / (w / 2)
            val y = -h * 0.4f * (t * t)
            if (first) {
                path.moveTo(cx + x, cy + y); first = false
            } else path.lineTo(cx + x, cy + y)
            x += w / 60f
        }
        canvas.drawPath(path, linePaint(w * 0.012f, 210, color))
    }

    /** A small bundle of vector arrows from a common origin. */
    fun drawVectors(canvas: Canvas, cx: Float, cy: Float, s: Float, color: Int) {
        val paint = linePaint(s * 0.05f, 210, color)
        val tips = listOf(
            Pair(cx + s, cy - s * 0.3f),
            Pair(cx + s * 0.2f, cy - s),
            Pair(cx + s * 1.05f, cy - s * 1.1f)
        )
        for ((tx, ty) in tips) {
            canvas.drawLine(cx, cy, tx, ty, paint)
            val ang = kotlin.math.atan2((ty - cy).toDouble(), (tx - cx).toDouble())
            for (da in listOf(0.5, -0.5)) {
                val ax = (tx - s * 0.18f * cos(ang + da)).toFloat()
                val ay = (ty - s * 0.18f * sin(ang + da)).toFloat()
                canvas.drawLine(tx, ty, ax, ay, paint)
            }
        }
    }

    /** A simple right-triangle geometry sketch with a right-angle mark. */
    fun drawTriangleGeo(canvas: Canvas, cx: Float, cy: Float, s: Float, color: Int) {
        val paint = linePaint(s * 0.035f, 200, color)
        val p1x = cx - s; val p1y = cy + s * 0.5f
        val p2x = cx + s; val p2y = cy + s * 0.5f
        val p3x = cx - s; val p3y = cy - s * 0.6f
        val path = Path()
        path.moveTo(p1x, p1y)
        path.lineTo(p2x, p2y)
        path.lineTo(p3x, p3y)
        path.close()
        canvas.drawPath(path, paint)
        canvas.drawRect(
            RectF(p1x, p1y - s * 0.12f, p1x + s * 0.12f, p1y),
            linePaint(s * 0.03f, 200, color)
        )
    }

    /** A small bar-chart / histogram sketch. */
    fun drawBarChart(canvas: Canvas, left: Float, top: Float, w: Float, h: Float, color: Int) {
        val paint = linePaint(w * 0.012f, 200, color)
        val bottom = top + h
        canvas.drawLine(left, top, left, bottom, paint)
        canvas.drawLine(left, bottom, left + w, bottom, paint)
        val heights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.3f)
        val bw = w / (heights.size * 1.6f)
        for ((i, hh) in heights.withIndex()) {
            val x0 = left + w * 0.08f + i * (bw * 1.6f)
            val y0 = bottom - h * hh * 0.85f
            canvas.drawRect(RectF(x0, y0, x0 + bw, bottom), paint)
        }
    }
}
