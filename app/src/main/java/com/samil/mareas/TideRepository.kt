package com.samil.mareas

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val ID_PORTO_VIGO = 3
private const val BASE_URL = "https://servizos.meteogalicia.gal/mgrss/predicion/mareas/jsonMareas.action"
private const val PREFS_NAME = "tide_cache"
private const val KEY_EVENTS_JSON = "events_json"
private const val KEY_FETCHED_AT = "fetched_at"

class TideRepository(private val context: Context) {

              private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                  private val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                      var lastErrorMessage: String? = null
                  private set

              fun syncFromNetwork(): Boolean {
                                lastErrorMessage = null
                                return try {
                                                      val today = LocalDate.now()
                                                                  val end = today.plusDays(29)
                                                                              val url = "$BASE_URL?idPorto=$ID_PORTO_VIGO" +
                                                          "&dataIni=${today.format(dateFmt)}&dataFin=${end.format(dateFmt)}"

                                                      val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                                                                                requestMethod = "GET"
                                                                                connectTimeout = 15000
                                                                                readTimeout = 15000
                                                                                setRequestProperty("User-Agent", "Mozilla/5.0 (Android) MareasSamilApp/1.0")
                                                                                                setRequestProperty("Accept", "application/json, text/plain, */*")
                                                      }

                                                                  val responseCode = connection.responseCode
                                                      if (responseCode !in 200..299) {
                                                                                lastErrorMessage = "HTTP $responseCode al llamar a MeteoGalicia"
                                                                                connection.disconnect()
                                                                                                return false
                                                      }

                                                                  val body = connection.inputStream.bufferedReader().use { it.readText() }
                                                                              connection.disconnect()

                                                                                          val events = parseEvents(body)
                                                                                                      if (events.isEmpty()) {
                                                                                                                                lastErrorMessage = "Respuesta sin mareas. Primeros 200 caracteres: " +
                                                                                                                                    body.take(200)
                                                                                                                                                    return false
                                                                                                      }
                                                                                                      
                                                                                                                  prefs.edit()
                                                                                                                                  .putString(KEY_EVENTS_JSON, body)
                                                                                                                                                  .putLong(KEY_FETCHED_AT, System.currentTimeMillis())
                                                                                                                                                                  .apply()
                                                                                                                                                                              true
                                } catch (e: Exception) {
                                                      lastErrorMessage = "${e.javaClass.simpleName}: ${e.message}"
                                                      false
                                }
              }

                  fun loadCachedEvents(): List<TideEvent> {
                                    val cached = prefs.getString(KEY_EVENTS_JSON, null) ?: return emptyList()
                                            return try {
                                                                  parseEvents(cached)
                                            } catch (e: Exception) {
                                                                  emptyList()
                                            }
                  }

                      fun isCacheStale(maxAgeHours: Long = 20): Boolean {
                                        val fetchedAt = prefs.getLong(KEY_FETCHED_AT, 0L)
                                                if (fetchedAt == 0L) return true
                                        val ageHours = (System.currentTimeMillis() - fetchedAt) / (1000 * 60 * 60)
                                                return ageHours >= maxAgeHours
                      }

                          private fun parseEvents(rawJson: String): List<TideEvent> {
                                            val trimmed = rawJson.trim()

                                                    val days: JSONArray = when {
                                                                          trimmed.startsWith("[") -> JSONArray(trimmed)
                                                                                      else -> {
                                                                                                                val obj = JSONObject(trimmed)
                                                                                                                                when {
                                                                                                                                                              obj.has("mareas") -> obj.getJSONArray("mareas")
                                                                                                                                                                                  obj.has("listaMareas") -> JSONArray().put(obj)
                                                                                                                                                                                                      else -> JSONArray()
                                                                                                                                                                                                                      }
                                                                                      }
                                                    }

                                                            val result = mutableListOf<TideEvent>()
                                                                    for (i in 0 until days.length()) {
                                                                                          val day = days.getJSONObject(i)
                                                                                                      val lista = day.optJSONArray("listaMareas") ?: continue
                                                                                          for (j in 0 until lista.length()) {
                                                                                                                    val e = lista.getJSONObject(j)
                                                                                                                                    val dt = TideEvent.parseDateTime(e.getString("data"))
                                                                                                                                                    val altura = e.optDouble("altura", 0.0)
                                                                                                                                                                    val idTipo = e.optInt("idTipoMarea", 0)
                                                                                                                                                                                    result.add(TideEvent(dt, altura, isHighTide = idTipo == 1))
                                                                                          }
                                                                    }
                                                                            return result.sortedBy { it.dateTime }
                          }

                              fun computeState(events: List<TideEvent>, now: LocalDateTime = LocalDateTime.now()): TideState? {
                                                if (events.size < 2) return null

                                                var prev: TideEvent? = null
                                                var next: TideEvent? = null
                                                for (i in 0 until events.size - 1) {
                                                                      if (!events[i].dateTime.isAfter(now) && events[i + 1].dateTime.isAfter(now)) {
                                                                                                prev = events[i]
                                                                                                next = events[i + 1]
                                                                                                break
                                                                      }
                                                }
                                                        if (prev == null || next == null) {
                                                                              if (now.isBefore(events.first().dateTime)) {
                                                                                                        prev = events[0]; next = events[1]
                                                                              } else {
                                                                                                        prev = events[events.size - 2]; next = events[events.size - 1]
                                                                              }
                                                        }

                                                                val totalSeconds = java.time.Duration.between(prev.dateTime, next.dateTime).seconds.toDouble()
                                                                        val elapsedSeconds = java.time.Duration.between(prev.dateTime, now).seconds.toDouble()
                                                                                val fraction = (elapsedSeconds / totalSeconds).coerceIn(0.0, 1.0)

                                                                                        val rising = prev.isHighTide.not()

                                                                                                val nextHigh = events.firstOrNull { it.dateTime.isAfter(now) && it.isHighTide }
                                                                                                        val nextLow = events.firstOrNull { it.dateTime.isAfter(now) && !it.isHighTide }
                                                                                                        
                                                                                                                return TideState(
                                                                                                                                      previousEvent = prev,
                                                                                                                                      nextEvent = next,
                                                                                                                                      progressFraction = fraction,
                                                                                                                                      rising = rising,
                                                                                                                                      nextHighTide = nextHigh,
                                                                                                                                      nextLowTide = nextLow
                                                                                                                                  )
                              }
}
