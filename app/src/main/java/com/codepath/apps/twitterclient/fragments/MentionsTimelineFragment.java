package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.twitterclient.listeners.EndlessScrollListener;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by litacho on 10/9/15.
 */
public class MentionsTimelineFragment extends TweetListFragment {

    public static MentionsTimelineFragment newInstance(User signedInUser) {
        MentionsTimelineFragment frag = new MentionsTimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", signedInUser);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    protected void propagateFromDatabase() {
//        List<Tweet> tweets = new Select().from(Tweet.class).limit(100).execute();
//        if (signedInUser == null) {
//            super.propagateFromDatabase();
//            return;
//        }
//        for (int i = 0; i < tweets.size(); i++) {
//            Tweet tweet = tweets.get(i);
//            if (tweet.getBody().contains(signedInUser.getScreenNameForView())) {
//                add(tweet);
//            }
//        }
//        swipeContainer.setRefreshing(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        super.onResume();
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline(-1, true, false);
            }
        });

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                Tweet lastTweet = tweets.get(tweets.size() - 1);
                populateTimeline(lastTweet.getUid(), false, false);
                return true;
            }
        });

        return view;
    }

    public void populateTimeline(long lastId, final boolean clear, final boolean startup) {

        if (!TwitterApplication.isNetworkAvailable(getActivity())) {
            propagateFromDatabase();
            return;
        }

        twitterClient.getMentionsTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (clear) {
                    clear();
                }
                addAll(Tweet.fromJSONArray(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                propagateFromDatabase();
                Log.i("DEUBG", errorResponse.toString());
            }
        }, lastId);
    }
}
