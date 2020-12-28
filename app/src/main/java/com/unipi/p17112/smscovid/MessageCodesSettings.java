package com.unipi.p17112.smscovid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MessageCodesSettings extends AppCompatActivity {
    public static SQLiteHelper sqLiteHelper;
    FloatingActionButton addButton, micButton;
    RecyclerView recyclerView;
    ArrayList<MessageCode> messageCodes;
    SettingsAdapter settingsAdapter;
    MyTts myTts;
    private static final int REC_RESULT = 653;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_codes_settings);

        //Views initialization
        addButton = findViewById(R.id.addButton);
        micButton = findViewById(R.id.floatingActionButton2);
        recyclerView = findViewById(R.id.recyclerView);

        //Initialization of the SQLite Helper
        sqLiteHelper = new SQLiteHelper(this, "MESSAGES.sqlite", null, 1);

        //This button when clicked opens the modal for the creation of a new code
        addButton.setOnClickListener((view -> createMessageCode()));
        //This button when starts the voice recognition
        micButton.setOnClickListener(view -> recognise());

        //Initialization of the text to speech
        myTts = new MyTts(this);

        //Initialization of the arrayList with the message codes
        messageCodes = new ArrayList<>();

        //Set up of the recycler view
        settingsAdapter = new SettingsAdapter(this, messageCodes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(settingsAdapter);

        //Animation of some views
        animation();
        //Update the list of the message codes
        updateList();
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
            if(matches.get(0).contains("προσθήκη")){
                //If the sentence of the user contains the word "προσθήκη", the user hears the response and open the modal for the code creation
                myTts.speak(getString(R.string.add_low));
                createMessageCode();
            } else if(matches.get(0).contains("επεξεργασία")){
                //Else if the sentence of the user contains the word "επεξεργασία", gets the number code the user says
                String numberOnly= matches.get(0).replaceAll("[^0-9]", "");
                if (numberOnly.equals("")){
                    Toast.makeText(getApplicationContext(), R.string.edit_code, Toast.LENGTH_LONG).show();
                    myTts.speak(getString(R.string.edit_code));
                } else {
                    if (findPosition(Integer.parseInt(numberOnly)) != -1 ){
                        //If the code exists user hears the response and then open the edit modal of this message code
                        myTts.speak(getString(R.string.edit_low) + " " + numberOnly);
                        recyclerView.findViewHolderForAdapterPosition(findPosition(Integer.parseInt(numberOnly))).itemView.findViewById(R.id.editButton).performClick();
                    } else {
                        //Else show and read the toast message
                        Toast.makeText(getApplicationContext(), R.string.code_not_exist, Toast.LENGTH_LONG).show();
                        myTts.speak(getString(R.string.code_not_exist));
                    }
                }
            } else if(matches.get(0).contains("διαγραφή")){
                //Else if the sentence of the user contains the word "διαγραφή", gets the number code the user says
                String numberOnly= matches.get(0).replaceAll("[^0-9]", "");
                if(numberOnly.equals("")){
                    Toast.makeText(getApplicationContext(), R.string.delete_code, Toast.LENGTH_LONG).show();
                    myTts.speak(getString(R.string.delete_code));
                } else {
                    if (findPosition(Integer.parseInt(numberOnly)) != -1 ) {
                        //If the code exists user hears the response and then open the delete modal of this message code
                        myTts.speak(getString(R.string.delete_low) + " " + numberOnly);
                        recyclerView.findViewHolderForAdapterPosition(findPosition(Integer.parseInt(numberOnly))).itemView.findViewById(R.id.deleteButton).performClick();
                    } else {
                        //Else show and read the toast message
                        Toast.makeText(getApplicationContext(), R.string.code_not_exist, Toast.LENGTH_LONG).show();
                        myTts.speak(getString(R.string.code_not_exist));
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.command_error, Toast.LENGTH_LONG).show();
                myTts.speak(getString(R.string.command_error));
            }
        }
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

    //Method that creates and opens a modal in order to create a new message code
    public void createMessageCode(){
        try {
            //Initialization of custom alert dialog
            AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.create_dialog,null);
            builder.setView(view);

            //Views initialization
            EditText smsCode = view.findViewById(R.id.createSMScode);
            EditText title = view.findViewById(R.id.createTitle);
            Button createButton = view.findViewById(R.id.createButton);

            //Create the dialog and show it
            AlertDialog dialog = builder.create();
            dialog.show();

            //This button when clicked creates a new message code
            createButton.setOnClickListener(view1 -> {
                //If at least one of the edittexts is empty show warning toast
                if (smsCode.getText().toString().isEmpty() || title.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.empty_text), Toast.LENGTH_SHORT).show();
                } else {
                    //Else insert data to the SQLite database, update the recycler view and close the dialog
                    sqLiteHelper.insertData(Integer.parseInt(smsCode.getText().toString()), title.getText().toString());
                    updateList();
                    dialog.dismiss();
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
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
        settingsAdapter.notifyDataSetChanged();
    }

    //Method for the animation of the views
    public void animation(){
        recyclerView.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),R.anim.move_up)
        );
        addButton.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(), R.anim.move_left)
        );
        micButton.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(), R.anim.move_up)
        );
    }

}