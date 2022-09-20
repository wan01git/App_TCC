package com.example.app_tcc

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.app_tcc.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

const val getCoordenada = 111
const val getGeofencing = 222
const val zoomCamera = 10f
const val raioGeofencing = 500
const val geofencing_id = "teste"
const val geofencing_expirado = 365 * 24 * 60 * 60 * 1000
const val tempo_dentro_geofencing = 10 * 1000

private val tag = MapsActivity::class.java.simpleName
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var localizacaoFundida: FusedLocationProviderClient
    private lateinit var geofence: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        localizacaoFundida =  LocationServices.getFusedLocationProviderClient(this)
        geofence = LocationServices.getGeofencingClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        if (!localizacaoConcedida()){
            val permissoes = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                permissoes.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                this,
                permissoes.toTypedArray(),
                getCoordenada
            )
        }
        else{
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
            localizacaoFundida.lastLocation.addOnSuccessListener{
                if (it !=  null){
                    with(mMap){
                        val cod = LatLng(it.latitude,it.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(cod, zoomCamera))
                    }
                }
                else{
                    with(mMap){
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(-16.685011777316088,-49.27508701832637),
                                zoomCamera)
                        )
                    }
                }
            }
        }

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun localizacaoConcedida(): Boolean{
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun click_longo(map: GoogleMap){
        map.setOnMapClickListener { latlng ->
            map.addMarker(
                MarkerOptions().position(latlng).title("local geofencing")
            ).showInfoWindow()
            map.addCircle(
                CircleOptions()
                    .center(latlng)
                    .strokeColor(Color.rgb(216,237,238))
                    .fillColor(Color.rgb(0,250,154))
                    .radius(raioGeofencing.toDouble())
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomCamera))
            val database = Firebase.database
            val referencia = database.getReference("dados")
            val key =  referencia.push().key
            if (key != null){
                val lembrete = lembrete(key, latlng.latitude, latlng.longitude)
                referencia.child(key).setValue(lembrete)
            }
            criarGeofencing(latlng, key!!,geofence)
        }
    }

    private fun criarGeofencing(location: LatLng, key: String, geofencingClient: GeofencingClient){
        val geofenceArea = Geofence.Builder()
            .setRequestId(geofencing_id)
            .setCircularRegion(location.latitude,location.longitude, raioGeofencing.toFloat())
            .setExpirationDuration(geofencing_expirado.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL).setLoiteringDelay(
                tempo_dentro_geofencing)
            .build()
        val geofenceRequisicao = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofenceArea)
            .build()

        val geofenceIntencao = Intent(this,GeofenceReceptor::class.java)
            .putExtra("key",key)
            .putExtra("mensagem","Geofence criada com sucesso")
        val intecaoPendente = PendingIntent.getBroadcast(
            applicationContext,
            0,
            geofenceIntencao,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}