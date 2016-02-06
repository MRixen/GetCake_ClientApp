package com.example.manuelrixen.getcake.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.manuelrixen.getcake.BaseData;

public class Receiver extends Thread {

    private Handler threadHandler;
    private final String ip = "192.168.1.63";
    private final String port = "4555";
    private NetClient nc;
    private Thread receiverThread = null;

    public Receiver() {
        if (nc == null) nc = new NetClient(this.ip, Integer.parseInt(this.port));
    }

    public void startThread() {
        receiverThread = new Thread(this);
        receiverThread.start();
    }

    public void run() {
        boolean connEstabl = nc.connectWithServer();

        if(connEstabl){
            // Send start motor signal at first
            nc.sendDataAsString("1\n");
        }
            Looper.prepare();
            // Handler to cancel the message loop
            threadHandler = new Handler();

            BaseData.sendToDataAcquisition = new Handler() {
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    int data = bundle.getInt("motor", 0);
                    nc.sendDataAsString(String.valueOf(data)+"\n");
                }
            };

            Looper.loop();
    }
}
