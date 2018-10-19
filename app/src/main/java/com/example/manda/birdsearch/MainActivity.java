package com.example.manda.birdsearch;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private ProgressDialog pDialog;
    ListView mListView;
    TextView mEmptyView;
    CustomAdapter adapter;

    // tämän määrittelyn tarvi Logien kirjottamiseen
    private final String TAG = MainActivity.class.getName();

    // Luodaan lista lintulajien nimille
    ArrayList<Bird> speciesList = new ArrayList<>();

    Bird bird; // olio
    private static int REQUEST_CODE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mListView = (ListView) findViewById(R.id.list);
        mEmptyView = (TextView) findViewById(R.id.emptyView);

        try {
            // Luodaan url
            URL url = new URL("http://atlas3.lintuatlas.fi/0/speciesDescriptions.txt");
            //String urlZip = "http://atlas3.lintuatlas.fi/0/lintuatlas12.zip";

            // Aloitetaan Asynctask jossa luetaan tiedostoa Url osoitteesta
            new ReadFileTask().execute(url);
            //new DownloadZip().execute(urlZip);
        }
        catch (MalformedURLException e) {
        }

        //

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Bird thisBird = adapter.getItem(i);

                // Tässä avataan uusi Activity jossa näytetään lajikuvaus
                Intent intent = new Intent(MainActivity.this, BirdView.class);
                intent.putExtra("BIRD", thisBird);
                startActivity(intent);
                Toast.makeText(MainActivity.this, thisBird.getName_latin(), Toast.LENGTH_SHORT).show();
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
                adapter.getFilter().filter(newText);
                //Log.i(TAG,"ADAPTER : " + newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public void addBird(ArrayList names, ArrayList descriptions) {

        Log.i(TAG, "ADDBIRD");

        String name_latin = "";
        String description = "";
        String author = "";
        String name_finnish = "";

        for (int i = 0; i < descriptions.size(); i++) {
            //Log.i(TAG, "DESCRIPTIONS: " + names.get(i).toString());

            String line = descriptions.get(i).toString();
            String getArr[] = line.split(",");
            //Log.i(TAG,"getarr: " + getArr[0]);


            for (int j = 0; j < names.size(); j++) {
                //Log.i(TAG, "NAMES: " +names.get(j).toString());

                String line2 = names.get(j).toString();
                String getArr2[] = line2.split(",");
                //Log.i(TAG,"getarr2: " + getArr2[0]);


                if((getArr[0].toString()).equalsIgnoreCase(getArr2[0].toString())){
                    //Log.i(TAG,"COMPARING: " + getArr[0] + " = " + getArr2[0]);

                    // jos luettujenn rivien latinankieliset nimet täsmää,
                    // tee uusi olio Bird
                    if(getArr != null && getArr2 != null){
                        name_latin = getArr2[0].toString();
                        Log.i(TAG, "NAME latin: " + name_latin);

                        description = getArr[1].toString();
                        Log.i(TAG, "description: " + description);

                        author = getArr[2].toString();
                        Log.i(TAG, "NAME author: " + author);

                        name_finnish = getArr2[1].toString();
                        Log.i(TAG, "NAME FINNISH: " + name_finnish);
                        bird = new Bird(name_latin, description, author, name_finnish);
                        speciesList.add(bird);
                        //Log.i(TAG, bird.getName_finnish().toString());
                    }
                }
            }
        }

        // Lisätään haettujen lintulajien lista UI:n listanäkymään
        adapter = new CustomAdapter(MainActivity.this, speciesList);
            /*final ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, result);*/
        mListView.setAdapter(adapter);
    }

    // AsyncTask datan hakemiseen url osoitteesta
    public class ReadFileTask extends AsyncTask<URL, Void, ArrayList> {

        private DownloadManager downloadManager;
        static final String zipURL = "http://atlas3.lintuatlas.fi/0/lintuatlas12.zip";
        static final String zip_name = "lintuatlas12.zip";
        final String zip_w_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + zip_name;
        static final String wanted_file = "lajit.csv";

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
        protected ArrayList doInBackground(URL... urls) {

            // Luodaan lista lintulajien nimille
            //ArrayList<Bird> speciesList = new ArrayList<>();
            ArrayList<String> txtList = new ArrayList<>();
            /*String latinName = "";
            String description = "";
            String author = "";*/
            String descriptions = "";

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
                    //Log.i(TAG,line);

                    // Jaetaan luettu rivi "kolumneihin" jotka on erotettu tabulaattorilla
                    String[] columns = line.split("\t");
                    /*latinName = columns[0]; // latinankielinen nimi
                    description = columns[1]; // lajikuvaus
                    author = columns[2]; // author*/
                    descriptions = columns[0] +","+ columns[1] +","+ columns[2];
                    //Log.i(TAG,"Column " + columns[0]);

                    txtList.add(descriptions);


                    // Luetaan uusi rivi
                    line = br.readLine();
                }

                // Poistetaan luodun listan ensimmäinen elementti joka ei ole lajinimi ("post_title")
                txtList.remove(0);

                br.close(); // suljetaan BufferReader


            } catch (Exception e) {
                e.printStackTrace();
                //close dialog if error occurs
            }

            return txtList;
        }

        // oma funktio zippitiedoston hakemiselle ja purkamiselle
        // Zip tiedostosta haetaan lajinimi suomeksi ja latinaks
        protected ArrayList getZip(String zipUri){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(zipUri);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, zip_name);
            Long reference = downloadManager.enqueue(request);
            //ZipFile my_zip = new ZipFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + zip_name);
            InputStream is;
            ZipInputStream zis;
            byte[] buffer;
            String str ="";
            ArrayList<String> csv_list = new ArrayList<>();

            String addNames = "";

            try {
                String filename;
                is = new FileInputStream(zip_w_path);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry mZipEntry;

                while ((mZipEntry = zis.getNextEntry()) != null) {
                    filename = mZipEntry.getName();
                    if (filename.equals(wanted_file))
                    {
                        BufferedReader r = new BufferedReader(new InputStreamReader(zis, "Windows-1250"));
                        //StringBuilder total = new StringBuilder();
                        String line;
                        String names[] = new String[3];

                        while ((line = r.readLine()) != null) {
                            String[] row = line.split(",");
                            names[0] = row[1]; // latinankielinen nimi
                            names[1] = row[2]; // suomenkileinen nimi
                            addNames = names[0] +","+ names[1];
                            csv_list.add(addNames);

                        }
                        break;
                    }
                }
                zis.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return csv_list;
        }


        @Override
        protected void onPostExecute(ArrayList result)
        {
            super.onPostExecute(result);

            // Lisätään haettujen lintulajien lista UI:n listanäkymään
            //adapter = new CustomAdapter(MainActivity.this, result);
            /*final ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, result);*/
            //mListView.setAdapter(adapter);


            ArrayList<String> names = getZip(zipURL);
            addBird(names, result);

            // Suljetaan progressDialog latausnäkymä
            if (pDialog.isShowing()) { pDialog.dismiss(); }
        }
    }

}