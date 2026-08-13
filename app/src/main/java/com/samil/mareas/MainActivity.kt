package com.samil.mareas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

              private val scope = MainScope()

                  override fun onCreate(savedInstanceState: Bundle?) {
                                    super.onCreate(savedInstanceState)
                                            setContentView(R.layout.activity_main)

                                                    val statusText = findViewById<TextView>(R.id.status_text)
                                                            TideWidgetProvider.schedulePeriodicRedraw(applicationContext)

                                                                    scope.launch {
                                                                                          val repo = TideRepository(applicationContext)
                                                                                                      val ok = withContext(Dispatchers.IO) { repo.syncFromNetwork() }
                                                                                                                  val events = repo.loadCachedEvents()
                                                                                                                              TideWidgetProvider.redrawAllWidgets(applicationContext)
                                                                                                                              
                                                                                                                                          statusText.text = if (ok && events.isNotEmpty()) {
                                                                                                                                                                    "Sincronizado: ${events.size} mareas cargadas.\nAnade el widget Mareas Samil a tu pantalla de inicio."
                                                                                                                                          } else {
                                                                                                                                                                    "Error al sincronizar:\n${repo.lastErrorMessage ?: "desconocido"}"
                                                                                                                                          }
                                                                    }
                  }

                      override fun onDestroy() {
                                        scope.cancel()
                                                super.onDestroy()
                      }
}
