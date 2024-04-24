package com.pince.app;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    
    private static final int REQUEST_ENABLE_BT = 1; // Constante pour identifier le résultat de la demande d'activation du Bluetooth
    // private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID pour le module Bluetooth protocol SPP utilisé pour le module Bluetooth HC-05
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID pour le module Bluetooth protocol SPP utilisé pour le module Bluetooth HC-05


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // ---- Connexion au module Bluetooth ----

        // Vérifier et activer le Bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Connexion au module Bluetooth
        try {
            connectToBluetoothDevice();
            Toast.makeText(MainActivity.this, "Connexion au module Bluetooth réussie", Toast.LENGTH_SHORT).show();
            System.out.println("Connexion au module Bluetooth réussie");
        } catch (IOException e) {
            // Toast
            Toast.makeText(MainActivity.this, "Erreur lors de la connexion au module Bluetooth : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("Erreur lors de la connexion au module Bluetooth : " + e.getMessage());
        }


        // --------------------------------------


        // Déclaration des variables
        Button button_active_pince = findViewById(R.id.button_active_pince);
        Button button_stop_pince = findViewById(R.id.button_stop_pince);
        Button button_save_preset = findViewById(R.id.button_save_preset);
        Button button_delete_all_preset = findViewById(R.id.button_delete_all_preset);
        EditText input_name_preset = findViewById(R.id.nomPreset);
        EditText input_force_preset = findViewById(R.id.forcePreset);
        Spinner select_preset = findViewById(R.id.spinner);



        // Récupérer le TextView pour la date et l'heure
        TextView dateTimeTextView = findViewById(R.id.textViewDateTime);

        // Obtenir la date et l'heure actuelles
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h'mm, EEEE dd MMMM", new Locale("fr", "FR"));
        String formattedDate = dateFormat.format(currentDate);

        // Définir la date et l'heure dans le TextView
        dateTimeTextView.setText(formattedDate);


        // Appel de la méthode afficherPresets
        // afficherPresets();
        afficherPresets(select_preset);


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
            button_delete_all_preset.setVisibility(View.GONE);
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
                    button_delete_all_preset.setVisibility(View.GONE);
                    input_name_preset.setVisibility(View.GONE);
                    input_force_preset.setVisibility(View.GONE);
                }
                // L'onglet "Personnalisable" est sélectionné
                else if (tab.getPosition() == 1) {
                    button_active_pince.setVisibility(View.GONE);
                    button_stop_pince.setVisibility(View.GONE);
                    select_preset.setVisibility(View.GONE);
                    button_save_preset.setVisibility(View.VISIBLE);
                    button_delete_all_preset.setVisibility(View.VISIBLE);
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


        // Supprimer tous les presets
        button_delete_all_preset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supprimerTousLesPresets();
                afficherPresets(select_preset);
            }
        });

        // Enregistrement des presets
        button_save_preset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomPreset = input_name_preset.getText().toString();
                String forcePreset = input_force_preset.getText().toString();

                // Vérifie si les champs sont bien remplis
                if (!nomPreset.isEmpty() && !forcePreset.isEmpty()) {
                    // Enregistre les données dans un fichier
                    // Si l'enregistrement est réussi, efface les champs de texte et affiche un message de succès
                    if(savePresetsToFile(nomPreset, forcePreset)) {
                        afficherPresets(select_preset);
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


    private void connectToBluetoothDevice() throws IOException {
        System.out.println("Connexion au module Bluetooth.....");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                System.out.println("Nom du périphérique : " + device.getName());
                if (device.getName().equals("HC-05")) {
                    System.out.println("Périphérique trouvé");


                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);

                    try {
                        socket.connect();
                        System.out.println("Connexion réussie au périphérique Bluetooth");
                    } catch (IOException e) {
                        System.out.println("Erreur lors de la connexion au périphérique Bluetooth : " + e.getMessage());
                    }



                    OutputStream outputStream = socket.getOutputStream();
                    try {
                        // outputStream.write("P".getBytes());
                        outputStream.write(79);
                        outputStream.flush();
                        System.out.println("Message envoyé au module Bluetooth");
                    } catch (IOException e) {
                        System.out.println("Erreur lors de l'envoi du message au module Bluetooth : " + e.getMessage());
                    }



                    // test
//                    InputStream inputStream = socket.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                    System.out.println("Envoie donnée");
//
//                    try {
//                        String receivedData;
//                        // Lire les messages jusqu'à ce qu'il n'y en ait plus
//                        while ((receivedData = reader.readLine()) != null) {
//                            // Traitez chaque message reçu
//                            System.out.println("Message reçu du module Bluetooth : " + receivedData);
//                        }
//
//                    } catch (IOException e) {
//                        System.err.println("Erreur lors de la lecture du message : " + e.getMessage());
//
//                    } finally {
//                        // Fermer les ressources
//                        try {
//                            reader.close();  // Fermez le BufferedReader
//                            inputStream.close();  // Fermez le flux d'entrée
//                            socket.close();  // Fermez le socket Bluetooth
//                        } catch (IOException e) {
//                            System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
//                        }
//                    }

                    // fermer le socket et le flux de sortie
                    outputStream.close();
                    socket.close();
                    break;
                }
            }
        }
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

            // Écrire les données dans le fichier
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



    private void supprimerTousLesPresets() {
        File file = new File(getExternalFilesDir(null), "presets.csv");
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(MainActivity.this, "Tous les presets ont été supprimés", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Erreur lors de la suppression des presets", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Aucun preset à supprimer", Toast.LENGTH_SHORT).show();
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


    // Affichage des presets enregistrés dans le Spinner sur l'onglet "Preset"
    void afficherPresets(Spinner spinner) {
        ArrayList<String> presetsList = new ArrayList<>(); // Pour stocker les données des presets

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
                    String nomPreset = parts[1];
                    String forcePreset = parts[2];

                    // Créer la chaîne à afficher dans le Spinner
                    String presetString = nomPreset + " - " + forcePreset + " Newton";

                    // Ajouter la chaîne à la liste des presets
                    presetsList.add(presetString);
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Erreur lors de la lecture du fichier presets.csv", Toast.LENGTH_SHORT).show();
        }

        // Si la liste des presets est vide, ajoutez un message indiquant qu'il n'y a pas de presets disponibles
        if (presetsList.isEmpty()) {
            presetsList.add("Aucun preset disponible");
        }

        // Créer un ArrayAdapter pour les données des presets
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, presetsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Définir l'adaptateur pour le Spinner passé en argument
        spinner.setAdapter(adapter);
    }


}
