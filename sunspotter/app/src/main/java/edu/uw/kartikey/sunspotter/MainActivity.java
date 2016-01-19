package edu.uw.kartikey.sunspotter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    static final String TAG = "Main Activity: ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String api_key = "0f6bcd4dd84f83a513571b0433badda6";

        String weather_data[]  = {"1","2","3"};


//Arrays.asList(weather_data)
        ArrayList<String> list = new ArrayList<String>();

        //controller
        adapter = new ArrayAdapter<String>(
                this, R.layout.list_item, R.id.txtItem, list);


        AdapterView listView = (AdapterView)findViewById(R.id.listView);
        listView.setAdapter(adapter);


        Log.v("ListActivity", "WORKING");


        final EditText text = (EditText)findViewById(R.id.searchField);
        final Button button = (Button)findViewById(R.id.searchButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadWeatherData dwd = new DownloadWeatherData();
                Log.v(TAG, "Button was pressed");
                String typed = text.getText().toString();
                dwd.execute(typed);
                Log.v(TAG, "You typed: " + typed);
            }
        });

    }


    public class DownloadWeatherData extends AsyncTask<String, Void, String[]> {


        protected String weather_array[];
        protected String day_array[];
        protected int day_date_array[];
        protected String time_array[];
        protected String am_pm_array[];
        protected int temperature_array[];

        protected String[] doInBackground(String... params){



            String zipcode = params[0];

            String urlString = "";
            try {
                urlString = "http://api.openweathermap.org/data/2.5/forecast?zip="+URLEncoder.encode(zipcode, "UTF-8") +"&APPID=0f6bcd4dd84f83a513571b0433badda6"; //+URLEncoder.encode(movie, "UTF-8") + "&type=movie";
            }catch(Exception uee){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String weatherData[] = null;


            try {

                Log.v("ListActivity", "BACKGROUN");

                URL url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {

                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                    return null;
                }
                String results = buffer.toString();

                Log.v(TAG, results);

                JSONObject jo = new JSONObject();

                    jo = new JSONObject(results);

                JSONArray arr = new JSONArray();


                    arr = jo.getJSONArray("list");
                    //Log.v(TAG,arr.toString());

                String[] days = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
                String[] ampm = {"AM","PM"};

                weatherData = new String[arr.length()];
                weather_array = new String[arr.length()];
                day_array = new String[arr.length()];
                day_date_array = new int[arr.length()];
                time_array = new String[arr.length()];
                am_pm_array = new String[arr.length()];
                temperature_array = new int[arr.length()];

                for(int i = 0;i < arr.length();i++) {
                    JSONObject temp = new JSONObject(arr.getString(i));
                    //temp.getString("dt_txt")
                    SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = simpleDate.parse(temp.getString("dt_txt"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int day = cal.get(Calendar.DAY_OF_WEEK);
                    int dayDate = cal.get(Calendar.DATE);
                    int time = cal.get(Calendar.HOUR);
                    int am_pm = cal.get(Calendar.AM_PM);
                    //JSONArray weatherArray = temp.getJSONArray("weather");
                    JSONObject weatherObject = new JSONObject(temp.getJSONArray("weather").getString(0).toString());
                    String weather = weatherObject.getString("main");
                    int temperature = (temp.getJSONObject("main")).getInt("temp");

                    weather_array[i] = weather;
                    day_date_array[i] = dayDate;
                    day_array[i] = days[day-1];
                    time_array[i] = ""+ time;
                    if(time ==0)
                        time_array[i]="12";
                    am_pm_array[i] = ampm[am_pm];
                    temperature_array[i] = temperature-273;

                    // For Checking if clear code works
                   /* if(i==5) {
                        weather_array[i] = "Clear";
                    }*/

                    weatherData[i] = weather_array[i] + " (" + day_date_array[i] + "-" + day_array[i] + " " +time_array[i]+":00 " + am_pm_array[i] + ") - " + temperature_array[i] + " C";
                    Log.v(TAG,weatherData[i]+"\n");
                }


            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                return null;
            }  catch (ParseException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (final IOException e) {
                    }
                }
            }

            return weatherData;
        }


        protected void onPostExecute(String[] data){

            if(data != null) {
                adapter.clear();




                ViewStub stub = (ViewStub) findViewById(R.id.stubView);
                //stub.setLayoutResource(R.layout.mid_section_nosun);
                stub.setLayoutResource(R.layout.mid_section);
                stub.inflate();

                TextView tv = (TextView) findViewById(R.id.sunText);
                TextView tv2 = (TextView) findViewById(R.id.willBeSun);
                ImageView iv = (ImageView) findViewById(R.id.sunImage);
                iv.setImageResource(R.drawable.sad39);
                tv.setVisibility(View.INVISIBLE);


                for (String d : data) {
                    adapter.add(d);
                }

                for(int i =0; i<weather_array.length;i++) {
                    if(weather_array[i].equals("Clear")) {

                        tv.setVisibility(View.VISIBLE);
                        tv.setText("There will be sun on " + day_date_array[i] + " " + day_array[i] + " at " + time_array[i] + " " + am_pm_array[i]);
                        tv2.setText("THERE WILL BE SUN!");
                        iv.setImageResource(R.drawable.sun);
                        Log.v(TAG,tv.getText().toString());

                        break;
                    }
                }



            }

        }

    }



}
