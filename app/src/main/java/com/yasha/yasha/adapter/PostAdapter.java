package com.yasha.yasha.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yasha.yasha.CommentActivity;
import com.yasha.yasha.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PostAdapter extends ArrayAdapter<ParseObject> {

    public PostAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.message_list_item, parent, false);
        }

        final TextView authorView = (TextView) convertView.findViewById(R.id.author_textview);
        final ParseImageView avatarView = (ParseImageView) convertView.findViewById(R.id.avatar_imageview);
        final TextView messageView = (TextView) convertView.findViewById(R.id.message_textview);
        final TextView dateView = (TextView) convertView.findViewById(R.id.date_textview);
        final TextView categoryView = (TextView) convertView.findViewById(R.id.category_textview);
        final TextView counterView = (TextView) convertView.findViewById(R.id.messages_counter);

        final ParseObject post = getItem(position);
        messageView.setText(post.getString("message"));
        dateView.setText(formatDate(post.getCreatedAt()));
        categoryView.setText(post.getString("category"));

        ParseUser author = post.getParseUser("author");
        authorView.setText(author.getUsername());

        ParseFile avatarFile = author.getParseFile("avatar");
        avatarView.setPlaceholder(getContext().getResources().getDrawable(R.drawable.avatar_placeholder));
        avatarView.setParseFile(avatarFile);
        avatarView.loadInBackground();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comment");
        query.whereEqualTo("post", post);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                Log.d("PostAdapter", "Count: " + count);
                counterView.setText(String.valueOf(count));
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

        return convertView;
    }

    private String formatDate(Date date) {
        if (isToday(date)) {
            DateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            return dateFormat.format(date);
        } else if (isBeforeDay(date)) {
            return "Yesterday";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("MMMM d", Locale.US);
            return dateFormat.format(date);
        }
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
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

    public static boolean isBeforeDay(Date date1) {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return isBeforeDay(cal1, cal2);
    }

    public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true;
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return false;
        return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
    }
}
