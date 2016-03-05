package com.yashaapp.yasha;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (intent != null) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                searchUsers(query);

                getSupportActionBar().setTitle(query);
            } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri detailUri = intent.getData();
                String id = detailUri.getLastPathSegment();

                if (!id.equals(ParseUser.getCurrentUser().getObjectId())) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    currentUser.addUnique("watchList", id);
                    currentUser.saveEventually();
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putString("user_id_history", id).apply();
                startActivity(new Intent(this, HistoryActivity.class));
                finish();
            }
        }
    }

    private void searchUsers(final String username) {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    findViewById(R.id.loading).setVisibility(View.GONE);

                    if (users != null && users.size() > 0) {
                        findViewById(R.id.empty).setVisibility(View.GONE);
                        populateUsersLayout(users);
                    } else {
                        TextView emptyView = (TextView) findViewById(R.id.empty);
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("No users matches “" + username + "”");
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void populateUsersLayout(final List<ParseUser> users) {
        final LinearLayout container = (LinearLayout) findViewById(R.id.list_container);
        container.removeAllViews();

        bindUserInfo(users, container);


        List<String> visitedUsersIds = ParseUser.getCurrentUser().getList("watchList");
        if (visitedUsersIds == null) return;

        ParseQuery<ParseUser> visitedPeopleQuery = ParseUser.getQuery();
        visitedPeopleQuery.whereContainedIn("objectId", visitedUsersIds);
        visitedPeopleQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> visitedUsers, ParseException e) {
                if (e == null) {
                    if (visitedUsers.size() > 0) {
                        View headerView = LayoutInflater.from(SearchActivity.this)
                                .inflate(R.layout.visited_pages_header, null);
                        container.addView(headerView);
                    }

                    bindUserInfo(visitedUsers, container);
                }
            }
        });
    }

    private void bindUserInfo(List<ParseUser> users, LinearLayout container) {
        for (ParseUser user : users) {
            View convertView = LayoutInflater.from(this)
                    .inflate(R.layout.user_list_item, null);

            final TextView nameView = (TextView) convertView.findViewById(R.id.name_textview);
            final TextView cityView = (TextView) convertView.findViewById(R.id.city_textview);
            final ImageView avatarView = (ImageView) convertView.findViewById(R.id.avatar_imageview);

            final ParseUser selectedUser = user;
            nameView.setText(selectedUser.getUsername());
            cityView.setText(selectedUser.getString("city"));

            ParseFile avatarFile = selectedUser.getParseFile("avatar");
            if (avatarFile != null) {
                avatarFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        File tempFile = null;
                        try {
                            tempFile = File.createTempFile("abc", "cba", null);
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            fos.write(bytes);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        Picasso.with(SearchActivity.this)
                                .load(tempFile)
                                .fit().centerCrop()
                                .transform(new CircleTransform())
                                .into(avatarView);
                    }
                });
            } else {
                Picasso.with(this)
                        .load(R.drawable.avatar_placeholder)
                        .fit().centerCrop()
                        .transform(new CircleTransform())
                        .into(avatarView);
            }

            View.OnClickListener userClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SearchActivity.this);
                    prefs.edit().putString("user_id_history", selectedUser.getObjectId()).apply();

                    if (!selectedUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        currentUser.addUnique("watchList", selectedUser.getObjectId());
                        currentUser.saveEventually();
                    }

                    Intent intent = new Intent(SearchActivity.this, HistoryActivity.class);
                    startActivity(intent);
                }
            };
            convertView.setOnClickListener(userClickListener);

            container.addView(convertView);
        }
    }
}
