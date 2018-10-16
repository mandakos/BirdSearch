package com.example.manda.birdsearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private ProgressDialog pDialog;
    ListView mListView;
    TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mListView = (ListView) findViewById(R.id.list);
        mEmptyView = (TextView) findViewById(R.id.emptyView);

        try {
            // Luodaan url
            URL url = new URL("http://atlas3.lintuatlas.fi/0/speciesDescriptions.txt");

            // Aloitetaan Asynctask jossa luetaan tiedostoa Url osoitteesta
            new ReadFileTask().execute(url);
        }
        catch (MalformedURLException e) {
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 1.
                Toast.makeText(MainActivity.this, adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT).show();
            }
        });

        mListView.setEmptyView(mEmptyView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Yläpalkin hakukentän määrittelyjä
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);

        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.getFilter().filter(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    // AsyncTask datan hakemiseen url osoitteesta
    private class ReadFileTask extends AsyncTask<URL, Void, ArrayList<String>> {

        // tämän määrittelyn tarvi Logien kirjottamiseen
        private final String TAG = MainActivity.class.getName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Näytetään progressdialog latausnäkymä
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(URL... urls) {

            // Luodaan lista lintulajien nimille
            ArrayList<String> speciesList = new ArrayList<>();

            try {
                // parametrinä tuotu url osoite
                URL url = urls[0];
                Log.i(TAG,"Url: " + url);

                //make a request to server
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //get InputStream instance
                InputStream is = con.getInputStream();
                //create BufferedReader object
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                (is), "UTF-16"));

                // Luetaan rivi tiedostosta
                String line = br.readLine();

                while (line != null) // luetaan tiedostoa niin kauan kunnes rivit loppuu
                {
                    Log.i(TAG,line);

                    // Jaetaan luettu rivi "kolumneihin" jotka on erotettu tabulaattorilla
                    String[] columns = line.split("\t");
                    Log.i(TAG,"Column " + columns[0]);
                    speciesList.add(columns[0]);

                    // Luetaan uusi rivi
                    line = br.readLine();
                }

                // Poistetaan luodun listan ensimmäinen elementti joka ei ole lajinimi ("post_title")
                speciesList.remove(0);

                br.close(); // suljetaan BufferReader

            } catch (Exception e) {
                e.printStackTrace();
                //close dialog if error occurs
            }

            return speciesList;
        }


        @Override
        protected void onPostExecute(ArrayList<String> result)
        {
            //super.onPostExecute(result);

            // Lisätään haettujen lintulajien lista UI:n listanäkymään
            final ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, result);
            mListView.setAdapter(arrayAdapter);

            // Suljetaan progressDialog latausnäkymä
            if (pDialog.isShowing()) { pDialog.dismiss(); }
        }
    }
}