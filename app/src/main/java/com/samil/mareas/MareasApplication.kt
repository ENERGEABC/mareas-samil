package com.samil.mareas

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Mientras el proceso de la app este vivo, escucha cuando se enciende la
 * pantalla o el usuario desbloquea el movil, y redibuja el widget al
 * instante usando los datos ya guardados (sin red). Esto complementa al
 * refresco periodico de 15 minutos de TideRedrawWorker, que sigue
 * funcionando aunque el proceso este cerrado.
 */
class MareasApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                TideWidgetProvider.redrawAllWidgets(applicationContext)
            }
        }, filter)

        TideWidgetProvider.schedulePeriodicRedraw(applicationContext)
    }
}

