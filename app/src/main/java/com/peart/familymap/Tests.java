package com.peart.familymap;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;

import Request.LoginRequest;
import Result.EventResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

/*
 * Truly these are the worst test cases I have ever created.
 * Please avert your eyes lol
 */

public class Tests {

    private HttpClient httpClient = new HttpClient();
    private URL login_url = new URL("http://" + "localhost" + ":" + "8080" + "/user/login");
    private URL register_url = new URL("http://" + "localhost" + ":" + "8080" + "/user/register");
    private URL person_url = new URL("http://" + "localhost" + ":" + "8080" + "/person");

    public Tests() throws MalformedURLException {
    }

    @Test
    public void testLoginPass() {
        LoginResult result = httpClient.signIn(login_url, "sheila", "parker");
        Assert.assertEquals(result.isSuccess(), true);
    }

    @Test
    public void testLoginFail() {
        LoginResult result = httpClient.signIn(login_url, "sheila", "parker");
        Assert.assertNotEquals(result.isSuccess(), false);
    }

    @Test
    public void registerUserPass() {
        RegisterResult registerResult = httpClient.register(register_url, "bob", "bob", "bob", "bob", "bob", "m");
        Assert.assertNotEquals(registerResult.isSuccess(), true);
    }

    @Test
    public void registerUserFail() {
        RegisterResult registerResult = httpClient.register(register_url, "bob", "bob", "bob", "bob", "bob", "m");
        Assert.assertFalse(registerResult.isSuccess());
    }

    @Test
    public void testPersonPass() {
        PersonResult personResult = httpClient.person(person_url, "ae8efb6d-ff9b-4a96-b407-e8a17353d3c5"); // might have to change this if you want to run it...
        Assert.assertNotEquals(personResult.getData().size(), 0);
    }

    @Test
    public void testPersonFail() {
        PersonResult personResult = httpClient.person(person_url, "nothing");
        Assert.assertNull(personResult);
    }

    @Test
    public void testEventPass() {
        EventResult result = httpClient.event(person_url, "ae8efb6d-ff9b-4a96-b407-e8a17353d3c5");
        Assert.assertNotEquals(result.getData().length, 0);
    }

    @Test
    public void testEventFail() {
        EventResult result = httpClient.event(person_url, "nothing");
        Assert.assertNull(result);
    }


}
