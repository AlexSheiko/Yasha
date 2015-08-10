package com.yasha.yasha;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yasha.yasha.adapter.CommentAdapter;

import java.util.List;

public class MyCommentsFragment extends Fragment {

    public MyCommentsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comments, container, false);

        final CommentAdapter commentAdapter = new CommentAdapter(getActivity());

        ListView commentList = (ListView) rootView.findViewById(R.id.comment_list);
        commentList.setAdapter(commentAdapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comment");
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> comments, ParseException e) {
                if (e == null) {
                    commentAdapter.clear();
                    commentAdapter.addAll(comments);
                }
            }
        });

        return rootView;
    }
}
