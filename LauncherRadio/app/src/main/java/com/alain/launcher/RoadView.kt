package com.alain.launcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.LinearGradient
import android.util.AttributeSet
import android.view.View

/**
 * Carretera en perspectiva con el auto de espaldas.
 * Si pones una imagen "auto.png" en res/drawable-nodpi/, se usa en lugar del auto dibujado.
 */
class RoadView(ctx: Context, attrs: AttributeSet?) : View(ctx, attrs) {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rect = RectF()
    private val autoBmp: Bitmap? = run {
        val id = resources.getIdentifier("auto", "drawable", ctx.packageName)
        if (id != 0) BitmapFactory.decodeResource(resources, id) else null
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val arriba = h * 0.50f
        val abajo = h * 0.86f

        // Calzada
        path.reset()
        path.moveTo(w * 0.43f, arriba)
        path.lineTo(w * 0.57f, arriba)
        path.lineTo(w, abajo)
        path.lineTo(0f, abajo)
        path.close()
        p.style = Paint.Style.FILL
        p.shader = LinearGradient(0f, arriba, 0f, abajo, 0x00202020, 0xFF303030.toInt(), Shader.TileMode.CLAMP)
        c.drawPath(path, p)
        p.shader = null

        // Línea central discontinua
        p.color = 0xCCFFFFFF.toInt()
        for (i in 0 until 6) {
            val t0 = i / 6f + 0.04f
            val t1 = t0 + 0.09f
            val y0 = arriba + (abajo - arriba) * t0 * t0
            val y1 = arriba + (abajo - arriba) * t1 * t1
            val ancho = 2f + 9f * t0
            c.drawRect(w / 2f - ancho / 2f, y0, w / 2f + ancho / 2f, y1, p)
        }

        // Luz de los faros
        p.shader = RadialGradient(
            w / 2f, arriba + (abajo - arriba) * 0.35f, w * 0.35f,
            0x66FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP
        )
        c.drawCircle(w / 2f, arriba + (abajo - arriba) * 0.35f, w * 0.35f, p)
        p.shader = null

        // Auto
        val bmp = autoBmp
        val cw = if (bmp != null) w * 0.46f else w * 0.34f
        val ch = if (bmp != null) cw * bmp.height / bmp.width else cw * 1.1f
        val l = (w - cw) / 2f
        val t = abajo - ch
        if (bmp != null) {
            p.alpha = 255
            p.isFilterBitmap = true
            rect.set(l, t, l + cw, t + ch)
            c.drawBitmap(bmp, null, rect, p)
            // Brillo rojo de las luces traseras
            for (fx in floatArrayOf(0.115f, 0.885f)) {
                val gx = l + cw * fx
                val gy = t + ch * 0.50f
                p.shader = RadialGradient(gx, gy, cw * 0.22f, 0x99FF2828.toInt(), 0x00FF2828, Shader.TileMode.CLAMP)
                c.drawCircle(gx, gy, cw * 0.22f, p)
            }
            p.shader = null
        } else {
            dibujarAuto(c, l, t, cw, ch)
        }
    }

    private fun dibujarAuto(c: Canvas, l: Float, t: Float, cw: Float, ch: Float) {
        val r = l + cw
        val b = t + ch
        val m = l + cw / 2f
        p.style = Paint.Style.FILL

        // Brillo de las luces traseras
        for (px in floatArrayOf(l + cw * .13f, r - cw * .13f)) {
            p.shader = RadialGradient(px, t + ch * .6f, cw * .35f, 0x8CFF2828.toInt(), 0x00FF2828, Shader.TileMode.CLAMP)
            c.drawCircle(px, t + ch * .6f, cw * .4f, p)
        }
        p.shader = null

        // Ruedas
        p.color = 0xFF050505.toInt()
        rect.set(l - cw * .02f, b - ch * .2f, l + cw * .08f, b)
        c.drawRect(rect, p)
        rect.set(r - cw * .08f, b - ch * .2f, r + cw * .02f, b)
        c.drawRect(rect, p)

        // Carrocería roja y espejos
        p.shader = LinearGradient(0f, t, 0f, b, 0xFFD7192F.toInt(), 0xFF7D0B1C.toInt(), Shader.TileMode.CLAMP)
        rect.set(l, t + ch * .14f, r, t + ch * .92f)
        c.drawRoundRect(rect, cw * .12f, cw * .12f, p)
        rect.set(l - cw * .05f, t + ch * .26f, l + cw * .05f, t + ch * .36f)
        c.drawRoundRect(rect, cw * .03f, cw * .03f, p)
        rect.set(r - cw * .05f, t + ch * .26f, r + cw * .05f, t + ch * .36f)
        c.drawRoundRect(rect, cw * .03f, cw * .03f, p)

        // Techo
        p.shader = LinearGradient(0f, t, 0f, t + ch * .3f, 0xFFEA3A4D.toInt(), 0xFFC4162B.toInt(), Shader.TileMode.CLAMP)
        rect.set(l + cw * .14f, t, r - cw * .14f, t + ch * .30f)
        c.drawRoundRect(rect, cw * .1f, cw * .1f, p)
        p.shader = null

        // Luneta
        path.reset()
        path.moveTo(l + cw * .17f, t + ch * .20f)
        path.lineTo(r - cw * .17f, t + ch * .20f)
        path.lineTo(r - cw * .10f, t + ch * .50f)
        path.lineTo(l + cw * .10f, t + ch * .50f)
        path.close()
        p.color = 0xFF0A141B.toInt()
        c.drawPath(path, p)

        // Luces traseras encendidas
        p.color = 0xFFFF2B2B.toInt()
        rect.set(l + cw * .03f, t + ch * .52f, l + cw * .20f, t + ch * .72f)
        c.drawRoundRect(rect, cw * .03f, cw * .03f, p)
        rect.set(r - cw * .20f, t + ch * .52f, r - cw * .03f, t + ch * .72f)
        c.drawRoundRect(rect, cw * .03f, cw * .03f, p)
        p.color = 0xFFFFB3B3.toInt()
        rect.set(l + cw * .06f, t + ch * .56f, l + cw * .11f, t + ch * .68f)
        c.drawRect(rect, p)
        rect.set(r - cw * .11f, t + ch * .56f, r - cw * .06f, t + ch * .68f)
        c.drawRect(rect, p)

        // Emblema
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        p.color = 0xFFE8E8E8.toInt()
        rect.set(m - cw * .07f, t + ch * .6f - cw * .045f, m + cw * .07f, t + ch * .6f + cw * .045f)
        c.drawOval(rect, p)

        // Paragolpes y placa
        p.style = Paint.Style.FILL
        p.color = 0xFF4A0813.toInt()
        rect.set(l + cw * .02f, t + ch * .76f, r - cw * .02f, t + ch * .92f)
        c.drawRoundRect(rect, cw * .05f, cw * .05f, p)
        p.color = 0xFFEEEEEE.toInt()
        rect.set(m - cw * .15f, t + ch * .78f, m + cw * .15f, t + ch * .86f)
        c.drawRect(rect, p)
    }
}
