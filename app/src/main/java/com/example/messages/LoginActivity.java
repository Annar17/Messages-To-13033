package com.example.messages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser cUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //Check if user is already logged in and send him to main activity
        cUser = FirebaseAuth.getInstance().getCurrentUser();
        if (cUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("email",cUser.getEmail());
            startActivity(intent);
        }

        mAuth = FirebaseAuth.getInstance();

        Button connect_bt = (Button) findViewById(R.id.connect_bt);

        //Go to register activity
        TextView register_bt = (TextView) findViewById(R.id.register_bt);
        register_bt.setPaintFlags(register_bt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        register_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    //Sign in user when clicking connect button
    public void signin(View view){
        EditText c_Email = (EditText) findViewById(R.id.c_Email);
        String Email = c_Email.getText().toString();
        EditText c_Password = (EditText) findViewById(R.id.c_Password);
        String Password = c_Password.getText().toString();
        if ((Email.matches("")) || (Password.matches(""))){ //Check if edittexts are empty
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill), Toast.LENGTH_LONG).show();
        } else if ((Email.length() > 0) && (Password.length() > 0)) {
            mAuth.signInWithEmailAndPassword(
                    c_Email.getText().toString(),c_Password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                cUser = mAuth.getCurrentUser();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connect_m), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("email",cUser.getEmail());
                                startActivity(intent);
                            }else {
                                Toast.makeText(getApplicationContext(),
                                        task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}
