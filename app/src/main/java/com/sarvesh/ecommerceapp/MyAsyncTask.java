package com.sarvesh.ecommerceapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
    String searchText;
    Activity activity;
    private static final String ACCESS_KEY_ID = "AKIAJLTQV6VXB2YBHWUA";
    private static final String SECRET_KEY = "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";
    private static final String ENDPOINT = "webservices.amazon.in";
    ArrayList<HashMap<String, String>> list;
    ArrayList<Bitmap> bitmaps;
    HttpURLConnection httpURLConnection;;
    InputStream inputStream;
    Boolean executeSuccesfully = false;


    MyAsyncTask(Activity activity, String searchText) {
        this.activity = activity;
        this.searchText = searchText;
    }

    public void onAttach(Context context) {
        this.activity = (Activity) context;
       if(list!=null && bitmaps!=null){
           ((MainActivity)activity).updateArrayList(list,bitmaps);
       }
    }

    public void onDetach() {
        activity = null;
    }

    @Override
    protected Boolean doInBackground(String... objects) {

        /* SignedRequestsHelper class i have took form amazon doc's
           which will convert our unsigned url to signed url
         */
        SignedRequestsHelper helper;
        String requestUrl = null;
        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, ACCESS_KEY_ID, SECRET_KEY);

            Map<String, String> params = new HashMap<String, String>();
            params.put("Service", "AWSECommerceService");
            params.put("Operation", "ItemSearch");
            params.put("AWSAccessKeyId", "AKIAJLTQV6VXB2YBHWUA");
            params.put("AssociateTag", "sarvesh0a-21");
            params.put("SearchIndex", "All");
            params.put("ResponseGroup", "Images,ItemAttributes");
            params.put("Keywords", searchText);
            //It will return us the signed url
            requestUrl = helper.sign(params);
        } catch (Exception e) {
            e.printStackTrace();
        }



        try {
            URL url = new URL(requestUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            inputStream = httpURLConnection.getInputStream();
            processXml(inputStream);
            executeSuccesfully=true;
        } catch (Exception e) {
            e.printStackTrace();
            executeSuccesfully=false;

        }

        //Close the connection
        finally {
           if(httpURLConnection!=null){
               httpURLConnection.disconnect();
           }
          if(inputStream!=null){
              try {
                  inputStream.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {


    }

    public void processXml(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {

        //I have used DOM parsing here since amazon response was in xml format

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = documentBuilder.parse(inputStream);
        Element rootElement = xmlDocument.getDocumentElement();

        NodeList ItemTag = rootElement.getElementsByTagName("Item");
        NodeList childOfItemTag = null;
        NodeList childOfsmallImage = null;

        int count = 0;
        list = new ArrayList<>();

        //I have create a hashmap and added it to a list and will return it to the main thread
        //each hasmap new instance will have a value for title,price and url

        for (int i = 0; i < ItemTag.getLength(); i++) {

            childOfItemTag = ItemTag.item(i).getChildNodes();
            for (int j = 0; j < childOfItemTag.getLength(); j++) {
                if (childOfItemTag.item(j).getNodeName().equals("SmallImage")) {
                    childOfsmallImage = childOfItemTag.item(j).getChildNodes();
                    for (int k = 0; k < childOfsmallImage.getLength(); k++) {
                        if (k == 0) {

                            HashMap<String, String> hashMap = new HashMap<>();

                            NodeList nodeListBrand = rootElement.getElementsByTagName("Brand");
                            NodeList nodeListModel = rootElement.getElementsByTagName("Model");
                            Log.d("something", nodeListBrand.item(count).getTextContent() + " " + nodeListModel.item(count).getTextContent());
                            hashMap.put("Title", nodeListBrand.item(count).getTextContent() + " " + nodeListModel.item(count).getTextContent());

                            NodeList nodeListFormatedPrice = rootElement.getElementsByTagName("FormattedPrice");
                            Log.d("something", nodeListFormatedPrice.item(count).getTextContent());
                            hashMap.put("FormattedPrice", nodeListFormatedPrice.item(count).getTextContent());

                            Log.d("something", childOfsmallImage.item(k).getTextContent());
                            hashMap.put("URL", childOfsmallImage.item(k).getTextContent());

                            count++;

                            list.add(hashMap);
                        }
                    }
                }
            }
        }

        //so in hashmap we also have url for images
        //which i have downloaded here it self since we cannot perform any network
        //operation on main thread
        //i have passed the bitmaps array to main thread inside onPostExecute()
        bitmaps = new ArrayList<>();
        Bitmap bmp = null;
        for (int i = 0; i < list.size(); i++) {
            URL url = new URL(list.get(i).get("URL"));
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            bitmaps.add(bmp);
        }


    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
       if(activity!=null){
           if(executeSuccesfully ){
               ((MainActivity) activity).initializeListView(list, bitmaps);
           }else {
               ((MainActivity) activity).showToast();
           }
       }
    }
}