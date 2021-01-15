package com.example.messages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        mAuth = FirebaseAuth.getInstance();

        Button back_bt2 = (Button) findViewById(R.id.back_bt2);
        back_bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public void signup(View view) {
        EditText r_Email = (EditText) findViewById(R.id.r_Email);
        r_Email.requestFocus();
        String email = r_Email.getText().toString();
        EditText r_Password = (EditText) findViewById(R.id.r_Password);
        String pass1 = r_Password.getText().toString();
        EditText r_Password2 = (EditText) findViewById(R.id.r_Password2);
        String pass2 = r_Password2.getText().toString();

        if ((TextUtils.isEmpty(email)) || (TextUtils.isEmpty(pass1)) || (TextUtils.isEmpty(pass2))) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill), Toast.LENGTH_LONG).show();
        } else {
            if (pass1.equals(pass2)) {
                mAuth.createUserWithEmailAndPassword(
                        r_Email.getText().toString(), r_Password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.register_m), Toast.LENGTH_LONG).show();
                                    currentUser = mAuth.getCurrentUser();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            } else if (!pass1.equals(pass2)) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_m), Toast.LENGTH_LONG).show();
            }
        }
    }
}
