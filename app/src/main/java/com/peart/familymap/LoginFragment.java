package com.peart.familymap;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Result.EventResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

public class LoginFragment extends Fragment {

    // definitions
    private static final String SUCCESS_KEY = "SUCCESS";
    private static final String PERSON_DATA_KEY = "PERSON_DATA_KEY";
    private static final String EVENT_DATA_KEY = "EVENT_DATA_KEY";
    private static final String AUTHTOKEN_KEY = "AUTHTOKEN_KEY";
    private static final String PERSON_ID_KEY = "PERSON_ID_KEY";

    // data
    private DataCache dataCache;

    // buttons
    private Button registerButton, signInButton;

    // edit texts
    private EditText serverHost, serverPort, username, password, firstName, lastName, email;

    // radio buttons
    private RadioGroup radioGroup;

    // data members
    private String gender, host, port, user, pwd, first, last, mail;

    // results
    private LoginResult loginResult;
    private RegisterResult registerResult;

    // listener
    private Listener listener;

    public interface Listener {
        void notifyDone();
    }

    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    public LoginFragment() { } // required

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // data
        dataCache = DataCache.getInstance();

        // buttons
        registerButton = view.findViewById(R.id.registerButton);
        signInButton = view.findViewById(R.id.signInButton);
        registerButton.setEnabled(false);
        signInButton.setEnabled(false);

        // edit texts
        serverHost = view.findViewById(R.id.serverHostField);
        serverPort = view.findViewById(R.id.serverPortField);
        username = view.findViewById(R.id.usernameField);
        password = view.findViewById(R.id.passwordField);
        firstName = view.findViewById(R.id.firstNameField);
        lastName = view.findViewById(R.id.lastNameField);
        email = view.findViewById(R.id.emailField);

        // radio group
        radioGroup = view.findViewById(R.id.radioButtonGroup);

        // listeners
        serverHost.addTextChangedListener(textChangedListener);
        serverPort.addTextChangedListener(textChangedListener);
        username.addTextChangedListener(textChangedListener);
        password.addTextChangedListener(textChangedListener);
        firstName.addTextChangedListener(textChangedListener);
        lastName.addTextChangedListener(textChangedListener);
        email.addTextChangedListener(textChangedListener);

