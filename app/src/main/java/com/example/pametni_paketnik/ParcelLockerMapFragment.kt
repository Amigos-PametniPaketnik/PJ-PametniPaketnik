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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


class ParcelLockerMapFragment : Fragment() {
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLoction: Location? = null
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: com.google.android.gms.location.LocationRequest
    private var requestingLocationUpdates = false

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
                mapController.setCenter(startPoint)
            }
        }
    }
    lateinit var map: MapView
    var startPoint: GeoPoint = GeoPoint(46.554650, 15.645881);

    lateinit var mapController: IMapController
    var marker: Marker? = null
    private var _binding: FragmentParcelLockerMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ParcelLockerMapViewModel
    private lateinit var adapter: RecyclerView.Adapter<ParcelLockersAdapter.ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentParcelLockerMapBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ParcelLockerMapViewModel::class.java)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Configuration.getInstance()
            .load(requireActivity().applicationContext, requireActivity().getPreferences(Context.MODE_PRIVATE))
       val binding = FragmentParcelLockerMapBinding.inflate(layoutInflater) //ADD THIS LINE

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
        viewModel.loadParcelLockers();
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


    fun initMap() {
        initLoaction()

        viewModel.parcelLockers.observe(viewLifecycleOwner, Observer { parcelLockers ->
            Toast.makeText(requireContext(), "V seznamu je ${parcelLockers.size} paketnikov!", Toast.LENGTH_LONG).show()
            for(i in parcelLockers){
                var startPoint1: GeoPoint = GeoPoint(46.554650, 15.645881);
                val markerInstructor:Marker
                markerInstructor=Marker(map)
                markerInstructor!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                startPoint1.latitude =i.location[0].toDouble()
                startPoint1.longitude=i.location[1].toDouble()
                markerInstructor!!.position=startPoint1
                markerInstructor.title="Ime: "+i.name + "\nŠtevilka paketnika: " + i.numberParcelLocker+ "\nOpis: "+i.description+"\nNaslov: "+i.address+"\nMesto: "+i.city+"\nPoštna številka: "+i.postal
                map.overlays.add(markerInstructor)
            }
            adapter = ParcelLockersAdapter(parcelLockers, object: ParcelLockersAdapter.MyOnClick {
                override fun onClick(p0: View?, position: Int) {
                    Toast.makeText(requireContext(), "Short Click!", Toast.LENGTH_SHORT).show()
                }

                override fun onLongClick(p0: View?, position: Int) {
                    Toast.makeText(requireContext(), "Long click!", Toast.LENGTH_SHORT).show()
                }
            })
            binding.recyclerViewParcelLockers.adapter = adapter
            binding.recyclerViewParcelLockers.layoutManager = LinearLayoutManager(requireContext())
            map.invalidate()
        })

        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
        mapController.setZoom(9.4)
        mapController.setCenter(startPoint);
        map.invalidate()
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