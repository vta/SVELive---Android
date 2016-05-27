package org.vta.svel.expresslaneslive;

import android.os.AsyncTask;
import android.util.Log;


import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by falconer_k on 5/26/16.
 */

public class WebSocketController {

    private static String SERVER;

    private static String TAG = "WebSocketController";

    private static boolean running = false;

    public WebSocketController(String server) {
        this.SERVER = server;
        running = true;
        new listenTask().execute(SERVER);
    }


    public void disconnect() {
        running = false;
    }

    private class listenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Log.d(TAG, "Trying to get web socket connection.");
            try {
                final Socket socket = IO.socket(urls[0]);

                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "Socket event 'connect' occurred.");
                        // socket.disconnect();
                    }

                }).on("toll", new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        if (!running) {
                            socket.disconnect();
                        }
                        Log.d(TAG, "Socket event 'toll' occurred.");
                        JSONArray tollResponse = (JSONArray) args[0];
                        EventBus.getDefault().post(new TollMessageEvent(tollResponse.toString()));

                    }

                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(TAG, "Socket event 'disconnect' occurred.");
                    }

                });
                socket.connect();
            } catch (URISyntaxException e) {
                Log.d(TAG, e.toString());
            }
            return null;
        }
    }

}
