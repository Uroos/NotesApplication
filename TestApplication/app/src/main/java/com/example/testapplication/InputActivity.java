package com.example.testapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InputActivity extends AppCompatActivity {

    EditText ednote;
    Button btnsave;
    private String userId;

    String note_to_save;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        ednote=findViewById(R.id.note);
        btnsave=findViewById(R.id.btvSave);

        if (getIntent() != null) {
            userId = getIntent().getStringExtra("id");
        }

        FirebaseApp.initializeApp(this);
        // Main access point for the database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Using the access point, get access to a specific place in database
        databaseReference = firebaseDatabase.getReference().child("notes").child(userId);

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note_to_save = ednote.getText().toString();

                //String key = databaseReference.push().getKey();
                Notes note = new Notes(note_to_save);

                databaseReference.push().setValue(note);
                //databaseReference.push().setValue(school);
                finish();
            }
        });
    }
}
