package com.example.ritziercard9.projectjanus;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewEventActivity extends AppCompatActivity {

    private static final String TAG = "NewEventActivity";
    private ActionBar ab;
    private Button sendButton, cancelButton;
    private ProgressBar creatingEventProgressCircle;
    private TextView bandTextView, capacityTextView, descriptionTextView;
    private Button dateButton, timeButton, addImageButton;
    private ImageView image;
    private CollectionReference eventsCollection;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String dateData, timeData, addressData;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private PlacesAutocompleteTextView addressTextView;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Uri resultUri;
    private String croppedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        eventsCollection = db.collection("events");

        Toolbar myToolbar = findViewById(R.id.newEventToolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_close);

        init();
        setupDateSetListener();
        setupTimeSetListener();
        setupAddressListener();
    }

    private void setupAddressListener() {
        addressTextView.setOnPlaceSelectedListener(place -> {
            Log.d(TAG, "onPlaceSelected: " + addressData);
            addressData = addressTextView.getText().toString();

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

    private void setupTimeSetListener() {
        mTimeSetListener = (timePicker, hour, minute) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(Calendar.HOUR_OF_DAY, hour);
            selectedTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            timeData = sdf.format(selectedTime.getTime());
            Log.d(TAG, "setupTimeSetListener: " + timeData);
            timeButton.setText(timeData);
        };
    }

    private void setupDateSetListener() {
        mDateSetListener = (datePicker, year, month, day) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, day);
            selectedDate.set(Calendar.YEAR, year);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dateData = sdf.format(selectedDate.getTime());
            Log.d(TAG, "onDateSet: " + dateData);
            dateButton.setText(dateData);
        };
    }

    private void init() {
        sendButton = findViewById(R.id.newEventSendButton);
        cancelButton = findViewById(R.id.newEventCancelButton);

        creatingEventProgressCircle = findViewById(R.id.newEventProgressCircle);

        dateButton = findViewById(R.id.newEventDateButton);
        timeButton = findViewById(R.id.newEventTimeButton);
        addImageButton = findViewById(R.id.newEventAddImageButton);
        image = findViewById(R.id.newEventImage);

        addressTextView = findViewById(R.id.newEventCity);

    }

    private void initTextViews() {
        bandTextView = findViewById(R.id.newEventBandName);
        capacityTextView = findViewById(R.id.newEventCapacity);
        descriptionTextView = findViewById(R.id.newEventDetails);

    }

    @Override
    public void onPrepareSupportNavigateUpTaskStack(@NonNull TaskStackBuilder builder) {
        builder.editIntentAt(builder.getIntentCount() - 1).putExtra("selected", "events");
    }

    @Override
    public boolean supportShouldUpRecreateTask(@NonNull Intent targetIntent) {
        return true;
    }

    public void onNewEventCancel(View view) {
        finish();
    }

    public void onNewEventSend(View view) {
        toggleButtonVisibilityOnProcessing(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        initTextViews();

        String band = bandTextView.getText().toString();
        String completeAddress = this.addressData;
        String dateString = this.dateData;
        String timeString = this.timeData;
        String capacity = capacityTextView.getText().toString();
        String description = descriptionTextView.getText().toString();

        if (TextUtils.isEmpty(band)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese el nombre de la banda.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        // TODO: 1/5/2018 test what happens when address is not found/selected
        if (TextUtils.isEmpty(completeAddress)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese su direccion.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(dateString)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese la fecha del evento.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(timeString)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese la hora del evento.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (TextUtils.isEmpty(capacity)) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor ingrese la capacidad del evento. Un estimado es suficiente.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (user == null) {
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        if (resultUri == null) {
            Snackbar.make(findViewById(R.id.newEventContainer), "Por favor agregue una imagen.", Snackbar.LENGTH_LONG).show();
            toggleButtonVisibilityOnProcessing(false);
            return;
        }

        // TODO: 1/5/2018 replace location with object containing relevant location props, update relevant views
        Map<String, Object> data = new HashMap<>();
        data.put("title", band);
        data.put("location", completeAddress);
        data.put("date", dateString);
        data.put("time", timeString);
        data.put("capacity", Integer.parseInt(capacity));
        data.put("details", description);

        uploadImage(user.getUid(), data, band, dateString);
    }

    private void uploadImage(String uid, Map<String, Object> data, String band, String dateString) {
        StorageReference storageRef = storage.getReference().child(uid + "/" + resultUri.getLastPathSegment());
        Log.d(TAG, "last path from image:" + resultUri.getLastPathSegment());

        storageRef.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                croppedImageUrl = taskSnapshot.getDownloadUrl().toString();
                Log.d(TAG, "event image uploaded successfully:" + croppedImageUrl);

                data.put("image", croppedImageUrl);

                eventsCollection.add(data).addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "onSuccess: Receipt added successfully with ID:" + documentReference.getId());
                    Intent intent = new Intent();
                    intent.putExtra("title", band);
                    intent.putExtra("date", dateString);
                    setResult(RESULT_OK, intent);
                    finish();
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: Error adding receipt.");
                    toggleButtonVisibilityOnProcessing(false);
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "event image upload ERROR:" + e.getMessage());
                toggleButtonVisibilityOnProcessing(false);
            }
        });

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

    private void toggleButtonVisibilityOnProcessing(boolean processing) {
        if (processing) {
            creatingEventProgressCircle.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
        } else {
            creatingEventProgressCircle.setVisibility(View.INVISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    }

    public void showTimePickerDialog(View view) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timeDialog = new TimePickerDialog(this, mTimeSetListener, hour, minute, DateFormat.is24HourFormat(this));
        timeDialog.show();
    }

    public void showDatePickerDialog(View view) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dateDialog = new DatePickerDialog(this, mDateSetListener, year, month, day);
        dateDialog.show();
    }

    public void onAddImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setFixAspectRatio(true)
                .setAspectRatio(16, 9)
                .start(this);
    }
}
