package com.inez.squarethingywithfirebase;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.sdk.pos.ChargeRequest;
import com.squareup.sdk.pos.PosApi;
import com.squareup.sdk.pos.PosClient;
import com.squareup.sdk.pos.PosSdk;

import java.util.concurrent.TimeUnit;

import static com.squareup.sdk.pos.CurrencyCode.USD;

public class ChargeActivity extends AppCompatActivity {

    public String TAG = "###ChargeActivity";

    private PosClient posClient;

    private static final int CHARGE_REQUEST_CODE = 1;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);

        posClient = PosSdk.createClient(this, "sq0idp-BIuYEejEF2F7vq-6epKXfQ");

        id = (String) getIntent().getSerializableExtra("ID");

        Log.d(TAG, "onCreate id:" + id);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("chargeRequests/" + id);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded key:" + dataSnapshot.getKey());
                Log.d(TAG, "onChildAdded value:" + dataSnapshot.getValue());

                int amount = Integer.parseInt((String) dataSnapshot.getValue()) * 100;

                ChargeRequest request = new ChargeRequest.Builder(amount, USD).autoReturn(3200, TimeUnit.MILLISECONDS).build();

                try {
                    Intent intent = posClient.createChargeIntent(request);
                    startActivityForResult(intent, CHARGE_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    showDialog("Error", "Square Point of Sale is not installed", null);
                    posClient.openPointOfSalePlayStoreListing();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHARGE_REQUEST_CODE) {
            if (data == null) {
                showDialog("Error", "Square Point of Sale was uninstalled or crashed", null);
                return;
            }
            if (resultCode == Activity.RESULT_OK) {
                ChargeRequest.Success success = posClient.parseChargeSuccess(data);
                String message = "Client transaction id: " + success.clientTransactionId;
//                showDialog("Success!", message, null);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("transactions/" + id);
                String key = myRef.push().getKey();
                myRef.child(key).setValue(success.clientTransactionId);
            } else {
                ChargeRequest.Error error = posClient.parseChargeError(data);

                if (error.code == ChargeRequest.ErrorCode.TRANSACTION_ALREADY_IN_PROGRESS) {
                    String title = "A transaction is already in progress";
                    String message = "Please complete the current transaction in Point of Sale.";

                    showDialog(title, message, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            // Some errors can only be fixed by launching Point of Sale
                            // from the Home screen.
                            posClient.launchPointOfSale();
                        }
                    });
                } else {
                    showDialog("Error: " + error.code, error.debugDescription, null);
                }
            }
        }
    }

    private void showDialog(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }
}
