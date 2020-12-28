package com.unipi.p17112.smscovid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText email_input, password_input, firstname_input, lastname_input, address_input;
    Button register_button;
    FirebaseUser currentUser;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_user);

        //Back button on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Animation of the constraintLayout
        findViewById(R.id.constraintLayout).startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_up
        ));

        //Firebase database initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        currentUser = mAuth.getCurrentUser();

        //Views initialization
        email_input = findViewById(R.id.editTextEmail);
        password_input = findViewById(R.id.editTextPassword);
        firstname_input = findViewById(R.id.editTextFirstName);
        lastname_input = findViewById(R.id.editTextLastName);
        address_input = findViewById(R.id.editTextAddress);
        register_button = findViewById(R.id.signInButton);

        //This button uses the method for the registration of the user
        register_button.setOnClickListener((view) -> singUpWithEmailAndPassword());

        //Add the functionality to remove the hint of the input when it is clicked
        focusRemoveHint(password_input, getString(R.string.password));
        focusRemoveHint(firstname_input, getString(R.string.firstname));
        focusRemoveHint(lastname_input, getString(R.string.lastname));
        focusRemoveHint(address_input, getString(R.string.address));
        focusRemoveHint(email_input, getString(R.string.email));
    }

    //Method that gets as arguments the edittext and its' hint
    public void focusRemoveHint(EditText editText, String hint){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            //When the focus of the edittext is changed, change the hint accordingly
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText.setHint("");
                } else {
                    editText.setHint(hint);
                }
            }
        });
    }

    //Method that hides the keyboard when the user taps outside of the edittexts
    public void hideKeyboardOnTap(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    //Method that signs up the user with email and password
    public void singUpWithEmailAndPassword(){
        //If at least one input is empty then show the warning toast
        if (address_input.getText().toString().isEmpty() || firstname_input.getText().toString().isEmpty() ||
                lastname_input.getText().toString().isEmpty() || password_input.getText().toString().isEmpty() ||
                email_input.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(),
                    getString(R.string.empty_text), Toast.LENGTH_SHORT).show();
        } else {
            //Else tell the user to wait for the completion of the registration
            Toast.makeText(getApplicationContext(),
                    getString(R.string.wait), Toast.LENGTH_SHORT).show();
            //Create the user with the firebase method
            mAuth.createUserWithEmailAndPassword(
                    email_input.getText().toString(),password_input.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //If the registration is completed successfully create a user object
                                User user_details = new User(
                                        firstname_input.getText().toString(),
                                        lastname_input.getText().toString(),
                                        email_input.getText().toString(),
                                        address_input.getText().toString());
                                currentUser = mAuth.getCurrentUser();
                                //add the user to the realtime database
                                myRef.child("Users").child(currentUser.getUid()).child("Details")
                                        .setValue(user_details).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //If the registration is completed successfully show the toast, open the Main Activity with the user
                                            Toast.makeText(getApplicationContext(),
                                                    getString(R.string.success), Toast.LENGTH_SHORT).show();
                                            currentUser = mAuth.getCurrentUser();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            //Else show the warning toast
                                            Toast.makeText(getApplicationContext(),
                                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                //Else show the warning toast
                                Toast.makeText(getApplicationContext(),
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}