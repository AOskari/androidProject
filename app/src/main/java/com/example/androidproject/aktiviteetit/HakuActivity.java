package com.example.androidproject.aktiviteetit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.androidproject.Ateria;
import com.example.androidproject.Elintarvike;
import com.example.androidproject.R;
import com.example.androidproject.HakuAdapter;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.lang.*;

public class HakuActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private List<Elintarvike> foodInfo;

    EditText input;
    ListView lv;
    TextView ilmoitus;

    Gson gson = new Gson();
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Ateria ateria;
    String ateriaJson;

    ProgressBar latausKuvake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haku);

        // Asetetaan takaisin nappi yläpalkkiin
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        foodInfo = new ArrayList<>();
        latausKuvake = findViewById(R.id.latauskuvake);
        latausKuvake.setVisibility(View.GONE);
        input = findViewById(R.id.elintarvike_haku);
        lv = findViewById(R.id.list_view);
        ilmoitus = findViewById(R.id.haku_ilmoitus);

        ilmoitus.setVisibility(View.GONE);

        // Haetaan yhteinen SharedPreferences-olio, jonka avulla talletetaan lisätyt Elintarvikkeet aterioihin.
        pref = getApplicationContext().getSharedPreferences("mainPref",0);
        editor = pref.edit();

        ateriaJson = pref.getString("ateria", "");
        ateria = gson.fromJson(ateriaJson, Ateria.class);
    }

    public void getInfo(View v) {
        latausKuvake.setVisibility(v.VISIBLE);
        String url = "https://fineli.fi/fineli/api/v1/foods?q=" + input.getText().toString();
        Log.i("getInfo","getInfo called");
        Log.i("itemname",input.getText().toString());
        Log.i("url", url);

        requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("response info", "onResponse called");
                        foodInfo.clear();
                        Log.d("current info", foodInfo.toString());
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                JSONObject names = obj.getJSONObject("name");
                                String name = names.getString("fi");

                                Log.d("looping", obj.toString() + " " + i);
                                Elintarvike elintarvike = new Elintarvike(name, obj.getDouble("salt") / 1000, obj.getDouble("energyKcal"), obj.getDouble("fat"),
                                        obj.getDouble("protein"), obj.getDouble("carbohydrate"),
                                        obj.getDouble("organicAcids"), obj.getDouble("saturatedFat"), obj.getDouble("sugar"),
                                        obj.getDouble("fiber"), 100.0
                                );

                                foodInfo.add(elintarvike);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        latausKuvake.setVisibility(View.GONE);
                        initList();
                        Log.d("Iterated", foodInfo.toString());
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("error", "error called: " + error.toString());

                    }
                });

        // Tehdään API-kutsu ainoastaan jos hakukentässä on tekstiä.
        if (input.getText().toString().length() > 0) {
            requestQueue.add(jsonArrayRequest);
            Log.d("checking info", foodInfo.toString());
        }
    }

    public void suljeNappaimisto() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Näyttää ilmoituksen ja poistaa ilmoitukset näkyvistä 5 sekunnin kuluttua.
     * @param nimi
     */
    public void naytaIlmoitus(String nimi) {
        ilmoitus.setText(nimi + " lisätty ateriaan.");
        ilmoitus.setVisibility(View.VISIBLE);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.setDuration(5000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                ilmoitus.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        AnimationSet animaatiot = new AnimationSet(false);
        animaatiot.addAnimation(fadeIn);
        animaatiot.addAnimation(fadeOut);
        ilmoitus.setAnimation(animaatiot);
    }

    private void initList() {
        // Päivitetään lista asettamalla RuokaAdapter ListViewille.
        HakuAdapter adapter = new HakuAdapter(this, foodInfo, pref, this);
        lv.setAdapter(adapter);

        // Lopuksi piilotetaan virtuaalinen näppäimistö
        suljeNappaimisto();
    }


}