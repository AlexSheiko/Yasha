package com.yasha.yasha.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.yasha.yasha.CircleTransform;
import com.yasha.yasha.HistoryActivity;
import com.yasha.yasha.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserAdapter extends ArrayAdapter<ParseUser> {

    public UserAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.user_list_item, parent, false);

        final TextView nameView = (TextView) convertView.findViewById(R.id.name_textview);
        final TextView cityView = (TextView) convertView.findViewById(R.id.city_textview);
        final ImageView avatarView = (ImageView) convertView.findViewById(R.id.avatar_imageview);

        ParseUser user = getItem(position);
        nameView.setText(user.getUsername());
        cityView.setText(user.getString("city"));

        ParseFile avatarFile = user.getParseFile("avatar");
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

        View.OnClickListener userClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HistoryActivity.class);
                getContext().startActivity(intent);
            }
        };
        convertView.setOnClickListener(userClickListener);

        return convertView;
    }
}
