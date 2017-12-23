package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewSellerActivity extends AppCompatActivity {

    private static final String TAG = "NewSellerActivity";
    private ActionBar ab;
    private Button sendButton, cancelButton;
    private ProgressBar creatingSellerProgressCircle;
    private TextView nameTextView, phoneTextView, addressTextView, cityTextView, emailTextView;
    private CollectionReference sellersCollection;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_seller);

        sellersCollection = db.collection("sellers");

        Toolbar myToolbar = findViewById(R.id.newSellerToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        init();
    }

    private void init() {
        sendButton = findViewById(R.id.newSellerSendButton);
        cancelButton = findViewById(R.id.newSellerCancelButton);

        creatingSellerProgressCircle = findViewById(R.id.newSellerProgressCircle);
    }

    private void initTextViews() {
        nameTextView = findViewById(R.id.newSellerName);
        phoneTextView = findViewById(R.id.newSellerPhone);
        addressTextView = findViewById(R.id.newSellerAddress);
        cityTextView = findViewById(R.id.newSellerCity);
        emailTextView = findViewById(R.id.newSellerEmail);
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("selected", "sellers");
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    public void onNewSellerCancel(View view) {
        finish();
    }

    public void onNewSellerSend(View view) {
        toggleButtonVisibilityOnProcessing(true);
        initTextViews();

        String name = nameTextView.getText().toString();
        String phone = phoneTextView.getText().toString();
        String address = addressTextView.getText().toString();
        String city = cityTextView.getText().toString();
        String email = emailTextView.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor ingrese el nombre de la tienda.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor ingrese un numero de telefono.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(address)) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor ingrese la direccion de la tienda.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(city)) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor la cuidad y el estado.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor ingrese el correo electronico.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("address", address);
        data.put("city", city);
        data.put("email", email);

        sellersCollection.add(data).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "onSuccess: Receipt added successfully with ID:" + documentReference.getId());
            Intent intent = new Intent();
            intent.putExtra("name", name);
            setResult(RESULT_OK, intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: Error adding receipt.");
            toggleButtonVisibilityOnProcessing(false);
        });
    }

    private void toggleButtonVisibilityOnProcessing(boolean processing) {
        if (processing) {
            creatingSellerProgressCircle.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
        } else {
            creatingSellerProgressCircle.setVisibility(View.INVISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }
}
