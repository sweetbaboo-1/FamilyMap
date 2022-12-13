package com.peart.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Model.Event;
import Model.Person;

public class SearchActivity extends AppCompatActivity {

    private static final int PERSON_VIEW_TYPE = 0;
    private static final int EVENT_VIEW_TYPE = 1;
    private static final String PERSON_ID_KEY = "PERSON_ID_KEY";
    private static final String FIRST_NAME_KEY = "FIRST_NAME_KEY";
    private static final String LAST_NAME_KEY = "LAST_NAME_KEY";
    private static final String GENDER_KEY = "GENDER_KEY";
    private static final String EVENT_DETAILS_KEY = "EVENT_DETAILS_KEY";

    private final DataCache dataCache = DataCache.getInstance();
    private SearchView searchView;
    private List<Person> people;
    private List<Event> events;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        people = new ArrayList<>();
        events = new ArrayList<>();

        SearchActivity.SearchActivityAdapter adapter = new SearchActivity.SearchActivityAdapter(events, people);
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextSubmit(String query) {
                events = dataCache.getFilteredEvents(query);
                people = dataCache.getFilteredPeople(query);
                SearchActivity.SearchActivityAdapter adapter = new SearchActivity.SearchActivityAdapter(events, people);
                recyclerView.setAdapter(adapter);
                return true;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println("Changing: " + newText);
                events = dataCache.getFilteredEvents(newText);
                people = dataCache.getFilteredPeople(newText);
                SearchActivity.SearchActivityAdapter adapter = new SearchActivity.SearchActivityAdapter(events, people);
                recyclerView.setAdapter(adapter);
                return true;
            }
        });
    }

    private class SearchActivityAdapter extends RecyclerView.Adapter<SearchActivity.SearchActivityViewHolder> {
        private final List<Event> events;
        private final List<Person> people;

        SearchActivityAdapter(List<Event> events, List<Person> people) {
            this.events = events;
            this.people = people;
        }

        @Override
        public int getItemViewType(int position) {
            return position < people.size() ? PERSON_VIEW_TYPE : EVENT_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchActivity.SearchActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == PERSON_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.person_layout, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.event_layout, parent, false);
            }
            return new SearchActivity.SearchActivityViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchActivity.SearchActivityViewHolder holder, int position) {
            if (position < people.size()) {
                holder.bind(people.get(position));
            } else {
                holder.bind(events.get(position - people.size()));
            }
        }

        @Override
        public int getItemCount() {
            return people.size() + events.size();
        }

    }

    private class SearchActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView details;
        private final int viewType;
        private Event event;
        private Person person;

        SearchActivityViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if(viewType == PERSON_VIEW_TYPE) {
                details = itemView.findViewById(R.id.person_details);
            } else {
                details = itemView.findViewById(R.id.event_details);
            }
        }

        private void bind(Event event) {
            this.event = event;
            Person person = dataCache.getPeople().get(event.getPersonID());
            assert person != null;
            String eventText = event.getEventType().toUpperCase() + ": " + event.getCity() + ", " + event.getCountry() + " (" + event.getYear() + ")\n" + person.getFirstName() + " " + person.getLastName();
            details.setText(eventText);
            details.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_location_on_24, 0, 0, 0);
        }

        private void bind(Person person) {
            this.person = person;
            assert person != null;
            String personText = person.getFirstName() + " " + person.getLastName() + "\n";
            details.setText(personText);
            if (person.getGender().equalsIgnoreCase("m")) {
                details.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_man_24, 0, 0, 0);
            } else {
                details.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_woman_24, 0, 0, 0);
            }
        }

        @Override
        public void onClick(View view) {
            if (viewType == PERSON_VIEW_TYPE) {
                Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra(FIRST_NAME_KEY, person.getFirstName());
                intent.putExtra(LAST_NAME_KEY, person.getLastName());
                intent.putExtra(GENDER_KEY, person.getGender());
                intent.putExtra(PERSON_ID_KEY, person.getPersonID());
                startActivity(intent);
            }

            if (viewType == EVENT_VIEW_TYPE) {
                Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra(EVENT_DETAILS_KEY, event.getEventID());
                startActivity(intent);
            }
        }
    }

}