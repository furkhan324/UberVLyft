# UberVLyft

## Synopsis

Compare Uber/Lyft services, fares, and duration all in real time. Built for Android(native).

<a href= "https://play.google.com/store/apps/details?id=com.furkhanapps2.furkhan324.myapplication" >Live on Google Play Store</a> (Published as "WatchFare")

## Demo

[![IMAGE ALT TEXT](http://mohammedabdulwahhab.me/readme.gif)](# "UberVLyft Demo")

## Dependancies

API's and Frameworks used:

- UberRides REST API [link](https://developer.uber.com/docs/rides/getting-started "UberRides"). To get Uber data
- Lyft REST API [link](https://developer.lyft.com/docs/availability-cost "Lyft API"). To get Lyft data
- Volley : HTTP Interfacing Library for Android

GET/POST Requests to Uber/Lyft(API.java)
<br>

- UBER Data Fetch
  - GET /v1/estimates/price (API endpoint)
  - Implemented using Volley queue
  - Server Token passed in via Request header (Overridden method)

<br>

  ```Java
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
                        // display response, callback upon completion
                        Log.d("Response", response.toString());
                        
                        parseStoreUberResponse(response); //Parses and stores JSON reponse to HashMap
                        
                        if (!lyftDurations.isEmpty()){
                            doUIChanges();
                        }
                    }
                }
                ...

  ```

<br>
- Lyft Data Fetch
  - GET 'https://api.lyft.com/v1/cost' (API endpoint)
  - Implemented using Volley queue
  - Bearer Token passed in, doPostForLyft -> bearer Token calls doGetForLyft
  
<br>
  ```Java
public void doGetForLyft(final String bearerToken){
        //function called from doPostLyft function
        RequestQueue queue = Volley.newRequestQueue(c);
        final String url = "https://api.lyft.com/v1/cost?start_lat="+start_latitude+"&start_lng="+start_longitude+"&end_lat="+end_latitude+"&end_lng="+end_longitude;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                    
                        parseStoreLyftResponse(response); //Parses and stores JSON reponse to HashMap
                        
                        Log.d("Response", response.toString());
                        if (!uberDurations.isEmpty()){
                            doUIChanges();
                        }
                    }
                }
                ...

```
<br>
<h3>Other Important Methods:</h3>
- parseStoreLyftResponse(), parseStoreUberResponse() --> both parse JSON responses and store parsed values to HashMap
- doPostForLyft() --> Needed to generate 24hr valid bearer token needed to authenticate GET calls to Lyft. This function's completion callback calls 'doGetForLyft()'
- doUIChanges() --> refresh UI spinners, textviews with new data
- geocodeAddress() --> geocodes Addresses from String to latitudes and longitudes

## Installation

Clone repo and open in Android Studio.

```
git clone https://github.com/furkhan324/ubervlyft.git
#open gradle.build file from AS
```

## Contributors

Mohammed Abdulwahhab (@furkhan324)

## License

Code may not be copied, edited, or reproduced in any form without the consent of the contributors.
