package com.pmj.where


import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.location.LocationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*
import java.util.*


class LocationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val context: Context = appContext
    private var tts: TextToSpeech? = null

    override fun doWork(): Result {

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // set US English as language for tts
                val result = tts!!.setLanguage(Locale("ml", "IN"))

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language specified is not supported!")
                } else {
                    Log.e("TTS", "Initilization Fail")
                }

            } else {
                Log.e("TTS", "Initilization Failed!")
            }
        }

        getLocation(context)
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private val locationRequestGPS by lazy {
        LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNumUpdates(1)
            .setSmallestDisplacement(1000f)
    }

    private val locationRequestNETWORK by lazy {
        LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_LOW_POWER)
            .setNumUpdates(1)
            .setSmallestDisplacement(1000f)
    }


    @SuppressLint("MissingPermission")
    fun getLocation(context: Context) {
        val ctx = context.applicationContext
        val manager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(manager)) {
            return
        } else {
            val service = LocationServices.getFusedLocationProviderClient(ctx)
            service.lastLocation
                .addOnCompleteListener {
                    locationRequest(manager, service)
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun locationRequest(
        locationManager: LocationManager,
        service: FusedLocationProviderClient
    ) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                service.removeLocationUpdates(this)
                getCompleteAddressString(p0.lastLocation.latitude, p0.lastLocation.longitude)
            }
        }

        when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                service.requestLocationUpdates(locationRequestGPS, callback, Looper.getMainLooper())
            }
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                service.requestLocationUpdates(
                    locationRequestNETWORK,
                    callback,
                    Looper.getMainLooper()
                )
            }
            else -> {
                return
            }
        }
    }

    private fun getCompleteAddressString(lat: Double, long: Double) {
        val geocoder = Geocoder(context, Locale.getDefault())

        val addresses: List<Address>? = geocoder.getFromLocation(lat, long, 1)
        if (addresses != null) {
            val returnedAddress: Address? = addresses.getOrNull(0)
            val strAdd = returnedAddress?.locality ?: ""
            if (strAdd.isNotBlank()) {
                Log.i("Jayy", strAdd)
                tts?.speak(strAdd, TextToSpeech.QUEUE_FLUSH, null, "")
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    tts?.speak(strAdd, TextToSpeech.QUEUE_FLUSH, null, "")
                }, 2000)

                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    tts?.speak(strAdd, TextToSpeech.QUEUE_FLUSH, null, "")
                }, 3000)
            }
        }
    }
}