package com.example.eva_4app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var btnMostrarDatos: Button
    private lateinit var tvTemperatura: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvTimestamp: TextView // Para mostrar el timestamp

    private val TAG = "MainActivity" // Tag para los logs

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        btnMostrarDatos = findViewById(R.id.btnMostrarDatos)
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvHumedad = findViewById(R.id.tvHumedad)
        tvTimestamp = findViewById(R.id.tvTimestamp)

        // Inicializar Firebase
        try {
            FirebaseApp.initializeApp(this)
            FirebaseDatabase.getInstance().setPersistenceEnabled(true) // Opcional: habilita el cache local
            Log.d("Firebase", "Firebase inicializado correctamente")
        } catch (e: Exception) {
            Log.e("Firebase", "Error al inicializar Firebase: ${e.message}")
        }

        // Probar conexión a Firebase (opcional, para depuración)
        verificarConexionFirebase()

        // Configurar listener del botón
        btnMostrarDatos.setOnClickListener {
            obtenerUltimosDatos()
        }
    }

    private fun verificarConexionFirebase() {
        val database = FirebaseDatabase.getInstance()
        val rootRef = database.reference

        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Conexión exitosa: ${snapshot.value}")
                } else {
                    Log.d(TAG, "Conexión exitosa, pero no hay datos")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al conectar con Firebase: ${error.message}")
            }
        })
    }

    private fun obtenerUltimosDatos() {
        // Referencia a la base de datos de Firebase
        val database = FirebaseDatabase.getInstance()
        val lecturasRef = database.getReference("lecturas/actual")

        // Agregar un log para indicar que la consulta se ha realizado
        Log.d(TAG, "Obteniendo datos de Firebase...")

        // Obtener los datos de "actual" (humedad, temperatura, timestamp)
        lecturasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val humedad = snapshot.child("humedad").getValue(Int::class.java)
                    val temperatura = snapshot.child("temperatura").getValue(Double::class.java)
                    val timestamp = snapshot.child("timestamp").getValue(String::class.java)

                    // Verificar si los valores son null y actualizarlos
                    if (humedad != null && temperatura != null && timestamp != null) {
                        // Mostrar los datos en los TextViews
                        tvTemperatura.text = "Temperatura: ${temperatura}°C"
                        tvHumedad.text = "Humedad: ${humedad}%"
                        tvTimestamp.text = "Última actualización: $timestamp"
                        Log.d(TAG, "Datos obtenidos correctamente: Temperatura = $temperatura, Humedad = $humedad, Timestamp = $timestamp")
                    } else {
                        // Si alguno de los valores es null, mostrar un mensaje
                        tvTemperatura.text = "Temperatura: No disponible"
                        tvHumedad.text = "Humedad: No disponible"
                        tvTimestamp.text = "Última actualización: No disponible"
                        Log.e(TAG, "Datos de Firebase incompletos: Temperatura, Humedad o Timestamp son nulos")
                    }
                } else {
                    // Si no existen datos
                    tvTemperatura.text = "Temperatura: No disponible"
                    tvHumedad.text = "Humedad: No disponible"
                    tvTimestamp.text = "Última actualización: No disponible"
                    Log.e(TAG, "No existen datos en la base de datos en la ruta especificada")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error de Firebase
                Toast.makeText(
                    this@MainActivity,
                    "Error al obtener datos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Error al obtener datos de Firebase: ${error.message}")
            }
        })
    }
}
