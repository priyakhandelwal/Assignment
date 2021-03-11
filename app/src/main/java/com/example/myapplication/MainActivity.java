package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    PostsAdapter postsAdapter;
    TextView timeTakenTextView;

    private static final String TAG = "ASSIGNMENT";

    private long postsTime = -1l;
    private long usersCountTime = -1l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        List<Post> posts = new ArrayList<>();
        postsAdapter = new PostsAdapter(posts, this);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(postsAdapter);

        timeTakenTextView = findViewById(R.id.timeTaken);

        final ExecutorService executor = Executors.newFixedThreadPool(3);

        final String postEndpoint = "https://jsonplaceholder.typicode.com/posts";
        final String userEndpoint = "https://jsonplaceholder.typicode.com/users";

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        trackLatchAndUpdateTimeTakenInProcessing(executor, countDownLatch);

        final Button startButton = findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getPosts(executor, postEndpoint, countDownLatch);
                countCharacters(executor, userEndpoint, countDownLatch);
            }
        });
    }

    private void trackLatchAndUpdateTimeTakenInProcessing(final Executor executor, final CountDownLatch countDownLatch) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    countDownLatch.await(10, TimeUnit.SECONDS);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, postsTime+usersCountTime + "");
                            timeTakenTextView.setText(Long.toString(postsTime+usersCountTime));
                        }
                    });
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void getPosts(final Executor executor, final String endpoint, final CountDownLatch countDownLatch) {
        Log.d(TAG, "startFetching");
        final List<Post> posts = new ArrayList<>();

        executor.execute(new Runnable() {

            @Override
            public void run() {
                final Handler handler = new Handler(Looper.getMainLooper());

                try {
                    final long startTime = System.currentTimeMillis();
                    final String webResponse = fetchWebResponse(endpoint);

                    if (webResponse == null) {
                        Log.e(TAG, "Received null web response. Ignoring..");
                        return;
                    }

                    JSONArray result = new JSONArray(webResponse);

                    for (int i = 0; i < result.length(); i++) {
                        Post post = convertToPost((JSONObject) result.get(i));
                        posts.add(post);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Posting to adapter");
                            postsAdapter.addAll(posts);
                            final long endTime = System.currentTimeMillis();
                            postsTime = endTime-startTime;
                            countDownLatch.countDown();
                        }
                    });
                } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                }

            }
        });
    }

    private void  countCharacters(final Executor executor, final String endpoint, final CountDownLatch countDownLatch) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final long startTime = System.currentTimeMillis();
                    final String webResponse = fetchWebResponse(endpoint);
                    if (webResponse == null) {
                        Log.e(TAG, "Received null web response. Ignoring..");
                        return;
                    }

                    final String responseWithoutNewLines = webResponse.replaceAll("\\n", "");

                    Log.i(TAG, "Number of characters = " + responseWithoutNewLines.length());
                    final long endTime = System.currentTimeMillis();
                    usersCountTime = endTime-startTime;
                    countDownLatch.countDown();

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

    }

    private String fetchWebResponse(final String endpoint) {
        try {
            URL url = new URL(endpoint);

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.connect();
            InputStream inputStream = connection.getInputStream();
            return readStream(inputStream);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    private String readStream(final InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    private Post convertToPost(final JSONObject jsonObject) {
        Gson gson = new Gson();
        Post post = gson.fromJson(jsonObject.toString(), Post.class);
        return post;
    }
}


