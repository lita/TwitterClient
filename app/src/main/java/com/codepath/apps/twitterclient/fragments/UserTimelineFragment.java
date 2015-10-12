package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by litacho on 10/10/15.
 */
public class UserTimelineFragment extends TweetListFragment {
    User user;

    public static UserTimelineFragment newInstance(User user) {
        UserTimelineFragment frag = new UserTimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(), tweets);
        twitterClient = TwitterApplication.getRestClient();
        user = getArguments().getParcelable("user");
    }

    public void populateTimeline(long lastId, final boolean clear, final boolean startup) {

        if (!TwitterApplication.isNetworkAvailable(getActivity())) {
            propagateFromDatabase();
            return;
        }

        String screenname = null;
        if (user != null) {
            screenname = user.getScreenName();
        }

        twitterClient.getUserTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (clear) {
                    clear();
                }
                addAll(Tweet.fromJSONArray(response));
                pbProgressAction.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                propagateFromDatabase();
                Log.i("DEUBG", errorResponse.toString());
            }
        }, lastId, screenname);
    }
}
