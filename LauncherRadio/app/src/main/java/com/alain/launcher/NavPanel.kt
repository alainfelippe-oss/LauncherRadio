package com.alain.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/** Panel derecho: estilo mapa nocturno. Al tocarlo se abre la app de navegación. */
class NavPanel(ctx: Context, attrs: AttributeSet?) : View(ctx, attrs) {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        p.style = Paint.Style.FILL
        p.color = 0xFF0B1418.toInt()
        c.drawRect(0f, 0f, w, h, p)

        // Cuadrícula de calles
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        p.color = 0xFF16262D.toInt()
        for (i in 1..8) {
            c.drawLine(0f, h * i / 9f, w, h * i / 9f, p)
            c.drawLine(w * i / 9f, 0f, w * i / 9f, h, p)
        }

        // Rotonda y autopista
        p.strokeWidth = 8f
        p.color = 0xFF14705F.toInt()
        c.drawCircle(w * 0.55f, h * 0.28f, w * 0.16f, p)
        c.drawLine(0f, h * 0.42f, w, h * 0.30f, p)

        // Avenida principal (azul)
        p.strokeWidth = 14f
        p.color = 0xFF1E50B4.toInt()
        c.drawLine(w * 0.40f, 0f, w * 0.40f, h, p)

        // Flecha de posición
        p.style = Paint.Style.FILL
        p.color = 0xFF2ECC40.toInt()
        path.reset()
        path.moveTo(w * 0.40f, h * 0.55f)
        path.lineTo(w * 0.40f + 22f, h * 0.55f + 56f)
        path.lineTo(w * 0.40f, h * 0.55f + 40f)
        path.lineTo(w * 0.40f - 22f, h * 0.55f + 56f)
        path.close()
        c.drawPath(path, p)

        // Degradado para fundir con el centro
        p.shader = LinearGradient(0f, 0f, w * 0.3f, 0f, Color.BLACK, 0x00000000, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w * 0.3f, h, p)
        p.shader = null

        // Texto
        p.color = 0xAAFFFFFF.toInt()
        p.textAlign = Paint.Align.CENTER
        p.textSize = h * 0.045f
        c.drawText("Toca para abrir el mapa", w * 0.55f, h * 0.80f, p)
    }
}
