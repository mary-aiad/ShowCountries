package com.example.user.displaycountries;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.displaycountries.Model.Country;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

class Adaptor extends RecyclerView.Adapter<Adaptor.ViewHolder> {

    private Context context;
    ArrayList<Country> countries = new ArrayList<>();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    public DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


    public Adaptor(Context context, ArrayList <Country> leadArrayList){
        this.context = context;
        this.countries = leadArrayList;
    }

    public Adaptor(Context context){
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recycler_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final Adaptor.ViewHolder viewHolder, final int position) {
        final Country country = countries.get(position);
        viewHolder.countryName.setText(country.getCountryName());
    }

    @Override
    public int getItemCount() {
        if(countries == null){
            System.err.println("error");
            return 0;
        }
        else
            return countries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView countryName;

        public ViewHolder(View itemView) {
            super(itemView);
            countryName = (TextView) itemView.findViewById(R.id.countryName);
        }
    }
}
