package com.peart.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch lifeStorySwitch, familyTreeLinesSwitch, spouseLinesSwitch, fathersSideSwitch, mothersSideSwitch, maleEventsSwitch, femaleEventsSwitch;
    private DataCache dataCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lifeStorySwitch       = findViewById(R.id.story_lines_switch);
        familyTreeLinesSwitch = findViewById(R.id.family_tree_lines_switch);
        spouseLinesSwitch     = findViewById(R.id.spouse_lines_switch);
        fathersSideSwitch     = findViewById(R.id.fathers_side_switch);
        mothersSideSwitch     = findViewById(R.id.mothers_side_switch);
        maleEventsSwitch      = findViewById(R.id.male_events_switch);
        femaleEventsSwitch    = findViewById(R.id.female_events_switch);
        TextView logout       = findViewById(R.id.log_out_view);
        dataCache             = DataCache.getInstance();

        // init
        lifeStorySwitch      .setChecked(dataCache.isLifeStoryLines());
        familyTreeLinesSwitch.setChecked(dataCache.isFamilyTreeLines());
        spouseLinesSwitch    .setChecked(dataCache.isSpouseLines());
        fathersSideSwitch    .setChecked(dataCache.isFathersSide());
        mothersSideSwitch    .setChecked(dataCache.isMothersSide());
        maleEventsSwitch     .setChecked(dataCache.isMaleEvents());
        femaleEventsSwitch   .setChecked(dataCache.isFemaleEvents());

        // on click listeners
        lifeStorySwitch.setOnClickListener(view -> {
            dataCache.setLifeStoryLines(lifeStorySwitch.isChecked());
        });

        familyTreeLinesSwitch.setOnClickListener(view -> {
            dataCache.setFamilyTreeLines(familyTreeLinesSwitch.isChecked());
        });

        spouseLinesSwitch.setOnClickListener(view -> {
            dataCache.setSpouseLines(spouseLinesSwitch.isChecked());
        });

        fathersSideSwitch.setOnClickListener(view -> {
            dataCache.setFathersSide(fathersSideSwitch.isChecked());
        });

        mothersSideSwitch.setOnClickListener(view -> {
            dataCache.setMothersSide(mothersSideSwitch.isChecked());
        });

        maleEventsSwitch.setOnClickListener(view -> {
            dataCache.setMaleEvents(maleEventsSwitch.isChecked());
        });

        femaleEventsSwitch.setOnClickListener(view -> {
            dataCache.setFemaleEvents(femaleEventsSwitch.isChecked());
        });

        logout.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
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
}