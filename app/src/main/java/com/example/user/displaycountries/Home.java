package com.example.user.displaycountries;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.user.displaycountries.Model.Country;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;

public class Home extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    public FirebaseUser currentUser;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList<Country> listOfCountries ;
    private Adaptor adaptor;
    private ProgressDialog progressDialog;
    private RequestQueue queue;
    private SharedPreferences sharedPreferences;

    public static final String URL ="http://api.geonames.org/countryInfoJSON?username=maryaiad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        recyclerView = (RecyclerView) findViewById(R.id.show_countries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listOfCountries = new ArrayList<>();

        getCountriesList();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCountriesList();
                Toast.makeText(getApplicationContext(), "Countries Refreshed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //this function get All Countries
    private void getCountriesList() {
        progressDialog.show();

        queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("geonames");
                            for(int i =0; i<jsonArray.length(); i++){
                                Country country = new Country();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                country.setCountryName(jsonObject.getString("countryName"));
                                listOfCountries.add(country);
                            }
                            adaptor = new Adaptor(Home.this, listOfCountries);
                            recyclerView.setAdapter(adaptor);
                            if(swipeRefreshLayout.isRefreshing()){
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if(swipeRefreshLayout.isRefreshing()){
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            progressDialog.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error ", Toast.LENGTH_SHORT).show();
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
                progressDialog.dismiss();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    //Menu to do logout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //logout with dialog to confirm logout or not
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.logout){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Log out Confirm")
                    .setMessage("Are you sure you need to log out ?!")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseAuth.getInstance().signOut();
                            sharedPreferences = getApplicationContext().
                                    getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.commit();

                            Toast.makeText(getApplicationContext(), "LogOut ", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Home.this, Login.class));
                            finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}