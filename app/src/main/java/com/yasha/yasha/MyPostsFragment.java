package com.yasha.yasha;

import android.os.Bundle;
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
import com.yasha.yasha.adapter.PostAdapter;

import java.util.List;

public class MyPostsFragment extends Fragment {

    public MyPostsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        final PostAdapter postAdapter = new PostAdapter(getActivity());

        ListView postList = (ListView) rootView.findViewById(R.id.post_list);
        postList.setAdapter(postAdapter);

        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.orderByDescending("createdAt");
        query.include("author");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

            ParseUser user = ParseUser.getCurrentUser();
            user.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject user, ParseException e) {
                    query.whereEqualTo("author", user);

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> posts, ParseException e) {
                            if (e == null) {
                                postAdapter.clear();
                                postAdapter.addAll(posts);
                            }
                        }
                    });
                }
            });

        return rootView;
    }
}
