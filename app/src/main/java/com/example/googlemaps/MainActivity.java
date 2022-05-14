package com.example.googlemaps;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //Błąd przy nieaktualnej wersji usług google

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(isServicesOK()){
            init();
        }
    }
    private void init(){
        Button buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }

    //Sprawdzanie poprawnej/minimalnej wersji Google services danego urządzenia

    public boolean isServicesOK(){
        Log.d(TAG, getString(R.string.isServicesOK_1));

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //Wszystko działa poprawnie
            Log.d(TAG, getString(R.string.isServicesOK_2));
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //Występuje jakiś błąd ale można go rozwiązać
            Log.d(TAG, getString(R.string.isServicesOK_3));
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, R.string.Cant_run_map, Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}