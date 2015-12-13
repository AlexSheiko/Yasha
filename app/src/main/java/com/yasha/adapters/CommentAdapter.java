package com.yasha.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yasha.CircleTransform;
import com.yasha.HistoryActivity;
import com.yasha.yasha.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommentAdapter extends ArrayAdapter<ParseObject> {

    private ListView mListView;
    private boolean mHistorySection = false;
    private View mRootView;

    public CommentAdapter(Context context, ListView listView) {
        super(context, 0);
        mListView = listView;
    }

    public CommentAdapter(Context context, boolean historySection, ListView listView) {
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
    public View getView(final int position, View rootView, ViewGroup parent) {

        rootView = LayoutInflater.from(getContext())
                .inflate(R.layout.comment_list_item, parent, false);
        mRootView = rootView;

        final TextView nameView = (TextView) mRootView.findViewById(R.id.name_textview);
        final TextView messageView = (TextView) mRootView.findViewById(R.id.message_textview);
        final ImageView avatarView = (ImageView) mRootView.findViewById(R.id.avatar_imageview);
        final TextView dateView = (TextView) mRootView.findViewById(R.id.date_textview);

        final ParseObject comment = getItem(position);

        final ParseUser author = comment.getParseUser("author");
        if (author != null) {
            author.fetchInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(final ParseUser author, ParseException e) {
                    if (e == null) {

                        ParseFile avatarFile = author.getParseFile("avatar");
                        if (avatarFile != null) {
                            avatarFile.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] bytes, ParseException e) {
                                    if (e == null) {
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
                                                .noFade()
                                                .into(avatarView, new Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        nameView.setText(author.getUsername());
                                                        messageView.setText(comment.getString("message"));

                                                        dateView.setVisibility(View.VISIBLE);
                                                        dateView.setText(formatDate(comment.getCreatedAt()));

                                                        if (position == mListView.getLastVisiblePosition()) {
                                                            mListView.setVisibility(View.VISIBLE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onError() {
                                                    }
                                                });
                                    }
                                }
                            });
                        } else {
                            Picasso.with(getContext())
                                    .load(R.drawable.avatar_placeholder)
                                    .fit().centerCrop()
                                    .transform(new CircleTransform())
                                    .noFade()
                                    .into(avatarView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            nameView.setText(author.getUsername());
                                            messageView.setText(comment.getString("message"));

                                            dateView.setVisibility(View.VISIBLE);
                                            dateView.setText(formatDate(comment.getCreatedAt()));

                                            if (position == mListView.getLastVisiblePosition()) {
                                                mListView.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onError() {
                                        }
                                    });
                        }
                    }
                }
            });
        }

        boolean myCommentsTab = comment.getParseUser("author").getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
        if (!mHistorySection || !myCommentsTab) {
            avatarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putString("user_id_history", comment.getParseUser("author").getObjectId()).apply();
                    getContext().startActivity(new Intent(getContext(), HistoryActivity.class));
                }
            });
        }

        if (!mHistorySection) {
            mRootView.setBackgroundColor(Color.parseColor("#F1F8E9"));
        }

        return mRootView;
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
