package com.inez.squarethingywithfirebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scandit.recognition.Barcode;

public class MainActivity extends AppCompatActivity {

    public String TAG = "###MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent launchIntent = new Intent(MainActivity.this, BarcodePickerActivity.class);
        launchIntent.putExtra("appKey", "JUDbjLm5EeCXI1/K4op7NSVBSQC5uRHglyqag3/fxZ0");
        launchIntent.putExtra("beep", false);
        launchIntent.putExtra("enabledSymbologies", new int[]{Barcode.SYMBOLOGY_QR});
        startActivityForResult(launchIntent, Ids.BARCODE_PICKER_REQUEST);

        /*
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        String key = myRef.push().getKey();
        myRef.child(key).setValue("Hello, World!");


        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        });
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Ids.BARCODE_PICKER_REQUEST) {
            if (data.getBooleanExtra("barcodeRecognized", false)) {
                String barcodeData = data.getStringExtra("barcodeData");

                Log.d(TAG, "onActivityResult barcodeData:" + barcodeData);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("links/" + barcodeData);
                myRef.setValue(true);

                Intent intent = new Intent(getBaseContext(), ChargeActivity.class);
                intent.putExtra("ID", barcodeData);
                startActivity(intent);
                finish();
            }
        }
    }
}
