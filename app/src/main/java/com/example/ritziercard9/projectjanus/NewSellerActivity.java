package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.PlaceDetails;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class NewSellerActivity extends AppCompatActivity {

    private static final String TAG = "NewSellerActivity";
    private ActionBar ab;
    private Button sendButton, cancelButton;
    private ProgressBar creatingSellerProgressCircle;

    private TextView nameTextView, phoneTextView, emailTextView;
    private PlacesAutocompleteTextView addressTextView;
    private ImageView image;

    private CollectionReference sellersCollection;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private Uri resultUri;
    private String croppedImageUrl;

    private String addressData;

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
        setupAddressListener();
    }

    private void init() {
        sendButton = findViewById(R.id.newSellerSendButton);
        cancelButton = findViewById(R.id.newSellerCancelButton);

        creatingSellerProgressCircle = findViewById(R.id.newSellerProgressCircle);

        addressTextView = findViewById(R.id.newSellerAddress);
        image = findViewById(R.id.newSellerImage);
    }

    private void initTextViews() {
        nameTextView = findViewById(R.id.newSellerName);
        phoneTextView = findViewById(R.id.newSellerPhone);
        emailTextView = findViewById(R.id.newSellerEmail);
    }

    private void setupAddressListener() {
        addressTextView.setOnPlaceSelectedListener(place -> {
            addressData = addressTextView.getText().toString();
            Log.d(TAG, "onPlaceSelected: " + addressData);

            addressTextView.getDetailsFor(place, new DetailsCallback() {
                @Override
                public void onSuccess(PlaceDetails placeDetails) {
                    Log.d(TAG, "==========extracting details for place==========");
                    Log.d(TAG, "details:" + placeDetails.formatted_address);
                    addressData = placeDetails.formatted_address;
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.d(TAG, "FAILED TO EXTRACT DETAILS FROM PLACE:", throwable);
                }
            });
        });
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        initTextViews();

        String name = nameTextView.getText().toString();
        String phone = phoneTextView.getText().toString();
        String address = this.addressData;
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

        // TODO: 1/16/2018 test what happens when address is not found/selected
        if (TextUtils.isEmpty(address)) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor ingrese la direccion de la tienda.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor ingrese el correo electronico.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (user == null) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "No se pudo validar el usario. Por favor inicie su sesio de nuevo.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (resultUri == null) {
            Snackbar.make(findViewById(R.id.newSellerContainer), "Por favor agregue una imagen.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("address", address);
        data.put("email", email);

        uploadImage(user.getUid(), data, name);
    }

    private void uploadImage(String uid, Map<String, Object> data, String name) {
        Log.d(TAG, "last path from image:" + resultUri.getLastPathSegment());
        StorageReference storageRef = storage.getReference().child("sellers/" + uid + "/" + resultUri.getLastPathSegment());

        storageRef.putFile(resultUri).addOnSuccessListener(taskSnapshot -> {
            croppedImageUrl = taskSnapshot.getDownloadUrl().toString();
            Log.d(TAG, "event image uploaded successfully:" + croppedImageUrl);

            data.put("image", croppedImageUrl);

            sellersCollection.add(data).addOnSuccessListener(documentReference -> {
                Log.d(TAG, "onSuccess: Seller added successfully with ID:" + documentReference.getId());
                Intent intent = new Intent();
                intent.putExtra("name", name);
                setResult(RESULT_OK, intent);
                finish();
            }).addOnFailureListener(e -> {
                Log.d(TAG, "onFailure: Error adding seller.");
                toggleButtonVisibilityOnProcessing(false);
            });
        }).addOnFailureListener(e -> {
            Log.d(TAG, "event image upload ERROR:" + e.getMessage());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Glide.with(getApplicationContext())
                        .load(resultUri)
                        .into(image);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "CROP ERROR:" + error.getMessage());
            }
        }
    }

    public void onAddImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setFixAspectRatio(true)
                .setAspectRatio(16, 9)
                .start(this);
    }
}
