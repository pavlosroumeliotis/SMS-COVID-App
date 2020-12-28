package com.unipi.p17112.smscovid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Profile extends AppCompatActivity {
    ConstraintLayout history_panel;
    String firstName, lastName, address, email, userID;
    TextView firstNameTextView, lastNameTextView, addressTextView, emailTextView;
    FloatingActionButton editProfileButton, micButton;
    FirebaseAuth mAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;
    RecyclerView history;
    HistoryAdapter adapter;
    ArrayList<History> historyList;
    MyTts myTts;
    private static final int REC_RESULT = 653;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        //Get the strings from the previous activity
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        address = getIntent().getStringExtra("address");
        email = getIntent().getStringExtra("email");
        userID = getIntent().getStringExtra("userID");

        //Views initialization
        firstNameTextView = findViewById(R.id.firstNameTextView);
        lastNameTextView = findViewById(R.id.lastNameTextView);
        addressTextView = findViewById(R.id.addressTextView);
        emailTextView = findViewById(R.id.emailTextView);
        editProfileButton = findViewById(R.id.floatingActionButton3);
        micButton = findViewById(R.id.floatingActionButton4);
        history = findViewById(R.id.historyRecycler);
        history_panel = findViewById(R.id.history_panel);

        //Set text of the textviews
        firstNameTextView.setText(firstName);
        lastNameTextView.setText(lastName);
        addressTextView.setText(address);
        emailTextView.setText(email);

        //This button when clicked opens the update profile modal
        editProfileButton.setOnClickListener((view -> openUpdateProfileModal()));
        //This button when starts the voice recognition
        micButton.setOnClickListener(view -> recognise());

        //Initialization of the arrayList with the history records
        historyList = new ArrayList<>();

        //Get the history records from the firebase realtime database
        myRef.child("Users").child(userID).child("History")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            History historyRecord = dataSnapshot.getValue(History.class);
                            historyList.add(historyRecord);
                            adapter.notifyDataSetChanged();
                        }
                        //Reverse the list to get the latest history records first
                        Collections.reverse(historyList);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        //Setup the recycler view for the history
        history.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, historyList);
        history.setAdapter(adapter);

        //Initialization of text to speech
        myTts = new MyTts(this);

        //Animation of the constraintLayout
        history_panel.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_right
        ));
    }

    //Method for voice recognition
    public void recognise(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ μιλήστε");
        startActivityForResult(intent,REC_RESULT);
    }

    //Method for the result of the speech recognition
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REC_RESULT && resultCode==RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //If the sentence of the user contains the word "επεξεργασία", the user hears the response and the edit profile modal opens
            if(matches.get(0).contains("επεξεργασία")){
                myTts.speak(getString(R.string.edit_profile));
                openUpdateProfileModal();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.command_error, Toast.LENGTH_LONG).show();
            myTts.speak(getString(R.string.command_error));
        }
    }

    //Method that creates and opens a modal in order to edit the profile
    public void openUpdateProfileModal(){
        //Initialization of custom alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.profile_modal,null);
        builder.setView(view);

        //Views initialization
        EditText firstNameText = view.findViewById(R.id.updateFirstName);
        EditText lastNameText = view.findViewById(R.id.updateLastName);
        EditText addressText = view.findViewById(R.id.updateAddress);
        Button updateButton = view.findViewById(R.id.updateProfileButton);

        //Set the text of the edittexts
        firstNameText.setText(firstNameTextView.getText());
        lastNameText.setText(lastNameTextView.getText());
        addressText.setText(addressTextView.getText());

        //Create the dialog and show it
        AlertDialog dialog = builder.create();
        dialog.show();

        //This button when clicked updates the user profile
        updateButton.setOnClickListener((view1) -> {
            //If at least one of the edittexts is empty show warning toast
            if(firstNameText.getText().toString().isEmpty() || lastNameText.getText().toString().isEmpty() ||
                    addressText.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(),
                        getString(R.string.wait), Toast.LENGTH_SHORT).show();
            } else {
                //Else create user object with the new values from the edittexts
                User user_details = new User(
                        firstNameText.getText().toString(),
                        lastNameText.getText().toString(),
                        email,
                        addressText.getText().toString());
                //Change the current user object with the new one
                myRef.child("Users").child(userID).child("Details")
                        .setValue(user_details).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            //If action is completed set text of the textviews and close the dialog
                            firstNameTextView.setText(firstNameText.getText().toString());
                            lastNameTextView.setText(lastNameText.getText().toString());
                            addressTextView.setText(addressText.getText().toString());
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }
}