        registerButton.setOnClickListener(v -> {
            try {
                @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        boolean success = bundle.getBoolean(SUCCESS_KEY, false);
                        if (success) {
                            String token = bundle.getString(AUTHTOKEN_KEY);
                            dataCache.setRoodID(bundle.getString(PERSON_ID_KEY));
                            getPeople(token);
                            getEvents(token);
                            // start map activity
                            listener.notifyDone();
                        } else {
                            Toast.makeText(getActivity(), "ERROR: registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                URL url = new URL("http://" + host + ":" + port + "/user/register");
                RegisterTask task = new RegisterTask(handler, url);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(task);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        signInButton.setOnClickListener(v -> {
            try {
                @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        boolean success = bundle.getBoolean(SUCCESS_KEY, false);
                        if (success) {
                            String token = bundle.getString(AUTHTOKEN_KEY);
                            dataCache.setRoodID(bundle.getString(PERSON_ID_KEY));
                            getPeople(token);
                            getEvents(token);
                            // start map activity
                            listener.notifyDone();
                        } else {
                            Toast.makeText(getActivity(), "ERROR: login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                URL url = new URL("http://" + host + ":" + port + "/user/login");
                LoginTask task = new LoginTask(handler, url);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(task);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            gender = "f";
            if (checkedId == 0) {
                gender = "m";
            }
            tryEnableButtons();
        });

        return view;
    }

    TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            host = serverHost.getText().toString().trim();
            port = serverPort.getText().toString().trim();
            user = username.getText().toString().trim();
            pwd = password.getText().toString().trim();
            first = firstName.getText().toString().trim();
            last = lastName.getText().toString().trim();
            mail = email.getText().toString().trim();
            tryEnableButtons();
        }
        @Override
        public void afterTextChanged(Editable s) {}
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    };

    private void tryEnableButtons() {
        registerButton.setEnabled(!host.isEmpty() && !port.isEmpty() && !user.isEmpty() && !pwd.isEmpty() && !first.isEmpty() && !last.isEmpty() && !mail.isEmpty() && gender != null);
        signInButton.setEnabled(!host.isEmpty() && !port.isEmpty() && !user.isEmpty() && !pwd.isEmpty());
    }

    public void getPeople(String authtoken) {
        try {
            @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    if (!bundle.getBoolean(SUCCESS_KEY, false)) {
                        Toast.makeText(getActivity(), "FAILED: NO PERSON FOUND", Toast.LENGTH_SHORT).show();
                    } else {
                        Gson gson = new Gson();
                        PersonResult result = gson.fromJson(bundle.getString(PERSON_DATA_KEY), PersonResult.class);
                        dataCache.addPeople(result.getData());
                    }
                }
            };
            URL url = new URL("http://" + host + ":" + port + "/person");
            PersonTask task = new PersonTask(handler, url, authtoken);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(task);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void getEvents(String authtoken) {
        try {
            @SuppressLint("HandlerLeak") Handler handler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    if (!bundle.getBoolean(SUCCESS_KEY, false)) {
                        Toast.makeText(getActivity(), "FAILED: NO EVENTS FOUND", Toast.LENGTH_SHORT).show();
                    } else {
                        Gson gson = new Gson();
                        EventResult result = gson.fromJson(bundle.getString(EVENT_DATA_KEY), EventResult.class);
                        dataCache.addEvent(result.getData());
                    }
                }
            };
            URL url = new URL("http://" + host + ":" + port + "/event");
            EventTask task = new EventTask(handler, url, authtoken);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(task);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class LoginTask implements Runnable {

        private final URL url;
        private final Handler handler;

        public LoginTask(Handler handler, URL url) {
            this.url = url;
            this.handler = handler;
        }

        @Override
        public void run() {
            HttpClient httpClient = new HttpClient();
            loginResult = httpClient.signIn(url, user, pwd);
            sendMessage(loginResult.isSuccess(), loginResult.getPersonID(), loginResult.getAuthtoken());
        }

        private void sendMessage(Boolean success, String personID, String authtoken) {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SUCCESS_KEY, success);
            bundle.putString(PERSON_ID_KEY, personID);
            bundle.putString(AUTHTOKEN_KEY, authtoken);
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    private class RegisterTask implements Runnable {
        private final URL url;
        private final Handler handler;
        public RegisterTask(Handler handler, URL url) {
            this.url = url;
            this.handler = handler;
        }

        @Override
        public void run() {
            HttpClient httpClient = new HttpClient();
            registerResult = httpClient.register(url, user, pwd, first, last, mail, gender);
            sendMessage(registerResult.isSuccess(), registerResult.getPersonID(), registerResult.getAuthtoken());
        }

        private void sendMessage(Boolean success, String personID, String authtoken) {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SUCCESS_KEY, success);
            bundle.putString(PERSON_ID_KEY, personID);
            bundle.putString(AUTHTOKEN_KEY, authtoken);
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    private static class PersonTask implements Runnable {
            private final URL url;
            private final Handler handler;
            private final String authtoken;
            public PersonTask(Handler handler, URL url, String authtoken) {
                this.url = url;
                this.handler = handler;
                this.authtoken = authtoken;
            }

            @Override
            public void run() {
                HttpClient httpClient = new HttpClient();
                PersonResult personResult = httpClient.person(url, authtoken);
                sendMessage(personResult);
            }

            private void sendMessage(PersonResult personResult) {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putBoolean(SUCCESS_KEY, personResult.isSuccess());
                Gson gson = new Gson();
                String personString = gson.toJson(personResult);
                bundle.putString(PERSON_DATA_KEY, personString);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }

    private static class EventTask implements Runnable {
        private final URL url;
        private final Handler handler;
        private final String authtoken;
        public EventTask(Handler handler, URL url, String authtoken) {
            this.url = url;
            this.handler = handler;
            this.authtoken = authtoken;
        }

        @Override
        public void run() {
            HttpClient httpClient = new HttpClient();
            EventResult eventResult = httpClient.event(url, authtoken);
            sendMessage(eventResult);
        }

        private void sendMessage(EventResult eventResult) {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SUCCESS_KEY, eventResult.isSuccess());
            Gson gson = new Gson();
            String eventString = gson.toJson(eventResult);
            bundle.putString(EVENT_DATA_KEY, eventString);
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }
}