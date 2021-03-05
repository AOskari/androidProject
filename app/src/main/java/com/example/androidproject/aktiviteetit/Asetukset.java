package com.example.androidproject.aktiviteetit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View.OnClickListener;

import com.example.androidproject.Paino;
import com.example.androidproject.R;
import com.example.androidproject.Trendi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Asetukset extends AppCompatActivity {
    private TextView yksikko1, yksikko2, paino, pituus, tav1, tav2;
    private Spinner tavoite1, tavoite2;
    private Button tallenna;
    private EditText nimi;
    private SharedPreferences asetukset;
    private SharedPreferences trendit;
    private SharedPreferences.Editor tiedot;
    private SharedPreferences.Editor tallListat;
    private Calendar kalenteri;
    private List<Paino> paTrendi;
    private Trendi trendi;
    private Gson gson = new Gson();

    private String trendiJson, kuka;
    private float kg, cm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asetukset);

        tavoite1 = findViewById(R.id.tavoite1);
        tavoite2 = findViewById(R.id.tavoite2);
        tallenna = findViewById(R.id.tallennus);
        nimi = findViewById(R.id.annaNimi);
        paino = findViewById(R.id.paino);
        pituus = findViewById(R.id.pituus);
        tav1 = findViewById(R.id.tav1);
        tav2 = findViewById(R.id.tav2);
        yksikko1 = findViewById(R.id.yksikko1);
        yksikko2 = findViewById(R.id.yksikko2);
        kalenteri = Calendar.getInstance();
        asetukset = getSharedPreferences("Tiedot", Activity.MODE_PRIVATE);
        trendit = getSharedPreferences("Trendit", Activity.MODE_PRIVATE);
        tallListat = trendit.edit();

        trendiJson = trendit.getString("Trendi", "");
        trendi = gson.fromJson(trendiJson, Trendi.class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        listaHae();

        // Asetetaan alapalkille kuuntelija, joka vaihtaa aktiviteettia nappien perusteella.
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(alaPalkkiMethod);
        bottomNavigationView.getMenu().findItem(R.id.profiili).setChecked(true);


        String[] valinta = getResources().getStringArray(R.array.valinta);

        tallenna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //   paTrendi.add(new Paino(Float.parseFloat(paino.getText().toString())));
                trendi.addPaino(new Paino(Float.parseFloat(paino.getText().toString())));
                tallennaTrendi();
                listaTall();
                tallenna();
            }
        });

        ArrayAdapter<String> val = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valinta);
        val.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tavoite1.setAdapter(val);
        tavoite2.setAdapter(val);

        tavoite1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nimi1 = tavoite1.getSelectedItem().toString();

                if (nimi1.equals("Valinta")) {
                    tav1.setText("");
                    yksikko1.setText("");
                } else if (nimi1.equals("Kalorit")) {
                    tav1.setText("");
                    yksikko1.setText("kcal/vrk");
                } else if (nimi1.equals("Proteiini")){
                    tav1.setText("");
                    yksikko1.setText("g/kg/vrk");
                } else {
                    tav1.setText("");
                    yksikko2.setText("g/vrk");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        tavoite2.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nimi2 = tavoite2.getSelectedItem().toString();

                if (nimi2.equals("Valinta")) {
                    tav2.setText("");
                    yksikko2.setText("");
                } else if (nimi2.equals("Kalorit")) {
                    tav2.setText("");
                    yksikko2.setText("kcal/vrk");
                } else if (nimi2.equals("Proteiini")){
                    tav2.setText("");
                    yksikko2.setText("g/kg/vrk");
                } else {
                    tav2.setText("");
                    yksikko2.setText("g/vrk");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        asetukset = getSharedPreferences("Tiedot", Activity.MODE_PRIVATE);
        kuka = asetukset.getString("Käyttäjä", "");
        Log.d("Testi", kuka);
        nimi.setText(kuka);
        kg = asetukset.getFloat("Paino", 0.0f);
        Log.d("Testi", Float.toString(kg));
        paino.setText(String.valueOf(kg));
        cm = asetukset.getFloat("Pituus", 0.0f);
        Log.d("Testi", Float.toString(cm));
        pituus.setText(String.valueOf(cm));
    }
    public void listaTall(){
        trendit = getSharedPreferences("Trendit", Activity.MODE_PRIVATE);
        String json = gson.toJson(paTrendi);
        tallListat.putString("Paino", json);
        tallListat.commit();
        Log.d("Listan koko ", String.valueOf(paTrendi.size()));
        for (int i=0; i<paTrendi.size(); i++){
            Log.d("Lista "+i, String.valueOf(paTrendi.get(i)));
        }
    }
    public void listaHae(){
    //    trendit = getSharedPreferences("Trendit", MODE_PRIVATE);
      //  Gson gson = new Gson();
      //  String json = trendit.getString("Paino", null);
       // Type type = new TypeToken<ArrayList<Paino>>() {}.getType();
        paTrendi = trendi.getPaino();

    /*    if (paTrendi == null){
            paTrendi = new ArrayList<>();
        } */
    }
    private String haePaiva() {
        String paiva, kuukausi, vuosi;
        paiva = String.valueOf(kalenteri.get(Calendar.DAY_OF_MONTH));
        kuukausi = String.valueOf(kalenteri.get(Calendar.MONTH)+1);
        vuosi = String.valueOf(kalenteri.get(Calendar.YEAR));
        return paiva + "/" + kuukausi + "/" + vuosi;
    }

    public void tallenna() {
        float tyhja = 0.0f;
        String kayttaja, pvm;
        float annaPaino;
        float annaPituus;
        float maara1;
        float maara2;
        pvm = haePaiva();

        tiedot = asetukset.edit();
        if (nimi.getText().toString().length() > 0) {
            kayttaja = nimi.getText().toString();
        } else {
            kayttaja = "";
        }
        if (paino.getText().length() > 0) {
            annaPaino = Float.parseFloat(paino.getText().toString());
        } else {
            annaPaino = tyhja;
        }
        if (pituus.getText().length() > 0){
            annaPituus = Float.parseFloat(pituus.getText().toString());
        } else {
            annaPituus = tyhja;
        }
        if (tav1.getText().length() > 0){
            maara1 = Float.parseFloat(tav1.getText().toString());
        } else {
            maara1 = tyhja;
        }
        if (tav2.getText().length() > 0){
            maara2 = Float.parseFloat(tav2.getText().toString());
        } else {
            maara2 = tyhja;
        }

        String naytaT1 = tavoite1.getSelectedItem().toString() + " " + maara1 + " " + yksikko1.getText();
        String naytaT2 = tavoite2.getSelectedItem().toString() + " " + maara2 + " " + yksikko2.getText();

        tiedot.putFloat("Tavoitemäärä1", maara1);
        tiedot.putFloat("Tavoitemäärä2", maara2);
        tiedot.putString("Käyttäjä", kayttaja);
        tiedot.putFloat("Paino", annaPaino);
        tiedot.putFloat("Pituus", annaPituus);
        if (maara1 != 0.0f) {
            tiedot.putString("Tavoite1", naytaT1);
        }
        if (maara2 != 0.0f) {
            tiedot.putString("Tavoite2", naytaT2);
        }
        tiedot.putString("Päiväys", pvm);
        tiedot.commit();
    }

    private void tallennaTrendi() {
        trendiJson = gson.toJson(trendi);
        tallListat.putString("Trendi", trendiJson);
        tallListat.commit();
    }


    /**
     * Alapalkin toiminnallisuus, aloittaa valitun aktiviteetin.
     * Tutoriaali alapalkin luomiseen: https://www.youtube.com/watch?v=fODp1hZxfng
     */
      private BottomNavigationView.OnNavigationItemSelectedListener alaPalkkiMethod = new
                BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.koti:
                                startActivity(new Intent(Asetukset.this, MainActivity.class));
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                Log.d("Menu", "Koti painettu");
                                finish();
                                break;
                            case R.id.suunnittele:
                                startActivity(new Intent(Asetukset.this, AteriatActivity.class));
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                Log.d("Menu", "Suunnittele painettu");
                                finish();
                                break;
                            case R.id.profiili:
                                startActivity(new Intent(Asetukset.this, Profiili.class));
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                Log.d("Menu", "Profiili painettu");
                                finish();
                                break;
                        }
                        return false;
                    }
                };
}