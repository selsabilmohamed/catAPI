package com.example.finalexam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private EditText breedInput;
    private TextView resultText;
    private ImageView catImage;
    private Button searchBtn;

    private final String API_KEY = "live_t0UyTv0a9xYIdLquZQnySXWdt55RTWLgPKnXN9qxGpO4QbbtOZjgknuKKaq82HCF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        breedInput = findViewById(R.id.breedInput);
        resultText = findViewById(R.id.resultText);
        catImage = findViewById(R.id.catImage);
        searchBtn = findViewById(R.id.searchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String breed = breedInput.getText().toString().trim();
                if (!breed.isEmpty()) {
                    new FetchCatDataTask().execute(breed);
                } else {
                    resultText.setText("Please enter a cat breed.");
                }
            }
        });
    }

    private class FetchCatDataTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String breedQuery = params[0];
            try {
                // 1. Get breed data
                URL breedUrl = new URL("https://api.thecatapi.com/v1/breeds/search?q=" + breedQuery);
                HttpURLConnection breedConn = (HttpURLConnection) breedUrl.openConnection();
                breedConn.setRequestProperty("x-api-key", API_KEY);

                Scanner scanner = new Scanner(breedConn.getInputStream());
                StringBuilder breedJson = new StringBuilder();
                while (scanner.hasNext()) breedJson.append(scanner.nextLine());
                scanner.close();

                JSONArray breedArray = new JSONArray(breedJson.toString());
                if (breedArray.length() == 0) return new String[]{"Breed not found"};

                JSONObject breedData = breedArray.getJSONObject(0);
                String breedId = breedData.getString("id");
                String description = breedData.getString("description");

                // 2. Get image
                URL imgUrl = new URL("https://api.thecatapi.com/v1/images/search?breed_ids=" + breedId);
                HttpURLConnection imgConn = (HttpURLConnection) imgUrl.openConnection();
                imgConn.setRequestProperty("x-api-key", API_KEY);

                Scanner imgScanner = new Scanner(imgConn.getInputStream());
                StringBuilder imgJson = new StringBuilder();
                while (imgScanner.hasNext()) imgJson.append(imgScanner.nextLine());
                imgScanner.close();

                JSONArray imgArray = new JSONArray(imgJson.toString());
                String imageUrl = imgArray.getJSONObject(0).getString("url");

                return new String[]{description, imageUrl};

            } catch (Exception e) {
                e.printStackTrace();
                return new String[]{"Error fetching data"};
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            resultText.setText(result[0]);
            if (result.length > 1) {
                new LoadImageTask().execute(result[1]);
            } else {
                catImage.setImageDrawable(null);
            }
        }
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap catBitmap = null;
            try {
                InputStream in = new URL(urlDisplay).openStream();
                catBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return catBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                catImage.setImageBitmap(result);
            }
        }
    }
}
