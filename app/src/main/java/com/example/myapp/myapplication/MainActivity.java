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

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;



import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.net.Uri;


public class MainActivity extends AppCompatActivity{

    private WebView result;
    private String webViewOut;

    private String dateUrl;


    private int mYear,mMonth,mDay;
    private String globalDate;


    String title;

    final Calendar c = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result =  findViewById(R.id.result);

        getCurrentNewsLetterDate();
        initialDownload();

        File temp = new File(getBaseContext().getFilesDir().toString() + "/" +globalDate);

       if(isNetworkAvailable() && !temp.exists()) {
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(dateUrl,globalDate);

        }else{
           webViewOut = readFromFile(getBaseContext(),globalDate);

           //File file = new File(getFilesDir() + date,"temp.html");

           setupWebView();

           //result.loadData(webViewOut,"text/html",null);
           result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" +globalDate, webViewOut, "text/html", "UTF-8", null);
           System.out.println("ONCREATE:" +globalDate);
       }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

/*
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
*/


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
        result.getSettings().setDefaultFontSize(30);
        //result.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

    }

    public void doThat(MenuItem menu){

        File temp = new File(getBaseContext().getFilesDir().toString() + "/" +globalDate);

        //if(!temp.exists()) {

        if(isNetworkAvailable() && !temp.exists()) {
            System.out.println("RUNNING ASYNC ");
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(dateUrl,globalDate);

        }else{
            webViewOut = readFromFile(getBaseContext(),globalDate);
            setupWebView();

            System.out.println("DO THAT:" +globalDate);

            result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" +globalDate, webViewOut, "text/html", "UTF-8", null);
        }
        //}



        //CLEAN DIRECTORIES TO RETEST DOWNLOAD
        //temp.delete();
        /*if (temp.isDirectory())
        {
            String[] children = temp.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(temp, children[i]).delete();
            }
        }
        */
    }


    public void initialDownload(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = null;
                    doc = Jsoup.connect("http://dailyshotbrief.com/feed/").timeout(50000).get();
                    Elements links = doc.getElementsByTag("link");

                    String pattern = ".*https://dailyshotbrief.com/the-daily-shot-brief-.*";

                    Pattern r = Pattern.compile(pattern);

                    for(Element link : links){
                        String url = "";
                        Matcher m = r.matcher(link.toString());

                        if(m.find()){
                            System.out.println("=============FOUND A DATE==================");
                            url = link.toString().replaceAll("<.*link>","");
                            url = url.replaceAll("\\n","");
                            //TODO: create a getDateFromUrl function so we store with the same date as selected

                            globalDate= getDateFromUrl(url);

                            dateUrl = url;
                            break;
                        }

                    }

                } catch (IOException e) {
                    System.out.println();
                }

            }
        }).start();
        try
        {
            Thread.sleep(3000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }


    public String getDateFromUrl(String url){
        String ret;
        //TODO: find a better way to do this
        ret = url.replaceAll("https://dailyshotbrief.com/the-daily-shot-brief-","");
        ret = ret.replaceAll("-2-2-2/","");
        ret = ret.replaceAll("-2-2","");
        ret = ret.replaceAll("-|th|st|nd|rd","");
        //add in 0 after month so we dont overlap dates
        ret = ret.replaceAll("jan","10");
        ret = ret.replaceAll("feb","20");
        ret = ret.replaceAll("mar","30");
        ret = ret.replaceAll("apr","40"); //TODO: apr or april?
        ret = ret.replaceAll("may","50");
        ret = ret.replaceAll("june","60");
        ret = ret.replaceAll("july","70");
        ret = ret.replaceAll("aug","80");
        ret = ret.replaceAll("sept","90"); //TODO: sept or sep?
        ret = ret.replaceAll("oct","100");
        ret = ret.replaceAll("nov","110");
        ret = ret.replaceAll("dec","120");
        ret = ret.replaceAll("/","");
        ret = ret.replaceAll(" ","");


        return ret;
    }



    public String getCurrentNewsLetterDate(){
        String ret;
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            ret = Integer.toString(month+1) + "0" + Integer.toString(day) + Integer.toString(year);
            globalDate= ret.toString();
        return ret;
    }

    public void doThis(MenuItem item){
        //final Calendar c = Calendar.getInstance();
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

                        globalDate= Integer.toString(monthOfYear+1) + "0" + Integer.toString(dayOfMonth) + Integer.toString(year);
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
        //int temp = day % 10;
        if(day == 1){
            url += "st-";
        }else if(day == 2){
            url += "nd-";
        }else if(day == 3){
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
            File html = new File(_path + "/temp.html");
            try {
                html.createNewFile();
            }catch(IOException e){
                Log.e("Exception", "File creation failed: " + e.toString());
            }
        }

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
            String file_name=context.getFilesDir().toString() + "/" + date +"/temp.html";
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

        private String resp = "done";
        ProgressDialog progressDialog;
        String getWebsiteOut = "";


        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()


            try {
                //final Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                Connection.Response con = Jsoup.connect(params[0]).timeout(600000).execute();
                Document doc = con.parse();
                getWebsiteOut = "";
                String _path = getBaseContext().getFilesDir().toString() + "/" + params[1];
                int statusCode = con.statusCode();

                Elements images = doc.select("img[src]");
                //getImages(images,date,context);

                Elements article = doc.getElementsByTag("article");
                Elements divs = doc.getElementsByTag("div");
                getWebsiteOut += "<!DOCTYPE HTML> \n" +
                        "<html> \n" +
                        //"<meta name=\"viewport\" content=\"target-densitydpi=high-dpi\" >";
                        "<meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=0.5 \" > \n";
                        //"<head> <style> p{ width: device-width; }</style>  </head> \n" ;

                getWebsiteOut += article.html();

                getWebsiteOut += "</html>";

                File tempFile = new File(_path);

                if(!tempFile.exists()) {
                    tempFile.mkdir();
                }

                if(statusCode == 200){
                    for(Element link : images){
                        //get image url and data
                        String temp = link.attr("src");
                        Connection.Response resultImageResponse = Jsoup.connect(temp).timeout(60000).ignoreContentType(true).execute();

                        //parse for file name and replace in html
                        Uri uri = Uri.parse(temp);
                        String imageName = uri.getLastPathSegment();
                        link.attr("src", imageName);


                        File file = new File(_path + "/" + imageName);

                        //if(!file.exists()) {
                        try {
                            FileOutputStream out = new FileOutputStream(new File(_path + "/" + imageName));
                            out.write(resultImageResponse.bodyAsBytes());
                            out.close();
                            getWebsiteOut = getWebsiteOut.replace(temp, "file:///" + _path + "/" + imageName);
                            //getWebsiteOut = getWebsiteOut.replace(temp, imageName);
                            getWebsiteOut = getWebsiteOut.replaceAll("width=\".*?\"", "style=\"display: inline; height: device-height; width: device-width;\"");
                            //getWebsiteOut = getWebsiteOut.replaceAll("width=\".*?\"", "");
                            //getWebsiteOut = getWebsiteOut.replaceAll("height=\".*?\"", "");
                            //link.attr("src",imageName);
                            //link.attr("style","max-width: 100%; height: auto;");

                        } catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }
                        //}
                    }
                }
                writeToFile(getWebsiteOut,getBaseContext(),globalDate);
                System.out.println("LINK WORKED, WRITING HTML");
            } catch (IOException e) {
                getWebsiteOut = "<!DOCTYPE HTML> " +
                        "<html>" +
                        "<meta name=\"viewport\" content='width=device-width, initial-scale=1.0,text/html,charset=utf-8' >";

                getWebsiteOut += "website is not available. please choose another date";

                getWebsiteOut += "</html>";
                try
                {
                    Thread.sleep(500);
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
                System.out.println("LINK DID NOT WORK, WRITING ERROR");
                writeToFile(getWebsiteOut,getBaseContext(),globalDate);
                e.printStackTrace();
            }

            /*try
            {
                Thread.sleep(5000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            } */

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            showPage();
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

        void showPage(){
            setupWebView();

            System.out.println("DO THAT 2:" +globalDate);

            result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" +globalDate, getWebsiteOut, "text/html", "UTF-8", null);
        }

    }
}

