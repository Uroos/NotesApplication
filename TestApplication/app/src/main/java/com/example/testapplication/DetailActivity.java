package com.example.testapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.awt.font.TextAttribute;

public class DetailActivity extends AppCompatActivity {

    EditText tv;
    Button btnUpdate;
    Button btnDelete;

    private String userId;
    private String note;
    private String position;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tv=findViewById(R.id.tvDetailNote);
        btnUpdate=findViewById(R.id.btnDetailUpdate);
        btnDelete=findViewById(R.id.btnDetailDelete);

        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (getIntent() != null) {
            userId = getIntent().getStringExtra("id");
            position=getIntent().getStringExtra("position");
            note = getIntent().getStringExtra("note");

            databaseReference = firebaseDatabase.getReference().child("notes").child(userId).child(position);
            tv.setText(note);
        }

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNote=tv.getText().toString();
                databaseReference.setValue(new Notes(newNote));
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.removeValue();
                finish();
            }
        });
    }
}
