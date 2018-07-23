package com.davidsunchu.test.dailyShotBrief;

import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;



import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
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
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.Iterator;
import java.util.regex.Pattern;


import android.net.Uri;


public class MainActivity extends AppCompatActivity {

    private WebView result;
    private String webViewOut;

    private String globalDate;

    String title;

    final Calendar c = Calendar.getInstance();


    Deque<myDate> myList = new ArrayDeque<myDate>();
    myDate  currentDate = new myDate();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result =  findViewById(R.id.result);

        String path = getBaseContext().getFilesDir().toString();

        File dir = new File(path);
        try {
            FileUtils.deleteDirectory(dir);
        }catch (IOException e){
            System.out.print(e);
        }

        getCurrentNewsLetterDate();
        initialDownload();
        showUpdatedPage();


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
        if (id == R.id.previous) {
            showPreviousPage();
            return true;
        }
        if (id == R.id.next) {
            showNextPage();
            return true;
        }

        if(id == R.id.downloaded_websites){
            showWebsiteList();
            return true;
        }

        if(id == R.id.about){
            String _path = getBaseContext().getFilesDir().toString() + "/about" ;
            File file = new File(_path);
            if(file.exists()){
                webViewOut = readFromFile(getBaseContext(),"about");
                setupWebView();

                result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/about", webViewOut, "text/html", "UTF-8", null);

            }else{
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute("https://dailyshotbrief.com/about/","about");
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }




    public void showWebsiteList(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setTitle("Select A Item ");
        alertBuilder.setView(R.layout.websitelist);


        ArrayList<String> items = new ArrayList<>();

        Iterator<myDate> it = myList.iterator();
        myDate tempDate = new myDate();
        while(it.hasNext()){
            tempDate = it.next();
            items.add(tempDate.getDatetime());

        }
        final CharSequence[] List = items.toArray(new CharSequence[items.size()]);
        alertBuilder.setItems(List, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.out.println(which);
                Integer index = 0 ;
                Iterator<myDate> it = myList.iterator();
                String directoryName = "";
                myDate temp = new myDate();
                while(it.hasNext()){
                    if(index == which && it.hasNext()){
                        temp = it.next();
                        directoryName = temp.getDirectoryName().replaceAll("/","");
                        directoryName = directoryName.replaceAll(":","");
                        directoryName = directoryName.replaceAll("\\.","");
                        String _path = getBaseContext().getFilesDir().toString() + "/" + directoryName;
                        File file = new File(_path);

                        if(file.exists()){
                            webViewOut = readFromFile(getBaseContext(),directoryName);
                            setupWebView();

                            System.out.println("DO THAT:" +globalDate);
                            result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" +directoryName, webViewOut, "text/html", "UTF-8", null);

                            //currentDate = temp;

                            //break;
                        }else{
                            AsyncTaskRunner runner = new AsyncTaskRunner();
                            runner.execute(temp.getDirectoryName(),temp.getDirectoryName());
                            temp.setDownloaded(true);
                        }

                        currentDate = temp;
                        break;
                    }
                    index++;
                    it.next();
                }
            }
        });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }


    //grabs the first entry in myList and shows it on
    public void showUpdatedPage(){
        String _path = "";
        String directoryName = "";
        for(myDate temp : myList) {
            //myDate temp = (myDate) myList.get(i);

            directoryName = temp.getDirectoryName().replaceAll("/","");
            directoryName = directoryName.replaceAll(":","");
            directoryName = directoryName.replaceAll("\\.","");
            _path = getBaseContext().getFilesDir().toString() + "/" + directoryName;
            File file = new File(_path);

            if(file.exists()){
                webViewOut = readFromFile(getBaseContext(),directoryName);
                setupWebView();

                System.out.println("DO THAT:" +globalDate);
                result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" +directoryName, webViewOut, "text/html", "UTF-8", null);

                //currentDate = temp;

                //break;
            }else{
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(temp.getDirectoryName(),temp.getDirectoryName());
                temp.setDownloaded(true);
            }

            currentDate = temp;
            break;
        }
    }

    public void showNextPage(){
        String _path = "";
        String directoryName = "";
        Iterator<myDate> it = myList.iterator();
        myDate next;

        while(it.hasNext()){
            next = it.next();
            if(next.getDirectoryName() == currentDate.getDirectoryName()){
                if(it.hasNext()) {
                    next  = it.next();
                    directoryName = next.getDirectoryName().replaceAll("/", "");
                    directoryName = directoryName.replaceAll(":", "");
                    directoryName = directoryName.replaceAll("\\.", "");
                    _path = getBaseContext().getFilesDir().toString() + "/" + directoryName;

                    File file = new File(_path);
                    if (file.exists()) {
                        webViewOut = readFromFile(getBaseContext(), directoryName);
                        setupWebView();

                        result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" + directoryName, webViewOut, "text/html", "UTF-8", null);

                        //currentDate = next;
                    } else {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.execute(next.getDirectoryName(), next.getDirectoryName());
                        next.setDownloaded(true);
                    }
                    currentDate = next;
                    break;
                }
            }
        }
    }

    //TODO: add in checking for the end of list cases, otherwise we'd be trying to reference a null object
    public void showPreviousPage(){
        Iterator<myDate> it = myList.descendingIterator();
        String _path = "";
        String directoryName = "";
        myDate next;
        while(it.hasNext()){
            next = it.next();
            if(next.getDirectoryName() == currentDate.getDirectoryName()){
                if(it.hasNext()) {
                    next = it.next();
                    directoryName = next.getDirectoryName().replaceAll("/", "");
                    directoryName = directoryName.replaceAll(":", "");
                    directoryName = directoryName.replaceAll("\\.", "");
                    _path = getBaseContext().getFilesDir().toString() + "/" + directoryName;

                    File file = new File(_path);
                    if (file.exists()) {
                        webViewOut = readFromFile(getBaseContext(), directoryName);
                        setupWebView();

                        result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" + directoryName, webViewOut, "text/html", "UTF-8", null);

                        //currentDate = next;
                    } else {
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.execute(next.getDirectoryName(), next.getDirectoryName());
                        next.setDownloaded(true);
                    }
                    currentDate = next;
                    break;
                }
            }
        }
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
        result.getSettings().setDefaultFontSize(30);
        //result.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

    }



    public void initialDownload(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = null;
                    doc = Jsoup.connect("http://dailyshotbrief.com/feed/").timeout(50000).get();
                    Elements links = doc.getElementsByTag("link");
                    Elements dates = doc.getElementsByTag("pubDate");

                    String pattern = ".*https://dailyshotbrief.com/the-daily-shot-brief-.*";

                    Pattern r = Pattern.compile(pattern);

                    createDateFromUrl(links,dates);

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


    //populate global date list with urls and dates
    public void createDateFromUrl(Elements urls, Elements dates){

        Deque<myDate> temp = new ArrayDeque<myDate>();
        Deque<String> dateHolder = new ArrayDeque<>();

        for(Element date : dates){
            String stringDate = date.toString().replaceAll("<.*pubDate>","");
            stringDate = stringDate.replaceAll("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]","");
            stringDate = stringDate.replaceAll("\\n","");
            stringDate = stringDate.replaceAll("\\+0000","");

            dateHolder.addLast(stringDate);
        }

            for(Element url : urls){
                //Pattern r = Pattern.compile(dayFromDate);
                //Matcher m = r.matcher(url.toString());
                //if(m.find()) {
                String Url = url.toString().replaceAll("<.*link>", "");
                Url = Url.replaceAll("\\n", "");
                Url = Url.replaceAll(" ", "");
                if( !Url.equals("https://dailyshotbrief.com")) {

                    //myDate m = myList.peek();
                    //if(m.getDirectoryName() != Url){
                        //push on to a temporary queue
                        myDate downloadedDate = new myDate();
                        downloadedDate.setDirectoryName(Url);
                        //temp.setDatetime(t);
                        downloadedDate.setDatetime( dateHolder.peek());
                        dateHolder.removeFirst();
                        temp.addLast(downloadedDate);

                }

            }


        //if there isn't a list, fill it up with the contents of the temp
        if(myList.isEmpty()){
            Iterator<myDate> it = temp.iterator();
            while(it.hasNext()){
                myList.addLast(it.next());
            }
        }else{
            //else push on to the head going from reverse order to keep list in order
            Iterator<myDate> reverseIt = temp.descendingIterator();
            while(reverseIt.hasNext()){
                for(myDate d : myList) {
                    if(reverseIt.next().getDirectoryName() != d.getDirectoryName()) {
                        myList.push(reverseIt.next());
                    }
                }
            }

        }
    }


    public static boolean notEmpty(String string) {
        if (string == null || string.length() == 0) {
            //throw new IllegalArgumentException("String must not be empty");
            return false;
        }else{
            return true;
        }
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



    //TODO: 'food for thought' section doesn't show all the images
    //=======================================================================================================
    //downloads the webpages and grabs images
    //writes html and images to files in directories formatted month + 0 + day + year
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp = "done";
        ProgressDialog progressDialog;
        String getWebsiteOut = "";
        String directoryName = "";


        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()


            try {
                //final Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
                Connection.Response con;
                if (notEmpty(params[0])) {
                    con = Jsoup.connect(params[0]).timeout(600000).execute();

                    Document doc = con.parse();
                    getWebsiteOut = "";

                    directoryName = params[1].replaceAll("/","");
                    directoryName = directoryName.replaceAll(":","");
                    directoryName = directoryName.replaceAll("\\.","");
                    String _path = getBaseContext().getFilesDir().toString() + "/" + directoryName;
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

                    if (!tempFile.exists()) {
                        tempFile.mkdir();
                    }

                    if (statusCode == 200) {
                        for (Element link : images) {
                            //get image url and data
                            String temp = link.attr("src");
                            Connection.Response resultImageResponse = Jsoup.connect(temp).timeout(60000).ignoreContentType(true).execute();

                            URL url = new URL(temp);
                            InputStream in = url.openStream();


                            //parse for file name and replace in html
                            Uri uri = Uri.parse(temp);
                            String imageName = uri.getLastPathSegment();
                            link.attr("src", imageName);


                            File file = new File(_path + "/" + imageName);

                            //if(!file.exists()) {
                            try {
                                OutputStream out = new BufferedOutputStream(new FileOutputStream(_path + "/" + imageName));
                                //FileOutputStream out = new FileOutputStream(new File(_path + "/" + imageName));
                                for (int b; (b = in.read()) != -1; ) {
                                    out.write(b);
                                    //out.write(resultImageResponse.bodyAsBytes());
                                }
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

                    System.out.println("LINK WORKED, WRITING HTML");
                }
                } catch(IOException e){
                    getWebsiteOut = "<!DOCTYPE HTML> " +
                            "<html>" +
                            "<meta name=\"viewport\" content='width=device-width, initial-scale=1.0,text/html,charset=utf-8' >";

                    getWebsiteOut += "website is not available. please choose another date";

                    getWebsiteOut += "</html>";
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("LINK DID NOT WORK, WRITING ERROR");

                    e.printStackTrace();
                }


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
            writeToFile(getWebsiteOut,getBaseContext(),directoryName);

            System.out.println("DO THAT 2:" +globalDate);

            result.loadDataWithBaseURL("file:///" + getBaseContext().getFilesDir().toString() + "/" +directoryName, getWebsiteOut, "text/html", "UTF-8", null);
        }

    }





    public static class myDate{
        private String date;
        private String directoryName = "";
        private boolean downloaded = false;

        public boolean isDownloaded() {
            return downloaded;
        }

        public void setDownloaded(boolean downloaded) {
            this.downloaded = downloaded;
        }

        public String getDatetime(){
            return date;
        }

        public void setDatetime(String datetime){
            this.date = datetime;
        }


        public String getDirectoryName(){
            return directoryName;
        }

        public void setDirectoryName(String directoryName){
            this.directoryName = directoryName;
        }

    }



}

