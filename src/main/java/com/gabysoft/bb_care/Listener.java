package com.gabysoft.bb_care;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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

public class Listener extends Service {

    private String ip, puerto;
    private String parameter;
    private static final String TAG = "TareaAsincrona";

    HttpRequestAsyncTask request;

    public Listener() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "Monitoreo Iniciado...", Toast.LENGTH_SHORT).show();
        request = new HttpRequestAsyncTask(this, parameter, ip, puerto, "pin");  //Instancio la tarea

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ip = intent.getStringExtra("ip");
        puerto = intent.getStringExtra("puerto");
        parameter = intent.getStringExtra("parametro");

        request = new HttpRequestAsyncTask(this, parameter, ip, puerto, "pin");  //Instancio la tarea

        request.execute(); //Inicio la tarea

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Monitoreo Detenido.", Toast.LENGTH_SHORT).show();
        request.onCancelled("");
        super.onDestroy();
        //nManager.cancel(ID_NOTIF);
    }


    private class HttpRequestAsyncTask extends AsyncTask<String, String, String>{

        // declare variables needed
        private String requestReply, ipAddress, portNumber;
        private Context context;
//        private AlertDialog alertDialog;
        private String parameter;
        private String parameterValue;
        private String mensaje;
        private int i;
        private boolean doRequest = false;

        private NotificationManager nManager;
        private static final int ID_NOTIF = 1234;

        //Configuro la notificación que voy a mostrar si hay alertas
        // Patrón de vibración: 1 segundo vibra, 0.5 segundos para, 1 segundo vibra
        long vibrate[]={1000,500,1000,2000,1000,500,1000};
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        private NotificationCompat.Builder notificationBuilder;


        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber, String parameter) {
            this.context = context;

            /*alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From IP Address:")
                    .setCancelable(true)
                    .create();*/

            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.parameter = parameter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            i=1;
            //Seteo la variable que va a hacer el request infinito
            doRequest = true;
//            Toast.makeText(context, "Valor de i: " + i, Toast.LENGTH_SHORT).show();

            //Inicializo el gestor de notificaciones
            nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //Creamos la notificacion y le pasamos el contexto,
            notificationBuilder = new NotificationCompat.Builder(
                    getBaseContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Alerta BB-Care")
                    .setVibrate(vibrate)    //Agrega vibración
                    .setSound(defaultSound) //Agrega el sonido de la notificación
                    .setAutoCancel(true)    //Permite que se elimine de la barra al pulsarla
                    .setLights(Color.RED, 1, 0)
                    .setWhen(System.currentTimeMillis());
        }

        @Override
        protected String doInBackground(String... params) {
            while(doRequest){
                requestReply = sendRequest(parameterValue, ipAddress, portNumber, parameter);
                Log.i(TAG, "AsyncTask" + "Recibió" + requestReply);
                publishProgress(""+i);

                try{
                    Thread.sleep(3000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(requestReply.contains("refused")) {
                Toast.makeText(getApplicationContext(), "Conexión Rechazada", Toast.LENGTH_SHORT).show();
            }
//            Toast.makeText(getApplicationContext(), requestReply.toString(), Toast.LENGTH_SHORT).show();
            switch (requestReply.toString()){
                case "0":
                    break;
                case "1":
                    mensaje = "Alerta de movimiento";
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    notificationBuilder.setContentText("Alerta de Movimiento");
                    //Hasta acá creé la notificación e indiqué cuando se inicia pero el notification manager es
                    //el que tiene que habilitarla
                    nManager.notify(ID_NOTIF, notificationBuilder.build());
                    break;
                case "2":
                    mensaje = "Alerta de ruido";
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    notificationBuilder.setContentText("Alerta de Ruido");
                    nManager.notify(ID_NOTIF, notificationBuilder.build());
                    break;
                case "3":
                    mensaje = "Alerta de movimiento y ruido";
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    notificationBuilder.setContentText("Alerta de Ruido y Movimiento");
                    nManager.notify(ID_NOTIF, notificationBuilder.build());
                    break;
            }
        }

        @Override
        protected void onCancelled(String s) {
//            Toast.makeText(context, "Valor de i: " + i, Toast.LENGTH_SHORT).show();
            super.onCancelled(s);
            i=101;
            doRequest = false;
            nManager.cancel(ID_NOTIF);
        }

        public String sendRequest(String parameterValue, String ipAddress, String portNumber, String parameterName) {
            String serverResponse = "ERROR";

            try {

                HttpClient httpclient = new DefaultHttpClient(); // create an HTTP client
                // define the URL e.g. http://myIpaddress:myport/?pin=13 (to toggle pin 13 for example)
                URI website = new URI("http://" + ipAddress + ":" + portNumber + "/?" + parameterName + "=" + parameterValue);
                HttpGet getRequest = new HttpGet(); // create an HTTP GET object
                getRequest.setURI(website); // set the URL of the GET request
                HttpResponse response = httpclient.execute(getRequest); // execute the request
                // get the ip address server's reply
                InputStream content = null;
                content = response.getEntity().getContent();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        content
                ));
                serverResponse = in.readLine();
                // Close the connection
                content.close();
            } catch (ClientProtocolException e) {
                // HTTP error
                serverResponse = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                // IO error
                serverResponse = e.getMessage();
                e.printStackTrace();
            } catch (URISyntaxException e) {
                // URL syntax error
                serverResponse = e.getMessage();
                e.printStackTrace();
            }
            // return the server's reply/response text
            return serverResponse;
        }
    }
}