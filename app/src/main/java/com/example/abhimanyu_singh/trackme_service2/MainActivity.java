package com.example.abhimanyu_singh.trackme_service2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {

    private static final int MENU_LOGOUT = Menu.FIRST + 4;
    public static final String PREFS_NAME = "MyPrefsFile";
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    SharedPreferences settings;
    Button bstart,bstop;
    Intent locationservice,location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("login", false);
        editor.putString("email", "");
        editor.commit();
        setContentView(R.layout.activity_main);
        startsignin();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        bstart=(Button)findViewById(R.id.bstart);
        bstop=(Button)findViewById(R.id.bstop);
        bstart.setOnClickListener(this);
        bstop.setOnClickListener(this);
    }

    private void startsignin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //signIn();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(settings.getBoolean("login",false)&&menu.findItem(MENU_LOGOUT)==null) {
            menu.add(0, MENU_LOGOUT, Menu.NONE, "Sign Out");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id== MENU_LOGOUT)
        {
            final MenuItem finalitem=item;
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // [START_EXCLUDE]
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("login", false);
                            editor.putString("email", "");
                            editor.commit();
                            stopService(locationservice);
                            finalitem.setVisible(false);
                            // [END_EXCLUDE]
                        }
                    });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.bstart:
                if(!settings.getBoolean("login",false))
                {
                    signIn();
                }
                else {
                    Toast.makeText(this, "Transmitting location with " + settings.getString("email", ""), Toast.LENGTH_LONG).show();
                    locationservice=new Intent(this,LocationService.class);
                    startService(locationservice);
                }
                final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    location = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(location);
                }

                break;
            case R.id.bstop:
                stopService(locationservice);
                break;
            case R.id.fab:Intent getloc=new Intent(this,getlocation.class);
                startActivity(getloc);
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            editor.putBoolean("login", true);
            editor.putString("email", acct.getEmail());
            Toast.makeText(this,"Transmitting location with "+acct.getEmail(),Toast.LENGTH_LONG).show();
            locationservice=new Intent(this,LocationService.class);
            startService(locationservice);
        } else {
            // Signed out, show unauthenticated UI.
            editor.putBoolean("login", false);
            System.exit(0);
        }
        editor.commit();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"Connection failed",Toast.LENGTH_LONG).show();
        finish();
    }
}
