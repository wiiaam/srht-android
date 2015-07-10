package io.github.wiiam.srhtapp.config;

import org.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by william on 9/07/15.
 */
public class Config {
    private static File jsonFile;
    private static JSONObject json;
    public static void load(File file) throws JSONException, IOException {
        jsonFile = file;
        if(!file.exists()){
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file);
            writer.println("{ \"apikey\":\"\",\"url\":\"sr.ht\" }");
            writer.close();
        }
        Scanner scan;
        scan = new Scanner(new FileInputStream(file));
        String jsonString = "";
        while(scan.hasNext()){
            jsonString += scan.next() + " ";
        }
        scan.close();
        json = (JSONObject) new JSONTokener(jsonString).nextValue();
    }

    public static String getApiKey() {
        if (!json.has("apikey")) {
            try {
                json.put("apikey","");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            return json.getString("apikey");
        } catch (JSONException e) {
            return "";
        }
    }

    public static void setApiKey(String apiKey) throws NullPointerException{
        try {
            json.put("apikey",apiKey);
            write();
        } catch (JSONException e) {
        }
    }

    public static String getUrl() {
        if (!json.has("url")) {
            try {
                json.put("url","");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            String url =json.getString("url");
            if(url.equals("")) setUrl("sr.ht");
            return json.getString("url");
        } catch (JSONException e) {
            return "";
        }
    }

    public static void setUrl(String url) throws NullPointerException{
        try {
            json.put("url",url);
            write();
        } catch (JSONException e) {
        }
    }

    private static void write(){
        try {
            PrintWriter writer = new PrintWriter(jsonFile);
            writer.println(json.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
