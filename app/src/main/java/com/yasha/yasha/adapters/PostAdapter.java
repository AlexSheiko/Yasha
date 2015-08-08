package com.yasha.yasha.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
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
        View rootView;

        if (convertView != null) {
            rootView = convertView;
        } else {
            rootView = LayoutInflater.from(getContext())
                    .inflate(R.layout.message_list_item, parent, false);
        }

        TextView authorView = (TextView) rootView.findViewById(R.id.author_textview);
        TextView messageView = (TextView) rootView.findViewById(R.id.message_textview);
        TextView dateView = (TextView) rootView.findViewById(R.id.date_textview);
        TextView categoryView = (TextView) rootView.findViewById(R.id.category_textview);

        ParseObject post = getItem(position);
        messageView.setText(post.getString("message"));
        dateView.setText(formatDate(post.getCreatedAt()));
        categoryView.setText(post.getString("category"));

        ParseUser author;
        try {
            author = post.getParseUser("author").fetchIfNeeded();
            authorView.setText(author.getUsername());



        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rootView;
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
