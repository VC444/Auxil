package com.example.googlemapsadding;

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;


public class LoginPageActivity extends Activity implements View.OnClickListener
{
    private Button registerButton;
    private EditText edEmail;
    private EditText edPassword;

    private ProgressDialog progressdialog;

    private FirebaseAuth firebaseauth;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        firebaseauth = FirebaseAuth.getInstance();

        progressdialog = new ProgressDialog(this);

        registerButton = (Button)findViewById(R.id.btnRegister);

        edEmail = (EditText)findViewById(R.id.editEmail);
        edPassword = (EditText)findViewById(R.id.editPassword);

        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (view == registerButton)
        {
            registerUser();
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
    }

    private void registerUser()
    {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            // Email is empty
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password))
        {
            // Password is empty
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressdialog.setMessage("Registering User...");
        progressdialog.show();

        firebaseauth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(LoginPageActivity.this, "Registered Succesfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(LoginPageActivity.this, "Could not register", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
