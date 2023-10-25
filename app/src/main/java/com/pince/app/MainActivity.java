package com.pince.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupérer le TextView pour la date et l'heure
        TextView dateTimeTextView = findViewById(R.id.textViewDateTime);

        // Obtenir la date et l'heure actuelles
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h'mm, EEEE dd MMMM", new Locale("fr", "FR"));
        String formattedDate = dateFormat.format(currentDate);

        // Définir la date et l'heure dans le TextView
        dateTimeTextView.setText(formattedDate);
    }
}