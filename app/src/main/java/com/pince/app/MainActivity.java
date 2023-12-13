package com.pince.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Date et heure ---------------


        // Récupérer le TextView pour la date et l'heure
        TextView dateTimeTextView = findViewById(R.id.textViewDateTime);

        // Obtenir la date et l'heure actuelles
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h'mm, EEEE dd MMMM", new Locale("fr", "FR"));
        String formattedDate = dateFormat.format(currentDate);

        // Définir la date et l'heure dans le TextView
        dateTimeTextView.setText(formattedDate);


        // --------------------------------



        // Onglets ---------------

        // Récupérez une référence à votre TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Sélectionnez l'onglet "Preset" par défaut
        TabLayout.Tab tab = tabLayout.getTabAt(0); // 0 pour le premier onglet, 1 pour le deuxième, etc.
        if (tab != null) {
            tab.select();
        }

        // Définissez un écouteur de sélection d'onglet
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // L'onglet "Preset" est sélectionné
                if (tab.getPosition() == 0) {
                    Button presetButton1 = findViewById(R.id.presetButton1);
                    Button presetButton2 = findViewById(R.id.presetButton2);
                    Button presetButton3 = findViewById(R.id.presetButton3);
                    EditText editTexteInput = findViewById(R.id.addTextPreset);
                    Spinner spinnerPreset = findViewById(R.id.spinner);
                    presetButton1.setVisibility(View.VISIBLE);
                    presetButton2.setVisibility(View.VISIBLE);
                    spinnerPreset.setVisibility(View.VISIBLE);
                    presetButton3.setVisibility(View.GONE);
                    editTexteInput.setVisibility(View.GONE);
                }
                // L'onglet "Personnalisable" est sélectionné
                else if (tab.getPosition() == 1) {
                    Button presetButton1 = findViewById(R.id.presetButton1);
                    Button presetButton2 = findViewById(R.id.presetButton2);
                    Button presetButton3 = findViewById(R.id.presetButton3);
                    EditText editTexteInput = findViewById(R.id.addTextPreset);
                    Spinner spinnerPreset = findViewById(R.id.spinner);
                    presetButton1.setVisibility(View.GONE);
                    presetButton2.setVisibility(View.GONE);
                    spinnerPreset.setVisibility(View.GONE);
                    presetButton3.setVisibility(View.VISIBLE);
                    editTexteInput.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Ne rien faire ici
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Ne rien faire ici
            }
        });

        // -----------------------------------------
    }
}
