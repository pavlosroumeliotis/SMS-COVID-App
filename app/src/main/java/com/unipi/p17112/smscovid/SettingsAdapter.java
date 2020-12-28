package com.unipi.p17112.smscovid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
    ArrayList<MessageCode> messageCodes;
    Context context;

    public SettingsAdapter(Context context, ArrayList<MessageCode> messageCodes){
        this.context = context;
        this.messageCodes = messageCodes;
        notifyDataSetChanged();
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.code_settings,parent,false);
        SettingsAdapter.ViewHolder viewHolder = new SettingsAdapter.ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        holder.bind(messageCodes.get(position),position);
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return messageCodes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView codeSubtitle, codeId;
        ImageButton editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            codeId = itemView.findViewById(R.id.codeId);
            codeSubtitle = itemView.findViewById(R.id.codeSubtitle);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(MessageCode messageCode, int position){
            String title = context.getString(R.string.code) + " " + String.valueOf(messageCode.getId());
            codeId.setText(title);
            codeSubtitle.setText(messageCode.getSubtitle());
            //This button when clicked opens the edit code modal
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openUpdateModal(position, messageCode.getId());
                }
            });
            //This button when clicked opens the delete code modal
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDeleteModal(position, messageCode.getId());
                }
            });
        }

        //Method that creates and opens a modal in order to delete a message code
        public void openDeleteModal(int position, int message_id){
            //Initialization of custom alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog,null);
            builder.setView(view);

            //If user click the positive button delete the data from the database and update the recycler view
            builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MessageCodesSettings.sqLiteHelper.deleteData(message_id);
                    messageCodes.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, messageCodes.size());
                }
            });

            //If user click the negative button then the dialog closes
            builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            //Create the dialog and show it
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        //Method that creates and opens a modal in order to update a message code
        public void openUpdateModal(int position, int message_id){
            //Initialization of custom alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.update_dialog,null);
            builder.setView(view);

            //Views initialization
            EditText smsCode = view.findViewById(R.id.createSMScode);
            EditText title = view.findViewById(R.id.createTitle);
            Button updateButton = view.findViewById(R.id.createButton);

            //Set text of the textviews
            smsCode.setText(String.valueOf(messageCodes.get(position).getId()));
            title.setText(messageCodes.get(position).getSubtitle());

            //Create the dialog and show it
            AlertDialog dialog = builder.create();
            dialog.show();

            //This button when clicked
            updateButton.setOnClickListener((view1) -> {
                //If at least one of the edittexts is empty show text
                if(smsCode.getText().toString().isEmpty() || title.getText().toString().isEmpty()){
                    Toast.makeText(context,
                            context.getString(R.string.empty_text), Toast.LENGTH_SHORT).show();
                } else {
                    //Else update the data in the database and update the recycler view
                    MessageCodesSettings.sqLiteHelper.updateData(Integer.parseInt(smsCode.getText().toString()), title.getText().toString(), message_id);
                    notifyItemChanged(position);
                    messageCodes.set(position, new MessageCode(Integer.parseInt(smsCode.getText().toString()),title.getText().toString()));
                    dialog.dismiss();
                }
            });
        }
    }
}
