package com.peart.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import Model.Event;

public class EventActivity extends AppCompatActivity {

    private static final String EVENT_DETAILS_KEY = "EVENT_DETAILS_KEY";
    private final DataCache dataCache = DataCache.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // init map fragment
        Intent intent = getIntent();
        String eventID = intent.getStringExtra(EVENT_DETAILS_KEY);
        Event event = dataCache.getEvents().get(eventID);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = new MapFragment(event);

        fragmentManager.beginTransaction()
                .add(R.id.event_frame_layout, fragment)
                .commit();
        dataCache.setFromEvent(true);
    }
}