package cz.matousek.zoo;


import android.annotation.TargetApi;
import android.graphics.Region;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.*;

import cz.matousek.zoo.entity.Animal;

public class MainActivity extends AppCompatActivity {
    private Client client  = null;
    private static final String IP_ADDRESS = "192.168.0.46";
    private static final int PORT = 8080;
    private static final String SERVER_ADDRESS_FORMAT = "http://%s:%s/zoo/";
    private Button sendButton;
    private Spinner spinner;
    private String serverAddress = String.format(SERVER_ADDRESS_FORMAT, IP_ADDRESS, PORT);
    private List<String> options = new ArrayList<>();
    private TextView textView;
    private EditText nameAnimal;
    private EditText idAnimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idAnimal = (EditText) findViewById(R.id.idAnimal);
        nameAnimal = (EditText) findViewById(R.id.nameAnimal);
        textView = (TextView) findViewById(R.id.textView);
        sendButton = (Button) findViewById(R.id.sendButton);
        spinner = (Spinner) findViewById(R.id.animals);
        createOptions();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
        spinner.setAdapter(dataAdapter);
        //default selection
        spinner.setSelection(0);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.CUPCAKE)
            @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
            @Override
            public void onClick(View v) {
                switch (spinner.getSelectedItem().toString()){
                    case "GET": listAll();
                        break;

                    case "POST": add(nameAnimal.getText().toString());
                        break;

                    case "PUT":
                        String name = nameAnimal.getText().toString();
                        update(Integer.parseInt(idAnimal.getText().toString()), name);
                        break;
                    case "DELETE": remove(Integer.parseInt(idAnimal.getText().toString()));
                        break;
                }
            }
        });
    }

    private void createOptions(){
        options.add(Operation.GET.name());
        options.add(Operation.POST.name());
        options.add(Operation.PUT.name());
        options.add(Operation.DELETE.name());
    }
    private void listAll(){
        String response = sendRequestWithExpectedResponse(Operation.GET.name(), Operation.GET.getUrl(), null);
        textView.setText(response);
    }

    private void update(int id, String name){
        String response = sendRequestWithExpectedResponse(Operation.PUT.name(), Operation.PUT.getUrl() + id, new Animal(id, name));
        textView.setText(response);
    }

    private void add(String name){
        String response = sendRequestWithExpectedResponse(Operation.POST.name(), Operation.POST.getUrl(), new Animal(-1, name));
        textView.setText(response);
    }

    private void remove(int id){
        String response = sendRequestWithExpectedResponse(Operation.DELETE.name(), Operation.DELETE.getUrl()+ id, null);
        textView.setText(response);
    }

    private void sendRequest(String httpRequestType, String url){
        client = new Client();
        client.execute(serverAddress + url, httpRequestType);
    }

    private String sendRequestWithExpectedResponse(String httpRequestType, String url, Animal animal){
        String body= "";
        JSONObject data = new JSONObject();
        ObjectMapper om = new ObjectMapper();
        ObjectNode on = om.createObjectNode();
        if(animal != null){
            on.put("id", animal.getId());
            on.put("name", animal.getName());
            body = on.toString();
        }

        String response = "";
        try {
            client = new Client();
            response = client.execute(serverAddress + url, httpRequestType, body).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    private class Client extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            URL url;
            String message = null;
            try {

                url = new URL(strings[0]);
                connection = (HttpURLConnection)url.openConnection();


                connection.setRequestMethod(strings[1]);
                if(!strings[2].isEmpty()){
                    //Send reques
                    /**
                    connection.setDoInput(true);
                    byte[] data = strings[2].getBytes( StandardCharsets.UTF_8 );
                    connection.setRequestProperty( "charset", "utf-8");
                    connection.setRequestProperty( "Content-Length", Integer.toString(data.length));
                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream ());
                    wr.write(data);
                    wr.flush();
                    wr.close ();
                     **/

                    connection.setDoOutput(true);
                    connection.setFixedLengthStreamingMode(
                            strings[2].getBytes().length);
                    connection.setRequestProperty("Content-Type",
                            "application/json");
                    connection.setRequestProperty("cache-control", "no-cache");
                    //send the POST out
                    OutputStreamWriter wr = new OutputStreamWriter(
                            connection.getOutputStream ());
                    wr.write(strings[2]);
                    wr.flush();
                    wr.close ();
                }
               // connection.connect();

                InputStream in = connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                message = out.toString();
                out.setLength(0);
                in.close();
                reader.close();



            } catch (Exception e) {

                e.printStackTrace();
                System.out.print(e.toString());
            } finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
            return message;
        }
    }

    private List<Animal> parseJsonArrayToObjects(String jsonArray){
        List<Animal> animals = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(jsonArray);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                animals.add(parseJsonToObject(jsonObject.toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return animals;
    }
    private Animal parseJsonToObject(String jsonObject) {
        ObjectMapper om = new ObjectMapper();
        Animal animal = null;
        try {
            animal = om.readValue(jsonObject, Animal.class);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return animal;
    }
}
