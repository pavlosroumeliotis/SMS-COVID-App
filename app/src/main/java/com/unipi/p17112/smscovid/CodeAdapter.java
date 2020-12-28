package com.unipi.p17112.smscovid;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CodeAdapter extends RecyclerView.Adapter<CodeAdapter.ViewHolder> {
    ArrayList<MessageCode> messageCodes;
    Context context;
    int checkedPosition = -1;

    public CodeAdapter(Context context, ArrayList<MessageCode> messageCodes){
        this.context = context;
        this.messageCodes = messageCodes;
    }

    public void setMessageCodes(ArrayList<MessageCode> messageCodes) {
        this.messageCodes = new ArrayList<>();
        this.messageCodes = messageCodes;
        notifyDataSetChanged();
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public CodeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.code_card,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull CodeAdapter.ViewHolder holder, int position) {
        holder.bind(messageCodes.get(position));
    }

    //Method that return the selected messageCode object
    public MessageCode getSelected(){
        if(checkedPosition != -1){
            return messageCodes.get(checkedPosition);
        }
        return null;
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return messageCodes.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textSubtitle;
        ImageView check;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textViewTitle);
            textSubtitle = itemView.findViewById(R.id.textViewSubtitle);
            check = itemView.findViewById(R.id.check);
            cardView = itemView.findViewById(R.id.cardView2);
        }

        public void bind(MessageCode messageCode){
            //Set text of the text views
            String title = context.getString(R.string.codeId) + ": " + messageCode.getId();
            textTitle.setText(title);
            textSubtitle.setText(messageCode.getSubtitle());
            //If no message code is selected change colors accordingly
            if (checkedPosition == -1){
                textTitle.setTextColor(getAttributeColor(context,R.attr.codeText_non_selected));
                textSubtitle.setTextColor(getAttributeColor(context,R.attr.codeText_non_selected));
                check.setColorFilter(getAttributeColor(context, R.attr.codeText_non_selected));
                check.setImageResource(R.drawable.ic_radio_button_unchecked);
                cardView.setCardBackgroundColor(getAttributeColor(context, R.attr.codeCard_non_selected));
            } else {
                //Else if a message code is selected
                //If the same message code is clicked change colors accordingly
                if (checkedPosition == getAdapterPosition()){
                    textTitle.setTextColor(getAttributeColor(context,R.attr.codeText_selected));
                    textSubtitle.setTextColor(getAttributeColor(context,R.attr.codeText_selected));
                    check.setColorFilter(getAttributeColor(context, R.attr.codeText_selected));
                    check.setImageResource(R.drawable.ic_check_circle_outline);
                    cardView.setCardBackgroundColor(getAttributeColor(context, R.attr.codeCard_selected));
                } else{
                    //Else if another message code is clicked change colors accordingly
                    textTitle.setTextColor(getAttributeColor(context,R.attr.codeText_non_selected));
                    textSubtitle.setTextColor(getAttributeColor(context,R.attr.codeText_non_selected));
                    check.setColorFilter(getAttributeColor(context, R.attr.codeText_non_selected));
                    check.setImageResource(R.drawable.ic_radio_button_unchecked);
                    cardView.setCardBackgroundColor(getAttributeColor(context, R.attr.codeCard_non_selected));
                }
            }
            //When the cardview is clicked change the colors and the checked position
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textTitle.setTextColor(getAttributeColor(context,R.attr.codeText_selected));
                    textSubtitle.setTextColor(getAttributeColor(context,R.attr.codeText_selected));
                    check.setColorFilter(getAttributeColor(context, R.attr.codeText_selected));
                    check.setImageResource(R.drawable.ic_check_circle_outline);
                    cardView.setCardBackgroundColor(getAttributeColor(context, R.attr.codeCard_selected));
                    if (checkedPosition != getAdapterPosition()){
                        notifyItemChanged(checkedPosition);
                        checkedPosition = getAdapterPosition();
                    }
                }
            });
        }

        //Method that returns a color from the attributes in int format
        public int getAttributeColor(Context context, int attributeId) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(attributeId, typedValue, true);
            int colorRes = typedValue.resourceId;
            int color = -1;
            try {
                color = context.getResources().getColor(colorRes);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
            return color;
        }
    }


}