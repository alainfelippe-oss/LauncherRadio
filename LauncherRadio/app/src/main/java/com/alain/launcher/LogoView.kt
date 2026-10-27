package com.alain.launcher

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Pantalla de inicio: el logo aparece de golpe, se mantiene y se desvanece
 * para mostrar el tablero. Si pones "logo_hyundai.png" en res/drawable-nodpi/,
 * se usa esa imagen en lugar del logo dibujado.
 */
class LogoView(ctx: Context, attrs: AttributeSet?) : View(ctx, attrs) {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rect = RectF()
    private var t = 0f

    private val logoBmp: Bitmap? = run {
        val id = resources.getIdentifier("logo_hyundai", "drawable", ctx.packageName)
        if (id != 0) BitmapFactory.decodeResource(resources, id) else null
    }

    // Letra H: dos patas inclinadas y el travesaño en sentido contrario
    private val formas = arrayOf(
        floatArrayOf(-.42f, .62f, -.30f, .62f, -.16f, -.62f, -.28f, -.62f),
        floatArrayOf(.16f, .62f, .28f, .62f, .42f, -.62f, .30f, -.62f),
        floatArrayOf(-.28f, .02f, .28f, -.24f, .28f, -.10f, -.28f, .16f)
    )

    fun iniciar() {
        visibility = VISIBLE
        alpha = 1f
        val an = ValueAnimator.ofFloat(0f, 1f)
        an.duration = 2600
        an.addUpdateListener { v ->
            t = v.animatedValue as Float
            alpha = 1f - maxOf(0f, (t - 0.8f) / 0.2f)
            invalidate()
        }
        an.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(a: Animator) {
                visibility = GONE
            }
        })
        an.start()
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        c.drawColor(Color.BLACK)

        // Entrada rápida con rebote ("pam"), luego se mantiene fijo
        val a = minOf(t / 0.16f, 1f)
        val c1 = 1.70158f
        val e = 1f + (c1 + 1f) * (a - 1f) * (a - 1f) * (a - 1f) + c1 * (a - 1f) * (a - 1f)
        val rx = minOf(w * 0.30f, h * 0.45f * 1.6f) * (0.4f + 0.6f * e)
        val ry = rx / 1.6f
        val op = (255 * minOf(1f, a * 1.5f)).toInt()
        val cx = w / 2f
        val cy = h / 2f

        if (logoBmp != null) {
            val bh = rx * 2f * logoBmp.height / logoBmp.width
            rect.set(cx - rx, cy - bh / 2f, cx + rx, cy + bh / 2f)
            p.isFilterBitmap = true
            p.alpha = op
            c.drawBitmap(logoBmp, null, rect, p)
            return
        }

        // Óvalo
        p.style = Paint.Style.STROKE
        p.strokeWidth = ry * 0.17f
        p.color = Color.argb(op, 0xDD, 0xE1, 0xE6)
        rect.set(cx - rx * 0.92f, cy - ry * 0.92f, cx + rx * 0.92f, cy + ry * 0.92f)
        c.drawOval(rect, p)

        // H
        p.style = Paint.Style.FILL
        p.color = Color.argb(op, 255, 255, 255)
        for (f in formas) {
            path.reset()
            for (i in 0 until 4) {
                val px = cx + f[i * 2] * rx
                val py = cy + f[i * 2 + 1] * ry
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            c.drawPath(path, p)
        }
    }
}
