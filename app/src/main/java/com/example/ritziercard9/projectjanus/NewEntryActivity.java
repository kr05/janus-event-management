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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class NewEntryActivity extends AppCompatActivity {

    private static final String TAG = "NewEntry";
    private ActionBar ab;

    private Button sendButton, cancelButton;
    private ProgressBar creatingEntryProgressCircle;

    private TextView nameTextView, phoneTextView, emailTextView;
    private ImageView image;

    private CollectionReference entryCollection;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private Uri resultUri;
    private String croppedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        entryCollection = db.collection("accessControl");

        Toolbar tb = findViewById(R.id.newEntryToolbar);
        setSupportActionBar(tb);

        // Get a support ActionBar corresponding to this toolbar
        ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        init();
    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("selected", "entry");
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    private void init() {
        sendButton = findViewById(R.id.newEntrySubmitButton);
        cancelButton = findViewById(R.id.newEntryCancelButton);

        creatingEntryProgressCircle = findViewById(R.id.newEntryProgressCircle);
        image = findViewById(R.id.newEntryImage);
    }

    private void initTextViews() {
        nameTextView = findViewById(R.id.newEntryName);
        phoneTextView = findViewById(R.id.newEntryPhone);
        emailTextView = findViewById(R.id.newEntryEmail);
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

    public void onAddEntryImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setFixAspectRatio(true)
                .setAspectRatio(1, 1)
                .start(this);
    }

    public void onCancelNewEntry(View view) {
        finish();
    }

    public void onSubmitNewEntry(View view) {
        toggleButtonVisibilityOnProcessing(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        initTextViews();

        String name = nameTextView.getText().toString();
        String phone = phoneTextView.getText().toString();
        String email = emailTextView.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Snackbar.make(findViewById(R.id.newEntryContainer), "Por favor ingrese el nombre de la tienda.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Snackbar.make(findViewById(R.id.newEntryContainer), "Por favor ingrese un numero de telefono.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(findViewById(R.id.newEntryContainer), "Por favor ingrese el correo electronico.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (user == null) {
            Snackbar.make(findViewById(R.id.newEntryContainer), "No se pudo validar el usario. Por favor inicie su sesio de nuevo.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (resultUri == null) {
            Snackbar.make(findViewById(R.id.newEntryContainer), "Por favor agregue una imagen.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("email", email);

        uploadImage(user.getUid(), data, name);
    }

    private void uploadImage(String uid, Map<String, Object> data, String name) {
        Log.d(TAG, "last path from image:" + resultUri.getLastPathSegment());
        StorageReference storageRef = storage.getReference().child("accessControl/" + uid + "/" + resultUri.getLastPathSegment());

        storageRef.putFile(resultUri).addOnSuccessListener(taskSnapshot -> {
            croppedImageUrl = taskSnapshot.getDownloadUrl().toString();
            Log.d(TAG, "event image uploaded successfully:" + croppedImageUrl);

            data.put("image", croppedImageUrl);

            entryCollection.add(data).addOnSuccessListener(documentReference -> {
                Log.d(TAG, "onSuccess: Entry added successfully with ID:" + documentReference.getId());
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
            creatingEntryProgressCircle.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
        } else {
            creatingEntryProgressCircle.setVisibility(View.INVISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }
}
