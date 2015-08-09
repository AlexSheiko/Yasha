package com.yasha.yasha;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class CommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        findViewById(R.id.date_textview).setVisibility(View.GONE);
        findViewById(R.id.category_textview).setVisibility(View.GONE);
        findViewById(R.id.messages_counter).setVisibility(View.GONE);
        findViewById(R.id.button_more).setVisibility(View.GONE);

        String postId = getIntent().getStringExtra("postId");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.include("author");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.getInBackground(postId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject post, ParseException e) {
                if (e == null) {
                    ParseUser author = post.getParseUser("author");

                    TextView authorView = (TextView) findViewById(R.id.author_textview);
                    TextView messageView = (TextView) findViewById(R.id.message_textview);

                    messageView.setText(post.getString("message"));
                    authorView.setText(author.getUsername());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
