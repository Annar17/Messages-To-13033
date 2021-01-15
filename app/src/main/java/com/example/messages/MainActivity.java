package com.example.messages;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Ref;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {
    SQLiteDatabase db;
    FirebaseDatabase database;
    private DatabaseReference myRef;
    FirebaseUser cUser;
    FirebaseAuth Auth;
    private static final int REC_RESULT = 653;
    double location, x, y;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String email = getIntent().getStringExtra("email");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        cUser = Auth.getInstance().getCurrentUser();
        String userUid = cUser.getUid();

        EditText edit_Name = (EditText) findViewById(R.id.edit_Name);
        edit_Name.requestFocus();
        EditText edit_Address = (EditText) findViewById(R.id.edit_Address);
        retrieveD();

        //Getting permission to send SMS
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 5434);
            return;
        }
        //Getting permission to use location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            return;
        }
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) this);

        //Create or access database in order to print messages
        db = openOrCreateDatabase("SMS_DB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(id int, message string)");
        //Insert messages if not exist
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '1', 'Μετάβαση σε φαρμακείο ή επίσκεψη σε γιατρό.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '1' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '2', 'Μετάβαση σε εν λειτουργία κατάστημα προμηθειών αγαθών πρώτης ανάγκης.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '2' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '3', 'Μετάβαση σε δημόσια υπηρεσία ή τράπεζα.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '3' ) LIMIT 1");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '4', 'Κίνηση για παροχή βοήθειας σε ανθρώπους που βρίσκονται σε ανάγκη ή συνοδεία ανηλίκων μαθητών από/προς το σχολείο.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '4' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '5', 'Μετάβαση σε τελετή(π.χ. κηδεία, γάμος, βάφτιση) ή μετάβαση διαζευγμένων γονέων ή γονέων που τελούν σε διάσταση που είναι αναγκαία για την διασφάλιση της επικοινωνίας γονέων και τέκνων.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '5' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '6', 'Σωματική άσκηση σε εξωτερικό χώρο ή κίνηση με κατοικίδιο ατομικά ή ανά δυο άτομα.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '6' ) LIMIT 1;");

        //Print messages to radio buttons
        RadioGroup Radio_Group = (RadioGroup) findViewById(R.id.radio_group1);
        String query = "SELECT * FROM MESSAGES";
        Cursor cursor = db.rawQuery(query, new String[]{});
        int i = 0;
        if (cursor.getCount() > 0) {
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                builder.append(cursor.getInt(0)).append(" ").append(cursor.getString(1));
                if((Radio_Group.getChildAt(i) instanceof RadioButton) && (i < Radio_Group.getChildCount())){
                    ((RadioButton) Radio_Group.getChildAt(i)).setText(builder.toString());
                } else {
                    RadioButton radioButton= new RadioButton(this);
                    radioButton.setId(View.generateViewId());
                    radioButton.setText(builder.toString());
                    Radio_Group.addView(radioButton);
                }
                i++;
                builder.delete(0, builder.length());
            }
        }

        //Two buttons to clear each edit text
        Button clear1_bt = (Button) findViewById(R.id.clear1_bt);
        clear1_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_Name.setText("");
            }
        });
        Button clear2_bt = (Button) findViewById(R.id.clear2_bt);
        clear2_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_Address.setText("");
            }
        });

        //Send SMS when button clicked
        Button send_bt = (Button) findViewById(R.id.send_bt);
        send_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_Name.getText().toString();
                String address = edit_Address.getText().toString();
                if ((Radio_Group.getCheckedRadioButtonId() == -1) || (name.matches("")) || (address.matches(""))) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill), Toast.LENGTH_LONG).show();
                } else {
                    int radioButtonID = Radio_Group.getCheckedRadioButtonId();
                    View radioButton = Radio_Group.findViewById(radioButtonID);
                    int idx = Radio_Group.indexOfChild(radioButton) + 1;
                    String message = idx + " " + name + " " + address;
                    SmsManager manager = SmsManager.getDefault();
                    manager.sendTextMessage("13033", null, message, null, null);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_sent), Toast.LENGTH_LONG).show();
                    location = + x + y;
                    updateUsers(email, name, address, idx, location);
                }
            }
        });

        //Back button
        Button back_bt = (Button) findViewById(R.id.back_bt);
        back_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //Edit Button
        Button edit_bt = (Button) findViewById(R.id.edit_bt);
        edit_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    public void fillTable(){
        //Create or access database in order to print messages
        db = openOrCreateDatabase("SMS_DB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(id int, message string)");
        //Insert messages if not exist
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '1', 'Μετάβαση σε φαρμακείο ή επίσκεψη σε γιατρό.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '1' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '2', 'Μετάβαση σε εν λειτουργία κατάστημα προμηθειών αγαθών πρώτης ανάγκης.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '2' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '3', 'Μετάβαση σε δημόσια υπηρεσία ή τράπεζα.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '3' ) LIMIT 1");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '4', 'Κίνηση για παροχή βοήθειας σε ανθρώπους που βρίσκονται σε ανάγκη ή συνοδεία ανηλίκων μαθητών από/προς το σχολείο.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '4' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '5', 'Μετάβαση σε τελετή(π.χ. κηδεία, γάμος, βάφτιση) ή μετάβαση διαζευγμένων γονέων ή γονέων που τελούν σε διάσταση που είναι αναγκαία για την διασφάλιση της επικοινωνίας γονέων και τέκνων.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '5' ) LIMIT 1;");
        db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '6', 'Σωματική άσκηση σε εξωτερικό χώρο ή κίνηση με κατοικίδιο ατομικά ή ανά δυο άτομα.') AS tmp WHERE NOT EXISTS ( SELECT id FROM MESSAGES WHERE id = '6' ) LIMIT 1;");
    }

    //Update or create user
    private void updateUsers(String email, String name, String address, int code, double location){
        DatabaseReference ref = myRef.child(cUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    ref.child("Email").setValue(email);
                    ref.child("Name").setValue(name);
                    ref.child("Address").setValue(address);
                    ref.child("Code").setValue(code);
                    ref.child("Stigma").setValue(location);
                } else {
                    ref.child("Email").setValue(email);
                    ref.child("Name").setValue(name);
                    ref.child("Address").setValue(address);
                    ref.child("Code").push().setValue(code);
                    ref.child("Stigma").push().setValue(location);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Retrieve details from firebase and print on EditTexts
    public void retrieveD(){
        DatabaseReference ref = myRef.child(cUser.getUid());
        EditText edit_Name = (EditText) findViewById(R.id.edit_Name);
        EditText edit_Address = (EditText) findViewById(R.id.edit_Address);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("Name").getValue() != null) && (snapshot.child("Address").getValue() != null)) {
                    final String fn = snapshot.child("Name").getValue().toString();
                    edit_Name.setText(fn);
                    final String a = snapshot.child("Address").getValue().toString();
                    edit_Address.setText(a);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Button for speech recognition
    public void recognize(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Please say something!");
        startActivityForResult(intent,REC_RESULT);
    }

    //What action to do when speech recognition finds a match
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        RadioGroup Radio_Group = (RadioGroup) findViewById(R.id.radio_group1);
        RadioButton sms1 = (RadioButton) findViewById(R.id.sms1);
        RadioButton sms2 = (RadioButton) findViewById(R.id.sms2);
        RadioButton sms3 = (RadioButton) findViewById(R.id.sms3);
        RadioButton sms4 = (RadioButton) findViewById(R.id.sms4);
        RadioButton sms5 = (RadioButton) findViewById(R.id.sms5);
        RadioButton sms6 = (RadioButton) findViewById(R.id.sms6);
        if (requestCode == REC_RESULT && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.contains("1")) { //sms1 check
                sms1.setChecked(true);
            }
            if (matches.contains("2")) { //sms2 check
                sms2.setChecked(true);
            }
            if (matches.contains("3")) { //sms3 check
                sms3.setChecked(true);
            }
            if (matches.contains("4")) { //sms4 check
                sms4.setChecked(true);
            }
            if (matches.contains("5")) { //sms5 check
                sms5.setChecked(true);
            }
            if (matches.contains("6")) { //sms6 check
                sms6.setChecked(true);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double x = location.getLatitude();
        double y = location.getLongitude();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}