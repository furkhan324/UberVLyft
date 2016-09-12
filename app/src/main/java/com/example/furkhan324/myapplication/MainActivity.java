package com.example.furkhan324.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String __LyftAccessResponse;
    String accessTokenForLyft;
    Context c;
    Geocoder gc;
    EditText tv1;
    EditText tv2;
    String start_latitude;
    String start_longitude;
    String end_latitude;
    String end_longitude;
    MainActivity ma;
    Spinner dynamicLyftSpinner;
    Spinner dynamicUberSpinner;
    HashMap<String, Integer> uberDurations = new LinkedHashMap<String, Integer>();
    HashMap<String, String> uberEstimates = new LinkedHashMap<String, String>();
    HashMap<String, Integer> lyftDurations = new LinkedHashMap<String, Integer>();
    HashMap<String, Integer> lyftEstimates = new LinkedHashMap<String, Integer>();
    String[] uberTypes = new String[]{};
    String[] lyftTypes = new String[]{};
    TextView tvPrice1;
    TextView tvPrice2;
    TextView tvPrice3;
    TextView tvPrice4;



    public void UISetup(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv1 = (EditText) findViewById(R.id.editText);
        tv2 = (EditText) findViewById(R.id.editText2);
        Typeface bariol= Typeface.createFromAsset(getAssets(), "fonts/bariol.otf");
        tv1.setTypeface(bariol);
        tv2.setTypeface(bariol);
        setSupportActionBar(toolbar);
        this.c = this;
        this.ma = this;
        this.gc = new Geocoder(this);
        tvPrice1 = (TextView) findViewById(R.id.textView);
        tvPrice2 = (TextView) findViewById(R.id.textView4);
        tvPrice3 = (TextView) findViewById(R.id.textView5);
        tvPrice4 = (TextView) findViewById(R.id.textView6);

        dynamicLyftSpinner = (Spinner) findViewById(R.id.spinner);
        String[] items = new String[] {};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        dynamicLyftSpinner.setAdapter(adapter);
        dynamicLyftSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                tvPrice1.setText("$"+lyftEstimates.get(lyftTypes[position])+"");
                tvPrice2.setText("~"+lyftDurations.get(lyftTypes[position])+" mins");
            }
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        dynamicUberSpinner = (Spinner) findViewById(R.id.spinner2);
        String[] items2 = new String[] {};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items2);
        dynamicUberSpinner.setAdapter(adapter2);
        dynamicUberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                tvPrice3.setText(uberEstimates.get(uberTypes[position])+"");
                tvPrice4.setText("~"+uberDurations.get(uberTypes[position])+" mins");            }

            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UISetup();

        Button refreshButton = (Button) findViewById(R.id.button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                geocodeAddresses();
                API api = new API(c,start_latitude, start_longitude,end_latitude,end_longitude,ma);
                api.performAPICalls();
            }
        });



    }

    public void geocodeAddresses() {
        //Function uses in-built geocoder to convert string address to latitudes and longtitudes
        String t1 = tv1.getText().toString();
        String t2 = tv2.getText().toString();
        if (gc.isPresent()) {
            try {
                Log.v("gc is present", "gc is present");
                List<Address> list = new ArrayList<Address>();

                list.add(gc.getFromLocationName(t1, 1).get(0));
                list.add(gc.getFromLocationName(t2, 1).get(0));

                tv1.setText(list.get(0).getAddressLine(0) + " " + list.get(0).getAddressLine(1));
                tv2.setText(list.get(1).getAddressLine(0)+" "+list.get(1).getAddressLine(1));

                start_latitude = list.get(0).getLatitude() + "";
                start_longitude = list.get(0).getLongitude() + "";
                end_latitude = list.get(1).getLatitude() + "";
                end_longitude = list.get(1).getLongitude() + "";


            } catch (Exception e) {
                Toast.makeText(this, "Address not found. Please try again.",
                        Toast.LENGTH_LONG).show();            }


        }else {
            Toast.makeText(this, "Geocoder not found. Please Try Again. ",
                    Toast.LENGTH_LONG).show();

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
