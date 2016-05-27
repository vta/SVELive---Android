package org.vta.svel.expresslaneslive;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String SERVER = "http://ec2-54-218-16-105.us-west-2.compute.amazonaws.com";

    Map<String, String> PLAZAS;


    private static WebSocketController websocketController;

    private static final String TAG = "SVEL_Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PLAZAS  = new HashMap<String, String>(){{
            put("CLW", getString(R.string.CLW));
            put("FSE", getString(R.string.FSE));
        }};

        setContentView(R.layout.activity_main);
    }

    private void setTollValue(Toll[] tolls) {
        StringBuilder message = new StringBuilder();
        String price = "";
        for (int i = 0; i < tolls.length; i++) {
            message.append(PLAZAS.get(tolls[i].Plaza_Name));
            price = TextUtils.isEmpty(tolls[i].Pricing_Module) ? "" : " : $" + tolls[i].Pricing_Module;
            message.append(price);
            message.append("\n"+tolls[i].Message_Module);
            message.append("\n\n");
        }
        TextView textView = (TextView) findViewById(R.id.el_textview);
        if (textView != null && !textView.getText().equals(message.toString())) {
            textView.setText(message.toString());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TollMessageEvent event) {
        Log.d(TAG, "onMessageEvent() "+event.message);
        Gson g = new Gson();
        Toll[] tolls = g.fromJson(event.message, Toll[].class);
        setTollValue(tolls);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        websocketController = new WebSocketController(SERVER);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        websocketController.disconnect();
        super.onStop();
    }


}
