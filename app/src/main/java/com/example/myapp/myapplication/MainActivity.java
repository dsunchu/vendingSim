package com.example.myapp.myapplication;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.TextView;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;



import android.net.Uri;


public class MainActivity extends AppCompatActivity{

    private WebView result;
    private String webViewOut;
    private String getWebsiteOut = "";
    private String dateUrl;


    private int mYear,mMonth,mDay;
    private String globaldate;


    private int screenHeight;
    private int screenWidth;

    String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result =  findViewById(R.id.result);




        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;




    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);

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



    public void setupWebView(){
        result.clearCache(true);
        result.clearView();
        result.reload();
        result.setInitialScale(1);
        result.getSettings().setUseWideViewPort(true);
        result.getSettings().setLoadWithOverviewMode(true);

        //result.getSettings().setAppCacheMaxSize(5 * 1024 * 1024);
        //result.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        result.getSettings().setAllowFileAccess( true );
        //result.getSettings().setAppCacheEnabled(true);
        result.getSettings().setJavaScriptEnabled(true);
        result.getSettings().setSaveFormData(true);
        result.getSettings().setBuiltInZoomControls(true);
        result.setWebViewClient(new WebViewClient());
        result.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );
        result.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        result.getSettings().setDefaultFontSize(40);

    }

    public void doThat(MenuItem item){

        File temp = new File(getBaseContext().getFilesDir().toString() + "/" + globaldate);

        //if(!temp.exists()) {


            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(dateUrl, globaldate);

            try
            {
                Thread.sleep(3000);
            }
            catch(InterruptedException ex)
            {
               Thread.currentThread().interrupt();
            }

            //writeToFile(getWebsiteOut,getBaseContext(),globaldate);

        //}




        webViewOut = readFromFile(getBaseContext(),globaldate);

        //File file = new File(getFilesDir() + date,"temp.html");

        setupWebView();

        //result.loadData(webViewOut,"text/html",null);
        result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" + globaldate,webViewOut,"text/html","UTF-8",null);
        //result.loadDataWithBaseURL(null,webViewOut,"text/html","UTF-8",null);
        //result.loadData(webViewOut,"text/html","UTF-8");
    }



    public void doThis(MenuItem item){
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);



        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // Display Selected date in textbox

                        if (year < mYear)
                            view.updateDate(mYear,mMonth,mDay);

                        if (monthOfYear < mMonth && year == mYear)
                            view.updateDate(mYear,mMonth,mDay);

                        if (dayOfMonth < mDay && year == mYear && monthOfYear == mMonth)
                            view.updateDate(mYear,mMonth,mDay);

                        globaldate = Integer.toString(dayOfMonth) + Integer.toString(monthOfYear+1) + Integer.toString(year);
                        dateUrl = "";
                        dateUrl = createUrlFromDate(year,monthOfYear,dayOfMonth);

                    }
                }, mYear, mMonth, mDay);
        //dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();

    }



    //update this function for 21st 22nd 23rd and so on...
    private String createUrlFromDate(int year,int month, int day){
        String url = "https://dailyshotbrief.com/the-daily-shot-brief-";

        switch (month + 1){
            case 1:
                url += "january-";
                break;
            case 2:
                url += "february-";
                break;
            case 3:
                url += "march-";
                break;
            case 4:
                url += "april-";
                break;
            case 5:
                url += "may-";
                break;
            case 6:
                url += "june-";
                break;
            case 7:
                url += "july-";
                break;
            case 8:
                url += "august-";
                break;
            case 9:
                url += "september-";
                break;
            case 10:
                url += "october-";
                break;
            case 11:
                url += "november-";
                break;
            case 12:
                url += "december-";
                break;
        }

        url += Integer.toString(day);
        int temp = day % 10;
        if(temp == 1){
            url += "st-";
        }else if(temp == 2){
            url += "nd-";
        }else if(temp == 3){
            url += "rd-";
        }else{
            url += "th-";
        }

        url += Integer.toString(year);

        return url;
    }



    private void writeToFile(String data,Context context, String date) {
        //add in code to create a directory named the date of the article
        String _path = context.getFilesDir().toString() + "/" + date;
        File temp = new File(_path);

        if(!temp.exists()) {
            temp.mkdir();
        }
                /*
            try {
                OutputStream outStream = new FileOutputStream( _path + "/temp.html");
                outStream = new BufferedOutputStream(outStream, 1024 * 1024);
                //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(  date + "/temp.html", Context.MODE_PRIVATE));
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
                outputStreamWriter.write(data);
                outputStreamWriter.close();
                outStream.close();

            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            } */

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(_path + "/temp.html"));
            out.write(data);
            out.close();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }

    private String readFromFile(Context context, String date) {

        String ret = "";

        try {
            String file_name=context.getFilesDir() + "/" + date +"/temp.html";
            File newFile = new File(file_name);
            //path.setText(newFile.getAbsolutePath());
            //InputStream inputStream = context.openFileInput(date + "/temp.html");
            InputStream inputStream = new FileInputStream( file_name);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }

            //newFile.delete(); //FOR TESTING RN
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }





    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()


            try {
                //final Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                Document doc = Jsoup.connect(params[0]).get();
                //getWebsiteOut = doc.toString();

                Elements images = doc.select("img[src]");
                //getImages(images,date,context);

                String _path = getApplication().getFilesDir().toString() + "/" + params[1];

                Elements article = doc.getElementsByTag("article");

                getWebsiteOut = getWebsiteOut + "<!DOCTYPE HTML> " +
                        "<html>" +
                        "<meta name=\"viewport\" content='width=device-width, initial-scale=1.0,text/html,charset=utf-8' >";

                getWebsiteOut = article.html();

                getWebsiteOut = getWebsiteOut + "</html>";

                File tempFile = new File(_path);

                if(!tempFile.exists()) {
                    tempFile.mkdir();
                }


                for(Element link : images){
                    //get image url and data
                    String temp = link.attr("src");
                    Connection.Response resultImageResponse = Jsoup.connect(temp).timeout(60000).ignoreContentType(true).execute();

                    //parse for file name and replace in html
                    Uri uri = Uri.parse(temp);
                    String imageName = uri.getLastPathSegment();
                    link.attr("src", imageName);

                    List<TextNode> tnList = link.textNodes();

                    int w = (int) Math.round(screenWidth * 0.2);
                    String width = "width=" +  Integer.toString(screenWidth);//Integer.toString(300);
                    int h = (int) Math.round(screenHeight * 0.2);
                    String height = "height=" + Integer.toString(200);

                    File file = new File(_path + "/" + imageName);

                    //if(!file.exists()) {

                        try {
                            FileOutputStream out = new FileOutputStream(new File(_path + "/" + imageName));
                            out.write(resultImageResponse.bodyAsBytes());
                            out.close();
                            getWebsiteOut = getWebsiteOut.replace(temp, "file:///" + _path + "/" + imageName);
                            //getWebsiteOut = getWebsiteOut.replace(temp, imageName);
                            //getWebsiteOut = getWebsiteOut.replaceAll("width=\".*?\"", "style=\"display: inline; \"");
                            //getWebsiteOut = getWebsiteOut.replaceAll("width=\".*?\"", width);
                            //getWebsiteOut = getWebsiteOut.replaceAll("height=\".*?\"", "");
                            //link.attr("src",imageName);
                            //link.attr("style","max-width: 100%; height: auto;");

                        } catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }

                        writeToFile(getWebsiteOut,getBaseContext(),globaldate);
                    //}
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "ProgressDialog",
                    "Waiting for download");
        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
}

