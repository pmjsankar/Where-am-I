package com.pmj.where

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pmj.where.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOCATION_WORK_TAG = "trackLocationWorker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val locationWorkRequest: WorkRequest =
//            OneTimeWorkRequestBuilder<LocationWorker>()
//                .build()
//        WorkManager
//            .getInstance(applicationContext)
//            .enqueue(locationWorkRequest)

        trackLocation()

        binding.btnLocation.setOnClickListener {
            if (binding.btnLocation.text.toString() == "START") {
                binding.btnLocation.text = "STOP"
                trackLocation()
            } else {
                binding.btnLocation.text = "START"
                stopTracking()
            }
        }
    }

    private fun trackLocation() {
        val locationWorkRequest =
            PeriodicWorkRequestBuilder<LocationWorker>(
                15, TimeUnit.MINUTES
            ).addTag(LOCATION_WORK_TAG).build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                LOCATION_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                locationWorkRequest
            )
    }

    private fun stopTracking() {
        WorkManager.getInstance(applicationContext).cancelAllWork()
    }
}