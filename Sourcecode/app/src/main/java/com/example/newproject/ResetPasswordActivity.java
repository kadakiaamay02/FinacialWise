package com.example.newproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompatSideChannelService;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class ResetPasswordActivity extends AppCompatActivity {
    //Button Variables
    private Button resetPasswordButton;
    private Button resetPasswordBackButton;

    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Progress Bar
    private ProgressBar progressBar;

    //TextVariables
    private EditText emailEditText;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //Firebase Setup
        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.forgot_password_email_account);
        progressBar = findViewById(R.id.forgot_password_progress);
        resetPasswordButton = findViewById(R.id.forgot_password_button);
        resetPasswordBackButton = findViewById(R.id.forgot_password_back_button);

        //Disable actionbar
        getSupportActionBar().hide();

        resetPasswordBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                if( TextUtils.isEmpty(email) )
                {
                    Toast.makeText(getApplication(), "Enter your registered email", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if( task.isSuccessful() )
                                {
                                    Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();

                                }

                                else
                                {
                                    Toast.makeText(ResetPasswordActivity.this, "There was an error sending reset instructions!", Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        });

    }
}