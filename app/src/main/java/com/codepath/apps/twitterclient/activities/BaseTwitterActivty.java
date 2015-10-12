package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.fragments.ComposeDialog;
import com.codepath.apps.twitterclient.fragments.TweetListFragment;
import com.codepath.apps.twitterclient.helpers.DialogHelpers;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by litacho on 10/11/15.
 */
public class BaseTwitterActivty extends AppCompatActivity implements ComposeDialog.ComposeTweetListener,
        TweetsArrayAdapter.OnTweetActionsListener, TweetListFragment.OnTweetItemListener {
    protected User user;
    protected TwitterRestClient twitterClient;
    final public int REQUEST_CODE = 1;

    @Override
    public void onReply(Tweet tweet) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog dialog = ComposeDialog.newInstance(user, tweet);
        dialog.show(fm, "fragment_compose_reply");
    }

    @Override
    public void onProfileClick(User user) {
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("user", user);
        startActivityForResult(i, REQUEST_CODE);
    }

    @Override
    public void sendTweet(String tweet, Tweet retweet) {
        long tweetId = -1;
        if (retweet != null) {
            tweetId = retweet.getUid();
        }
        twitterClient.setComposeTweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet tweet = Tweet.fromJSON(response);
                Intent data = new Intent();
                data.putExtra("tweet", tweet);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("DEUBG", "Failed to send tweet. Got error code " + Integer.toString(statusCode));
                DialogHelpers.showAlert(BaseTwitterActivty.this, "Networking Error",
                        "We couldn't post your tweet. Please make sure you are connected to the internet.");
            }

        }, tweet, tweetId);
    }

    @Override
    public void launchDetailTweetView(Tweet tweet) {
        Intent i = new Intent(this, DetailTweetActivity.class);
        i.putExtra("tweet", tweet);
        i.putExtra("signedInUser", user);
        startActivityForResult(i, REQUEST_CODE);
    }
}
