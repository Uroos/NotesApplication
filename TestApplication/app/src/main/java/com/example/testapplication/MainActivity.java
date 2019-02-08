package com.example.testapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.NotesAdapterOnClickHandler {
    public static final int RC_SIGN_IN = 1;
    private static final String TAG = MainActivity.class.getName();

    RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    ArrayList<Notes> notes;
    ArrayList<String> pushid;
    FloatingActionButton fab;
    LinearLayoutManager layoutManager;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // To resolve the error of not getting instance of database
        // Solution from: https://stackoverflow.com/questions/40081539/default-firebaseapp-is-not-initialized
        // Version com.google.gms:google-services:4.1.0 was crashing but com.google.gms:google-services:4.0.1 worked
        // so it was changed in the build.gradle file.
        FirebaseApp.initializeApp(this);
        // Main access point for the database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Using the access point, get access to a specific place in database
        databaseReference = firebaseDatabase.getReference().child("notes");
        firebaseAuth = FirebaseAuth.getInstance();

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.rv_notes);

        notes = new ArrayList<>();
        pushid = new ArrayList<>();

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        notesAdapter = new NotesAdapter(this, notes, this);
        recyclerView.setAdapter(notesAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    Intent intent = new Intent(MainActivity.this, InputActivity.class);
                    intent.putExtra("id", user.getUid());
                    startActivity(intent);
                }
            }
        });
        if (user == null) {
            startSignInFlow();
        } else {
            loadUserNotes();
        }
    }

    @Override
    public void onClick(int position) {
        Toast.makeText(this, "Note: " + notes.get(position).getNote() + " is clicked.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("id", user.getUid());
        //Sending the value of push id key so it can be deleted at that position or updated at that position
        intent.putExtra("position", pushid.get(position));
        intent.putExtra("note", notes.get(position).getNote());
        startActivity(intent);
    }

    private void startSignInFlow() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());

                if (user != null) {
                    // user is signed in
                    Toast.makeText(MainActivity.this, "You are signed in.", Toast.LENGTH_SHORT).show();
                    loadUserNotes();
                } else {
                    // User is not signed in
                    // Create and launch sign-in intent. When it is successful it calls onActivityResult()
                    // with the request code = RC_SIGN_IN.
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                //Toast.makeText(this, "You are now logged in with email: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // If you don't call finish(), it will result in infinite loop where activity won't exit
                // on pressing the back button.
                finish();
            }
        }
    }

    // Removing Auth State listener
    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }

    }

    // Attaching Auth State listener
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.v(this.getClass().getName(), "here on resume called");
        if (authStateListener != null) {
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                if (user != null) {
                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MainActivity.this, "You are now logged out", Toast.LENGTH_SHORT).show();
                                    notes = null;
                                    notesAdapter.setNotesData(null);
                                    notesAdapter.notifyDataSetChanged();
                                }
                            });
                    //finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserNotes() {
        String userId = "";
        notes = new ArrayList<>();
        pushid = new ArrayList<>();

        if (user != null) {
            userId = user.getUid();
            Log.v(MainActivity.this.getClass().getName(), "user id is: " + userId);
        }
        DatabaseReference myref = databaseReference.child(userId);
        myref.addValueEventListener(new ValueEventListener() {
            /**
             * This method will be invoked any time the data on the database changes.
             * It will also get invoked as soon as we connect the listener, so that we can get an initial
             * snapshot of the data.
             *
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get all the children under the node "notes/uid"
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // Iterate over all the children
                for (DataSnapshot child : children) {
                    String nameofnode = child.getKey();
                    Log.v(TAG, "name of node is: " + nameofnode);

                    Notes note = child.getValue(Notes.class);
                    pushid.add(nameofnode);
                    // Only load the notes of the user that is logged in
                    // Log.v(TAG, "push id is: " + nameofnode);
                    Log.v(TAG, "user note is: " + note.getNote());
                    notes.add(new Notes(note.getNote()));
                    Log.v(TAG, "notes size is: " + notes.size());
                }
                Log.v(TAG, "notes size outside is: " + notes.size());
                Log.v(TAG, "pushid size outside is: " + pushid.size());
                //loadUserNotes();
                notesAdapter.setNotesData(notes);
                recyclerView.setAdapter(notesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
