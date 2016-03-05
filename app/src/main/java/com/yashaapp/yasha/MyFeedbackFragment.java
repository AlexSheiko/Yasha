package com.yashaapp.yasha;

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
import com.yashaapp.yasha.adapters.CommentAdapter;

import java.util.List;

public class MyFeedbackFragment extends Fragment {

    private View mRootView;
    private CommentAdapter mCommentAdapter;
    private List<ParseObject> mPosts;
    private int mIndex;

    public MyFeedbackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_feedback, container, false);

        ListView commentList = (ListView) mRootView.findViewById(R.id.comment_list);
        mCommentAdapter = new CommentAdapter(getActivity(), true, commentList);
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
                mPosts = posts;
                if (posts != null && posts.size() > 0) {
                    mRootView.findViewById(R.id.empty).setVisibility(View.GONE);

                    mCommentAdapter.clear();
                    for (int i = 0; i < posts.size(); i++) {
                        mIndex = i;
                        final ParseObject post = posts.get(i);
                        ParseQuery<ParseObject> commentQuery = new ParseQuery<>("Comment");
                        commentQuery.orderByDescending("createdAt");
                        commentQuery.include("post");
                        commentQuery.include("author");
                        commentQuery.whereEqualTo("post", post);
                        commentQuery.whereNotEqualTo("author", ParseUser.getCurrentUser());
                        commentQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> comments, ParseException e) {
                                if (e == null) {
                                    mCommentAdapter.addAll(comments);

                                    if (mIndex == (mPosts.size() - 1)) {
                                        if (mCommentAdapter.getCount() > 0) {
                                            mRootView.findViewById(R.id.empty).setVisibility(View.GONE);
                                        } else {
                                            mRootView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                } else {
                    mRootView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                }

            }
        });
    }
}
