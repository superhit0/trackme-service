package com.example.abhimanyu_singh.trackme_service2;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import static com.google.android.gms.internal.zzip.runOnUiThread;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, Emitter.Listener {

    GoogleApiClient mGoogleApiClient;
    public static final String PREFS_NAME = "MyPrefsFile";
    LocationRequest mLocationRequest;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private Socket socket;
    int mNotificationId = 001;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyMgr;

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();
        mGoogleApiClient.connect();
        try {
            socket = IO.socket("http://188.166.229.68:3000");
        } catch (URISyntaxException e) {
        } finally {
            socket.on("server-event", this);
            socket.connect();
        }

        return START_STICKY;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

        @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setContentTitle("You Are Being Tracked!")
                        .setSmallIcon(R.drawable.loc)
                        .setContentText("You Are Being Tracked!");
        mNotifyMgr=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, location.getLatitude() + " "+ location.getLongitude(), Toast.LENGTH_LONG).show();
        mBuilder.setContentText("Lat:"+location.getLatitude() + " Long:"+ location.getLongitude());
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
        emitit(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        socket.disconnect();
        socket.off("server-event", this);
        mNotifyMgr.cancel(mNotificationId);
    }

    private void emitit(Location loc) {
        JSONObject obj = new JSONObject();
        try {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            obj.put("email", settings.getString("email",""));
            obj.put("lat", Double.valueOf(loc.getLatitude()));
            obj.put("lon", Double.valueOf(loc.getLongitude()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("client-event", obj);
    }

    @Override
    public void call(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String chats;
                try {
                    chats = data.getString("text");
                } catch (JSONException e) {
                    return;
                }

                // add the message to view

            }
        });
    }
}
