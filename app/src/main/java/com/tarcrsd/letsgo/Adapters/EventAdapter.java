package com.tarcrsd.letsgo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tarcrsd.letsgo.EventDetailsActivity;
import com.tarcrsd.letsgo.Models.Events;
import com.tarcrsd.letsgo.Module.DateFormatterModule;
import com.tarcrsd.letsgo.Module.GlideApp;
import com.tarcrsd.letsgo.R;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    // Member variables.
    private Context mContext;
    private StorageReference mStorageRef;
    private ArrayList<Events> mEventsData;

    /**
     * Constructor that passes in the events data and the context.
     *
     * @param eventsData ArrayList containing the events data.
     * @param context    Context of the application.
     */
    public EventAdapter(Context context, ArrayList<Events> eventsData) {
        this.mContext = context;
        this.mEventsData = eventsData;
        this.mStorageRef = FirebaseStorage.getInstance().getReference();
    }


    /**
     * Required method for creating the viewholder objects.
     *
     * @param parent   The ViewGroup into which the new View will be added
     *                 after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly created ViewHolder.
     */
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_event, parent, false));
    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder   The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, int position) {
        // Get current event.
        Events currentEvent = mEventsData.get(position);

        // Populate the textviews with data.
        holder.bindTo(currentEvent);
    }


    /**
     * Required method for determining the size of the data set.
     *
     * @return Size of the data set.
     */
    @Override
    public int getItemCount() {
        return mEventsData.size();
    }

    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */
    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // Member Variables for the TextViews
        private ImageView mEventImage;
        private TextView mTitleText;
        private TextView mDetailText;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item.xml layout file.
         */
        ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views.
            mEventImage = itemView.findViewById(R.id.eventImage);
            mTitleText = itemView.findViewById(R.id.lblEventTitle);
            mDetailText = itemView.findViewById(R.id.lblEventDetails);

            // Set the OnClickListener to the entire view.
            itemView.setOnClickListener(this);
        }

        void bindTo(Events currentEvent) {
            // Populate the textviews with data.
            mTitleText.setText(currentEvent.getName());
            mDetailText.setText(String.format("%s (%s) - %s",
                    DateFormatterModule.getDate(currentEvent.getDate()),
                    DateFormatterModule.getTime(currentEvent.getTime()),
                    currentEvent.getLocality()));

            // Load the images into the ImageView using the Glide library.
            GlideApp.with(mContext)
                    .load(mStorageRef.child(currentEvent.getImage()))
                    .into(mEventImage);
        }

        /**
         * Handle click to show DetailActivity.
         *
         * @param view View that is clicked.
         */
        @Override
        public void onClick(View view) {
            // Get the selected event index
            Events currentEvent = mEventsData.get(getAdapterPosition());

            // Start event details activity
            Intent detailIntent = new Intent(mContext, EventDetailsActivity.class);
            detailIntent.putExtra("eventID", currentEvent.getEventID());
            mContext.startActivity(detailIntent);
        }
    }
}
