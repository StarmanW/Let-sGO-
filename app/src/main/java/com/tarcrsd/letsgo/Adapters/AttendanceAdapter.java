package com.tarcrsd.letsgo.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.tarcrsd.letsgo.Models.EventAttendees;
import com.tarcrsd.letsgo.Models.User;
import com.tarcrsd.letsgo.R;

import java.util.ArrayList;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    // Member variables.
    private Context mContext;
    private ArrayList<EventAttendees> eventAttendees;
    private FirebaseFirestore db;

    /**
     * Constructor that passes in the event attendees data and the context.
     *
     * @param eventAttendees ArrayList containing the event attendees data.
     * @param context    Context of the application.
     */
    public AttendanceAdapter(Context context, ArrayList<EventAttendees> eventAttendees) {
        this.mContext = context;
        this.eventAttendees = eventAttendees;
        this.db = FirebaseFirestore.getInstance();
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
    public AttendanceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AttendanceAdapter.ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_attendance, parent, false));
    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder   The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(AttendanceAdapter.ViewHolder holder, int position) {
        // Get current event.
        EventAttendees eventAttendee = eventAttendees.get(position);

        // Populate the textviews with data.
        holder.bindTo(eventAttendee);
    }


    /**
     * Required method for determining the size of the data set.
     *
     * @return Size of the data set.
     */
    @Override
    public int getItemCount() {
        return eventAttendees.size();
    }

    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */
    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // Member Variables for the TextViews
        private TextView txtName;
        private CheckBox checkBox;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item.xml layout file.
         */
        ViewHolder(View itemView) {
            super(itemView);

            // Initialize the view.
            txtName = itemView.findViewById(R.id.txtName);
            checkBox = itemView.findViewById(R.id.checkBox);

            // Set the OnClickListener to the entire view.
            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        void bindTo(EventAttendees eventAttendee) {
            // Display default checkbox status
            if (eventAttendee.getStatus() == 1) {
                checkBox.setChecked(false);
            } else if (eventAttendee.getStatus() == 2) {
                checkBox.setChecked(true);
            }

            // Populate the text view with data.
            eventAttendee.getUserUID()
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User user = task.getResult().toObject(User.class);
                            txtName.setText(user.getName());
                        }
                    });
        }

        /**
         * Handle click to show DetailActivity.
         *
         * @param view View that is clicked.
         */
        @Override
        public void onClick(View view) {
            // Get the selected event attendees index
            EventAttendees eventAttendee = eventAttendees.get(getAdapterPosition());
            if (eventAttendee.getStatus() == 1) {
                checkBox.setChecked(true);
                eventAttendee.setStatus(2);
            } else if (eventAttendee.getStatus() == 2) {
                checkBox.setChecked(false);
                eventAttendee.setStatus(1);
            }

            db.document("/eventAttendees/" + eventAttendee.getId())
                    .set(eventAttendee, SetOptions.merge());
        }
    }
}
