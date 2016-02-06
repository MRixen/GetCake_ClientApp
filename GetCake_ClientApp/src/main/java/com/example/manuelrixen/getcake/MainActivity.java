package com.example.manuelrixen.getcake;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.manuelrixen.getcake.Socket.Receiver;

import static android.os.Process.myPid;

public class MainActivity extends BaseData {

    private static final int MAX_COUNTDOWN_VALUE = 3;
    private NetworkInfo mWifi;
    private PowerManager.WakeLock wl;
    private Receiver receiver = new Receiver();
    private Button cakeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


        cakeButton = (Button)findViewById(R.id.getCakeButton);
        receiver.startThread();
    }

    private Message setDataBundle(String key, int value) {
        Bundle bundle = new Bundle();
        Message msgStopMotor = Message.obtain();
        bundle.putInt(key, value);
        msgStopMotor.setData(bundle);

        return msgStopMotor;
    }

    private void executeButtonCountdown(final Activity activity, final Button button) {

        Bundle bundle = new Bundle();
        final Message msgStartMotor = Message.obtain();
        bundle.putInt("motor", 1);
        msgStartMotor.setData(bundle);

        Thread executeButtonCountdownThread = new Thread() {
            int countdown = MAX_COUNTDOWN_VALUE;

            @Override
            public void run() {
                while(countdown>-1) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Set countdown text to button
                            if(countdown > 0) button.setText(String.valueOf(countdown));
                            else{
                                button.setText("GET CAKE");
                                BaseData.sendToDataAcquisition.sendMessage(msgStartMotor);
                            }
                            countdown -= 1;
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        executeButtonCountdownThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wl.release();
        android.os.Process.killProcess(myPid());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Send stop-signal to server and show countdown
        try{
            BaseData.sendToDataAcquisition.sendMessage(setDataBundle("motor", 0));
        } catch (NullPointerException ex) {
            Log.d("onPause", String.valueOf(ex));
        }
    }

    public void onCakeButtonClicked(View view) {
        if(cakeButton.getText().equals("GET CAKE")) {
            try {
                // Send stop-signal to server and show countdown
                BaseData.sendToDataAcquisition.sendMessage(setDataBundle("motor", 0));
                executeButtonCountdown(this, cakeButton);
            } catch (NullPointerException ex) {
                Log.d("onCakeButtonClicked", String.valueOf(ex));
            }
        }
        else{
            // Do nothing until Countdown isnt finished
        }
    }
}
