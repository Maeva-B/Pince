package com.pince.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.widget.Toast;

// écriture et lecture de fichier
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Déclaration des variables
        Button button_active_pince = findViewById(R.id.button_active_pince);
        Button button_stop_pince = findViewById(R.id.button_stop_pince);
        Button button_save_preset = findViewById(R.id.button_save_preset);
        EditText input_name_preset = findViewById(R.id.nomPreset);
        EditText input_force_preset = findViewById(R.id.forcePreset);
        Spinner select_preset = findViewById(R.id.spinner);



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

        // Appel de la méthode afficherPresets
        afficherPresets();

        // Onglets ---------------

        // Récupérez une référence à votre TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);


        // Sélectionnez l'onglet "Preset" par défaut

        TabLayout.Tab tab = tabLayout.getTabAt(0); // 0 pour le premier onglet, 1 pour le deuxième, etc.

        if (tab != null) {
            tab.select();
            button_active_pince.setVisibility(View.VISIBLE);
            button_stop_pince.setVisibility(View.VISIBLE);
            select_preset.setVisibility(View.VISIBLE);
            button_save_preset.setVisibility(View.GONE);
            input_name_preset.setVisibility(View.GONE);
            input_force_preset.setVisibility(View.GONE);
        }

        // Définissez un écouteur de sélection d'onglet
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // L'onglet "Preset" est sélectionné
                if (tab.getPosition() == 0) {
                    button_active_pince.setVisibility(View.VISIBLE);
                    button_stop_pince.setVisibility(View.VISIBLE);
                    select_preset.setVisibility(View.VISIBLE);
                    button_save_preset.setVisibility(View.GONE);
                    input_name_preset.setVisibility(View.GONE);
                    input_force_preset.setVisibility(View.GONE);
                }
                // L'onglet "Personnalisable" est sélectionné
                else if (tab.getPosition() == 1) {
                    button_active_pince.setVisibility(View.GONE);
                    button_stop_pince.setVisibility(View.GONE);
                    select_preset.setVisibility(View.GONE);
                    button_save_preset.setVisibility(View.VISIBLE);
                    input_name_preset.setVisibility(View.VISIBLE);
                    input_force_preset.setVisibility(View.VISIBLE);
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



        // -----------------------------------------

        // Enregistrement des presets
        button_save_preset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idPreset = "1"; // Vous pouvez générer un ID unique ou basé sur le temps
                String nomPreset = input_name_preset.getText().toString();
                String forcePreset = input_force_preset.getText().toString();

                // Vérifiez si les champs sont bien remplis
                if (!nomPreset.isEmpty() && !forcePreset.isEmpty()) {
                    // Enregistrez les données dans un fichier
                    // Si l'enregistrement est réussi, effacez les champs de texte et affichez un message de succès
                    if(savePresetsToFile(nomPreset, forcePreset)) {
                        input_name_preset.setText("");
                        input_force_preset.setText("");
                        Toast.makeText(MainActivity.this, "Données enregistrées", Toast.LENGTH_SHORT).show();
                    }
                    // Sinon, affichez un message d'erreur
                    else {
                        Toast.makeText(MainActivity.this, "Erreur lors de l'enregistrement, la méthode savePresetsToFile à retourné false", Toast.LENGTH_SHORT).show();
                    }
                }
                // Sinon, affichez un message pour demander à l'utilisateur de remplir tous les champs
                else {
                    Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean savePresetsToFile(String nomPreset, String forcePreset) {
        try {
            File file = new File(getExternalFilesDir(null), "presets.csv");

            if (!file.exists()) {
                file.createNewFile();
            }

            // Lire le dernier ID enregistré
            int lastId = getLastId();

            // Incrémenter l'ID pour obtenir le nouvel ID
            int newId = lastId + 1;

            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            // Utiliser le nouvel ID lors de l'enregistrement
            osw.write(newId + "," + nomPreset + "," + forcePreset + "\n");
            osw.flush();
            osw.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Erreur lors de l'enregistrement : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Méthode pour obtenir le dernier ID enregistré dans le fichier presets.csv
    private int getLastId() throws IOException {
        File file = new File(getExternalFilesDir(null), "presets.csv");
        if (!file.exists()) {
            return 0; // Si le fichier n'existe pas, retourne 0 comme dernier ID
        }

        FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line;
        int lastId = 0;

        // Lire chaque ligne du fichier pour obtenir le dernier ID enregistré
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length > 0) {
                int id = Integer.parseInt(parts[0]);
                lastId = Math.max(lastId, id);
            }
        }

        reader.close();
        return lastId;
    }


    // Affichage des presets enregistrés
    void afficherPresets() {

        TextView text_list_preset = findViewById(R.id.text_list_preset);

        try {
            File file = new File(getExternalFilesDir(null), "presets.csv");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;

            // Lire chaque ligne du fichier
            while ((line = reader.readLine()) != null) {

                // Diviser la ligne en utilisant le séparateur ","
                String[] parts = line.split(",");

                // 3 parties (idPreset, nomPreset, forcePreset)
                if (parts.length == 3) {
                    String idPreset = parts[0];
                    String nomPreset = parts[1];
                    String forcePreset = parts[2];

                    // Afficher les données dans votre TextView
                    String presetText = "ID: " + idPreset + ", Nom: " + nomPreset + ", Force: " + forcePreset;
                    text_list_preset.append(presetText + "\n");
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Erreur lors de la lecture du fichier presets.csv", Toast.LENGTH_SHORT).show();
        }
    };


    // Utilisez des requêtes HTTP pour appeler les points d'accès de l'API PHP depuis l'application Android
//    private void makeHttpRequest(String apiUrl) {
//        try {
//            URL url = new URL(apiUrl);
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//
//            try {
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//                StringBuilder stringBuilder = new StringBuilder();
//                String line;
//
//                while ((line = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(line).append("\n");
//                }
//
//                bufferedReader.close();
//                String response = stringBuilder.toString();
//                // Traiter la réponse ici
//
//            } finally {
//                urlConnection.disconnect();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


}
