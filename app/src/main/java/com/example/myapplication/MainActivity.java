package com.example.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageButton capture, home, gallery, info;
    ChipGroup chipGroupView;
    ArrayList<Integer> allergenSelected = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        capture = findViewById(R.id.ccapture);
        home = findViewById(R.id.chome);
        gallery = findViewById(R.id.cgallery);
        chipGroupView = findViewById(R.id.chipGroup);
        info = findViewById(R.id.info);

        capture.setOnClickListener(view -> {
            checkAllergenSelected(false);
        });

        gallery.setOnClickListener(view -> {
            checkAllergenSelected(true);
        });

        home.setOnClickListener(view -> finish());

        info.setOnClickListener(view -> {
            MainActivity.this.startActivity(new Intent(MainActivity.this, InfoActivity.class));
        });

        chipGroupView.setOnCheckedStateChangeListener((chipGroup, list) -> {
            allergenSelected = (ArrayList<Integer>) list;
//                Toast.makeText(MainActivity2.this, "Choice" + list + " is Clicked", Toast.LENGTH_SHORT).show();
        });

        setChipColorStateList(R.id.choice1);
        setChipColorStateList(R.id.choice2);
        setChipColorStateList(R.id.choice3);
        setChipColorStateList(R.id.choice4);
        setChipColorStateList(R.id.choice5);
        setChipColorStateList(R.id.choice6);
        setChipColorStateList(R.id.choice7);
        setChipColorStateList(R.id.choice8);
        setChipColorStateList(R.id.choice9);
        setChipColorStateList(R.id.choice10);
    }

    private void checkAllergenSelected(boolean isOpenGallery) {
        if (allergenSelected == null || allergenSelected.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // Add the buttons.
            builder.setMessage("Please tell us your allergen information!").setTitle("Crunch Guard").setIcon(R.drawable.logo);
            builder.setPositiveButton("Okay", (dialog, id) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
            dialog.show();
        } else {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            intent.putIntegerArrayListExtra("allergenSelected", allergenSelected);
            intent.putExtra("gallery", isOpenGallery);
            MainActivity.this.startActivity(intent);
        }
    }

    private void setChipColorStateList(int chipId) {
        Chip chip = findViewById(chipId);
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] {-android.R.attr.state_pressed}  // unpressed
        };
        int[] colors = new int[] {
                getColor(R.color.white), getColor(R.color.chipClick)
        };
        int[] textColors = new int[] {
                getColor(R.color.text), getColor(R.color.white)
        };
        ColorStateList colorsStateList = new ColorStateList(states, colors);
        ColorStateList textColorsStateList = new ColorStateList(states, textColors);
        chip.setChipBackgroundColor(colorsStateList);
        chip.setTextColor(textColorsStateList);
    }
}