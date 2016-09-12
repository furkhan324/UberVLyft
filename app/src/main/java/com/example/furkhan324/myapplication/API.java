package com.example.furkhan324.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class API {

    String __LyftAccessResponse;
    String accessTokenForLyft;
    Context c;
    String start_latitude;
    String start_longitude;
    String end_latitude;
    String end_longitude;
    HashMap<String, Integer> uberDurations = new LinkedHashMap<String, Integer>();
    HashMap<String, String> uberEstimates = new LinkedHashMap<String, String>();
    HashMap<String, Integer> lyftDurations = new LinkedHashMap<String, Integer>();
    HashMap<String, Integer> lyftEstimates = new LinkedHashMap<String, Integer>();
    MainActivity ma;
    ProgressDialog pDialog2;


    public API(Context c, String start_lat, String start_long, String end_lat, String end_long, MainActivity ma){

        this.c = c;
        this.start_latitude = start_lat;
        this.start_longitude = start_long;
        this.end_latitude = end_lat;
        this.end_longitude = end_long;
        this.ma = ma;
    }


    public void performAPICalls(){
        doPostForLyft();
        //callback in doPostLyft calls doGetForLyft to make sure bearerToken is available to the function
        doGetForUber();
    }

    public void parseStoreLyftResponse(JSONObject response){
        //method used to parse and store JSON response from the Lyft API
        try {
            JSONArray array = response.getJSONArray("cost_estimates");
            for(int i = 0 ; i < array.length() ; i++){
                String dName = array.getJSONObject(i).getString("display_name");
                lyftDurations.put(dName, Math.round(array.getJSONObject(i).getInt("estimated_duration_seconds") / 60));
                lyftEstimates.put(dName, Math.round((array.getJSONObject(i).getInt("estimated_cost_cents_max") + array.getJSONObject(i).getInt("estimated_cost_cents_min")) / 200) );

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void parseStoreUberResponse(JSONObject response){
        //parse and store JSON response from the Uber api
        try {
            JSONArray array = response.getJSONArray("prices");
            for(int i = 0 ; i < array.length() ; i++){
                String dName = array.getJSONObject(i).getString("display_name");
                uberDurations.put(dName, Math.round(array.getJSONObject(i).getInt("duration") / 60));
                uberEstimates.put(dName, array.getJSONObject(i).getString("estimate"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void doGetForLyft(final String bearerToken){
        //function called from doPostLyft function
        RequestQueue queue = Volley.newRequestQueue(c);
        final String url = "https://api.lyft.com/v1/cost?start_lat="+start_latitude+"&start_lng="+start_longitude+"&end_lat="+end_latitude+"&end_lng="+end_longitude;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseStoreLyftResponse(response);
                        Log.d("Response", response.toString());
                        if (!uberDurations.isEmpty()){
                            doUIChanges();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ){
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "bearer "+ bearerToken);
                return headers;
            }
        };

        // add it to the RequestQueue
        queue.add(getRequest);

    }

    public void doGetForUber(){
        //Get request for uber api
        RequestQueue queue = Volley.newRequestQueue(c);
        final String url = "https://api.uber.com/v1/estimates/price?start_latitude="+start_latitude+"&start_longitude="+start_longitude+"&end_latitude="+end_latitude+"&end_longitude="+end_longitude;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        parseStoreUberResponse(response);
                        if (!lyftDurations.isEmpty()){
                            doUIChanges();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ){
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Token Y9tB3Insqwmwd8Ryk20Q60AMpST77RN_Yzq0nGvP");
                return headers;
            }
        };

        // add it to the RequestQueue
        queue.add(getRequest);

    }

    public void doPostForLyft(){
        //function generates bearer token for lyft api and calls get request to Lyft api after token is generated
        class SendPostReqAsyncTask extends AsyncTask < String, Void, String > {
            final Context c;
            SendPostReqAsyncTask(Context c){
                this.c = c;
            }
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog2 = new ProgressDialog(c);
                //

                pDialog2.setMessage("Comparing .....");
                pDialog2.setIndeterminate(false);
                pDialog2.setCancelable(true);
                pDialog2.show();

            }
            protected String doInBackground(String...params) {
                try { /************** For getting response from HTTP URL start ***************/
                    String url = "https://api.lyft.com/oauth/token";
                    URL object = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) object.openConnection();
                    connection.setReadTimeout(60 * 1000);
                    connection.setConnectTimeout(60 * 1000);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    String authorization = "geKV51d1Fr_z" + ":" + "aWqWqewzgS6PlK6lQIjBc5owHfeg9d1p";
                    byte[] __data = authorization.getBytes("UTF-8");
                    String base64 = Base64.encodeToString(__data, Base64.DEFAULT);
                    String encodedAuth = "Basic " + base64;
                    connection.setRequestProperty("Authorization", encodedAuth);
                    String data =  "{\"grant_type\": \"client_credentials\", \"scope\": \"public\"}";
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write(data);
                    out.close();
                    connection.setRequestMethod("POST");
                    int responseCode = connection.getResponseCode(); //String responseMsg = connection.getResponseMessage();
                    //__LfytAccessResponse=responseMsg;

                    Log.v("LyftAccess", "httpStatus :" + responseCode);
                    if (responseCode == 200) {
                        InputStream inputStr = connection.getInputStream();
                        String encoding = connection.getContentEncoding() == null ? "UTF-8" : connection.getContentEncoding();
                        __LyftAccessResponse = IOUtils.toString(inputStr, encoding);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "success";
            }
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Log.i(" __LfytAccessResponse", __LyftAccessResponse);
                pDialog2.hide();
                try {
                    JSONObject jsonObject = new JSONObject(__LyftAccessResponse);
                    String accessToken = jsonObject.getString("access_token");
                    doGetForLyft(accessToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask(this.c);
        sendPostReqAsyncTask.execute();
    }

    public void doUIChanges(){
        //function to implement UIChanges on the results of the API Calls
        Log.i("uber time", uberDurations.toString());
        Log.i("uber price", uberEstimates.toString());
        Log.i("lyft time", lyftDurations.toString());
        Log.i("lyft price", lyftEstimates.toString());
        //ma.tvs.setText(uberDurations.toString() + uberEstimates.toString() + lyftEstimates.toString()+ lyftDurations.toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ma,
                android.R.layout.simple_spinner_item, lyftDurations.keySet().toArray(new String[lyftDurations.keySet().size()]));
        ma.dynamicLyftSpinner.setAdapter(adapter);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(ma,
                android.R.layout.simple_spinner_item, uberDurations.keySet().toArray(new String[uberDurations.keySet().size()]));
        ma.dynamicUberSpinner.setAdapter(adapter2);
        ma.uberDurations = uberDurations;
        ma.uberEstimates = uberEstimates;
        ma.lyftDurations = lyftDurations;
        ma.lyftEstimates = lyftEstimates;

        ma.uberTypes = uberDurations.keySet().toArray(new String[uberDurations.keySet().size()]);
        ma.lyftTypes = lyftDurations.keySet().toArray(new String[lyftDurations.keySet().size()]);
    }
}
