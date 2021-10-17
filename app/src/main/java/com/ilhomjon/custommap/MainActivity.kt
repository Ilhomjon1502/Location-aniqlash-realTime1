package com.ilhomjon.custommap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.ilhomjon.custommap.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    //video link: https://youtu.be/4eWoXPSpA5Y?t=466

    private val TAG = "MainActivity"
    val REQUEST_CODE_PERMISSION = 1000
    lateinit var locationRequest: LocationRequest
    private lateinit var geocoder: Geocoder

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var locationCallback = object : LocationCallback(){
        override fun onLocationResult(location: LocationResult) {
           if (location == null){
               return
           }
            for (location:Location in location.locations){
                Log.d(TAG, "onLocationResult: ${location.toString()}")
            }
            var addressList: List<Address> =
                geocoder.getFromLocation(location.lastLocation.latitude, location.lastLocation.longitude, 1);                                                                                                      val address: Address = addressList.get(0)
            binding.txt1.setText(address.getAddressLine(0))
        }
    }

    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.setInterval(4000)
        locationRequest.setFastestInterval(2000)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        geocoder = Geocoder(this, Locale.getDefault())
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            getLastLocation()
            checkSettingsAndStartUpdates()
        } else {
            askLocationPermission()
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    fun checkSettingsAndStartUpdates(){
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        val client = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask: Task<LocationSettingsResponse> =
            client.checkLocationSettings(request)
        locationSettingsResponseTask.addOnSuccessListener {
            //Settings of device are satisfied and we can start location updates
            startLocationUpdates()
        }
        locationSettingsResponseTask.addOnFailureListener{
            Log.d(TAG, "checkSettingsAndStartUpdates: Error")
            Toast.makeText(this, "Xatolik \ncheckSettingsAndStartUpdates", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val locationTask: Task<Location> = fusedLocationProviderClient.lastLocation
        locationTask.addOnSuccessListener {
            if (it != null) {
                //We have a location
                Log.d(TAG, "getLastLocation: ${it.toString()}")
                Log.d(TAG, "getLastLocation: ${it.latitude}")
                Log.d(TAG, "getLastLocation: ${it.longitude}")
            } else {
                Log.d(TAG, "getLastLocation: location was null,,,,,,,,,,,,,,,,,,,...............")
            }
        }
        locationTask.addOnFailureListener {
            Log.d(TAG, "getLastLocation: ${it.message}")
        }
    }

    fun askLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Log.d(TAG, "askLocationPermission: siz dialog chiqarishingiz mumkin")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_PERMISSION
                )

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_PERMISSION
                )

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
//                getLastLocation()
                checkSettingsAndStartUpdates()
            } else {
                //permission not granted
            }
        }
    }
}