package com.hsganesh.chalkboardwallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.TypedValue
import android.view.SurfaceHolder
import androidx.core.content.res.ResourcesCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min
import kotlin.random.Random

class ChalkboardWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ChalkboardEngine()

    private inner class ChalkboardEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var destroyed = false

        private var surfaceWidth = 0
        private var surfaceHeight = 0

        // Cached static layer: background + dust + formulas + diagrams.
        // Regenerated only when the surface size changes, so the per-second
        // redraw (for the clock) is cheap.
        private var staticLayer: Bitmap? = null
        private var staticLayerWidth = 0

        private var xOffset = 0f

        private lateinit var clockTypeface: Typeface
        private lateinit var formulaTypeface: Typeface

        private val boardBg = Color.parseColor("#0D1117")
        private val chalkWhite = Color.parseColor("#E9EEF4")
        private val chalkDim = Color.parseColor("#8B98A5")

        private val captions = listOf(
            "Good Things Take Time",
            "Discipline Creates Freedom",
            "A Better Version Of Me",
            "Same Mindset, Different Results",
            "Small Steps, Every Day",
            "Stay Consistent"
        )

        private val drawRunnable = Runnable { drawFrame() }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            clockTypeface = ResourcesCompat.getFont(applicationContext, R.font.chalk_clock)
                ?: Typeface.DEFAULT
            formulaTypeface = ResourcesCompat.getFont(applicationContext, R.font.chalk_formula)
                ?: Typeface.DEFAULT
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
            buildStaticLayer()
            scheduleNextFrame(0)
        }

        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float,
            xOffsetStep: Float, yOffsetStep: Float,
            xPixelOffset: Int, yPixelOffset: Int
        ) {
            this.xOffset = xOffset
            drawFrame()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                scheduleNextFrame(0)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            destroyed = true
            visible = false
            handler.removeCallbacks(drawRunnable)
            staticLayer?.recycle()
            staticLayer = null
        }

        private fun scheduleNextFrame(delayMillis: Long) {
            handler.removeCallbacks(drawRunnable)
            if (visible && !destroyed) {
                handler.postDelayed(drawRunnable, delayMillis)
            }
        }

        private fun spToPx(sp: Float): Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics
        )

        /** Builds the (mostly) static background: board, chalk dust, formulas, diagrams. */
        private fun buildStaticLayer() {
            if (surfaceWidth <= 0 || surfaceHeight <= 0) return
            staticLayer?.recycle()

            // Slightly wider than the screen so we can pan it horizontally
            // for a subtle home-screen parallax effect.
            staticLayerWidth = (surfaceWidth * 1.18f).toInt()
            val bmp = Bitmap.createBitmap(staticLayerWidth, surfaceHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(boardBg)

            drawChalkDust(canvas, staticLayerWidth, surfaceHeight)
            drawDiagrams(canvas, staticLayerWidth, surfaceHeight)
            drawFormulas(canvas, staticLayerWidth, surfaceHeight)
            drawVignette(canvas, staticLayerWidth, surfaceHeight)

            staticLayer = bmp
        }

        /**
         * Very subtle board texture: a handful of large soft smudges (like
         * chalk rubbed and re-used over time) plus a few faint scratch
         * strokes. Deliberately NOT a field of small dots — that reads as
         * a starfield rather than a chalkboard.
         */
        private fun drawChalkDust(canvas: Canvas, w: Int, h: Int) {
            val random = Random(20260922L)
            repeat(12) {
                val cx = random.nextFloat() * w
                val cy = random.nextFloat() * h
                val r = w * (0.05f + random.nextFloat() * 0.09f)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    shader = android.graphics.RadialGradient(
                        cx, cy, r,
                        intArrayOf(Color.argb(random.nextInt(5, 11), 90, 94, 100), Color.TRANSPARENT),
                        floatArrayOf(0f, 1f),
                        android.graphics.Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(cx, cy, r, paint)
            }
            val scratch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                strokeCap = Paint.Cap.ROUND
                color = Color.argb(20, 100, 104, 110)
            }
            repeat(6) {
                val sx = random.nextFloat() * w
                val sy = random.nextFloat() * h
                val len = w * (0.06f + random.nextFloat() * 0.10f)
                val angle = (random.nextFloat() - 0.5f) * 0.6f
                canvas.drawLine(
                    sx, sy,
                    sx + len * kotlin.math.cos(angle.toDouble()).toFloat(),
                    sy + len * kotlin.math.sin(angle.toDouble()).toFloat(),
                    scratch
                )
            }
        }

        /** Fractional (x0,x1,y0,y1) zones reserved for hand-sketched diagrams. */
        private val diagramZones = listOf(
            floatArrayOf(0.56f, 1.00f, 0.00f, 0.10f), // sine wave, top right
            floatArrayOf(0.20f, 0.50f, 0.09f, 0.21f), // magnetic field
            floatArrayOf(0.00f, 0.16f, 0.05f, 0.15f), // circuit, top left
            floatArrayOf(0.75f, 1.00f, 0.19f, 0.38f), // atom
            floatArrayOf(0.00f, 0.20f, 0.14f, 0.30f), // sphere angles
            floatArrayOf(0.72f, 1.00f, 0.58f, 0.74f), // coordinate curve
            floatArrayOf(0.00f, 0.20f, 0.53f, 0.68f), // vector bundle
            floatArrayOf(0.75f, 1.00f, 0.79f, 0.93f), // triangle geometry
            floatArrayOf(0.00f, 0.16f, 0.80f, 0.94f)  // bar chart
        )

        private fun drawDiagrams(canvas: Canvas, w: Int, h: Int) {
            val dim = Color.argb(195, 172, 180, 188)
            val minWH = min(w, h)
            ChalkDiagrams.drawSineWave(canvas, w * 0.735f, h * 0.040f, w * 0.28f, h * 0.038f, dim)
            ChalkDiagrams.drawMagneticField(canvas, w * 0.355f, h * 0.145f, w * 0.22f, h * 0.05f, dim)
            ChalkDiagrams.drawCircuit(canvas, w * 0.045f, h * 0.075f, w * 0.15f, h * 0.045f, dim)
            ChalkDiagrams.drawAtom(canvas, w * 0.865f, h * 0.275f, minWH * 0.065f, dim)
            ChalkDiagrams.drawSphereAngles(canvas, w * 0.11f, h * 0.225f, minWH * 0.062f, dim)
            ChalkDiagrams.drawCoordCurve(canvas, w * 0.855f, h * 0.655f, w * 0.19f, h * 0.075f, dim)
            ChalkDiagrams.drawVectors(canvas, w * 0.10f, h * 0.605f, w * 0.12f, dim)
            ChalkDiagrams.drawTriangleGeo(canvas, w * 0.86f, h * 0.845f, minWH * 0.055f, dim)
            ChalkDiagrams.drawBarChart(canvas, w * 0.045f, h * 0.845f, w * 0.15f, h * 0.045f, dim)
        }

        private fun inZone(xf: Float, yf: Float, zones: List<FloatArray>): Boolean {
            for (z in zones) {
                if (xf in z[0]..z[1] && yf in z[2]..z[3]) return true
            }
            return false
        }

        /**
         * Densely fills the board with formulas using a jittered grid: a
         * regular grid of cells, each nudged randomly and populated (or
         * skipped) so the layout reads as genuinely hand-filled rather than
         * a template, while a lightweight overlap check keeps neighbouring
         * lines from colliding into unreadable mush.
         */
        private fun drawFormulas(canvas: Canvas, w: Int, h: Int) {
            val random = Random(7L)
            val pool = FormulaBank.pool.shuffled(Random(7L))
            var poolIndex = 0
            fun nextText(): String {
                val t = pool[poolIndex % pool.size]
                poolIndex++
                return t
            }

            val cols = 7
            val rows = 13
            val marginX = 0.05f
            val clockX0 = 0.16f; val clockX1 = 0.84f
            val clockY0 = 0.375f; val clockY1 = 0.605f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = formulaTypeface }
            val occupied = ArrayList<RectF>()
            val bounds = Rect()

            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val xf = marginX + (col + 0.5f) / cols * (1 - 2 * marginX) +
                        (random.nextFloat() - 0.5f) * 0.05f
                    val yf = (row + 0.5f) / rows + (random.nextFloat() - 0.5f) * 0.032f

                    if (xf in clockX0..clockX1 && yf in clockY0..clockY1) continue
                    if (inZone(xf, yf, diagramZones)) continue
                    if (random.nextFloat() < 0.06f) continue

                    val isHero = row != 0 && row != rows - 1 && random.nextFloat() < 0.045f
                    val text = if (isHero) FormulaBank.heroPool[random.nextInt(FormulaBank.heroPool.size)] else nextText()
                    val sizeFrac = if (isHero) {
                        0.020f + random.nextFloat() * 0.005f
                    } else {
                        0.0110f + random.nextFloat() * 0.0055f
                    }
                    paint.textSize = h * sizeFrac
                    paint.alpha = if (isHero) 255 else 160 + random.nextInt(75)
                    paint.color = chalkWhite

                    val px = w * xf
                    val py = h * yf
                    paint.getTextBounds(text, 0, text.length, bounds)
                    val tw = bounds.width().toFloat()
                    val th = bounds.height().toFloat()

                    val candidate = RectF(px, py - th, px + tw, py + th * 0.3f)
                    var collides = false
                    for (r in occupied) {
                        if (RectF.intersects(candidate, r)) { collides = true; break }
                    }
                    if (collides) continue
                    occupied.add(candidate)

                    val rot = (random.nextFloat() - 0.5f) * 12f
                    canvas.save()
                    canvas.rotate(rot, px, py)
                    canvas.drawText(text, px, py, paint)
                    canvas.restore()
                }
            }
        }

        private fun drawVignette(canvas: Canvas, w: Int, h: Int) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = android.graphics.RadialGradient(
                    w / 2f, h / 2f, min(w, h) * 0.85f,
                    intArrayOf(Color.argb(0, 0, 0, 0), Color.argb(90, 0, 0, 0)),
                    floatArrayOf(0.6f, 1f),
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    render(canvas)
                }
            } catch (_: Exception) {
                // Surface may be gone; ignore and let the schedule loop stop.
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (_: Exception) {
                    }
                }
            }
            if (visible && !destroyed) {
                // Align next redraw to the top of the next second so the
                // clock ticks precisely, like a real clock.
                val msToNextSecond = 1000L - (System.currentTimeMillis() % 1000L)
                scheduleNextFrame(msToNextSecond)
            }
        }

        private fun render(canvas: Canvas) {
            val layer = staticLayer
            if (layer == null) {
                canvas.drawColor(boardBg)
            } else {
                val maxShift = (staticLayerWidth - surfaceWidth).toFloat().coerceAtLeast(0f)
                val shift = maxShift * xOffset
                val src = Rect(shift.toInt(), 0, (shift + surfaceWidth).toInt(), surfaceHeight)
                val dst = Rect(0, 0, surfaceWidth, surfaceHeight)
                canvas.drawBitmap(layer, src, dst, null)
            }
            drawClock(canvas)
        }

        private fun drawClock(canvas: Canvas) {
            val cal = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val timeText = timeFormat.format(cal.time)
            val dateText = dateFormat.format(cal.time).uppercase(Locale.getDefault())

            val cx = surfaceWidth / 2f
            val cy = surfaceHeight * 0.47f

            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = formulaTypeface
                color = chalkDim
                textAlign = Paint.Align.CENTER
                textSize = spToPx(20f) * (surfaceWidth / 1080f).coerceIn(0.85f, 1.6f)
                alpha = 235
            }
            canvas.drawText(dateText, cx, cy - spToPx(46f), datePaint)

            val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = clockTypeface
                color = chalkWhite
                textAlign = Paint.Align.CENTER
                textSize = surfaceWidth * 0.19f
                alpha = 255
            }
            // Soft glow behind the clock so it reads like bright chalk under
            // ambient light, and to keep it legible over the busy board.
            clockPaint.setShadowLayer(surfaceWidth * 0.02f, 0f, 0f, Color.argb(120, 255, 255, 255))
            canvas.drawText(timeText, cx, cy, clockPaint)
            clockPaint.clearShadowLayer()

            // Underline, hand-drawn feel (slightly irregular).
            val underlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = surfaceWidth * 0.006f
                strokeCap = Paint.Cap.ROUND
                color = chalkWhite
                alpha = 200
            }
            val underlineY = cy + surfaceWidth * 0.045f
            val halfW = surfaceWidth * 0.20f
            val path = android.graphics.Path()
            path.moveTo(cx - halfW, underlineY)
            path.quadTo(cx, underlineY + surfaceWidth * 0.006f, cx + halfW, underlineY - surfaceWidth * 0.004f)
            canvas.drawPath(path, underlinePaint)

            val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = formulaTypeface
                color = chalkDim
                textAlign = Paint.Align.CENTER
                textSize = spToPx(20f) * (surfaceWidth / 1080f).coerceIn(0.85f, 1.6f)
                alpha = 220
            }
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            val caption = captions[dayOfYear % captions.size]
            canvas.drawText(caption, cx, underlineY + spToPx(34f), captionPaint)
        }
    }
}
