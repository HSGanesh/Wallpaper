package com.hsganesh.chalkboardwallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
            "Time Is An Illusion",
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

        private fun drawChalkDust(canvas: Canvas, w: Int, h: Int) {
            val random = Random(20260922L)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val count = (w * h / 1400)
            repeat(count) {
                val x = random.nextFloat() * w
                val y = random.nextFloat() * h
                val a = random.nextInt(6, 26)
                val r = if (random.nextFloat() < 0.85f) 1f else 1.8f
                paint.color = Color.argb(a, 200, 205, 210)
                canvas.drawCircle(x, y, r, paint)
            }
            // A handful of longer faint chalk smudge strokes for realism.
            val smudge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = h * 0.004f
                strokeCap = Paint.Cap.ROUND
                color = Color.argb(14, 210, 210, 210)
            }
            repeat(10) {
                val sx = random.nextFloat() * w
                val sy = random.nextFloat() * h
                val len = w * (0.05f + random.nextFloat() * 0.10f)
                canvas.drawLine(sx, sy, sx + len, sy + (random.nextFloat() - 0.5f) * 20f, smudge)
            }
        }

        private fun drawDiagrams(canvas: Canvas, w: Int, h: Int) {
            val dim = Color.argb(200, 180, 188, 196)
            ChalkDiagrams.drawSineWave(
                canvas, w * 0.80f, h * 0.045f, w * 0.30f, h * 0.06f, dim
            )
            ChalkDiagrams.drawMagneticField(
                canvas, w * 0.40f, h * 0.20f, w * 0.28f, h * 0.09f, dim
            )
            ChalkDiagrams.drawCircuit(
                canvas, w * 0.30f, h * 0.015f, w * 0.14f, h * 0.06f, dim
            )
            ChalkDiagrams.drawAtom(
                canvas, w * 0.42f, h * 0.755f, min(w, h) * 0.075f, dim
            )
            ChalkDiagrams.drawSphereAngles(
                canvas, w * 0.13f, h * 0.10f, min(w, h) * 0.09f, dim
            )
        }

        private fun drawFormulas(canvas: Canvas, w: Int, h: Int) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = formulaTypeface
                color = chalkWhite
            }
            for (item in FormulaBank.all()) {
                paint.alpha = item.alpha
                paint.textSize = spToPx(item.sizeSp) * (w / 1080f).coerceIn(0.85f, 1.6f)
                val lines = item.text.split("\n")
                canvas.save()
                val px = w * item.xFrac
                val py = h * item.yFrac
                canvas.rotate(item.rotationDeg, px, py)
                var ly = py
                for (line in lines) {
                    canvas.drawText(line, px, ly, paint)
                    ly += paint.textSize * 1.05f
                }
                canvas.restore()
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
