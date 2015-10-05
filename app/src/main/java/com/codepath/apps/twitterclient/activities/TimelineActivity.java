package com.codepath.apps.twitterclient.activities;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.query.Select;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.fragments.ComposeDialog;
import com.codepath.apps.twitterclient.helpers.DialogHelpers;
import com.codepath.apps.twitterclient.listeners.EndlessScrollListener;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.codepath.apps.twitterclient.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends AppCompatActivity implements ComposeDialog.ComposeTweetListener {
    private TwitterRestClient twitterClient;
    private TweetsArrayAdapter aTweets;
    private ArrayList<Tweet> tweets;
    private ListView lvTweets;
    private User signedInUser;
    private ComposeDialog dialog;
    private SwipeRefreshLayout swipeContainer;
    final private int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        signedInUser = new User();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View header = inflater.inflate(R.layout.action_bar_home, null);
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this,
                    R.drawable.main_twitter_actionbar_background));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(header);
            actionBar.setDisplayShowCustomEnabled(true);
        }
        lvTweets = (ListView) findViewById(R.id.lvTweets);
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(this, tweets);
        setupListView();
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline(-1, true, false);
            }
        });
        twitterClient = TwitterApplication.getRestClient();
        generateSignedInUserData();
        populateTimeline(-1, false, true);
    }

    private void setupListView() {
        lvTweets.setAdapter(aTweets);
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                Tweet lastTweet = tweets.get(tweets.size() - 1);
                populateTimeline(lastTweet.getUid(), false, false);
                return true;
            }
        });
        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(TimelineActivity.this, DetailTweetActivity.class);
                Tweet tweet = tweets.get(position);
                i.putExtra("tweet", tweet);
                i.putExtra("signedInUser", signedInUser);
                startActivityForResult(i, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            Tweet tweet = data.getExtras().getParcelable("tweet");
            if (tweet == null) {
                return;
            }
            aTweets.insert(tweet, 0);
            aTweets.notifyDataSetChanged();
        }
    }

    private void propagateFromDatabase() {
        List<Tweet> tweets = new Select().from(Tweet.class).limit(100).execute();
        aTweets.addAll(tweets);
        swipeContainer.setRefreshing(false);
    }

    private void populateTimeline(long lastId, final boolean clear, final boolean startup) {

        if (!isNetworkAvailable()) {
            propagateFromDatabase();
            return;
        }

        twitterClient.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (clear) {
                    aTweets.clear();
                    swipeContainer.setRefreshing(false);
                }
                aTweets.addAll(Tweet.fromJSONArray(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                propagateFromDatabase();
                Log.i("DEUBG", errorResponse.toString());
            }
        }, lastId);
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
        if (id == R.id.action_compose) {
            if (isNetworkAvailable()) {
                showComposeDialog();
            } else {
                // show a popup that network is not available.
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showComposeDialog() {
        FragmentManager fm = getFragmentManager();
        ComposeDialog dialog = ComposeDialog.newInstance(signedInUser);
        dialog.show(fm, "fragment_compose");
    }

    @Override
    public void sendTweet(final String tweet) {
        twitterClient.setComposeTweet(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                aTweets.insert(Tweet.fromJSON(response), 0);
                aTweets.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("DEUBG", "Failed to send tweet. Got error code " + Integer.toString(statusCode) + " Response " + responseString);
                DialogHelpers.showAlert(TimelineActivity.this, "Networking Error",
                        "We couldn't post your tweet. Please make sure you are connected to the internet.");
            }

        }, tweet);
    }

    public void generateSignedInUserData() {
        if (!isNetworkAvailable()) {
            return;
        }

        twitterClient.getSignedInUserData(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                signedInUser = User.fromJSON(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("DEBUG", "Code: " + Integer.toString(statusCode) + " We failed to get user data: " + errorResponse.toString());

            }
        });
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
