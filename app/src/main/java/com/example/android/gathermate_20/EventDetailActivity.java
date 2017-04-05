package com.example.android.gathermate_20;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EVENT_DETAIL";

    TextView locationView;
    TextView dateView;
    TextView timeView;
    TextView descView;
    TextView nameView;
    Button deleteButton;
    boolean isOwner;

    private DatabaseReference databaseEvents;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Intent intent = getIntent();
        final Event event = intent.getParcelableExtra("event");

        //Location
        locationView = (TextView) findViewById(R.id.detailLocation);
        locationView.setText(event.location);

        //Date and Time
        dateView = (TextView) findViewById(R.id.detailDate);
        timeView = (TextView) findViewById(R.id.detailTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(event.time));
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
        Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
        Integer minute = calendar.get(Calendar.MINUTE);

        String timeText = String.format("%d:%02d", hour, minute) + " " + "AM";
        if(hour >= 12) {
            if(hour > 12){
                hour -= 12;
            }
            timeText = String.format("%d:%02d", hour, minute) + " " + "PM";
        }else if(hour == 0) {
            hour = 12;
            timeText = String.format("%d:%02d", hour, minute) + " " + "AM";
        }
        dateView.setText(month + "/" + day + "/" + year);
        timeView.setText(timeText);

        //Description
        descView = (TextView) findViewById(R.id.detailDesc);
        descView.setText(event.description);

        //Name
        nameView = (TextView) findViewById(R.id.detailName);

        //Delete
        deleteButton = (Button) findViewById(R.id.eventDeleteButton);
        isOwner = intent.getBooleanExtra("isOwner",false);
        if (isOwner) {
            nameView.setText(event.name + " (me)");
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { deleteEvent(v, event.uid, event.eventId); }
            });
        } else {
            nameView.setText(event.name);
            deleteButton.setVisibility(View.INVISIBLE);
        }

        databaseEvents = FirebaseDatabase.getInstance().getReference();
    }

    public void deleteEvent (View v, String uid, String eventId) {
        databaseEvents.child(uid).child(eventId).removeValue();
        Intent intent = new Intent(EventDetailActivity.this, EventsActivity.class);
        startActivity(intent);
    }
}
