package com.bouami.com.clientsocket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by Mohammed on 15/02/2017.
 */

public class ClientConnexion implements Runnable {
    private Socket connexion = null;
    private PrintWriter writer = null;
    private BufferedInputStream reader = null;
    private int mport = 0;
    private String madresseserver = null;
    private Handler mUpdateHandler;


    //Notre liste de commandes. Le serveur nous répondra différemment selon la commande utilisée.
    private String[] listCommands = {"FULL", "DATE", "HOUR", "CLOSE"};
    private static int count = 0;
    private String name = "Client-";

    public ClientConnexion(String host, int port,Handler handler){
        name += ++count;
        madresseserver = host;
        mport = port;
        mUpdateHandler = handler;
//        try {
//            connexion = new Socket(madresseserver, mport);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void AfficherMessage(String message) {
        Message clientMessage = Message.obtain();
        clientMessage.obj = message;
        mUpdateHandler.sendMessage(clientMessage);
    }

    @Override
    public void run() {
        try {
            connexion = new Socket(madresseserver, mport);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //nous n'allons faire que 10 demandes par thread...
//        for(int i =0; i < 10; i++){
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {


                writer = new PrintWriter(connexion.getOutputStream(), true);
                reader = new BufferedInputStream(connexion.getInputStream());
                //On envoie la commande au serveur

                String commande = getCommand();
                writer.write(commande);
                //TOUJOURS UTILISER flush() POUR ENVOYER RÉELLEMENT DES INFOS AU SERVEUR
                writer.flush();
                //On attend la réponse
                String response = read();
                AfficherMessage(name + " Commande : "+commande + " envoyée au serveur"+ " : Réponse reçue " + response);
//                MainActivity.getZoneAffichage().setText("CLIENT_CONNEXION : " + name + " : Réponse reçue " + response);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        }

        writer.write("CLOSE");
        writer.flush();
        writer.close();
    }

    //Méthode qui permet d'envoyer des commandeS de façon aléatoire
    private String getCommand(){
        Random rand = new Random();
        return listCommands[rand.nextInt(listCommands.length)];
    }

    //Méthode pour lire les réponses du serveur
    private String read() throws IOException{
        String response = "";
        int stream;
        byte[] b = new byte[4096];
        stream = reader.read(b);
        response = new String(b, 0, stream);
        return response;
    }
}
