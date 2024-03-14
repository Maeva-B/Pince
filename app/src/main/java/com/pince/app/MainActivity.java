package com.pince.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.widget.Toast;

// écriture et lecture de fichier
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // listener pour le bouton "Activer la pince"
        button_active_pince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // A changer à mettre au lancement de l'app
                String reponse = connectBluetooth();
                Toast.makeText(MainActivity.this, reponse, Toast.LENGTH_SHORT).show();

                // envoyerDonneesPince(v);
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
                    if (savePresetsToFile(nomPreset, forcePreset)) {
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

    private String connectBluetooth() {

        // ----------------- Partie bluetooth -----------------
        // basée sur la création d'un socket Bluetooth SPP (Serial Port Profile) pour envoyer des données

        // Vérifier si le périphérique prend en charge le Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            return "Bluetooth non pris en charge sur cet appareil";
        }

        // Message d'erreur si le Bluetooth n'est pas activé
        if (!bluetoothAdapter.isEnabled()) {
            return "Veuillez activer le Bluetooth";
        }

        // Obtenir une référence à l'appareil Bluetooth auquel on souhaite se connecter
        // Adresse mac du module bluetooth : String deviceAddress = "60:8A:10:6A:B1:80"; /
        //  =test : "6C:EC:EB:22:5C:BD"
        // Adresse mac du iphone maeva : String deviceAddress = "44:DA:30:8C:A7:07";
        // Adresse mac du téléphone benji : String deviceAddress = "E4:12:1D:3B:CF:AB";

        // String deviceAddress = "54:08:3B:C1:C0:4C";

        String deviceAddress = "6C:EC:EB:22:5C:BD";

        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        // tester si le device est null
        if (bluetoothDevice == null) {
            return "Erreur lors de la connexion Bluetooth, le périphérique est null";
        }

        // Connecter le socket Bluetooth
        try {

//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // Demander la permission de se connecter au périphérique Bluetooth
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.BLUETOOTH_CONNECT)) {
//                   ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
//                } else {
//                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
//                }
//                return "Erreur de connexion Bluetooth UUID 1";
//            }

            UUID uuid = UUID.fromString("0000aaa1-0000-1000-8000-aabbccddeeff"); // UUID
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }

            // bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            // bluetoothSocket.connect();


            bluetoothDevice.connectGatt(this, false, gattCallBack);
            // outputStream = bluetoothSocket.getOutputStream();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "Bluetooth connecté";
    }

    private BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            System.out.println("onConnectionStateChange");
            //super.onConnectionStateChange(gatt, status, newState);
            gatt.discoverServices();

            if(newState == BluetoothGatt.STATE_CONNECTED){
                System.out.println("Connected to GATT server.");
            } else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                System.out.println("Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("onServicesDiscovered");
            gatt.readCharacteristic(gatt.getServices("0000aaa1-0000-1000-8000-aabbccddeeff").get(0).getCharacteristics().get(0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // System.out.println("onCharacteristicRead");
            //super.onCharacteristicRead(gatt, characteristic, status);

            String value = characteristic.getValue().toString();
            System.out.println("onCharacteristicRead value : " + value);
        }
    };



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

    // Méthode pour envoyer les données du preset sélectionné à la pince, via bluetooth
    public void envoyerDonneesPince(View view) {

        // Récupérer le Spinner
        Spinner select_preset = findViewById(R.id.spinner);

        // Récupérer la valeur sélectionnée dans le Spinner
        String selectedPreset = select_preset.getSelectedItem().toString();

        // Diviser la chaîne en utilisant le séparateur " - "
        String[] parts = selectedPreset.split(" - ");

        // 2 parties (nomPreset, forcePreset)
        if (parts.length == 2) {
            String nomPreset = parts[0];
            String forcePreset = parts[1].replace(" Newton", "");

            // Afficher un message pour vérifier si les données sont correctes
            Toast.makeText(MainActivity.this, "Nom du preset : " + nomPreset + ", Force du preset : " + forcePreset, Toast.LENGTH_SHORT).show();

            // Appeler la méthode pour envoyer les données via Bluetooth
            String donnees = "nomPreset=" + nomPreset + "&forcePreset=" + forcePreset;
            envoyerDonneesBluetooth(donnees);
        } else {
            Toast.makeText(MainActivity.this, "Erreur lors de la récupération des données du preset", Toast.LENGTH_SHORT).show();
        }
    }


    // Méthode pour envoyer une chaîne de caractères via Bluetooth
    private void envoyerDonneesBluetooth(String donnees) {
        if (outputStream != null) {
            try {
                outputStream.write(donnees.getBytes());
                Toast.makeText(this, "Données envoyées via Bluetooth: " + donnees, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors de l'envoi des données via Bluetooth", Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(this, "OutputStream est null, vérifiez la connexion Bluetooth. Données envoyées : " + donnees, Toast.LENGTH_SHORT).show();
        }
    }
}