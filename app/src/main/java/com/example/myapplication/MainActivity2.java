package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    FrameLayout capture, home, gallery;
    ChipGroup chipGroupView;

    ArrayList<Integer> allergenSelected = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        capture = findViewById(R.id.capture);
        home = findViewById(R.id.home);
        gallery = findViewById(R.id.gallery);
        chipGroupView = findViewById(R.id.chipGroup);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allergenSelected == null || allergenSelected.size() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                    // Add the buttons.
                    builder.setMessage("Please tell us your allergen information!").setTitle("Crunch Guard").setIcon(R.drawable.logo);
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Intent intent = new Intent(MainActivity2.this, CaptureActivity.class);
                    intent.putIntegerArrayListExtra("allergenSelected", allergenSelected);
                    MainActivity2.this.startActivity(intent);
                }
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        chipGroupView.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
                allergenSelected = (ArrayList<Integer>) list;
                Toast.makeText(MainActivity2.this, "Choice" + list + " is Clicked", Toast.LENGTH_SHORT).show();
            }
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

    public void setChipColorStateList(int chipId) {
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