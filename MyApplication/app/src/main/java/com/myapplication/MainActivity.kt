package com.myapplication

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.location.LocationServices
import android.widget.Toast
import com.google.android.gms.location.LocationSettingsStatusCodes
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.ApiException
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.OnSuccessListener
import android.app.Activity
import android.content.Intent
import android.support.design.widget.Snackbar

import android.util.Log
import android.support.v4.app.ActivityCompat
import android.view.View




class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_CHECK_SETTINGS = 0x1
    private var mRequestingLocationUpdates = true
    private lateinit var locationManager:MyLocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationManager = MyLocationManager(LocationServices.getFusedLocationProviderClient(this),
                LocationServices.getSettingsClient(this), settingsSuccessResponse, settingsFailResponse)
    }

    override fun onResume() {
        super.onResume()
        if (mRequestingLocationUpdates && checkPermissions()) {
            //start location updates
            locationManager.requestLocation()
        } else if (!checkPermissions()) {
            //request permissions
            requestPermissions()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.i(TAG, "User agreed to make required location settings changes.")
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                }
            }// Nothing to do. startLocationupdates() gets called in onResume again.
            //updateUI();
        }
    }


    private val settingsSuccessResponse = OnSuccessListener<LocationSettingsResponse> {
        if (checkPermissions()) {
            locationManager.requestLocationUpdates()
        }
    }

    private fun checkPermissions(): Boolean {
        return PermissionUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val settingsFailResponse = OnFailureListener { e ->
        Log.v("location", "failed")
        val statusCode = (e as ApiException).statusCode
        when (statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the
                    // result in onActivityResult().
                    val rae = e as ResolvableApiException
                    rae.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                } catch (sie: IntentSender.SendIntentException) {
                    Log.i(TAG, "PendingIntent unable to execute request.")
                }

            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                val errorMessage = "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                Log.e(TAG, errorMessage)
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                mRequestingLocationUpdates = false
            }
        }
    }


    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, object : View.OnClickListener {
                override fun onClick(view: View) {
                    // Request permission
                    ActivityCompat.requestPermissions(this@MainActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_PERMISSIONS_REQUEST_CODE)
                }
            })
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }


    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Snackbar.make(
                findViewById<View>(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show()
    }
}
