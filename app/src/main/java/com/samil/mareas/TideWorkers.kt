package com.samil.mareas

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TideSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
      override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
                val repo = TideRepository(applicationContext)
                        val ok = repo.syncFromNetwork()
                                TideWidgetProvider.redrawAllWidgets(applicationContext)
                                        if (ok) Result.success() else Result.retry()
      }
}

class TideRedrawWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
      override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
                val repo = TideRepository(applicationContext)
                        if (repo.isCacheStale()) {
                                      repo.syncFromNetwork()
                        }
                                TideWidgetProvider.redrawAllWidgets(applicationContext)
                                        Result.success()
      }
}
