package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
public class HomeTimelineFragment extends TweetListFragment {

    public static HomeTimelineFragment newInstance(User signedInUser) {
        HomeTimelineFragment frag = new HomeTimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", signedInUser);
        frag.setArguments(bundle);
        return frag;
    }

    public void populateTimeline(long lastId, final boolean clear, final boolean startup) {

        if (!TwitterApplication.isNetworkAvailable(getActivity())) {
            propagateFromDatabase();
            return;
        }

        twitterClient.getHomeTimeline(new JsonHttpResponseHandler() {
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
        }, lastId);
    }
}
