package com.yasha.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yasha.yasha.adapter.PostAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PostAdapter mPostAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mPostAdapter = new PostAdapter(this);

        ListView messagesList = (ListView) findViewById(R.id.messages_list);
        messagesList.setAdapter(mPostAdapter);


        View postButton = findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.orderByDescending("createdAt");
        query.include("author");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        try {
            ParseUser user = ParseUser.getCurrentUser();
            user.fetch();
            if (user.getString("city") != null) {
                query.whereEqualTo("city", user.getString("city"));
            } else {
                query.whereEqualTo("city", "No city");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> posts, ParseException e) {
                if (e == null) {
                    mPostAdapter.clear();
                    mPostAdapter.addAll(posts);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
