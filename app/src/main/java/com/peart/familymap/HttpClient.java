package com.peart.familymap;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.EventResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

public class HttpClient {
    // request methods
    public static final String POST_METHOD = "POST";
    public static final String GET_METHOD = "GET";

    public LoginResult signIn(URL url, String username, String password) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(POST_METHOD);

            // Writing the data to the HTTP request body
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            Gson gson = new Gson();
            LoginRequest loginRequest = new LoginRequest(username, password);
            gson.toJson(loginRequest);
            bufferedWriter.write(gson.toJson(loginRequest));
            bufferedWriter.close();

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader
                        (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                }
                return gson.fromJson(textBuilder.toString(), LoginResult.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LoginResult(null, null, null, null, false);
    }

    public RegisterResult register(URL url, String username, String password, String first, String last, String mail, String gender) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(GET_METHOD);

            // Writing the data to the HTTP request body
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            Gson gson = new Gson();
            RegisterRequest registerRequest = new RegisterRequest(username, password, mail, first, last, gender);
            gson.toJson(registerRequest);
            bufferedWriter.write(gson.toJson(registerRequest));
            bufferedWriter.close();

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader
                        (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                }
                return gson.fromJson(textBuilder.toString(), RegisterResult.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RegisterResult(null, null, null, null, false);
    }

    public PersonResult person(URL url, String authtoken) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(GET_METHOD);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", authtoken);

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader
                        (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                }
                Gson gson = new Gson();
                return gson.fromJson(textBuilder.toString(), PersonResult.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public EventResult event(URL url, String authtoken) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(GET_METHOD);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", authtoken);

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader
                        (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                }
                Gson gson = new Gson();
                return gson.fromJson(textBuilder.toString(), EventResult.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
