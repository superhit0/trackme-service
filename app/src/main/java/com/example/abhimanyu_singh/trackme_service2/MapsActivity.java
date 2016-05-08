package com.example.abhimanyu_singh.trackme_service2;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Emitter.Listener {

    private GoogleMap mMap;
    public static final String PREFS_NAME = "MyPrefsFile";
    private Socket socket;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            email = extras.getString("email");
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            socket = IO.socket("http://188.166.229.68:3001");
        } catch (URISyntaxException e) {
        } finally {
            socket.on("server-event", this);
            socket.on("not-found-server-event",new Emitter.Listener(){

                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapsActivity.this,"Not Registered User",Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                }
            });
            socket.connect();
        }
        emitmail();
    }

    private void emitmail() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("client-event", obj);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }


    @Override
    public void call(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                try {
                    mMap.clear();
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    String lat = data.getString("latitude");
                    String lon = data.getString("longitude");
                    LatLng loc=new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                    mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .title(settings.getString("email","")));

                } catch (JSONException e) {
                    return;
                }

                // add the message to view

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        socket.disconnect();
        socket.off("server-event", this);
    }
}
