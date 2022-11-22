package com.example.app_tcc

import android.content.BroadcastReceiver
import android.content.Context

import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class GeofenceReceptor: BroadcastReceiver() {
    lateinit var key: String
    lateinit var mensagem: String
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p0 != null){
            val geofencingEvent = p1?.let { GeofencingEvent.fromIntent(it) }
            val geofenceTransition = geofencingEvent?.geofenceTransition
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
                if (p1 != null){
                    key = p1.getStringExtra("key")!!
                    mensagem = p1.getStringExtra("mensagem")!!
                    var user = FirebaseAuth.getInstance().currentUser!!.uid
                    val firebase = Firebase.database
                    val reference = firebase.getReference(user)
                    val receptorLister = object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val lembrete = snapshot.getValue<lembrete>()
                            if (lembrete != null){
                                MapsActivity.showNotification(
                                    p0.applicationContext,
                                    mensagem
                                )
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            println("Dados: onCancelled:${error.details}")
                        }

                    }
                    val child = reference.child(key)
                    child.addValueEventListener(receptorLister)
                     }
                }
            }
    }

}