package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Clase principal de la aplicación que permite enviar alertas de estado junto con la ubicación del usuario
 * a través de WhatsApp.
 */
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // Código de solicitud para el permiso de ubicación
    private FusedLocationProviderClient fusedLocationClient; // Cliente para obtener la ubicación
    private String numeroWhatsApp = "+51930822985"; // Número de WhatsApp al que se enviarán las alertas

    /**
     * Método que se ejecuta al crear la actividad. Configura los botones y solicita la ubicación.
     * @param savedInstanceState Estado previo de la aplicación.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtiene referencias a los botones
        Button btnRojo = findViewById(R.id.btnRojo);
        Button btnAmbar = findViewById(R.id.btnAmbar);
        Button btnVerde = findViewById(R.id.btnVerde);

        // Configura los listeners para enviar alertas según el estado seleccionado
        btnRojo.setOnClickListener(view -> sendAlert("Rojo - Grave"));
        btnAmbar.setOnClickListener(view -> sendAlert("Ámbar - Precaución"));
        btnVerde.setOnClickListener(view -> sendAlert("Verde - Seguro"));
    }

    /**
     * Método para enviar una alerta con el estado seleccionado y la ubicación del usuario.
     * @param status Estado de la alerta a enviar.
     */
    private void sendAlert(String status) {
        // Verifica si se tiene permiso de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Obtiene la última ubicación conocida
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Construye el mensaje con la ubicación
                String locationMessage = "Ubicación: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                String alertMessage = "Estado: " + status + "\n" + locationMessage;
                sendWhatsAppMessage(alertMessage);
            } else {
                Toast.makeText(MainActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para enviar un mensaje de alerta a través de WhatsApp.
     * @param message Mensaje que se enviará.
     */
    private void sendWhatsAppMessage(String message) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + numeroWhatsApp + "&text=" + Uri.encode(message)));
        sendIntent.setPackage("com.whatsapp");

        // Verifica si WhatsApp está instalado antes de intentar enviar el mensaje
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(sendIntent);
        } else {
            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método que maneja la respuesta del usuario a la solicitud de permisos.
     * @param requestCode Código de solicitud de permisos.
     * @param permissions Permisos solicitados.
     * @param grantResults Resultados de los permisos concedidos o denegados.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
