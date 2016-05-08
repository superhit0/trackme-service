package com.example.abhimanyu_singh.trackme_service2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class getlocation extends AppCompatActivity implements View.OnClickListener {

    EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getlocation);
        Button showloc=(Button) findViewById(R.id.button);
        email=(EditText) findViewById(R.id.editText);
        showloc.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button:
                Intent map=new Intent(this,MapsActivity.class);
                map.putExtra("email",email.getText().toString());
                startActivity(map);
                break;
        }
    }
}
