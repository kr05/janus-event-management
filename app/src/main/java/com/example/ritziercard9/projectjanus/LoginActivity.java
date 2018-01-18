package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {
    private static String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private ProgressBar loginProgressBar;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreate: login activity created!");

        loginProgressBar = findViewById(R.id.loginProgressBar);
        loginButton = findViewById(R.id.loginButton);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        checkUser();
    }

    private void checkUser() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            user.getIdToken(true).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken().split("\\.")[1];

                    byte[] data = Base64.decode(idToken, Base64.DEFAULT);
                    String text = null;
                    try {
                        text = new String(data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    JSONObject payload = null;
                    try {
                        payload = new JSONObject(text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (payload.optBoolean("organizer") || payload.optBoolean("admin")) {
                        Intent intent = new Intent(this, OrganizerMainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    if (payload.optBoolean("sales")) {
                        String uid = user.getUid();
                        Intent intent = new Intent(this, EventsListActivity.class);
                        intent.putExtra("sellerUID", uid);
                        startActivity(intent);
                        finish();
                    }

                    if (payload.optBoolean("entry")) {
                        String uid = user.getUid();
                        Intent intent = new Intent(this, ScannerEventsListActivity.class);
                        intent.putExtra("entryUID", uid);
                        startActivity(intent);
                        finish();
                    }




                } else {
                    Log.d(TAG, "checkUser: unable to extract id token");
                    toggleButtonAndProgressVisibilityOnLogin(false);
                    Snackbar.make(findViewById(R.id.loginContainer), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    mAuth.signOut();
                    return;
                }
            });

//            Log.d(TAG, "updateUI: user is not null, starting main activity...");
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
        }
    }

    public void onIngresarClick(View view) {
        Log.d(TAG, "onIngresarClick: Click!");

        toggleButtonAndProgressVisibilityOnLogin(true);

        emailEditText = findViewById(R.id.loginEmailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(findViewById(R.id.loginContainer), "Por favor ingrese su correo electronico.", Snackbar.LENGTH_LONG).show();
            toggleButtonAndProgressVisibilityOnLogin(false);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Snackbar.make(findViewById(R.id.loginContainer), "Por favor ingrese su contraseña.", Snackbar.LENGTH_LONG).show();
            toggleButtonAndProgressVisibilityOnLogin(false);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    checkUser();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Snackbar.make(findViewById(R.id.loginContainer), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    toggleButtonAndProgressVisibilityOnLogin(false);
                }
            });
    }

    private void toggleButtonAndProgressVisibilityOnLogin(boolean loggingIn) {
        if (loggingIn) {
            loginProgressBar.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
        } else {
            loginProgressBar.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }
}
