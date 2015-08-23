package com.yasha.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yasha.yasha.adapters.CommentAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommentActivity extends AppCompatActivity {

    private ParseObject mPost;

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
        query.include("comments");
        query.getInBackground(postId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject post, ParseException e) {
                if (e == null) {
                    mPost = post;
                    showPostContents();
                    showCommentList();
                }
            }
        });
    }

    private void showPostContents() {
        final ParseUser author = mPost.getParseUser("author");

        final TextView authorView = (TextView) findViewById(R.id.author_textview);
        final TextView messageView = (TextView) findViewById(R.id.message_textview);
        final ImageView avatarView = (ImageView) findViewById(R.id.avatar_imageview);
        final TextView dateView = (TextView) findViewById(R.id.date_textview);

        ParseFile avatarFile = author.getParseFile("avatar");
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

                    Picasso.with(CommentActivity.this)
                            .load(tempFile)
                            .fit().centerCrop()
                            .transform(new CircleTransform())
                            .into(avatarView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    messageView.setText(mPost.getString("message"));
                                    messageView.setMaxLines(Integer.MAX_VALUE);
                                    authorView.setText(author.getUsername());

                                    dateView.setVisibility(View.VISIBLE);
                                    dateView.setText(formatDate(mPost.getCreatedAt()));
                                }

                                @Override
                                public void onError() {
                                }
                            });
                }
            });
        } else {
            Picasso.with(this)
                    .load(R.drawable.avatar_placeholder)
                    .fit().centerCrop()
                    .transform(new CircleTransform())
                    .into(avatarView, new Callback() {
                        @Override
                        public void onSuccess() {
                            messageView.setText(mPost.getString("message"));
                            messageView.setMaxLines(Integer.MAX_VALUE);
                            authorView.setText(author.getUsername());

                            dateView.setVisibility(View.VISIBLE);
                            dateView.setText(formatDate(mPost.getCreatedAt()));
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }
    }

    private void showCommentList() {
        ListView commentList = (ListView) findViewById(R.id.comment_list);
        final CommentAdapter adapter = new CommentAdapter(this, commentList);
        commentList.setAdapter(adapter);

        ParseQuery<ParseObject> query = new ParseQuery<>("Comment");
        query.whereEqualTo("post", mPost);
        query.orderByAscending("createdAt");
        query.include("author");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> comments, ParseException e) {
                if (e == null) {
                    adapter.clear();
                    adapter.addAll(comments);

                    if (mPost.getParseUser("author").getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                        markAsRead(comments);
                    }
                }
            }
        });
    }

    private void markAsRead(List<ParseObject> comments) {
        for (final ParseObject comment : comments) {
            if (!comment.getBoolean("readByAuthor")) {
                comment.put("readByAuthor", true);
                comment.saveEventually();
            }
        }
    }

    public void onClickSend(View view) {
        EditText messageField = (EditText) findViewById(R.id.message_field);
        String message = messageField.getText().toString().trim();

        if (message.isEmpty()) {
            messageField.setError("Enter your message");
            return;
        }

        ParseUser user = ParseUser.getCurrentUser();
        ParseObject comment = new ParseObject("Comment");

        comment.put("author", user);
        comment.put("message", message);
        comment.put("post", mPost);
        comment.put("readByAuthor", false);

        comment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // refresh page
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(CommentActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String formatDate(Date date) {
        if (isToday(date)) {
            DateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            return dateFormat.format(date);
        } else if (isYesterday(date)) {
            return "Yesterday";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.US);
            return dateFormat.format(date);
        }
    }

    public static boolean isToday(Date date1) {
        return isSameDay(date1, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
    }

    public static boolean isYesterday(Date date1) {
        Date date2 = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        return isSameDay(date1, date2);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
