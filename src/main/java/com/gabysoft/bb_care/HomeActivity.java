package com.gabysoft.bb_care;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class HomeActivity extends Activity implements View.OnClickListener {

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    // Uso estas variables para guardar la IP y el puerto ingresado para no tener que cargarlo siempre que se abra la app.
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    // declaro botones e inputs
    private Button buttonStartListener, buttonStopListener, buttonSensores;
    private EditText editTextIPAddress, editTextPortNumber;
    private TextView textViewEstado;

    //Declaro el Intent que le voy a pasar al servicio
    private Intent miIntent;
    private Intent miIntent2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // asigno los botones
        buttonStartListener = (Button) findViewById(R.id.buttonStartListener);
        buttonStopListener = (Button) findViewById(R.id.buttonStopListener);
        buttonSensores = (Button) findViewById(R.id.buttonSensores);

        // asigno los cuadros de texto
        editTextIPAddress = (EditText) findViewById(R.id.editTextIPAddress);
        editTextPortNumber = (EditText) findViewById(R.id.editTextPortNumber);
        textViewEstado = (TextView) findViewById(R.id.textView4);

        // seteo el listener (this class)
        buttonStartListener.setOnClickListener(this);
        buttonStopListener.setOnClickListener(this);
        buttonSensores.setOnClickListener(this);

        // recupero la IP y el puerto de la última sesión
        // pongo vacío si es la primera vez
        editTextIPAddress.setText(sharedPreferences.getString(PREF_IP, ""));
        editTextPortNumber.setText(sharedPreferences.getString(PREF_PORT, ""));

        miIntent = new Intent(this, Listener.class);//Instancio el Intent que le voy a pasar al servicio
        miIntent2 = new Intent(this, Sensores.class);
    }


    @Override
    public void onClick(View view) {

        // seteo el pinNumber
        String parameterValue = "";
        // tomo la ip
        String ipAddress = editTextIPAddress.getText().toString().trim();
        // tomo el puerto
        String portNumber = editTextPortNumber.getText().toString().trim();

        miIntent.putExtra("ip", ipAddress);
        miIntent.putExtra("puerto", portNumber);


        // Guardo la IP y el puerto para el próximo uso
        editor.putString(PREF_IP, ipAddress);
        editor.putString(PREF_PORT, portNumber);
        editor.commit(); // guardo IP y puerto

        // recuepero el pinNumber del botón presionado
        if (view.getId() == buttonStartListener.getId()) {
            textViewEstado.setText("INICIADO");
            textViewEstado.setTextColor(Color.GREEN);
            parameterValue = "99";
            miIntent.putExtra("parametro", parameterValue); //Cargo el parametro en el Intent
            startService(miIntent);
        } else if (view.getId() == buttonStopListener.getId()) {
            textViewEstado.setText("DETENIDO");
            textViewEstado.setTextColor(Color.RED);
            parameterValue = "98";
            miIntent.putExtra("parametro", parameterValue); //Cargo el parametro en el Intent
            stopService(miIntent);
        } else if (view.getId() == buttonSensores.getId()){
            startActivity(miIntent2);
        }
    }
}