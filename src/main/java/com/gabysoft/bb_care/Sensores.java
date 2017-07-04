package com.gabysoft.bb_care;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.text.DecimalFormat;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class Sensores extends Activity implements SensorEventListener {

    private SensorManager miSensorManager;
    private TextView      acelerometro;
    private TextView      giroscopo;
    private TextView      gravedad;
    private TextView      proximity;
    private TextView      detecta;
    private float xAcelerometro;
    private float yAcelerometro;
    private float zAcelerometro;

    private float xGiroscopo;
    private float yGiroscopo;
    private float zGiroscopo;

    private float xGravedad;
    private float yGravedad;
    private float zGravedad;

    private String txtProximity;
    private String txtAcelerometro;
    private String txtGravedad;
    private String txtGiroscopo;

    private String ipAddress;
    private String portNumber;

    private NotificationManager nManager;
    private Intent miIntent;

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;

    DecimalFormat         dosdecimales = new DecimalFormat("###.###");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensores);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        miIntent = new Intent(this, Listener.class);//Instancio el Intent que le voy a pasar al servicio

        ipAddress = sharedPreferences.getString(PREF_IP, "");
        portNumber = sharedPreferences.getString(PREF_PORT, "");
        miIntent.putExtra("ip", ipAddress);
        miIntent.putExtra("puerto", portNumber);
        miIntent.putExtra("parametro", "99");

        // Defino los botones
        Button sensoresButton = (Button) findViewById(R.id.listado);
        Button limpiaButton   = (Button) findViewById(R.id.limpia);

        // Defino los TXT para representar los datos de los sensores
        acelerometro  = (TextView) findViewById(R.id.acelerometro);
        giroscopo     = (TextView) findViewById(R.id.giroscopo);
        proximity     = (TextView) findViewById(R.id.proximity);
        gravedad      = (TextView) findViewById(R.id.gravedad);

        detecta     = (TextView) findViewById(R.id.detecta);
        // Accedemos al servicio de sensores
        miSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensoresButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Intent i = new Intent();
                i.setClass(Sensores.this, ListaSensoresActivity.class);

                startActivity(i);
            }
        });

        // Limpio el texto de la deteccion
        limpiaButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                detecta.setText("");
                detecta.setBackgroundColor(Color.parseColor("#000000"));
            }
        });

    }

    // Metodo para iniciar el acceso a los sensores
    protected void Ini_Sensores() {
        miSensorManager.registerListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        miSensorManager.registerListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        miSensorManager.registerListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),       SensorManager.SENSOR_DELAY_NORMAL);
        miSensorManager.registerListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),         SensorManager.SENSOR_DELAY_NORMAL);


    }

    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores() {

        miSensorManager.unregisterListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        miSensorManager.unregisterListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        miSensorManager.unregisterListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        miSensorManager.unregisterListener(this, miSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));

    }


    // Metodo que escucha el cambio de los sensores

    @Override
    public void onSensorChanged(SensorEvent event) {

        txtProximity = "";
        txtAcelerometro = "";
        txtGravedad = "";
        txtGiroscopo = "";

        // Cada sensor puede lanzar un thread que pase por aqui
        // Para asegurarnos ante los accesos simult�neos sincronizamos esto

        synchronized (this) {
            Log.d("sensor", event.sensor.getName());

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:


                    xAcelerometro = event.values[0];
                    yAcelerometro = event.values[1];
                    zAcelerometro = event.values[2];


                    break;

                case Sensor.TYPE_GYROSCOPE:

                    xGiroscopo = event.values[0];
                    yGiroscopo = event.values[1];
                    zGiroscopo = event.values[2];
                    break;

                case Sensor.TYPE_GRAVITY :


                    xGravedad = event.values[0];
                    yGravedad = event.values[1];
                    zGravedad = event.values[2];
                    break;

                case Sensor.TYPE_PROXIMITY :

                    txtProximity = "proximity\n" + event.values[0];
            }


        }

        txtAcelerometro += "acelerometro\n";
        txtAcelerometro += "x: " + dosdecimales.format(xAcelerometro) + " m/seg2 \n";
        txtAcelerometro += "y: " + dosdecimales.format(yAcelerometro) + " m/seg2 \n";
        txtAcelerometro += "z: " + dosdecimales.format(zAcelerometro) + " m/seg2 \n";
        acelerometro.setText(txtAcelerometro);

        if ((xAcelerometro > 15) || (yAcelerometro > 15) || (zAcelerometro > 15)) {
            detecta.setBackgroundColor(Color.parseColor("#cf091c"));
            detecta.setText("Vibracion Detectada"+ xAcelerometro + " " + yAcelerometro+" "+zAcelerometro);
            nManager.cancel(1234);
            startService(miIntent);
        }

        txtGiroscopo += "giroscopo\n";
        txtGiroscopo += "x: " + dosdecimales.format(xGiroscopo) + " deg/s \n";
        txtGiroscopo += "y: " + dosdecimales.format(yGiroscopo) + " deg/s \n";
        txtGiroscopo += "z: " + dosdecimales.format(zGiroscopo) + " deg/s \n";
        giroscopo.setText(txtGiroscopo);

        txtGravedad += "Gravedad\n";
        txtGravedad += "x: " + event.values[0] + "\n";
        txtGravedad += "y: " + event.values[1] + "\n";
        txtGravedad += "z: " + event.values[2] + "\n";

        gravedad.setText(txtGravedad);

        proximity.setText(txtProximity);
    }

    // Metodo que escucha el cambio de sensibilidad de los sensores

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop()
    {

        Parar_Sensores();

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Parar_Sensores();

        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        Parar_Sensores();

        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Ini_Sensores();
    }

}


