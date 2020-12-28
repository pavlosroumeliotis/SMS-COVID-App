package com.unipi.p17112.smscovid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class WelcomePage extends AppCompatActivity {
    TextView register;
    Button button;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    SharedPreferences sharedPreferences;
    boolean keep_logged_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        //Views initialization
        register = findViewById(R.id.textView5);
        button = findViewById(R.id.button);

        //This textview when clicked opens the register activity
        register.setOnClickListener((view) -> startActivity(new Intent(getApplicationContext(), RegisterUser.class)));
        //This button opens the login activity
        button.setOnClickListener((view) -> startActivity(new Intent(getApplicationContext(), LoginUser.class)));

        //Shared preference that returns true when the user wants to be logged in when he opens the app next time
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Default value is false
        keep_logged_in = sharedPreferences.getBoolean("Keep Logged In", false);

        //If the value is true and the user not null then open the Main Activity
        if(keep_logged_in){
            if (user != null) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    }

    //If the user press the back button the app closes
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}