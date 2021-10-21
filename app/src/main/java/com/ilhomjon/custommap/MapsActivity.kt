package com.ilhomjon.custommap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
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

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.ilhomjon.custommap.databinding.ActivityMapsBinding
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    //link api: https://console.cloud.google.com/apis/credentials?highlightKey=3cce6ed7-8ab1-4f47-80c9-2449875c0092&project=my-project-329604&authuser=1

    private var mMap: GoogleMap? = null
    private lateinit var binding: ActivityMapsBinding
    lateinit var geocoder: Geocoder
    private val TAG = "MapsActivity"
    val ACCES_FIND_LOCATION_CODE = 1000
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geocoder = Geocoder(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.setInterval(500)
        locationRequest.setFastestInterval(500)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
        mMap?.setOnMapLongClickListener(this)

        //markerni harakatlanishi
        mMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(p0: Marker) {
                Log.d(TAG, "onMarkerDragStart: ")
            }

            override fun onMarkerDrag(p0: Marker) {
                Log.d(TAG, "onMarkerDrag: ")
            }

            override fun onMarkerDragEnd(p0: Marker) {
                Log.d(TAG, "onMarkerDragEnd: ")

                val latLng = p0.position

                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses.size > 0) {
                    val addres = addresses[0]
                    val stringAddress = addres.getAddressLine(0)
                    p0.title = stringAddress
                }

            }
        })


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            enableUserLocation()
//            zoomToUserLocation()

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                //We can show user a dialog why this permission is necessary
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCES_FIND_LOCATION_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCES_FIND_LOCATION_CODE
                )
            }
        }

//        // Add a marker in Sydney and move the camera
//        val latLng = LatLng(27.1751, 78.8421)
//        val markerOptions = MarkerOptions().position(latLng).title("Taj Mahal").snippet("Wonder of the word")
//        mMap.addMarker(markerOptions)
//        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.0F)
//        mMap.animateCamera(cameraUpdate)

        try {
            val addresses: List<Address> = geocoder.getFromLocationName("Fergana", 1)

            if (addresses.isNotEmpty()) {
                val address = addresses.get(0)
                val london = LatLng(address.latitude, address.longitude)
                val markerOptions =
                    MarkerOptions().position(london)
                        .title(address.locality)
                        .draggable(true) //true qilsa keyin markerni surish mumkin

                mMap?.addMarker(markerOptions)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(london, 16F))
            } else {
                Toast.makeText(this, "Manzil topilmadi", Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onMapLongClick(p0: LatLng) {
        Log.d(TAG, "onMapLongClick: ${p0.toString()}")
        Toast.makeText(this, "${p0.toString()}", Toast.LENGTH_SHORT).show()

        val addresses = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
        if (addresses.size > 0) {
            val addres = addresses[0]
            val stringAddress = addres.getAddressLine(0)
            mMap?.addMarker(MarkerOptions().position(p0).title(stringAddress).draggable(true))
        }
    }


    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            Log.d(TAG, "onLocationResult: ${p0.lastLocation}")
            if (mMap != null){
                setUserLocationMarker(p0.lastLocation)
            }
        }
    }

     var userLocationMarker:Marker? = null
     var userLocationAcuracyCircle:Circle? = null
    fun setUserLocationMarker(location: Location){
        val latLng = LatLng(location.latitude, location.longitude)
        if (userLocationMarker == null){
            // Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red_car))
            markerOptions.rotation(location.bearing)
            markerOptions.anchor(0.5F, 0.5F)//mashinani o'rtaga qo'yish
            markerOptions.position(latLng)
            userLocationMarker = mMap?.addMarker(markerOptions)
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0F))
        }else{
            // Use the previausly created marker
            userLocationMarker?.position = latLng
            userLocationMarker?.rotation = location.bearing // mashinani oldini harakat tomonga yo'naltirish
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0F))
        }

        if (userLocationAcuracyCircle == null){
            val circleOptions = CircleOptions()
            circleOptions.center(latLng)
            circleOptions.strokeWidth(4F)
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
            circleOptions.fillColor(Color.argb(32, 255, 0, 0))
            circleOptions.radius(location.accuracy.toDouble())
            userLocationAcuracyCircle = mMap?.addCircle(circleOptions)
        }else{
            userLocationAcuracyCircle?.center = (latLng)
            userLocationAcuracyCircle?.radius = location.accuracy.toDouble()

        }
    }


    @SuppressLint("MissingPermission")
    fun startLocationUpdate(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    fun stopLocationUpdate(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            startLocationUpdate()
        }else{
            // you need request permission.......
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdate()
    }

    @SuppressLint("MissingPermission")
    fun enableUserLocation() {
        mMap?.isMyLocationEnabled = true
    }

    @SuppressLint("MissingPermission")
    fun zoomToUserLocation(){
        val locationTask = fusedLocationProviderClient.lastLocation
        locationTask.addOnSuccessListener {
            val latLng = LatLng(it.latitude, it.longitude)
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0F))
//            mMap.addMarker(MarkerOptions().position(latLng))
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCES_FIND_LOCATION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
                zoomToUserLocation()
            }else{
                //We can show a dialog that permission is not granted....
            }
        }
    }
}