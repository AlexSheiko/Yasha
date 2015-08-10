package com.yasha.yasha;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yasha.yasha.adapter.UserAdapter;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private UserAdapter mUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchUsers(query);

            getSupportActionBar().setTitle(query);
        }

        mUserAdapter = new UserAdapter(this);

        ListView userList = (ListView) findViewById(R.id.user_listview);
        userList.setAdapter(mUserAdapter);
    }

    private void searchUsers(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                mUserAdapter.addAll(users);
            }
        });
    }
}
