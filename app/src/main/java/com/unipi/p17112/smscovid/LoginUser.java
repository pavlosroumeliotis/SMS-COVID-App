package com.unipi.p17112.smscovid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginUser extends AppCompatActivity {
    EditText email_input, password_input;
    Button signIn_button;
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    CheckBox checkBox;
    TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_user);

        //Back button on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Animation of the constraintLayout
        findViewById(R.id.constraintLayout).startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_up
        ));

        //Shared preferences initialization
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();

        //Views initialization
        email_input = findViewById(R.id.editTextEmail2);
        password_input = findViewById(R.id.editTextPassword2);
        signIn_button = findViewById(R.id.signInButton);
        checkBox = findViewById(R.id.checkBox);
        forgotPassword = findViewById(R.id.forgotPassword);

        //Add the functionality to remove the hint of the input when it is clicked
        focusRemoveHint(email_input, getString(R.string.email));
        focusRemoveHint(password_input, getString(R.string.password));

        //This textview when clicked starts the forgotPassword method
        forgotPassword.setOnClickListener(view -> forgotPassword());
        //This button uses the method for the login of the user
        signIn_button.setOnClickListener((view) -> signInWithEmailAndPassword());
    }

    //Method for the reset of the password if the user forget it
    public void forgotPassword(){
        if(email_input.getText().toString().isEmpty()){
            //If the email input is empty show warning toast for its' completion
            Toast.makeText(getApplicationContext(), getString(R.string.forgot_email_empty),Toast.LENGTH_LONG).show();
        } else {
            //Else use the firebase method for password reset
            mAuth.sendPasswordResetEmail(email_input.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        //If the action is completed successfully show the toast
                        Toast.makeText(getApplicationContext(), getString(R.string.forgot_success), Toast.LENGTH_SHORT).show();
                    } else {
                        //Else show the warning toast
                        Toast.makeText(getApplicationContext(),
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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

    //Method that sign in the user with email and password
    public void signInWithEmailAndPassword(){
        //If at least one input is empty then show the warning toast
        if (password_input.getText().toString().isEmpty() || email_input.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(),
                    getString(R.string.empty_text), Toast.LENGTH_SHORT).show();
        } else {
            //Else tell the user to wait for the completion of the log in
            Toast.makeText(getApplicationContext(),
                    getString(R.string.wait), Toast.LENGTH_SHORT).show();

            //Save the checkbox value to the shared preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Keep Logged In", checkBox.isChecked());
            editor.apply();

            //Sign in the user with the firebase method
            mAuth.signInWithEmailAndPassword(email_input.getText().toString(), password_input.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //If the registration is completed successfully show the toast, open the Main Activity
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.success), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
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
