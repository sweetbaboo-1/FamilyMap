package com.peart.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {

    private static final String PERSON_ID_KEY = "PERSON_ID_KEY";
    private static final String FIRST_NAME_KEY = "FIRST_NAME_KEY";
    private static final String LAST_NAME_KEY = "LAST_NAME_KEY";
    private static final String GENDER_KEY = "GENDER_KEY";
    private static final String EVENT_DETAILS_KEY = "EVENT_DETAILS_KEY";

    private final DataCache dataCache = DataCache.getInstance();
    private Person selectedPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        // setup the stuff we already know -- fn, ln, gender

        TextView firstName = findViewById(R.id.person_activity_first_name_textview);
        TextView lastName = findViewById(R.id.person_activity_last_name_textview);
        TextView gender = findViewById(R.id.person_activity_gender_textview);

        Intent intent = getIntent();

        firstName.setText(intent.getExtras().getString(FIRST_NAME_KEY));
        lastName.setText(intent.getExtras().getString(LAST_NAME_KEY));

        String g = intent.getExtras().getString(GENDER_KEY);
        gender.setText(g.equalsIgnoreCase("m") ? "Male" : "Female");

        String personID = intent.getExtras().getString(PERSON_ID_KEY);
        selectedPerson = dataCache.getPeople().get(personID);

        ExpandableListView expandableListView = findViewById(R.id.expandable_list_view_person);

        List<Event> events = dataCache.getFilteredPersonEvents(personID);
        List<Person> people = dataCache.getPersonFamily(personID);

        expandableListView.setAdapter(new ExpandableListAdapter(events, people));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.up_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.up_button) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENT_GROUP_INDEX = 0;
        private static final int PEOPLE_GROUP_INDEX = 1;

        private final List<Event> events;
        private final List<Person> people;

        ExpandableListAdapter(List<Event> events, List<Person> people) {
            this.events = events;
            this.people = people;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int i) {
            switch (i) {
                case EVENT_GROUP_INDEX:
                    return events.size();
                case PEOPLE_GROUP_INDEX:
                    return people.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.list_title);

            switch (groupPosition) {
                case EVENT_GROUP_INDEX:
                    titleView.setText(R.string.events_group_title);
                    break;
                case PEOPLE_GROUP_INDEX:
                    titleView.setText(R.string.family_group_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch (groupPosition) {
                case EVENT_GROUP_INDEX:
                    itemView = getLayoutInflater().inflate(R.layout.event_layout, parent, false);
                    initializeEventGroupView(itemView, childPosition);
                    break;
                case PEOPLE_GROUP_INDEX:
                    itemView = getLayoutInflater().inflate(R.layout.person_layout, parent, false);
                    initializePersonGroupView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return itemView;
        }

        private void initializeEventGroupView(View itemView, int childPosition) {
            TextView eventNameView = itemView.findViewById(R.id.event_details);
            String eventID = events.get(childPosition).getEventID();
            String eventText = events.get(childPosition).getEventType().toUpperCase() + ": " + events.get(childPosition).getCity() + ", " + events.get(childPosition).getCountry() + " (" + events.get(childPosition).getYear() + ")\n" + selectedPerson.getFirstName() + " " + selectedPerson.getLastName();
            eventNameView.setText(eventText);

            eventNameView.setOnClickListener(view -> {
                Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                // need to pass something here to select the marker and center the camera on it
                intent.putExtra(EVENT_DETAILS_KEY, eventID);
                startActivity(intent);
            });
        }

        private void initializePersonGroupView(View itemView, int childPosition) {
            TextView personView = itemView.findViewById(R.id.person_details);

            Person person = people.get(childPosition);

            if (person.getGender().equalsIgnoreCase("m")) {
                personView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_man_24, 0, 0, 0);
            } else {
                personView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_woman_24, 0, 0, 0);
            }

            String personText = person.getFirstName() + " " + person.getLastName() + "\n";
            String relation = "Child";
            if (Objects.equals(person.getPersonID(), selectedPerson.getMotherID())) {
                relation = "Mother";
            }
            if (Objects.equals(person.getPersonID(), selectedPerson.getFatherID())) {
                relation = "Father";
            }
            if (Objects.equals(person.getPersonID(), selectedPerson.getSpouseID())) {
                relation = "Spouse";
            }
            personText += relation;

            personView.setText(personText);

            personView.setOnClickListener(view -> {
                Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                intent.putExtra(FIRST_NAME_KEY, person.getFirstName());
                intent.putExtra(LAST_NAME_KEY, person.getLastName());
                intent.putExtra(GENDER_KEY, person.getGender());
                intent.putExtra(PERSON_ID_KEY, person.getPersonID());
                startActivity(intent);
            });
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
    }
}