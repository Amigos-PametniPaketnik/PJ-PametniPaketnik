package com.example.pametni_paketnik

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.content.*
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Looper
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pametni_paketnik.databinding.FragmentCitysMapBinding
import com.example.pametni_paketnik.databinding.FragmentParcelLockerMapBinding
import com.example.pametni_paketnik.models.ParcelLocker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import timber.log.Timber
import java.util.*
import java.util.ArrayList

import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.PolylineOptions
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import org.osmdroid.bonuspack.routing.GoogleRoadManager
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Button
import android.widget.TextView
import kotlin.math.roundToInt


class CitysMapFragment : Fragment() {
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLoction: Location? = null
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: com.google.android.gms.location.LocationRequest
    private var requestingLocationUpdates = false
    private lateinit var app: MyApplication
    private lateinit var citysViewModel: CitysViewModel
    private var road : MutableLiveData<Road> = MutableLiveData()

    companion object {
        val REQUEST_CHECK_SETTINGS = 20202
    }
    init {
        locationRequest = com.google.android.gms.location.LocationRequest.create()
            .apply {
                interval = 1000 //can be much higher
                fastestInterval = 500
                smallestDisplacement = 10f //10m
                priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                maxWaitTime = 1000
            }
        locationCallback = object : LocationCallback() {}

        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            Timber.d("Permissions granted $allAreGranted")
            if (allAreGranted) {
                initMap()
                getPath()
                mapController.setCenter(startPoint)
            }
        }
    }
    lateinit var map: MapView
    var startPoint: GeoPoint = GeoPoint(46.554650, 15.645881);
    lateinit var roadManager: OSRMRoadManager

    lateinit var mapController: IMapController
    var marker: Marker? = null
    private var _binding: FragmentCitysMapBinding? = null
    private val binding get() = _binding!!
    var path1: org.osmdroid.views.overlay.Polyline? = null
    private lateinit var navController: NavController
    private lateinit var currentRoad: Road


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCitysMapBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        app = requireActivity().application as MyApplication
        citysViewModel = ViewModelProvider(this).get(CitysViewModel::class.java)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Configuration.getInstance()
            .load(requireActivity().applicationContext, requireActivity().getPreferences(Context.MODE_PRIVATE))
       val binding = FragmentCitysMapBinding.inflate(layoutInflater) //ADD THIS LINE


        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller
        requireActivity().setContentView(binding.root)
        val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        mapController.setCenter(startPoint)
        activityResultLauncher.launch(appPerms)

        citysViewModel.road.observe(viewLifecycleOwner, Observer { returnedroad ->
            var roadOverlay = RoadManager.buildRoadOverlay(returnedroad, Color.BLUE, 10f)
            path1 = roadOverlay
            map.overlayManager.add(path1)
            map.invalidate()
            var btn = activity!!.findViewById<View>(R.id.distanceTime) as TextView
            if (app.matrixTime){
                btn.text = "Time to travel: "+(returnedroad.mDuration/60).roundToInt()+" min"
            }
            else{
                btn.text = "Distance: "+returnedroad.mLength.roundToInt()+" km"
            }
        })
    }
    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (requestingLocationUpdates) {
            requestingLocationUpdates = false
            stopLocationUpdates()
        }
        binding.map.onPause()
    }

    fun initLoaction() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        readLastKnownLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() { //onResume
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() { //onPause
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    @SuppressLint("MissingPermission")
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                var targetlocation = Location(LocationManager.GPS_PROVIDER)
                targetlocation.latitude = 46.554650
                targetlocation.longitude = 15.645881
                location?.let { updateLocation(targetlocation) }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("Settings onActivityResult for $requestCode result $resultCode")
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                initMap()
            }
        }
    }

    fun updateLocation(newLocation: Location) {
        lastLoction = newLocation
        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint
        map.invalidate()

    }


    @SuppressLint("SetTextI18n")
    fun initMap() {
        roadManager = OSRMRoadManager(context)
        initLoaction()


            for(i in app.citysList){
                var startPoint1: GeoPoint = GeoPoint(46.554650, 15.645881);
                val markerInstructor:Marker
                markerInstructor=Marker(map)
                markerInstructor!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                startPoint1.latitude =i.latitude.toDouble()
                startPoint1.longitude=i.longitude.toDouble()
                markerInstructor.title="Indeks: "+i.index + "\nNaslov: " + i.address+ "\nPošta: "+i.postOffice+" "+i.postNumber+"\nŠtevilo paketnikov: "+i.parcelLockerCount

                markerInstructor!!.position=startPoint1
                markerInstructor.image = resources.getDrawable(R.drawable.paketnik_icon)
                //markerInstructor.icon = resources.getDrawable(R.drawable.paketnik_location_icon)
                markerInstructor.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                map.overlays.add(markerInstructor)
            }
        var waypoints: ArrayList<GeoPoint> = kotlin.collections.ArrayList()
        for (city in app.citysList) {
            var startPoint1: GeoPoint = GeoPoint(46.554650, 15.645881)
            startPoint1.latitude = city.latitude.toDouble()
            startPoint1.longitude = city.longitude.toDouble()
            waypoints.add(startPoint1)
        }
        var startPoint2: GeoPoint = GeoPoint(46.55, 15.64)
        startPoint2.latitude = app.citysList[0].latitude.toDouble()
        startPoint2.longitude = app.citysList[0].longitude.toDouble()
        map.invalidate()


        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
        mapController.setZoom(9.4)
        mapController.setCenter(startPoint);
        map.invalidate()
    }
    private fun getPath() { //Singelton
            var waypoints: ArrayList<GeoPoint> = kotlin.collections.ArrayList()
            for (city in app.citysList) {
                var startPoint1: GeoPoint = GeoPoint(46.554650, 15.645881)
                startPoint1.latitude = city.latitude.toDouble()
                startPoint1.longitude = city.longitude.toDouble()
                waypoints.add(startPoint1)
            }
            var startPoint2: GeoPoint = GeoPoint(46.55, 15.64)
            startPoint2.latitude = app.citysList[0].latitude.toDouble()
            startPoint2.longitude = app.citysList[0].longitude.toDouble()
            waypoints.add(startPoint2)

            citysViewModel.loadRoad(waypoints, requireContext()) // Load road with ViewModel
    }


    private fun getPositionMarker(): Marker {
        if (marker == null) {
            marker = Marker(map)
            marker!!.position=startPoint
            mapController.setCenter(startPoint)
            marker!!.title = "Here I am"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
         // map.overlays.add(marker)
        }
        return marker!!
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


