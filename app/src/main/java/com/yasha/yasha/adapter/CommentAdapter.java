package com.yasha.yasha.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.yasha.yasha.R;

public class CommentAdapter extends ArrayAdapter<ParseObject> {

    public CommentAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.comment_list_item, parent, false);
        }

        final TextView nameView = (TextView) convertView.findViewById(R.id.name_textview);
        final TextView messageView = (TextView) convertView.findViewById(R.id.message_textview);
        final ParseImageView avatarView = (ParseImageView) convertView.findViewById(R.id.avatar_imageview);

        final ParseObject post = getItem(position);
        messageView.setText(post.getString("message"));

        final ParseUser author = post.getParseUser("author");
        try {
            author.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        nameView.setText(author.getUsername());

        ParseFile avatarFile = author.getParseFile("avatar");
        avatarView.setPlaceholder(getContext().getResources().getDrawable(R.drawable.avatar_placeholder));
        avatarView.setParseFile(avatarFile);
        avatarView.loadInBackground();

//        View.OnClickListener userClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), HistoryActivity.class);
//                getContext().startActivity(intent);
//            }
//        };
//        convertView.setOnClickListener(userClickListener);

        return convertView;
    }
}
