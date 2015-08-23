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
import com.yasha.yasha.adapters.CommentAdapter;

import java.util.List;

public class MyCommentsFragment extends Fragment {

    private View mRootView;

    public MyCommentsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_comments, container, false);

        ListView commentList = (ListView) mRootView.findViewById(R.id.comment_list);
        final CommentAdapter commentAdapter = new CommentAdapter(getActivity(), true, commentList);
        commentList.setAdapter(commentAdapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comment");
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> comments, ParseException e) {
                if (e == null) {
                    if (comments.size() > 0) {
                        mRootView.findViewById(R.id.empty).setVisibility(View.GONE);
                        commentAdapter.clear();
                        commentAdapter.addAll(comments);
                    } else {
                        mRootView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        return mRootView;
    }
}
