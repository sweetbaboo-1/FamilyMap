package com.peart.familymap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import Model.Event;
import Model.Person;


public class DataCache {

    private static final DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private String id;
    private final Map<String, Person> people;
    private final Map<String, Event> events;
    private final Map<String, List<Event>> personEvents;
    private final Set<String> paternalAncestors;
    private final Set<String> maternalAncestors;
    private final List<Event> fatherSideEvents, motherSideEvents;
    private boolean lifeStoryLines = true, familyTreeLines = true, spouseLines = true, fathersSide = true, mothersSide = true, maleEvents = true, femaleEvents = true, fromEvent = false;

    private DataCache() {
        people = new HashMap<>();
        events = new HashMap<>();
        personEvents = new HashMap<>();
        paternalAncestors = new HashSet<>();
        maternalAncestors = new HashSet<>();
        fatherSideEvents = new ArrayList<>();
        motherSideEvents = new ArrayList<>();
    }

    // methods

    public void addPeople(List<Person> personList) {
        for (Person p: personList) {
            people.put(p.getPersonID(), p);
        }
    }

    public void addEvent(Event[] data) {
        for (Event e: data) {
            events.put(e.getEventID(), e);
            // populate personEvents
            if (personEvents.get(e.getPersonID()) == null) {
                personEvents.put(e.getPersonID(), new ArrayList<>());
            }
            Objects.requireNonNull(personEvents.get(e.getPersonID())).add(e);
        }
    }

