package com.alain.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), LocationListener {

    private var apps: List<ResolveInfo> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private val horaFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val inicio = SystemClock.elapsedRealtime()

    private lateinit var reloj: TextView
    private lateinit var speedo: SpeedoView
    private lateinit var eqIzq: EqView
    private lateinit var eqDer: EqView
    private lateinit var btnPlay: ImageButton
    private lateinit var cajon: View
    private lateinit var audio: AudioManager
    private var dias: List<TextView> = emptyList()

    private var metros = 0f
    private var ultima: Location? = null

    private val tick = object : Runnable {
        override fun run() {
            reloj.text = horaFmt.format(Date())
            marcarDia()
            val sonando = audio.isMusicActive
            btnPlay.setImageResource(if (sonando) R.drawable.ic_pausa else R.drawable.ic_play)
            eqIzq.setActive(sonando)
            eqDer.setActive(sonando)
            speedo.setTrip(SystemClock.elapsedRealtime() - inicio, metros)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        audio = getSystemService(AUDIO_SERVICE) as AudioManager

        reloj = findViewById(R.id.reloj) as TextView
        speedo = findViewById(R.id.speedo) as SpeedoView
        eqIzq = findViewById(R.id.eqIzq) as EqView
        eqDer = findViewById(R.id.eqDer) as EqView
        btnPlay = findViewById(R.id.btnPlay) as ImageButton
        cajon = findViewById(R.id.drawer)

        crearDias()

        // Intro con el logo solo al arrancar el launcher
        if (savedInstanceState == null) (findViewById(R.id.splash) as LogoView).iniciar()

        findViewById(R.id.btnAjustes).setOnClickListener {
            try { startActivity(Intent(Settings.ACTION_SETTINGS)) } catch (e: Exception) {}
        }
        findViewById(R.id.btnMusica).setOnClickListener { abrirMusica() }
        findViewById(R.id.btnAnterior).setOnClickListener { tecla(KeyEvent.KEYCODE_MEDIA_PREVIOUS) }
        findViewById(R.id.btnPlay).setOnClickListener { tecla(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
        findViewById(R.id.btnSiguiente).setOnClickListener { tecla(KeyEvent.KEYCODE_MEDIA_NEXT) }
        findViewById(R.id.btnApps).setOnClickListener { mostrarCajon() }
        findViewById(R.id.btnCerrar).setOnClickListener { ocultarCajon() }
        findViewById(R.id.nav).setOnClickListener { abrirMapa() }

        val grid = findViewById(R.id.grid) as GridView
        grid.adapter = AppsAdapter()
        grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
            val intent = packageManager.getLaunchIntentForPackage(apps[pos].activityInfo.packageName)
            ocultarCajon()
            if (intent != null) startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
        try {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this)
        } catch (e: Exception) {
        }
        if (Build.VERSION.SDK_INT >= 14) {
            var flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            if (Build.VERSION.SDK_INT >= 16) flags = flags or View.SYSTEM_UI_FLAG_FULLSCREEN
            if (Build.VERSION.SDK_INT >= 19) flags = flags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            window.decorView.systemUiVisibility = flags
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
        try {
            (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(this)
        } catch (e: Exception) {
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ocultarCajon()
    }

    override fun onBackPressed() {
        if (cajon.visibility == View.VISIBLE) ocultarCajon()
    }

    // ---- GPS ----
    override fun onLocationChanged(loc: Location) {
        val kmh = if (loc.hasSpeed()) loc.speed * 3.6f else 0f
        val v = if (kmh < 2f) 0f else kmh
        speedo.setSpeed(v)
        val prev = ultima
        if (prev != null && v > 3f && loc.accuracy < 40f) metros += prev.distanceTo(loc)
        ultima = loc
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    // ---- Días de la semana ----
    private fun crearDias() {
        val cont = findViewById(R.id.dias) as LinearLayout
        val nombres = arrayOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM")
        val lista = ArrayList<TextView>()
        val pad = (6 * resources.displayMetrics.density).toInt()
        for (n in nombres) {
            val tv = TextView(this)
            tv.text = n
            tv.textSize = 11f
            tv.setPadding(pad, 2, pad, 2)
            cont.addView(tv)
            lista.add(tv)
        }
        dias = lista
    }

    private fun marcarDia() {
        val idx = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
        for (i in dias.indices) {
            val activo = i == idx
            dias[i].setTextColor(if (activo) Color.BLACK else 0xFF9A9A9A.toInt())
            dias[i].setBackgroundColor(if (activo) 0xFFEDEDED.toInt() else Color.TRANSPARENT)
        }
    }

    // ---- Música y mapa ----
    private fun tecla(codigo: Int) {
        for (accion in intArrayOf(KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP)) {
            val i = Intent(Intent.ACTION_MEDIA_BUTTON)
            i.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(accion, codigo))
            sendOrderedBroadcast(i, null)
        }
    }

    private fun abrirMusica() {
        try {
            if (Build.VERSION.SDK_INT >= 15) {
                startActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC))
                return
            }
        } catch (e: Exception) {
        }
        mostrarCajon()
    }

    private fun abrirMapa() {
        for (pkg in arrayOf("com.google.android.apps.maps", "com.waze", "com.sygic.aura")) {
            val i = packageManager.getLaunchIntentForPackage(pkg)
            if (i != null) {
                startActivity(i)
                return
            }
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0")))
        } catch (e: Exception) {
            mostrarCajon()
        }
    }

    // ---- Cajón de apps ----
    private fun mostrarCajon() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().toLowerCase(Locale.getDefault()) }
        ((findViewById(R.id.grid) as GridView).adapter as BaseAdapter).notifyDataSetChanged()
        cajon.visibility = View.VISIBLE
    }

    private fun ocultarCajon() {
        cajon.visibility = View.GONE
    }

    inner class AppsAdapter : BaseAdapter() {
        override fun getCount() = apps.size
        override fun getItem(position: Int) = apps[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView
                ?: LayoutInflater.from(this@MainActivity).inflate(R.layout.item_app, parent, false)
            val info = apps[position]
            (v.findViewById(R.id.icono) as ImageView).setImageDrawable(info.loadIcon(packageManager))
            (v.findViewById(R.id.nombre) as TextView).text = info.loadLabel(packageManager)
            return v
        }
    }
}
