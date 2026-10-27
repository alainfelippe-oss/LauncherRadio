package com.alain.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Random

class EqView(ctx: Context, attrs: AttributeSet?) : View(ctx, attrs) {

    private val barras = 14
    private val segmentos = 8
    private val niveles = FloatArray(barras) { 0.05f }
    private var activo = false
    private val azar = Random()
    private val p = Paint()

    private val bucle = object : Runnable {
        override fun run() {
            for (i in 0 until barras) {
                val meta = if (activo) 0.15f + azar.nextFloat() * 0.85f else 0.05f
                niveles[i] += (meta - niveles[i]) * 0.5f
            }
            invalidate()
            postDelayed(this, 110)
        }
    }

    fun setActive(a: Boolean) {
        activo = a
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(bucle)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(bucle)
        super.onDetachedFromWindow()
    }

    private fun mezcla(a: Int, b: Int, t: Float): Int {
        val r = ((a shr 16 and 0xFF) + ((b shr 16 and 0xFF) - (a shr 16 and 0xFF)) * t).toInt()
        val g = ((a shr 8 and 0xFF) + ((b shr 8 and 0xFF) - (a shr 8 and 0xFF)) * t).toInt()
        val bl = ((a and 0xFF) + ((b and 0xFF) - (a and 0xFF)) * t).toInt()
        return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or bl
    }

    private fun color(t: Float): Int {
        val cian = 0x13C7C7
        val verde = 0x2ECC40
        val amarillo = 0xFFD60A
        val rojo = 0xFF2A1A
        return when {
            t < 0.35f -> mezcla(cian, verde, t / 0.35f)
            t < 0.70f -> mezcla(verde, amarillo, (t - 0.35f) / 0.35f)
            else -> mezcla(amarillo, rojo, (t - 0.70f) / 0.30f)
        }
    }

    override fun onDraw(c: Canvas) {
        val bw = width.toFloat() / barras
        val sh = height.toFloat() / segmentos
        for (i in 0 until barras) {
            val encendidos = Math.round(niveles[i] * segmentos)
            for (s in 0 until encendidos) {
                p.color = color(s / (segmentos - 1f))
                val top = height - (s + 1) * sh
                c.drawRect(i * bw + 2f, top + 1.5f, (i + 1) * bw - 2f, top + sh - 1.5f, p)
            }
        }
    }
}
