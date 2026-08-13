package com.samil.mareas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {
      override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                        setContentView(R.layout.activity_main)

                                TideWidgetProvider.schedulePeriodicRedraw(applicationContext)
                                        WorkManager.getInstance(applicationContext)
                                                    .enqueue(OneTimeWorkRequestBuilder<TideSyncWorker>().build())
      }
}
