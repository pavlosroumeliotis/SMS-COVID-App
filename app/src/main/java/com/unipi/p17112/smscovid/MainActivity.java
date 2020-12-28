package com.unipi.p17112.smscovid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener {
    TextView fullname, address;
    Button sendButton;
    RecyclerView messageCodesRecyclerView;
    FloatingActionButton micButton;
    CardView cardView;
    ProgressBar progressBar;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseUser user;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    User currentUser;
    ArrayList<MessageCode> messageCodes;
    SQLiteHelper sqLiteHelper;
    CodeAdapter adapter;
    LocationManager locationManager;
    AlertDialog.Builder alert;
    AlertDialog alertDialog;
    MyTts myTts;
    private static final int REC_RESULT = 653;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Location manager initialization
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        //SQLiteHelper initialization
        sqLiteHelper = new SQLiteHelper(this, "MESSAGES.sqlite", null, 1);

        alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("ΠΡΟΣΟΧΗ!")
                .setMessage("Αναμονή για GPS...")
                .setCancelable(false);
        alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        //Views initialization
        fullname = findViewById(R.id.fullName);
        address = findViewById(R.id.address);
        messageCodesRecyclerView = findViewById(R.id.messageCodes);
        sendButton = findViewById(R.id.sendButton);
        micButton = findViewById(R.id.floatingActionButton);
        cardView = findViewById(R.id.cardView);
        progressBar = findViewById(R.id.progressBar);

        //This button when clicked sends the message
        sendButton.setOnClickListener(view -> send());
        //This button when clicked opens the profile activity
        cardView.setOnClickListener((view) -> openProfile());
        //This button when starts the voice recognition
        micButton.setOnClickListener(view -> recognise());

        //Set visibility of views
        messageCodesRecyclerView.setVisibility(View.INVISIBLE);
        cardView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.INVISIBLE);
        micButton.setVisibility(View.INVISIBLE);

        //Initialization of the arrayList with the message codes
        messageCodes = new ArrayList<>();

        //Set up of the recycler view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        messageCodesRecyclerView.setLayoutManager(gridLayoutManager);
        adapter = new CodeAdapter(this, messageCodes);
        adapter.setMessageCodes(messageCodes);
        messageCodesRecyclerView.setAdapter(adapter);

        //Update the list of the message codes
        updateList();

        //Gets data for the profile of the user from the firebase and then start the animation
        myRef.child("Users").child(user.getUid()).child("Details")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(User.class);
                        fullname.setText(String.format("%s %s", currentUser.getFirstName(), currentUser.getLastName()));
                        address.setText(currentUser.getAddress());
                        animation();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        //Initialization of the text to speech
        myTts = new MyTts(this);
    }

    //Method for voice recognition
    public void recognise(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ μιλήστε");
        startActivityForResult(intent,REC_RESULT);
    }

    //Method that returns the position of a messageCode object in the arrayList where the id matches the argument
    public int findPosition(int find){
        int index = -1;
        for(int i = 0; i < messageCodes.size(); i++) {
            if(messageCodes.get(i).id == find) {
                index = i;
                break;
            }
        }
        return index;
    }

    //Method for the result of the speech recognition
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REC_RESULT && resultCode==RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //If the sentence of the user contains the word "προφίλ", the user hears the response and the profile activity is opened
            if(matches.get(0).contains("προφίλ")){
                myTts.speak(getString(R.string.go_profile));
                openProfile();
            } else if(matches.get(0).contains("κωδικοι sms")){
                //Else if the sentence of the user contains the word "κωδικοί sms", the user hears the response and the message codes settings activity is opened
                myTts.speak(getString(R.string.go_codes));
                openMessageCodesSettings();
            } else if(matches.get(0).contains("αποσύνδεση")){
                //Else if the sentence of the user contains the word "αποσύνδεση", the user hears the response and the user is sign out
                myTts.speak(getString(R.string.signout));
                signOut();
            } else if (matches.get(0).contains("βοήθεια")){
                //Else if the sentence of the user contains the word "βοήθεια", the user hears the response and the help activity is opened
                myTts.speak(getString(R.string.go_help));
                openHelp();
            } else if(matches.get(0).contains("αποστολή")) {
                //Else if the sentence of the user contains the word "αποστολή" and a number code
                String numberOnly= matches.get(0).replaceAll("[^0-9]", "");
                //If the user doesn't say a number code
                if(numberOnly.equals("")){
                    myTts.speak(getString(R.string.send));
                    send();
                } else {
                    if (findPosition(Integer.parseInt(numberOnly)) != -1 ) {
                        //Else if the user says a number that exists select it and send the message
                        messageCodesRecyclerView.findViewHolderForAdapterPosition(findPosition(Integer.parseInt(numberOnly))).itemView.findViewById(R.id.cardView2).performClick();
                        myTts.speak(getString(R.string.send) + " " + numberOnly);
                        send();
                    } else {
                        //Else if message code doesn't exist
                        myTts.speak(getString(R.string.code_not_exist));
                        Toast.makeText(getApplicationContext(), getString(R.string.code_not_exist), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.command_error, Toast.LENGTH_LONG).show();
                myTts.speak(getString(R.string.command_error));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    //Method that creates the custom option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //Method that opens the correct activity based on the menu item that is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profile:
                openProfile();
                return true;
            case R.id.messageCodesSettings:
                openMessageCodesSettings();
                return true;
            case R.id.help:
                openHelp();
                return true;
            case R.id.singOut:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Method that signs the user out and returns to the welcome page activity
    public void signOut(){
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(), WelcomePage.class));
    }

    //Method that opens the profile activity and send data to it
    public void openProfile(){
        Intent intent = new Intent(getApplicationContext(), Profile.class);
        intent.putExtra("firstName", currentUser.getFirstName());
        intent.putExtra("lastName", currentUser.getLastName());
        intent.putExtra("email", currentUser.getEmail());
        intent.putExtra("address", currentUser.getAddress());
        intent.putExtra("userID", user.getUid());
        startActivity(intent);
    }

    //Method that gets data from the database and updates the recycler view
    public void updateList(){
        Cursor cursor = sqLiteHelper.getData("SELECT * FROM MESSAGES");
        messageCodes.clear();
        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String subtitle = cursor.getString(1);
            messageCodes.add(new MessageCode(id, subtitle));
        }
        adapter.notifyDataSetChanged();
    }

    //Method that check if there is network in use
    public boolean isNetworkAvailable(){
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (manager!=null){
                networkInfo = manager.getActiveNetworkInfo();
            }
            return networkInfo != null && networkInfo.isConnected();
        } catch (NullPointerException e){
            return false;
        }
    }

    //Method tha sends the message
    @SuppressLint("UnlocalizedSms")
    public void send(){
        //If no message code is selected show toast
        if(adapter.getSelected() == null){
            Toast.makeText(getApplicationContext(),
                    getString(R.string.sms_non_selected), Toast.LENGTH_SHORT).show();
        } else {
            //Else
            //If there is no internet connection show toast
            if(!isNetworkAvailable()){
                Toast.makeText(getApplicationContext(),
                        getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            } else {
                //Else
                //If sms permission is not granted request permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
                } else {
                    //Else
                    //If gps permission is not granted request permission
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    } else {
                        //Else
                        //If gps is off send message with null coordinates
                        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            sendMessage(null, null);
                        } else {
                            //Else show the dialog and start the location updates
                            alertDialog.show();
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        }
                    }
                }
            }
        }
    }

    //Method that send the sms and save the location, timestamp and code id to firebase
    public void sendMessage(String longitude, String latitude){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        History history = new History(longitude, latitude, timestamp,  adapter.getSelected().getId());
        myRef.child("Users").child(user.getUid()).child("History").child(timestamp)
                .setValue(history).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                    String message = adapter.getSelected().getId() + " " + currentUser.getLastName() + " " + currentUser.getFirstName() + " " + currentUser.getAddress();
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("13033", null, message.toUpperCase(), null, null);
                } else {
                    Toast.makeText(getApplicationContext(),
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Method that executes code after the result of the permissions requests
    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        if(requestCode==0)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                send();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.permission), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode==1) {
            send();
        }
    }

    //Method for the animation of the views
    public void animation(){
        progressBar.setVisibility(View.INVISIBLE);
        messageCodesRecyclerView.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
        micButton.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.VISIBLE);
        cardView.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_right
        ));
        messageCodesRecyclerView.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_up
        ));
        sendButton.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_up
        ));
        micButton.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_up
        ));
    }

    //Method that opens the help activity
    public void openHelp(){
        startActivity(new Intent(getApplicationContext(), Help.class));
    }

    //Method that opens the message code settings activity
    public void openMessageCodesSettings(){
        startActivity(new Intent(getApplicationContext(), MessageCodesSettings.class));
    }

    //When first signal of gps is sent get coordinates, send the message, close the dialog and stop the signal of the gps
    @Override
    public void onLocationChanged(@NonNull Location location) {
        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        sendMessage(longitude, latitude);
        alertDialog.dismiss();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    //If gps is disabled send message with null location
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        sendMessage(null, null);
    }

    //Method that closes the app when back button is pressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}