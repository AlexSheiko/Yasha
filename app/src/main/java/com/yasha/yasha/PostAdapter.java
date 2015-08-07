package com.yasha.yasha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

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

        ParseObject post = getItem(position);

        messageView.setText(post.getString("message"));

        return rootView;
    }
}
