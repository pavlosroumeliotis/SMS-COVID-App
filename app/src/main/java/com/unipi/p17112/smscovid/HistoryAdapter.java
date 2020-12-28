package com.unipi.p17112.smscovid;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    ArrayList<History> historyList;
    Context context;

    public HistoryAdapter(Context context, ArrayList<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_record,parent,false);
        HistoryAdapter.ViewHolder viewHolder = new HistoryAdapter.ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        holder.bind(historyList.get(position), position);
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView longitude, latitude, timestamp, code;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            longitude = itemView.findViewById(R.id.textViewLongitude);
            latitude = itemView.findViewById(R.id.textViewLatitude);
            timestamp = itemView.findViewById(R.id.textViewTimestamp);
            code = itemView.findViewById(R.id.textViewCode);
            cardView = itemView.findViewById(R.id.cardViewHistory);
        }

        public void bind(History history, int position){
            String x = "X: ";
            String y = "Y: ";
            //If the coordinates are null set text as unknown
            if (history.getLatitude() == null && history.getLongitude() == null){
                x = x + context.getString(R.string.unknown);
                y = y + context.getString(R.string.unknown);
            } else {
                //Else set text with the coordinates
                x = x + history.getLongitude();
                y = y + history.getLatitude();
            }
            longitude.setText(x);
            latitude.setText(y);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date = new Date();
            try{
                date = dateFormat.parse(history.getTimestamp());
            } catch (ParseException e){
                e.printStackTrace();
            }
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String timestampText = dateFormat1.format(date);
            timestamp.setText(timestampText);
            code.setText(String.valueOf(history.getCodeID()));

            //When cardview clicked, Google maps is opened with the coordinates of the history record
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //If location is null show warning toast
                    if (historyList.get(position).getLatitude() == null && historyList.get(position).getLongitude() == null){
                        Toast.makeText(context, context.getString(R.string.google_maps_unknown), Toast.LENGTH_LONG).show();
                    } else {
                        //Else
                        //Opens new intent
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        //Opens with google maps
                        intent.setPackage("com.google.android.apps.maps");
                        //Set the data
                        intent.setData(Uri.parse("https://www.google.com/maps/search/?api=1&query="+
                                historyList.get(position).getLatitude() +","+historyList.get(position).getLongitude()));
                        view.getContext().startActivity(intent);
                    }
                }
            });
        }
    }
}
