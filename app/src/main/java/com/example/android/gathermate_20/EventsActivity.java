package com.example.android.gathermate_20;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "EVENTS";

    private final Activity context = this;

    ListView listViewEvents;
    String uid;
    DatabaseReference databaseEvents;
    DatabaseReference databaseThisUser;
    MenuItem searchEmailItem;
    MenuItem searchNameItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the UID for this user, the user database, the event database, and initialize friendList
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseThisUser = FirebaseDatabase.getInstance().getReference().child("userdb").child(uid);
        databaseEvents = FirebaseDatabase.getInstance().getReference().child("eventdb");

        listViewEvents = (ListView) findViewById(R.id.listViewEvents);

        //Get Events only for friends and yourself
        getFriendEvents();

        listViewEvents.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("event", event);
            startActivityForResult(intent, 0);
        });

        //Find AddEventButton and create listener to start AddEventActivity
        FloatingActionButton addEventButton = (FloatingActionButton) findViewById(R.id.eventAddEventButton);
        addEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddEventActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the App Bar
        getMenuInflater().inflate(R.menu.activity_events_app_bar, menu);

        //Create MenuItem variables to be used in onOptionSelected
        searchEmailItem = menu.findItem(R.id.appBarAddFriendsEmail);
        searchNameItem = menu.findItem(R.id.appBarAddFriendsName);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //When the user selects the social icon, open the start the friends activity
            case R.id.appBarSocial:
                Intent intent = new Intent(context, FriendsActivity.class);
                startActivity(intent);
                return true;

            //Do something when the user selects settings
            case R.id.appBarSettings:
                //TODO: Implement Settings Activity
                //Manual XML coding to make the cog icon generate a list
                //Talk to Chris before proceeding
                System.out.println("SETTINGS_CLICKED");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //When we receive a confirm or deny of location service permission,
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 11: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.recreate();
                } else {
                    //TODO: Handle Location Permission Denied
                    //Presumably never request an update again
                    //For now we are letting Android hide the prompts if the user selects "Never Show Again"
                }
            }
        }
    }

    /**
     * Populate friendList with users that you have registered as friends
     * For each member in friendList, populate the events list with that friend's events.
     **/
    private void getFriendEvents() {
        final List<String> friendList = new ArrayList<>();

        //Get friends from the this users database
        databaseThisUser.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendList.clear();
                //Add this user to list and populate the list with friends
                friendList.add(uid);
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    friendList.add(friendSnapshot.getKey());
                }

                getEventsFrom(friendList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getEventsFrom(List<String> friendList) {
        final List<Event> eventList = new ArrayList<>();
        EventsListAdapter adapter = new EventsListAdapter(EventsActivity.this, eventList);
        listViewEvents.setAdapter(adapter);

        for (String friend : friendList) {
            //Create listener for each friends events

            databaseEvents.child(friend).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Event event = dataSnapshot.getValue(Event.class);
                    event.uid = friend;
                    event.eventId = dataSnapshot.getKey();
                    eventList.add(event);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.e(TAG, dataSnapshot.getKey());
                    for (Event event : eventList) {
                        if (event.eventId.equals(dataSnapshot.getKey())) {
                            eventList.remove(event);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
}
