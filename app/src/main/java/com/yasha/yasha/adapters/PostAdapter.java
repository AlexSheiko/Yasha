package com.yasha.yasha.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yasha.yasha.CircleTransform;
import com.yasha.yasha.CommentActivity;
import com.yasha.yasha.EmailClient;
import com.yasha.yasha.HistoryActivity;
import com.yasha.yasha.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PostAdapter extends ArrayAdapter<ParseObject> {

    private ListView mListView;
    private boolean mHistorySection = false;

    public PostAdapter(Context context, ListView listView) {
        super(context, 0);
        mListView = listView;
    }

    public PostAdapter(Context context, boolean historySection, ListView listView) {
        super(context, 0);
        mHistorySection = historySection;
        mListView = listView;
    }

    @Override
    public void addAll(Collection<? extends ParseObject> collection) {
        mListView.setVisibility(View.INVISIBLE);
        super.addAll(collection);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.post_list_item, parent, false);

        final TextView authorView = (TextView) convertView.findViewById(R.id.author_textview);
        final ImageView avatarView = (ImageView) convertView.findViewById(R.id.avatar_imageview);
        final TextView messageView = (TextView) convertView.findViewById(R.id.message_textview);
        final TextView dateView = (TextView) convertView.findViewById(R.id.date_textview);
        final TextView categoryView = (TextView) convertView.findViewById(R.id.category_textview);
        final TextView counterView = (TextView) convertView.findViewById(R.id.messages_counter);
        final View buttonMore = convertView.findViewById(R.id.button_more);

        final ParseObject post = getItem(position);
        final ParseUser author = post.getParseUser("author");

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

                    Picasso.with(getContext())
                            .load(tempFile)
                            .fit().centerCrop()
                            .transform(new CircleTransform())
                            .into(avatarView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    messageView.setText(post.getString("message"));
                                    dateView.setText(formatDate(post.getCreatedAt()));
                                    categoryView.setText(post.getString("category"));

                                    final ParseUser author = post.getParseUser("author");
                                    authorView.setText(author.getUsername());

                                    categoryView.setVisibility(View.VISIBLE);
                                    counterView.setVisibility(View.VISIBLE);
                                    buttonMore.setVisibility(View.VISIBLE);

                                    if (position == mListView.getLastVisiblePosition()) {
                                        mListView.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onError() {
                                }
                            });
                }
            });
        } else {
            Picasso.with(getContext())
                    .load(R.drawable.avatar_placeholder)
                    .fit().centerCrop()
                    .transform(new CircleTransform())
                    .into(avatarView, new Callback() {
                        @Override
                        public void onSuccess() {
                            messageView.setText(post.getString("message"));
                            dateView.setText(formatDate(post.getCreatedAt()));
                            categoryView.setText(post.getString("category"));

                            final ParseUser author = post.getParseUser("author");
                            authorView.setText(author.getUsername());

                            categoryView.setVisibility(View.VISIBLE);
                            counterView.setVisibility(View.VISIBLE);
                            buttonMore.setVisibility(View.VISIBLE);

                            if (position == mListView.getLastVisiblePosition()) {
                                mListView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comment");
        query.whereEqualTo("post", post);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null) {
                    counterView.setText(String.valueOf(count));
                }
            }
        });

        ParseQuery<ParseObject> unreadQuery = ParseQuery.getQuery("Comment");
        unreadQuery.whereEqualTo("post", post);
        unreadQuery.whereNotEqualTo("author", ParseUser.getCurrentUser());
        unreadQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> comments, ParseException e) {
                if (e == null) {
                    boolean isAuthor = post.getParseUser("author").getUsername().equals(ParseUser.getCurrentUser().getUsername());
                    if (hasUnread(comments) && isAuthor) {
                        counterView.setTextColor(Color.parseColor("#1aad44"));
                        counterView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getContext().getResources().getDrawable(R.drawable.ic_message_unread), null);
                    } else {
                        counterView.setTextColor(Color.parseColor("#90000000"));
                        counterView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getContext().getResources().getDrawable(R.drawable.ic_message), null);
                    }
                }
            }
        });


        View.OnClickListener commentsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CommentActivity.class);
                intent.putExtra("postId", post.getObjectId());
                getContext().startActivity(intent);
            }
        };
        counterView.setOnClickListener(commentsClickListener);
        convertView.setOnClickListener(commentsClickListener);

        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, author, post);
            }
        });

        if (!mHistorySection) {
            avatarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putString("user_id_history", post.getParseUser("author").getObjectId()).apply();
                    getContext().startActivity(new Intent(getContext(), HistoryActivity.class));
                }
            });
        }

        return convertView;
    }

    private boolean hasUnread(List<ParseObject> comments) {
        boolean hasUnread = false;

        for (ParseObject comment : comments) {
            boolean read = comment.getBoolean("readByAuthor");
            if (!read) {
                hasUnread = true;
            }
        }

        return hasUnread;
    }

    private void showPopup(View v, ParseUser author, final ParseObject post) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();

        int menuRes;

        if (ParseUser.getCurrentUser().getUsername().equals(author.getUsername())) {
            menuRes = R.menu.menu_post_me;
        } else {
            menuRes = R.menu.menu_post_others;
        }


        inflater.inflate(menuRes, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        post.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    refreshScreen();
                                }
                            }
                        });
                        return true;
                    case R.id.action_report:
                        report(post);
                        return true;
                    case R.id.action_block:
                        block(post.getParseUser("author"));
                        return true;
                    default:
                        return false;
                }
            }
        });

        popup.show();
    }

    private void block(final ParseUser author) {
        ParseUser user = ParseUser.getCurrentUser();
        user.addUnique("blackList", author.getUsername());
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getContext(), "You won't see posts from " + author.getUsername() + " anymore", Toast.LENGTH_SHORT).show();
                    refreshScreen();
                }
            }
        });
    }

    private void report(ParseObject post) {
        new SendEmailTask().execute(post.getString("message"), post.getParseUser("author").getUsername());
    }

    private class SendEmailTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            String contents = strings[0];
            String author = strings[1];

            String message = ParseUser.getCurrentUser().getUsername() + " complains about this:\n\n"
                    + "“" + contents + "”\n"
                    + "-- by " + author + "\n\n"
                    + "Manage posts:\n"
                    + "https://www.parse.com/apps/yasha/collections#class/Post";

            EmailClient m = new EmailClient("yasha.android.app@gmail.com", "yashaapp");

            String[] toArr = {"report@yasha.me"};
            m.setTo(toArr);
            m.setFrom("yasha.user@gmail.com");
            m.setSubject("Explicit post");
            m.setBody(message);

            try {
                m.send();
            } catch(Exception e) {
                Log.e("PostAdapter", "Could not send email", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Toast.makeText(getContext(), "Your report has been sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshScreen() {
        Activity activity = (Activity) getContext();

        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);

        ((Activity)getContext()).overridePendingTransition(0, 0);
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
