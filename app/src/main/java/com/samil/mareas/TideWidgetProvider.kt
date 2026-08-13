package com.samil.mareas

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class TideWidgetProvider : AppWidgetProvider() {

      override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
                for (id in appWidgetIds) {
                              redrawWidget(context, appWidgetManager, id)
                }
                        WorkManager.getInstance(context).enqueueUniqueWork(
                                      "tide_initial_sync",
                                      ExistingWorkPolicy.KEEP,
                                      OneTimeWorkRequestBuilder<TideSyncWorker>().build()
                                              )
                                schedulePeriodicRedraw(context)
      }

          override fun onEnabled(context: Context) {
                    schedulePeriodicRedraw(context)
          }

              companion object {
                        fun schedulePeriodicRedraw(context: Context) {
                                      val request = PeriodicWorkRequestBuilder<TideRedrawWorker>(15, TimeUnit.MINUTES).build()
                                                  WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                                                    "tide_redraw_periodic",
                                                                    ExistingPeriodicWorkPolicy.KEEP,
                                                                    request
                                                                )

                                                              val syncRequest = PeriodicWorkRequestBuilder<TideSyncWorker>(24, TimeUnit.HOURS).build()
                                                                          WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                                                                            "tide_sync_daily",
                                                                                            ExistingPeriodicWorkPolicy.KEEP,
                                                                                            syncRequest
                                                                                        )
                        }

                                fun redrawWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
                                              val repo = TideRepository(context)
                                                          val events = repo.loadCachedEvents()
                                                                      val views = RemoteViews(context.packageName, R.layout.widget_tide)

                                                                                  val state = repo.computeState(events)
                                                                                              if (state != null) {
                                                                                                                val bitmap = TideDialRenderer.render(size = 600, state = state)
                                                                                                                                views.setImageViewBitmap(R.id.tide_dial_image, bitmap)
                                                                                              }
                                                                                                          appWidgetManager.updateAppWidget(appWidgetId, views)
                                }

                                        fun redrawAllWidgets(context: Context) {
                                                      val manager = AppWidgetManager.getInstance(context)
                                                                  val ids = manager.getAppWidgetIds(
                                                                                    android.content.ComponentName(context, TideWidgetProvider::class.java)
                                                                                                )
                                                                              for (id in ids) {
                                                                                                redrawWidget(context, manager, id)
                                                                              }
                                        }
              }
}
