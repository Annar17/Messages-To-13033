package com.example.messages;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        TextView number = (TextView) findViewById(R.id.number);
        EditText textview_choice = (EditText) findViewById(R.id.textview_choice);
        EditText edit_n = (EditText) findViewById(R.id.enter_0);
        EditText add_message = (EditText) findViewById(R.id.add_message);

        //Back button
        Button back_bt3 = (Button) findViewById(R.id.back_bt3);
        back_bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //Dropdown list of messages
        Spinner dropdown =  (Spinner) findViewById(R.id.spinner1);
        ArrayList<String> arrayList = new ArrayList<>();

        db = openOrCreateDatabase("SMS_DB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(id int, message string)");

        String query = "SELECT * FROM MESSAGES";
        Cursor cursor = db.rawQuery(query, new String[]{});
        if (cursor.getCount() > 0) {
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                builder.append(cursor.getInt(0)).append(" ").append(cursor.getString(1));
                arrayList.add(builder.toString());
                builder.delete(0, builder.length());
            }
        } else {
            arrayList.add("No records available");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, arrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        //When we click an item from the droplist
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = dropdown.getSelectedItem().toString();
                String[] parts = text.split(" ", 2);
                number.setText(parts[0]);
                textview_choice.setText(parts[1]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        //Modify messages
        Button correct_bt = (Button) findViewById(R.id.correct_bt);
        correct_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                cv.put("id", number.getText().toString());
                cv.put("message", textview_choice.getText().toString());
                db.update("MESSAGES", cv, "id = ?", new String[]{number.getText().toString()});
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_success), Toast.LENGTH_LONG).show();
            }
        });

        //Create messages
        Button create_bt = (Button) findViewById(R.id.create_bt);
        create_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = edit_n.getText().toString();
                String m = add_message.getText().toString();
                if ((n.matches("")) || (m.matches(""))) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill), Toast.LENGTH_LONG).show();
                } else {
                    int n1 = Integer.parseInt(n);
                    db.execSQL("INSERT INTO  MESSAGES (id, message) SELECT * FROM (SELECT '" + n1 + "', '" + m + "') AS tmp WHERE NOT EXISTS (SELECT id FROM MESSAGES WHERE id = '" + n1 + "') LIMIT 1;");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.new_success), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
