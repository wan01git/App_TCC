package com.example.app_tcc

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
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
import kotlin.random.Random

const val getCoordenada = 111
const val geofenceRequerida = 12345567
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

        click_longo(mMap)
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
                    .strokeColor(Color.rgb(206,207,208))
                    .fillColor(Color.rgb(0,150,154))
                    .radius(raioGeofencing.toDouble())
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomCamera))
            val database = Firebase.database
            val referencia = database.getReference("Geofences")
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    geofenceRequerida
                )
            } else{
                geofencingClient.addGeofences(geofenceRequisicao, intecaoPendente)
            }
        }else{
            geofencingClient.addGeofences(geofenceRequisicao, intecaoPendente)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == geofenceRequerida){
            if (permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(
                    this,
                    "O aplicativo para seu funcionamento correto, necessita da permissão de acesso à localização!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        if(requestCode == getCoordenada){
            if(grantResults.isNotEmpty()&& (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)){
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
                onMapReady(mMap)
            }
            else{
                Toast.makeText(
                    this,
                    "O aplicativo para seu funcionamento correto, necessita da permissão de acesso à localização!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }
     companion object {
        fun showNotification(context: Context, mensagem: String){
            val canal_id = "canal de notificação dos dados"
            var notificationId =  6669
            notificationId += Random(notificationId).nextInt(1,100)
            val notificationBuild = NotificationCompat.Builder(context.applicationContext, canal_id)
                .setSmallIcon(R.drawable.ic_notifications_24)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(mensagem)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(mensagem)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            val NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as  NotificationManager
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val canal = NotificationChannel(
                    canal_id,
                    context.getString(R.string.app_name),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = context.getString(R.string.app_name) }
                NotificationManager.createNotificationChannel(canal)
                }
            NotificationManager.notify(notificationId,notificationBuild.build())
            }
    }

}