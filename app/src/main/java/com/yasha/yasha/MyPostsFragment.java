package com.yasha.yasha;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yasha.yasha.adapters.PostAdapter;

import java.util.List;

public class MyPostsFragment extends Fragment {

    private PostAdapter mPostAdapter;
    private View mRootView;

    public MyPostsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_posts, container, false);

        mPostAdapter = new PostAdapter(getActivity(), true);

        ListView postList = (ListView) mRootView.findViewById(R.id.post_list);
        postList.setAdapter(mPostAdapter);

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.orderByDescending("createdAt");
        query.include("author");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = prefs.getString("user_id_history", ParseUser.getCurrentUser().getObjectId());

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    query.whereEqualTo("author", user);

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> posts, ParseException e) {
                            if (e == null) {
                                if (posts.size() > 0) {
                                    mRootView.findViewById(R.id.empty).setVisibility(View.GONE);
                                    mPostAdapter.clear();
                                    mPostAdapter.addAll(posts);
                                } else {
                                    mRootView.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
