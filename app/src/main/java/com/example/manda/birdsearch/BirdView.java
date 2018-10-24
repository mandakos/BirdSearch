package com.example.manda.birdsearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class BirdView extends AppCompatActivity {

    TextView species, latin, description, author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bird_view);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String name = extras.getString("BirdName");
        String desc = extras.getString("BirdDesc");
        String lat = extras.getString("BirdLatin");
        String auth = extras.getString("BirdAuthor");


        species = (TextView) findViewById(R.id.tvLaji);
        description = (TextView) findViewById(R.id.tvKuvaus);
        description.setMovementMethod(new ScrollingMovementMethod()); // kuvauksesta scrollattava
        latin = (TextView) findViewById(R.id.tvLatin);
        author = (TextView) findViewById(R.id.tvAuthor);

        species.setText(name);
        description.setText(desc);
        latin.setText(lat);
        author.setText(auth);
        //author.setText("http://atlas3.lintuatlas.fi/0/speciesDescriptions.txt / " + bird.getAuthor());

    }
}
