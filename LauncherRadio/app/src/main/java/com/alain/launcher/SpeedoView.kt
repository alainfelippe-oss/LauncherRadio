package com.alain.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import java.util.Locale

class SpeedoView(ctx: Context, attrs: AttributeSet?) : View(ctx, attrs) {

    private val maxKmh = 180f
    private val inicio = 125f
    private val barrido = 215f
    private var objetivo = 0f
    private var mostrado = 0f
    private var tripMs = 0L
    private var tripM = 0f

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val path = Path()
    private val negrita = Typeface.DEFAULT_BOLD
    private val cursiva = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)

    fun setSpeed(kmh: Float) {
        objetivo = kmh.coerceIn(0f, maxKmh)
        invalidate()
    }

    fun setTrip(ms: Long, metros: Float) {
        tripMs = ms
        tripM = metros
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val r = minOf(w * 0.40f, h * 0.34f)
        val cx = w * 0.5f
        val cy = h * 0.47f

        // Arco de colores
        rect.set(cx - r, cy - r, cx + r, cy + r)
        p.style = Paint.Style.STROKE
        p.strokeWidth = r * 0.035f
        p.typeface = Typeface.DEFAULT
        p.color = 0xFF1E5BFF.toInt()
        c.drawArc(rect, inicio, barrido * 0.60f, false, p)
        p.color = 0xFF1FA52E.toInt()
        c.drawArc(rect, inicio + barrido * 0.60f, barrido * 0.25f, false, p)
        p.color = 0xFFE01B1B.toInt()
        c.drawArc(rect, inicio + barrido * 0.85f, barrido * 0.15f, false, p)

        // Marcas y números
        p.textAlign = Paint.Align.CENTER
        p.textSize = r * 0.11f
        var v = 0
        while (v <= maxKmh.toInt()) {
            val a = Math.toRadians((inicio + barrido * v / maxKmh).toDouble())
            val co = Math.cos(a).toFloat()
            val si = Math.sin(a).toFloat()
            val mayor = v % 20 == 0
            val largo = if (mayor) r * 0.12f else r * 0.06f
            p.style = Paint.Style.STROKE
            p.color = Color.WHITE
            p.strokeWidth = if (mayor) 3f else 1.5f
            c.drawLine(
                cx + co * r * 0.94f, cy + si * r * 0.94f,
                cx + co * (r * 0.94f - largo), cy + si * (r * 0.94f - largo), p
            )
            if (mayor) {
                p.style = Paint.Style.FILL
                c.drawText(v.toString(), cx + co * r * 0.72f, cy + si * r * 0.72f + p.textSize / 3f, p)
            }
            v += 10
        }

        // Reloj analógico pequeño
        val dx = cx
        val dy = cy - r * 0.40f
        val dr = r * 0.24f
        p.style = Paint.Style.FILL
        p.color = 0xFF0B0B0B.toInt()
        c.drawCircle(dx, dy, dr, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        p.color = 0xFF555555.toInt()
        c.drawCircle(dx, dy, dr, p)
        p.color = Color.WHITE
        for (i in 0 until 12) {
            val a = Math.toRadians(i * 30.0)
            val s = Math.sin(a).toFloat()
            val k = Math.cos(a).toFloat()
            c.drawLine(dx + s * dr * 0.82f, dy - k * dr * 0.82f, dx + s * dr * 0.95f, dy - k * dr * 0.95f, p)
        }
        val cal = Calendar.getInstance()
        val min = cal.get(Calendar.MINUTE)
        val hora = cal.get(Calendar.HOUR)
        val ah = Math.toRadians((hora + min / 60.0) * 30.0)
        p.strokeWidth = 4f
        p.color = 0xFFFFC107.toInt()
        c.drawLine(dx, dy, dx + Math.sin(ah).toFloat() * dr * 0.5f, dy - Math.cos(ah).toFloat() * dr * 0.5f, p)
        val am = Math.toRadians(min * 6.0)
        p.strokeWidth = 3f
        p.color = Color.WHITE
        c.drawLine(dx, dy, dx + Math.sin(am).toFloat() * dr * 0.8f, dy - Math.cos(am).toFloat() * dr * 0.8f, p)

        // Aguja
        val an = Math.toRadians((inicio + barrido * mostrado / maxKmh).toDouble())
        val nc = Math.cos(an).toFloat()
        val ns = Math.sin(an).toFloat()
        val px = -ns
        val py = nc
        val hw = r * 0.03f
        path.reset()
        path.moveTo(cx + nc * r * 0.92f, cy + ns * r * 0.92f)
        path.lineTo(cx + px * hw - nc * r * 0.12f, cy + py * hw - ns * r * 0.12f)
        path.lineTo(cx - px * hw - nc * r * 0.12f, cy - py * hw - ns * r * 0.12f)
        path.close()
        p.style = Paint.Style.FILL
        p.color = 0xFFE01B1B.toInt()
        c.drawPath(path, p)
        p.color = 0xFF777777.toInt()
        c.drawCircle(cx, cy, r * 0.07f, p)
        p.color = 0xFF222222.toInt()
        c.drawCircle(cx, cy, r * 0.04f, p)

        // "Km/H"
        p.color = Color.WHITE
        p.textAlign = Paint.Align.LEFT
        p.textSize = r * 0.10f
        p.typeface = cursiva
        c.drawText("Km/H", cx - r * 0.72f, cy + r * 0.14f, p)

        // Velocidad digital (3 casillas)
        val bl = cx - r * 0.36f
        val bt = cy + r * 0.28f
        val bw = r * 0.72f
        val bh = r * 0.30f
        val casilla = bw / 3f
        val num = String.format(Locale.US, "%03d", mostrado.toInt())
        p.typeface = negrita
        p.textAlign = Paint.Align.CENTER
        p.textSize = bh * 0.85f
        for (i in 0..2) {
            p.color = 0xFFEDEDED.toInt()
            c.drawRect(bl + i * casilla + 1f, bt, bl + (i + 1) * casilla - 1f, bt + bh, p)
            p.color = Color.BLACK
            c.drawText(num[i].toString(), bl + i * casilla + casilla / 2f, bt + bh * 0.80f, p)
        }

        // Cuadro de viaje
        val tl = cx - r * 0.98f
        val tt = cy + r * 0.72f
        val tw = r * 0.95f
        val th = r * 0.40f
        rect.set(tl, tt, tl + tw, tt + th)
        p.style = Paint.Style.FILL
        p.color = 0xFF151515.toInt()
        c.drawRoundRect(rect, 12f, 12f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f
        p.color = 0xFF444444.toInt()
        c.drawRoundRect(rect, 12f, 12f, p)
        val horas = tripMs / 3600000L
        val mins = (tripMs / 60000L) % 60L
        p.style = Paint.Style.FILL
        p.color = Color.WHITE
        p.textSize = th * 0.45f
        c.drawText(String.format(Locale.US, "%02d:%02d", horas, mins), tl + tw / 2f, tt + th * 0.52f, p)
        p.typeface = Typeface.DEFAULT
        p.textSize = th * 0.22f
        p.color = 0xFFAAAAAA.toInt()
        c.drawText(String.format(Locale.US, "TRIP  %.1f km", tripM / 1000f), tl + tw / 2f, tt + th * 0.86f, p)

        // Animación suave de la aguja
        if (Math.abs(objetivo - mostrado) > 0.15f) {
            mostrado += (objetivo - mostrado) * 0.15f
            postInvalidateDelayed(16)
        } else {
            mostrado = objetivo
        }
    }
}
