package com.example.uberapp.activities

import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.example.uberapp.R
import com.example.uberapp.databinding.ActivityTripInfoBinding
import com.example.uberapp.models.Prices
import com.example.uberapp.providers.ConfigProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class TripInfoActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private lateinit var binding: ActivityTripInfoBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null

    private var extraOriginName = ""
    private var extraDestinationName = ""
    private var extraOriginLat = 0.0
    private var extraOriginLng = 0.0
    private var extraDestinationLat = 0.0
    private var extraDestinationLng = 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var markerOrigin: Marker? = null
    private var markerDestination: Marker? = null

    private var configProvider = ConfigProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripInfoBinding.inflate(layoutInflater)
        // se hace referencia al archivo activity trip info xml
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // EXTRAS
        extraOriginName = intent.getStringExtra("origin")!!
        extraDestinationName = intent.getStringExtra("destination")!!
        extraOriginLat = intent.getDoubleExtra("origin_lat", 0.0)
        extraOriginLng = intent.getDoubleExtra("origin_lng", 0.0)
        extraDestinationLat = intent.getDoubleExtra("destination_lat", 0.0)
        extraDestinationLng = intent.getDoubleExtra("destination_lng", 0.0)

        originLatLng = LatLng(extraOriginLat, extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat, extraDestinationLng)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        binding.textViewOrigin.text = extraOriginName
        binding.textViewDestination.text = extraDestinationName

        Log.d("LOCALIZACION", "Origin lat: ${originLatLng?.latitude}")
        Log.d("LOCALIZACION", "Origin lng: ${originLatLng?.longitude}")
        Log.d("LOCALIZACION", "Destination lat: ${destinationLatLng?.latitude}")
        Log.d("LOCALIZACION", "Destination lng: ${destinationLatLng?.longitude}")

        binding.imageViewBack.setOnClickListener { finish() }
    }

    private fun getPrices(distance: Double, time: Double) {

        configProvider.getPrices().addOnSuccessListener { document ->
            if (document.exists()) {
                val prices = document.toObject(Prices::class.java) // DOCUMENTO CON LA INFORMACION

                val totalDistance = distance * prices?.km!! // VALOR POR KM
                Log.d("PRICES", "totalDistance: $totalDistance")
                val totalTime = time * prices?.min!! // VALOR POR MIN
                Log.d("PRICES", "totalTime: $totalTime")
                var total = totalDistance + totalTime // TOTAL
                Log.d("PRICES", "total: $total")
                total = if (total < 5000.0) prices?.minValue!! else total
                Log.d("PRICES", "new total: $total")

                var minTotal = total - prices?.difference!! // TOTAL - 2000
                Log.d("PRICES", "minTotal: $minTotal")
                var maxTotal = total + prices?.difference!! // TOTAL + 2000
                Log.d("PRICES", "maxTotal: $maxTotal")

                val minTotalString = String.format("%.0f", minTotal)
                val maxTotalString = String.format("%.0f", maxTotal)

                binding.textViewPrice.text = "$minTotalString - $maxTotalString pesos"
            }
        }
    }

    private fun addOriginMarker() {
        markerOrigin = googleMap?.addMarker(MarkerOptions().position(originLatLng!!).title("Mi posicion")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinationMarker() {
        markerDestination = googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("LLevar a")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
    }

    private fun easyDrawRoute() {
        wayPoints.add(originLatLng!!)
        wayPoints.add(destinationLatLng!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng!!)
            .build()

        directionUtil.initPath()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition( // permite hacer zoom y girar mapa
            CameraPosition.builder().target(originLatLng!!).zoom(13f).build()
        ))

        easyDrawRoute()
        addOriginMarker()
        addDestinationMarker()

        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )
            if (!success!!) {
                Log.d("MAPAS", "No se pudo econtrar el estilo")
            }
        } catch (e: Resources.NotFoundException) {
            Log.d("MAPAS", "Error: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {

    }

    override fun locationCancelled() {

    }

    override fun onDestroy() { // se ejecuta cuando se cierra la aplicación o se pasa a otra activity
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        var distance = polyLineDetailsArray[1].distance.toDouble() // METROS
        var time = polyLineDetailsArray[1].time.toDouble() // SEGUNDOS
        distance = if (distance < 1000.0) 1000.0 else distance // SI ES MENOS DE 1000 METROS EN 1 KM
        time = if (time < 60.0) 60.0 else time

        distance = distance / 1000 // KM
        time = time / 60 // MIN

        val timeString = String.format("%.2f", time)
        val distanceString = String.format("%.2f", distance)

        getPrices(distance, time)
        binding.textViewTimeAndDistance.text = "$timeString min - $distanceString km"

        directionUtil.drawPath(WAY_POINT_TAG)
    }
}