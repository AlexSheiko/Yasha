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

public class MyFeedbackFragment extends Fragment {

    private View mRootView;
    private CommentAdapter mCommentAdapter;

    public MyFeedbackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_feedback, container, false);

        mCommentAdapter = new CommentAdapter(getActivity(), true);

        ListView commentList = (ListView) mRootView.findViewById(R.id.comment_list);
        commentList.setAdapter(mCommentAdapter);

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> posts, ParseException e) {
                if (e == null) {
                    mCommentAdapter.clear();
                    for (ParseObject post : posts) {
                        ParseQuery<ParseObject> commentQuery = new ParseQuery<>("Comment");
                        commentQuery.include("post");
                        commentQuery.include("author");
                        commentQuery.whereEqualTo("post", post);
                        commentQuery.whereNotEqualTo("author", ParseUser.getCurrentUser());
                        commentQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> comments, ParseException e) {
                                if (e == null) {
                                    mCommentAdapter.addAll(comments);
                                }

                                if (comments != null && comments.size() > 0) {
                                    mRootView.findViewById(R.id.empty).setVisibility(View.GONE);
                                } else {
                                    mRootView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    if (posts.size() == 0) {
                        mRootView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                    } else {
                        mRootView.findViewById(R.id.empty).setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