    public void setRoodID(String id) {
        this.id = id;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public List<Event> getFilteredPersonEvents(String personID) {
        List<Event> events = new ArrayList<>();
        Map<String, Event> filteredEvents = getFilteredEvents();
        for (Event event: filteredEvents.values()) {
            if (Objects.equals(event.getPersonID(), personID)) {
                events.add(event);
            }
        }
        return events;
    }

    public Set<String> getPaternalAncestors(String personID) {
        paternalAncestors.clear();
        initPaternalAncestors(personID, true);
        return paternalAncestors;
    }

    public Set<String> getMaternalAncestors(String personID) {
        maternalAncestors.clear();
        initMaternalAncestors(personID, true);
        return maternalAncestors;
    }

    public List<Person> getPersonFamily(String personID) {
        // get the family members of the person requested
        Person self = people.get(personID);
        if (self == null) {
            return new ArrayList<>();
        }

        List<Person> family = new ArrayList<>();

        // get dad, mom, and spouse if they exist
        if (people.get(self.getFatherID()) != null) {
            family.add(people.get(self.getFatherID()));
        }
        if (people.get(self.getMotherID()) != null) {
            family.add(people.get(self.getMotherID()));
        }
        if (people.get(self.getSpouseID()) != null) {
            family.add(people.get(self.getSpouseID()));
        }

        // get the children
        for (Person p: people.values()) {
            if (Objects.equals(p.getMotherID(), personID) || Objects.equals(p.getFatherID(), personID)) {
                family.add(p);
            }
        }
        return family;
    }

    public boolean isLifeStoryLines() {
        return lifeStoryLines;
    }

    public void setLifeStoryLines(boolean lifeStoryLines) {
        this.lifeStoryLines = lifeStoryLines;
    }

    public boolean isFamilyTreeLines() {
        return familyTreeLines;
    }

    public void setFamilyTreeLines(boolean familyTreeLines) {
        this.familyTreeLines = familyTreeLines;
    }

    public boolean isSpouseLines() {
        return spouseLines;
    }

    public void setSpouseLines(boolean spouseLines) {
        this.spouseLines = spouseLines;
    }

    public boolean isFathersSide() {
        return fathersSide;
    }

    public void setFathersSide(boolean fathersSide) {
        this.fathersSide = fathersSide;
    }

    public boolean isMothersSide() {
        return mothersSide;
    }

    public void setMothersSide(boolean mothersSide) {
        this.mothersSide = mothersSide;
    }

    public boolean isMaleEvents() {
        return maleEvents;
    }

    public void setMaleEvents(boolean maleEvents) {
        this.maleEvents = maleEvents;
    }

    public boolean isFemaleEvents() {
        return femaleEvents;
    }

    public void setFemaleEvents(boolean femaleEvents) {
        this.femaleEvents = femaleEvents;
    }

    private void initPaternalAncestors(String personID, boolean isFirst) {
        Person self = people.get(personID);

        if (self == null) {
            return;
        }

        Person father = people.get(self.getFatherID());
        Person mother = people.get(self.getMotherID());

        if (father != null) {
            paternalAncestors.add(father.getPersonID());
            initPaternalAncestors(father.getPersonID(), false);
        }

        if (!isFirst) {
            if (mother != null) {
                paternalAncestors.add(mother.getPersonID());
                initPaternalAncestors(mother.getPersonID(), false);
            }
        }
    }

    private void initMaternalAncestors(String personID, boolean isFirst) {
        Person self = people.get(personID);

        if (self == null) {
            return;
        }

        Person father = people.get(self.getFatherID());
        Person mother = people.get(self.getMotherID());

        if (!isFirst) {
            if (father != null) {
                maternalAncestors.add(father.getPersonID());
                initMaternalAncestors(father.getPersonID(), false);
            }
        }

        if (mother != null) {
            maternalAncestors.add(mother.getPersonID());
            initMaternalAncestors(mother.getPersonID(), false);
        }
    }

    public Map<String, Event> getFilteredEvents() {
        // create a custom map of events according to the settings
        Set<Event> filteredEvents = new HashSet<>(events.values());
        // for each event check if it matches the filters and if it does, add it to the map
        for (Event event: events.values()) {

            // if we shouldn't have male events
            if (Objects.requireNonNull(people.get(event.getPersonID())).getGender().equalsIgnoreCase("m") && !maleEvents) {
                filteredEvents.remove(event);
            }

            // if we shouldn't have female events
            if (Objects.requireNonNull(people.get(event.getPersonID())).getGender().equalsIgnoreCase("f") && !femaleEvents) {
                filteredEvents.remove(event);
            }

            // if we shouldn't have father's side events
            // make a list of all the events on the father's side, then iterate through and pull out events that match
            if (!fathersSide) {
                getFatherSideEvents(event.getPersonID());
                for (Event e: fatherSideEvents) {
                    filteredEvents.remove(e);
                }
            }

            // if we shouldn't have father's side events
            // make a list of all the events on the mother's side, then iterate through and pull out events that match
            if (!mothersSide) {
                getMotherSideEvents(event.getPersonID());
                for (Event e: motherSideEvents) {
                    filteredEvents.remove(e);
                }
            }
        }

        Map<String, Event> filteredMap = new HashMap<>();
        for (Event e: filteredEvents) {
            filteredMap.put(e.getEventID(), e);
        }

        return filteredMap;
    }

    public List<Event> getFilteredEvents(String query) {
        List<Event> filteredEvents = new ArrayList<>();
        // for each event, if it doesn't match the search string at all remove it
        for (Event event: getFilteredEvents().values()) {
            String year = "" + event.getYear();
            if (event.getEventType().toLowerCase().contains(query) || year.toLowerCase().contains(query) || event.getCountry().toLowerCase().contains(query) || event.getCity().toLowerCase().contains(query)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    public List<Person> getFilteredPeople(String query) {
        List<Person> filteredPeople = new ArrayList<>();
        for (Person person: people.values()) {
            if (person.getFirstName().toLowerCase().contains(query.toLowerCase()) || person.getLastName().toLowerCase().contains(query.toLowerCase())) {
                filteredPeople.add(person);
            }
        }
        return filteredPeople;
    }

    private void getFatherSideEvents(String personID) {
        fatherSideEvents.clear();
        Set<String> fatherSide = getPaternalAncestors(personID);
        for (String id : fatherSide) {
            fatherSideEvents.addAll(Objects.requireNonNull(personEvents.get(id)));
        }
    }

    private void getMotherSideEvents(String personID) {
        motherSideEvents.clear();
        Set<String> fatherSide = getMaternalAncestors(personID);
        for (String id : fatherSide) {
            motherSideEvents.addAll(Objects.requireNonNull(personEvents.get(id)));
        }
    }

    public void setFromEvent(boolean b) {
        fromEvent = b;
    }

    public boolean isFromEvent() {
        return fromEvent;
    }
}
