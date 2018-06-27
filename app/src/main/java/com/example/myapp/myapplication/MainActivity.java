package com.example.myapp.myapplication;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

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
import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


import android.net.Uri;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private WebView result;
    private Button getBtn;
    private String outputTest;
    private TextView path;
    private String webViewOut;
    private String getWebsiteOut = "";
    private String dateUrl;
    private ClipData.Item download;// = new Button(getApplicationContext());

    private String testDeleteOut;

    private int mYear,mMonth,mDay;
    private String globaldate;
    private ClipData.Item pickDate;
    private Button dlwebpage;// = new Button(getApplicationContext());
    private TextView textView;
    private Toolbar toolbar;
    private Menu menu;
    private String inBedMenuTitle = "Set to 'In bed'";
    private String outOfBedMenuTitle = "Set to 'Out of bed'";
    private boolean inBed = false;

    private AdvancedWebView mWebView;

    String title;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result =  findViewById(R.id.result);

        path = findViewById(R.id.path);

        getBtn = (Button) findViewById(R.id.getBtn);
        getBtn.setOnClickListener(this); // calling onClick() method

        //Button download = new Button(getApplicationContext());
        //download = (Button) findViewById(R.id.download_website);
        //download.setOnClickListener(this);

        //pickDate =  findViewById(R.id.pick_date);
        //pickDate.setOnClickListener(this);

        //mWebView.setListener(this, this);


        // Attaching the layout to the toolbar object
        //toolbar = (Toolbar) findViewById(R.id.tool_bar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        //setSupportActionBar(toolbar);

        //Button dlwebpage = new Button(getApplicationContext());
        dlwebpage = (Button) findViewById(R.id.dlwebpage);
        dlwebpage.setOnClickListener(this);



        textView = (TextView) findViewById(R.id.date);

        dlwebpage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.performClick();
                }
            }
        });

        dlwebpage.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction())
                {
                    view.performClick();
                    return true;
                }
                return false;
            }
        });

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




    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.getBtn:
                path.setMovementMethod(new ScrollingMovementMethod());

                webViewOut = readFromFile(getBaseContext(),globaldate);

                //File file = new File(getFilesDir() + date,"temp.html");
                path.setText(globaldate);

                result.clearCache(true);
                result.clearView();
                result.reload();

                //result.getSettings().setAppCacheMaxSize(5 * 1024 * 1024);
                //result.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
                result.getSettings().setAllowFileAccess( true );
                //result.getSettings().setAppCacheEnabled(true);
                result.getSettings().setJavaScriptEnabled(true);
                result.getSettings().setSaveFormData(true);
                result.getSettings().setBuiltInZoomControls(true);
                result.setWebViewClient(new WebViewClient());
                result.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );

                //result.loadData(webViewOut,"text/html",null);
                result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" + globaldate,webViewOut,"text/html","UTF-8",null);

                //result.reload();

                if ( !isNetworkAvailable() ) { // loading offline
                    result.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
                }

                //result.loadUrl( dateUrl);


                //esult.setWebViewClient(new WebViewClient());

                break;

            case R.id.dlwebpage:

                //getWebsiteFromUrl(dateUrl);
                //TheTask task = new TheTask(false,dateUrl);
                //task.execute();
                writeToFile(getWebsiteOut, getApplicationContext(), globaldate);

                break;
            default:
                break;
        }

    }

    public void doThat(MenuItem item){
        //path.setText("DOWNLOAD IN PROGRESS");
        //getWebsiteFromUrl(dateUrl, globaldate,getBaseContext());

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(dateUrl,globaldate);

        try
        {
            Thread.sleep(2000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
        writeToFile(getWebsiteOut,getBaseContext(),globaldate);
        path.setText("DOWNLOAD COMPLETE");
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

                        //pickDate.setText( (monthOfYear + 1) + "-"+dayOfMonth  + "-" + year);
                        //menu.getItem(R.id.pick_date).setTitle("test");
                        //pickDate.getText();
                        globaldate = Integer.toString(dayOfMonth) + Integer.toString(monthOfYear+1) + Integer.toString(year);
                        dateUrl = "";
                        dateUrl = createUrlFromDate(year,monthOfYear,dayOfMonth);
                        //getWebsiteFromUrl(dateUrl);
                        /*try
                        {
                            Thread.sleep(5000);
                        }
                        catch(InterruptedException ex)
                        {
                            Thread.currentThread().interrupt();
                        }*/
                        //writeToFile(getWebsiteOut, getBaseContext(), globaldate);

                        //path.setText(dateUrl);

                    }
                }, mYear, mMonth, mDay);
        //dpd.getDatePicker().setMinDate(System.currentTimeMillis());
        dpd.show();

    }



    private void getWebsiteFromUrl(final String url,final String date,final Context context) {
        final String ret = "";
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //final Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                    Document doc = Jsoup.connect(url).get();
                    getWebsiteOut = doc.toString();

                    Elements images = doc.select("img[src]");
                    //getImages(images,date,context);

                    String _path = context.getFilesDir().toString() + "/" + date;

                    File tempFile = new File(_path);

                    if(!tempFile.exists()) {
                        tempFile.mkdir();
                    }


                    for(Element link : images){
                        //get image url and data
                        String temp = link.attr("src");
                        Connection.Response resultImageResponse = Jsoup.connect(temp).ignoreContentType(true).execute();

                        //parse for file name and replace in html
                        Uri uri = Uri.parse(temp);
                        String imageName = uri.getLastPathSegment();
                        link.attr("src", imageName);

                        List<TextNode> tnList = link.textNodes();

                        try {
                            FileOutputStream out = new FileOutputStream(new File(_path + "/" + imageName));
                            out.write(resultImageResponse.bodyAsBytes());
                            out.close();
                            getWebsiteOut = getWebsiteOut.replace(temp,"file:///" + _path + "/" + imageName);

                        } catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }

                    }

                    Elements scripts = doc.getElementsByTag("script");
                    Elements css     = doc.getElementsByTag("link");

                    for(Element c : scripts) {
                        String url = c.absUrl("href");
                        String rel = c.attr("type") == null ? "" : c.attr("type");
                        if(!url.isEmpty() && rel.equals("text/javascript")){
                            System.out.println(url);
                            Uri uri = Uri.parse(url);
                            Document docScript = Jsoup
                                    .connect(url)
                                    .userAgent("Mozilla")
                                    .ignoreContentType(true)
                                    .get();

                            String filename = uri.getLastPathSegment();
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(_path + "/" + filename));
                                out.write(docScript.toString());
                                out.close();
                                getWebsiteOut = getWebsiteOut.replace(url, "file:///" + _path + "/" + filename);
                            }catch (IOException e) {
                                Log.e("Exception", "File write failed: " + e.toString());
                            }
                            System.out.println(docScript);
                            System.out.println("--------------------------------------------");
                        }
                    }


                    for(Element c : css) {
                        String url = c.absUrl("href");
                        String rel = c.attr("rel") == null ? "" : c.attr("rel");
                        if(!url.isEmpty() && rel.equals("stylesheet")) {
                            System.out.println(url);
                            Uri uri = Uri.parse(url);
                            Document docScript = Jsoup
                                    .connect(url)
                                    .userAgent("Mozilla")
                                    .ignoreContentType(true)
                                    .get();

                            String filename = uri.getLastPathSegment();
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(_path + "/" + filename));
                                out.write(docScript.toString());
                                out.close();
                                getWebsiteOut = getWebsiteOut.replace(url, "file:///" + _path + "/" + filename);
                            }catch (IOException e) {
                                Log.e("Exception", "File write failed: " + e.toString());
                            }
                            System.out.println(docScript);
                            System.out.println("--------------------------------------------");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        task.start();
    }




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

            /*try {
                int time = Integer.parseInt(params[0])*1000;

                Thread.sleep(time);
                resp = "Slept for " + params[0] + " seconds";
            } catch (InterruptedException e) {
                e.printStackTrace();
                resp = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            } */

            try {
                //final Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                Document doc = Jsoup.connect(params[0]).get();
                getWebsiteOut = doc.toString();

                Elements images = doc.select("img[src]");
                //getImages(images,date,context);

                String _path = getApplication().getFilesDir().toString() + "/" + params[1];

                File tempFile = new File(_path);

                if(!tempFile.exists()) {
                    tempFile.mkdir();
                }


                for(Element link : images){
                    //get image url and data
                    String temp = link.attr("src");
                    Connection.Response resultImageResponse = Jsoup.connect(temp).ignoreContentType(true).execute();

                    //parse for file name and replace in html
                    Uri uri = Uri.parse(temp);
                    String imageName = uri.getLastPathSegment();
                    link.attr("src", imageName);

                    List<TextNode> tnList = link.textNodes();

                    try {
                        FileOutputStream out = new FileOutputStream(new File(_path + "/" + imageName));
                        out.write(resultImageResponse.bodyAsBytes());
                        out.close();
                        getWebsiteOut = getWebsiteOut.replace(temp,imageName);

                    } catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }

                }

                Elements scripts = doc.getElementsByTag("script");
                Elements css     = doc.getElementsByTag("link");

                for(Element c : scripts){


                    String url = c.absUrl("src");
                    String rel = c.attr("type") == null ? "" : c.attr("type");
                    if(!url.isEmpty() && rel.equals("text/javascript")) {
                        System.out.println(url);
                        Uri uri = Uri.parse(url);
                        Connection.Response docScript = Jsoup
                                .connect(url)
                                .userAgent("Mozilla")
                                .ignoreContentType(true)
                                .execute();

                        String filename = uri.getLastPathSegment();
                        try {
                                /*
                                BufferedWriter out = new BufferedWriter(new FileWriter(_path + "/" + filename));
                                out.write(docScript.toString());
                                out.close();
                                */

                            FileOutputStream out2 = new FileOutputStream(new File(_path + "/" + filename));
                            out2.write(docScript.bodyAsBytes());
                            out2.close();
                            getWebsiteOut = getWebsiteOut.replace(url, "file:///" + _path + "/" + filename);
                            //getWebsiteOut = getWebsiteOut.replace(url,  filename);
                        }catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }
                        System.out.println(docScript);
                        System.out.println("--------------------------------------------");
                    }
                }


                    for(Element c : css) {
                        String url = c.absUrl("href");
                        String rel = c.attr("rel") == null ? "" : c.attr("rel");
                        if(!url.isEmpty() && rel.equals("stylesheet")) {
                            System.out.println(url);
                            Uri uri = Uri.parse(url);
                            Connection.Response docScript = Jsoup
                                    .connect(url)
                                    .userAgent("Mozilla")
                                    .ignoreContentType(true)
                                    .execute();

                            String filename = uri.getLastPathSegment();
                            try {
                                /*
                                BufferedWriter out = new BufferedWriter(new FileWriter(_path + "/" + filename));
                                out.write(docScript.toString());
                                out.close();
                                */

                                FileOutputStream out2 = new FileOutputStream(new File(_path + "/" + filename));
                                out2.write(docScript.bodyAsBytes());
                                out2.close();
                                //getWebsiteOut = getWebsiteOut.replace(url, "file:///" + _path + "/" + filename);
                                getWebsiteOut = getWebsiteOut.replace(url, filename);
                            }catch (IOException e) {
                                Log.e("Exception", "File write failed: " + e.toString());
                            }
                            System.out.println(docScript);
                            System.out.println("--------------------------------------------");
                        }
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
            path.setText(result);
        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "ProgressDialog",
                    "Waiting for download");
        }


        @Override
        protected void onProgressUpdate(String... text) {
            path.setText(text[0]);

        }
    }
}

