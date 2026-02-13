package com.novachat.core.network

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class PlayServicesChecker(
    private val googleApiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
) {
    fun checkAvailability(context: Context): Result<Unit> {
        val availabilityCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        if (availabilityCode == ConnectionResult.SUCCESS) {
            return Result.success(Unit)
        }

        val errorString = googleApiAvailability.getErrorString(availabilityCode)
        val message =
            "Google Play Services is unavailable ($errorString / code=$availabilityCode). " +
                "Update Play Services in system settings and retry."
        return Result.failure(SecurityException(message))
    }
}
