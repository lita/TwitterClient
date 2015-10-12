package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.adapters.TweetsPagerAdapter;
import com.codepath.apps.twitterclient.fragments.ComposeDialog;
import com.codepath.apps.twitterclient.fragments.HomeTimelineFragment;
import com.codepath.apps.twitterclient.helpers.DialogHelpers;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.codepath.apps.twitterclient.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

public class TimelineActivity extends BaseTwitterActivty  {
    protected TwitterRestClient twitterClient;
    TweetsPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        twitterClient = TwitterApplication.getRestClient();
        generateSignedInUserData();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View header = inflater.inflate(R.layout.action_bar, null);
            TextView tvTitle = (TextView) header.findViewById(R.id.tvTitle);
            tvTitle.setText(getResources().getString(R.string.title_activity_timeline));
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this,
                    R.drawable.main_twitter_actionbar_background));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(header);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        if (savedInstanceState == null) {
            ViewPager vpPagers = (ViewPager) findViewById(R.id.viewpager);
            pagerAdapter = new TweetsPagerAdapter(getSupportFragmentManager(), user);
            vpPagers.setAdapter(pagerAdapter);
            TabLayout tabStrip = (TabLayout) findViewById(R.id.tabs);
            tabStrip.setupWithViewPager(vpPagers);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            Tweet tweet = data.getExtras().getParcelable("tweet");
            if (tweet == null) {
                return;
            }
            HomeTimelineFragment homeTweetFrag = (HomeTimelineFragment) pagerAdapter.getRegisteredFragment(0);
            homeTweetFrag.insert(tweet, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.miCompose) {
            if (TwitterApplication.isNetworkAvailable(this)) {
                showComposeDialog();
            } else {
                // show a popup that network is not available.
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog dialog = ComposeDialog.newInstance(user);
        dialog.show(fm, "fragment_compose");
    }

    public void generateSignedInUserData() {
        if (!TwitterApplication.isNetworkAvailable(this)) {
            return;
        }

        twitterClient.getSignedInUserData(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                user = User.fromJSON(response);
                saveSignedInUser(user);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                user = getSignedInUserOffline();
                Log.i("DEBUG", "Code: " + Integer.toString(statusCode) + " We failed to get user data: " + errorResponse.toString());

            }
        });
    }

    public void onProfileView(MenuItem item) {
        onProfileClick(user);
    }

    @Override
    public void sendTweet(final String tweet, Tweet retweet) {
        long retweetId = -1;
        if (retweet != null) {
            retweetId = retweet.getUid();
        }

        twitterClient.setComposeTweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                HomeTimelineFragment homeTweetFrag = (HomeTimelineFragment) pagerAdapter.getRegisteredFragment(0);
                homeTweetFrag.insert(Tweet.fromJSON(response), 0);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("DEUBG", "Failed to send tweet. Got error code " + Integer.toString(statusCode) + " Response " + responseString);
                DialogHelpers.showAlert(TimelineActivity.this, "Networking Error",
                        "We couldn't post your tweet. Please make sure you are connected to the internet.");
            }

        }, tweet, retweetId);
    }

    public void saveSignedInUser(User user) {
        SharedPreferences mSettings = this.getSharedPreferences("TwitterSettings", 0);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("signedInUser", user.getScreenName());
    }

    public User getSignedInUserOffline() {
        SharedPreferences mSettings = this.getSharedPreferences("TwitterSettings", 0);
        String signedInUserScreenName = mSettings.getString("signedInUser", null);
        if (signedInUserScreenName != null) {
            List<User> user = new Select().from(User.class).where("screenName = ?", signedInUserScreenName).execute();
            return user.get(0);
        }
        return null;
    }
}
