package com.myapplication

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

import java.text.DateFormat
import java.util.Date

class MyLocationManager(
        /**
         * Provides access to the Fused Location Provider API.
         */
        private val mFusedLocationClient: FusedLocationProviderClient,
        /**
         * Provides access to the Location Settings API.
         */
        private val mSettingsClient: SettingsClient, private val mSettingsSuccessResponse: OnSuccessListener<LocationSettingsResponse>, private val mSettingsFailResponse: OnFailureListener) {


    private var mLocationRequest: LocationRequest? = null

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private var mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Callback for Location events.
     */
    private var mLocationCallback: LocationCallback? = null


    /**
     * Represents a geographical location.
     */
    private var mCurrentLocation: Location? = null

    /**
     * Time when the location was updated represented as a String.
     */
    private var mLastUpdateTime: String? = null

    val isRequestingUpdates = false


    init {
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest!!.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)

        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

    /**
     * Creates a callback for receiving location events.
     */
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                mCurrentLocation = locationResult.getLastLocation()
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                //updateLocationUI();
                Log.v("location", mCurrentLocation!!.latitude.toString() + " " + mCurrentLocation!!.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper())
    }


    fun requestLocation() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(mSettingsSuccessResponse)
                .addOnFailureListener(mSettingsFailResponse)
    }



        private val TAG = "MyLocationManager"

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        /**
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */
        private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

}
