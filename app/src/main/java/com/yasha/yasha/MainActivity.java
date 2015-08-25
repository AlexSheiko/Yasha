package com.yasha.yasha;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yasha.yasha.adapters.PostAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PostAdapter mPostAdapter;
    private Toolbar mToolbar;
    private int mUnreadComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        ListView postList = (ListView) findViewById(R.id.post_list);
        mPostAdapter = new PostAdapter(this, postList);
        postList.setAdapter(mPostAdapter);


        View postButton = findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostActivity.class));
            }
        });
    }

    private void showUnreadComments() {
        ParseQuery<ParseObject> query = new ParseQuery<>("Post");
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> posts, ParseException e) {
                if (e == null) {
                    mUnreadComments = 0;
                    for (final ParseObject post : posts) {
                        ParseQuery<ParseObject> commentsQuery = new ParseQuery<>("Comment");
                        commentsQuery.whereEqualTo("post", post);
                        commentsQuery.whereNotEqualTo("author", ParseUser.getCurrentUser());
                        commentsQuery.whereEqualTo("readByAuthor", false);
                        commentsQuery.countInBackground(new CountCallback() {
                            @Override
                            public void done(int count, ParseException e) {
                                if (e == null) {
                                    mUnreadComments += count;

                                    TextView unreadView = (TextView) mToolbar.findViewById(R.id.unread_textview);
                                    if (mUnreadComments == 0) {
                                        unreadView.setVisibility(View.GONE);
                                    } else {
                                        unreadView.setVisibility(View.VISIBLE);
                                        unreadView.setText(mUnreadComments + "");
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.empty).setVisibility(View.GONE);
        findViewById(R.id.loading).setVisibility(View.VISIBLE);

        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.orderByDescending("createdAt");
        query.include("author");

        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject user, ParseException e) {
                if (e == null) {
                    if (user.getString("city") != null) {
                        query.whereEqualTo("city", user.getString("city"));
                    } else {
                        query.whereEqualTo("city", "No city");
                    }
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> posts, ParseException e) {
                            findViewById(R.id.loading).setVisibility(View.GONE);
                            if (e == null) {
                                if (posts.size() == 0) {
                                    TextView emptyView = (TextView) findViewById(R.id.empty);
                                    emptyView.setVisibility(View.VISIBLE);
                                    String userCity = ParseUser.getCurrentUser().getString("city").split(",")[0];
                                    if (!userCity.equals("No city")) {
                                        emptyView.setText("Be the first to post anything in " + userCity + "!\n" +
                                                "Tap pencil icon to create a message");
                                    } else {
                                        emptyView.setText("No posts. Be first to add a new message!");
                                    }
                                    return;
                                }
                                mPostAdapter.clear();
                                mPostAdapter.addAll(posts);
                            }
                        }
                    });
                } else if (e.getMessage().contains("invalid session token")) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    Toast.makeText(MainActivity.this, "Session was expired. Please login again", Toast.LENGTH_LONG).show();
                }
            }
        });

        showUnreadComments();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setPadding(dpTpPixels(12), searchView.getPaddingTop(), searchView.getPaddingRight(), searchView.getPaddingBottom());
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.title_textview).setVisibility(View.INVISIBLE);
                findViewById(R.id.post_button).setVisibility(View.GONE);
                findViewById(R.id.unread_textview).setVisibility(View.GONE);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                findViewById(R.id.title_textview).setVisibility(View.VISIBLE);
                findViewById(R.id.post_button).setVisibility(View.VISIBLE);
                showUnreadComments();
                return false;
            }
        });

        return true;
    }

    private int dpTpPixels(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
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
