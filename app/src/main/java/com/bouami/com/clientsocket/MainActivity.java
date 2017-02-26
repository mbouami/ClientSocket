package com.bouami.com.clientsocket;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements DataDisplay {
    private static TextView serverMessage = null;
    Thread m_objThreadClient;
    Socket clientSocket;
    private WifiManager wifiManager;
    private String wifiencours;
    private Handler mUpdateHandler;
    DataDisplay m_dataDisplay;
    private final int REQUEST_PERMISSION_STATE = 1;
    private  static int PORT=60123;
    private static String IPADRESSE="192.168.1.60";
//    private static final String IPADRESSE="127.0.0.1";

    public static TextView getZoneAffichage() {
        return serverMessage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverMessage = (TextView) findViewById(R.id.message);
        ((TextView) findViewById(R.id.adresseip)).setText(IPADRESSE);
        ((TextView) findViewById(R.id.port)).setText(""+PORT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_STATE);
        }
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = (connMgr.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI);
        boolean isMobileConn = (connMgr.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_MOBILE);
        wifiManager= (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            if (isWifiConn) wifiencours = "Connexion Wifi activée : "+wifiManager.getConnectionInfo().getSSID();
        }

        if (isMobileConn) {
            wifiencours = "Connexion Mobile activée : "+ connMgr.getActiveNetworkInfo().getTypeName();
        }
        mUpdateHandler = new Handler(){

            @Override
            public void handleMessage(Message status) {
                serverMessage.setText(status.obj.toString());
            }
        };

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STATE: {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                return;
            }
        }
    }
    public void Start(View view) {
        m_objThreadClient = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket(IPADRESSE,PORT);
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject("Coucou....Type de connexion : "+wifiencours);
                    Message serverMessage = Message.obtain();
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    String strMessage = (String) ois.readObject();
                    serverMessage.obj = strMessage;
                    mUpdateHandler.sendMessage(serverMessage);
                    ois.close();
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        m_objThreadClient.start();
    }

    public void Timer(View view){
//        TimeServer ts = new TimeServer(host, port);
//        ts.open();

//        System.out.println("Serveur initialisé.");
        Log.d("CLIENT_CONNEXION","Client qui demande.");
        IPADRESSE = ((TextView) findViewById(R.id.adresseip)).getText().toString();
        PORT = parseInt(((TextView) findViewById(R.id.port)).getText().toString());
//        for(int i = 0; i < 5; i++){
            Thread t = new Thread(new ClientConnexion(IPADRESSE, PORT,mUpdateHandler));
            t.start();
//        }

    }

    @Override
    public void Display(String message) {
        serverMessage.setText(""+message);
    }
}
