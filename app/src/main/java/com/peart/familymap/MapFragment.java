package com.peart.familymap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Model.Event;
import Model.Person;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private static final String PERSON_ID_KEY = "PERSON_ID_KEY";
    private static final String FIRST_NAME_KEY = "FIRST_NAME_KEY";
    private static final String LAST_NAME_KEY = "LAST_NAME_KEY";
    private static final String GENDER_KEY = "GENDER_KEY";
    private static final String MENU_KEY = "MENU_KEY";

    private GoogleMap map;
    private final DataCache dataCache = DataCache.getInstance();
    private TextView eventDescription, iconView;
    private Person selectedPerson;
    private final Event selectedEvent;
    private int whichMenu;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        setHasOptionsMenu(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        eventDescription = view.findViewById(R.id.fragment_map_textview);
        iconView = view.findViewById(R.id.fragment_map_icon);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (dataCache.isFromEvent()) {
            inflater.inflate(R.menu.up_button, menu);
        } else {
            inflater.inflate(R.menu.map_menu, menu);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsMenuItem:
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.searchMenuItem:
                intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.up_button:
                intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            default: return super.onOptionsItemSelected(item);
        }
    }

    public MapFragment() {
        // Required empty public constructor
        selectedEvent = null;
    }

    public MapFragment(Event event) {
        selectedEvent = event;
    }

    @Override
    public void onMapLoaded() {
        // shouldn't need
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapLoadedCallback(this);

        makeMarkers();

        // run this code if we were passed an event
        // centers the camera on the passed event, and selects it
        if (selectedEvent != null) {
            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude())));

            Person person = dataCache.getPeople().get(selectedEvent.getPersonID());
            assert person != null;
            String displayString = person.getFirstName() + " " + person.getLastName() + "\n" + selectedEvent.getEventType().toUpperCase() + ": " + selectedEvent.getCity() + " " + selectedEvent.getCountry() + " (" + selectedEvent.getYear() + ")";
            eventDescription.setText(displayString);
            if (person.getGender().equalsIgnoreCase("m")) {
                iconView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_man_24, 0, 0, 0);
            } else {
                iconView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_woman_24, 0, 0, 0);
            }
            selectedPerson = person;
            handleLines(selectedEvent);
        }

        map.setOnMarkerClickListener(marker -> {
            Event event = (Event) marker.getTag();
            assert event != null;
            Person person = dataCache.getPeople().get(event.getPersonID());
            assert person != null;
            String displayString = person.getFirstName() + " " + person.getLastName() + "\n" + event.getEventType().toUpperCase() + ": " + event.getCity() + " " + event.getCountry() + " (" + event.getYear() + ")";
            eventDescription.setText(displayString);
            if (person.getGender().equalsIgnoreCase("m")) {
                iconView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_man_24, 0, 0, 0);
            } else {
                iconView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_woman_24, 0, 0, 0);
            }

            handleLines(event);

            selectedPerson = person;
            return true;
        });

        eventDescription.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), PersonActivity.class);
            intent.putExtra(PERSON_ID_KEY, selectedPerson.getPersonID());
            intent.putExtra(FIRST_NAME_KEY, selectedPerson.getFirstName());
            intent.putExtra(LAST_NAME_KEY, selectedPerson.getLastName());
            intent.putExtra(GENDER_KEY, selectedPerson.getGender());
            startActivity(intent);
        });
    }

    private void handleLines(Event event) {
        map.clear();
        makeMarkers();
        drawSpouseLines(event);
        drawFamilyTreeLines(event, 15);
        drawLifeStoryLines(event);
    }

    private void drawLifeStoryLines(Event event) {
        // only draw if the setting wants us to
        if (!dataCache.isLifeStoryLines()) {
            return;
        }
        // get all the events associated with the person to whom the given event belongs
        // draw a line from the first event to the 2nd event, and from the 2nd to the third, and so on
        List<Event> events = new ArrayList<>();
        Event birth = null, death = null;
        for (Event e: dataCache.getFilteredEvents().values()) {
            if (Objects.equals(e.getPersonID(), event.getPersonID())) {
                events.add(e);
                if (e.getEventType().equalsIgnoreCase("birth")) {
                    birth = e;
                }
                if (e.getEventType().equalsIgnoreCase("death")) {
                    death = e;
                }
            }
        }

        // if there are no events then don't worry about drawing logic
        if (events.size() == 0) {
            return;
        }

        // make a list that will hold the events in order
        List<Event> orderedEvents = new ArrayList<>();

        // pull out the birth event and make it the first item in the orderedList
        if (birth != null) {
            orderedEvents.add(birth);
            events.remove(birth);
        }

        // pull out the death event
        if (death != null) {
            events.remove(death);
        }

        // sort all non birth/death events
        while (!events.isEmpty()) {
            Event e = getEarliestEvent(events);
            events.remove(e);
            orderedEvents.add(e);
        }

        // assuming they died, add it to the end of the list
        if (death != null) {
            orderedEvents.add(death);
        }

        // draw the lines for the events
        while (orderedEvents.size() > 1) {
            // get the two events to connect
            Event curr = orderedEvents.get(0);
            Event next = orderedEvents.get(1);

            // make our line
            PolylineOptions options = new PolylineOptions().add(new LatLng(curr.getLatitude(), curr.getLongitude())).add(new LatLng(next.getLatitude(), next.getLongitude())).color(0xfafafafa);
            map.addPolyline(options);
            orderedEvents.remove(curr);
        }
    }

    private void drawFamilyTreeLines(Event event, int width) {
        if (!dataCache.isFamilyTreeLines()) {
            return;
        }
        if (width == 0) {
            width = 1;
        }
        String fatherID, motherID, selfID;
        Event fatherEvent = null, motherEvent = null;
        selfID = event.getPersonID();
        fatherID = Objects.requireNonNull(dataCache.getPeople().get(selfID)).getFatherID();
        motherID = Objects.requireNonNull(dataCache.getPeople().get(selfID)).getMotherID();

        if (fatherID != null) {
            fatherEvent = getEarliestEvent(fatherID);
        }

        if (motherID != null) {
            motherEvent = getEarliestEvent(motherID);
        }

        if (fatherEvent != null) {
            LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());
            LatLng endPoint = new LatLng(fatherEvent.getLatitude(), fatherEvent.getLongitude());

            PolylineOptions options = new PolylineOptions().add(startPoint).add(endPoint).width(width).color(0xff4169e1);
            map.addPolyline(options);
            drawFamilyTreeLines(fatherEvent, width - 5);
        }

        if (motherEvent != null) {
            LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());
            LatLng endPoint = new LatLng(motherEvent.getLatitude(), motherEvent.getLongitude());

            PolylineOptions options = new PolylineOptions().add(startPoint).add(endPoint).width(width).color(0xffffc0cb);
            map.addPolyline(options);
            drawFamilyTreeLines(motherEvent, width - 5);
        }
    }

    private void makeMarkers() {
        Map<String, Event> events = dataCache.getFilteredEvents();
        List<String> eventTypes = new ArrayList<>();

        // get list of event types
        for (Event event: events.values()) {
            if (!eventTypes.contains(event.getEventType())) {
                eventTypes.add(event.getEventType().toLowerCase());
            }
        }
        for (Event event: events.values()) {
            // get the color for the event marker
            float color = 30 * eventTypes.indexOf(event.getEventType().toLowerCase());
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(event.getLatitude(), event.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(color % 360)));
            assert marker != null;
            marker.setTag(event);
        }
    }

    private void drawSpouseLines(@NonNull Event event) {
        if (!dataCache.isSpouseLines()) {
            return;
        }

        // line from selected event to birth event of person's spouse
        Map<String, Person> people = dataCache.getPeople();

        // self is the person to whom the clicked event belongs too
        Person self = people.get(event.getPersonID());

        // spouse is the spouse (if exists) of the person whose event was clicked on
        assert self != null;
        Person spouse = people.get(self.getSpouseID());

        // if there is no spouse no line is drawn
        if (spouse == null) {
            return;
        }

        Event e = getEarliestEvent(spouse.getPersonID());

        if (e == null) {
            return;
        }

        // create the poly line and add it to the map
        LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());
        LatLng endPoint = new LatLng(e.getLatitude(), e.getLongitude());

        PolylineOptions options = new PolylineOptions()
                .add(startPoint)
                .add(endPoint);
        map.addPolyline(options);
    }

    @Nullable
    private Event getEarliestEvent(String personID) {
        List<Event> events = new ArrayList<>();
        for (Event event: dataCache.getFilteredEvents().values()) {
            if (Objects.equals(event.getPersonID(), personID)) {
                events.add(event);
            }
        }

        if (events.size() == 0) {
            return null;
        }
        Event event = events.get(0);
        for (Event e: events) {
            // birth is always "the earliest"
            if (e.getEventType().equalsIgnoreCase("birth")) {
                return e;
            }
            if (event.getYear() < e.getYear()) {
                event = e;
            }
        }
        return event;
    }

    private Event getEarliestEvent(@NonNull List<Event> events) {
        Event minEvent = events.get(0);
        for (Event e: events) {
            if (e.getYear() < minEvent.getYear()) {
                minEvent = e;
            }
        }
        return minEvent;
    }
}
