package com.example.ritziercard9.projectjanus;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Snackbar.make(findViewById(R.id.loginContainer), "No se pudo iniciar sesion. Intente de nuevo.", Snackbar.LENGTH_LONG).show();
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
