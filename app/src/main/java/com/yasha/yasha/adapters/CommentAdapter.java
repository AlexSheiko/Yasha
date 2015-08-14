package com.yasha.yasha.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.yasha.yasha.CircleTransform;
import com.yasha.yasha.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommentAdapter extends ArrayAdapter<ParseObject> {

    private boolean mHistorySection = false;

    public CommentAdapter(Context context) {
        super(context, 0);
    }

    public CommentAdapter(Context context, Boolean historySection) {
        super(context, 0);
        mHistorySection = historySection;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.comment_list_item, parent, false);

        final TextView nameView = (TextView) convertView.findViewById(R.id.name_textview);
        final TextView messageView = (TextView) convertView.findViewById(R.id.message_textview);
        final ImageView avatarView = (ImageView) convertView.findViewById(R.id.avatar_imageview);

        final ParseObject comment = getItem(position);
        messageView.setText(comment.getString("message"));

        final ParseUser author = comment.getParseUser("author");
        if (author != null) {
            author.fetchInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser author, ParseException e) {
                    if (e == null) {
                        nameView.setText(author.getUsername());

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
                                            .fit()
                                            .transform(new CircleTransform())
                                            .noFade()
                                            .into(avatarView);
                                }
                            });
                        } else {
                            Picasso.with(getContext())
                                    .load(R.drawable.avatar_placeholder)
                                    .fit()
                                    .transform(new CircleTransform())
                                    .noFade()
                                    .into(avatarView);
                        }
                    }
                }
            });
        }

        if (mHistorySection) {
            TextView dateView = (TextView) convertView.findViewById(R.id.date_textview);
            dateView.setVisibility(View.VISIBLE);
            dateView.setText(formatDate(comment.getCreatedAt()));
        }


        return convertView;
    }

    private String formatDate(Date date) {
        if (isToday(date)) {
            DateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return dateFormat.format(date);
        } else if (isYesterday(date)) {
            return "Yesterday";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());
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
