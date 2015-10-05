package com.codepath.apps.twitterclient.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.fragments.ComposeDialog;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.codepath.apps.twitterclient.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailTweetActivity extends AppCompatActivity implements ComposeDialog.ComposeTweetListener {
    private TextView tvFullNameDetail;
    private TextView tvUserNameDetial;
    private TextView tvDetailBody;
    private TextView tvDetailTweetStats;
    private TextView tvDetailViewCreatedAt;
    private ImageButton ibReplyDetail;
    private Tweet tweet;
    private RoundedImageView ivDetailProfileImage;
    private TwitterRestClient twitterClient;
    private User signedInUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweet = getIntent().getParcelableExtra("tweet");
        signedInUser = getIntent().getParcelableExtra("signedInUser");
        setContentView(R.layout.activity_detail_tweet_view);
        twitterClient = TwitterApplication.getRestClient();
        initalizeViews();
        setupActionBar();
    }

    protected void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View header = inflater.inflate(R.layout.action_bar_detail, null);
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this,
                    R.drawable.main_twitter_actionbar_background))
            ;
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(header);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    protected void initalizeViews() {
        tvFullNameDetail = (TextView) findViewById(R.id.tvFullNameDetial);
        tvUserNameDetial = (TextView) findViewById(R.id.tvUserNameDetial);
        tvDetailBody = (TextView) findViewById(R.id.tvDetailBody);
        tvDetailTweetStats = (TextView) findViewById(R.id.tvDetailTweetStats);
        tvDetailViewCreatedAt = (TextView) findViewById(R.id.tvDetailViewCreatedAt);
        ivDetailProfileImage = (RoundedImageView) findViewById(R.id.ivDetailProfileImage);
        ibReplyDetail = (ImageButton) findViewById(R.id.ibReplyDetail);
        tvFullNameDetail.setText(tweet.getUser().getScreenNameForView());
        tvUserNameDetial.setText(tweet.getUser().getScreenName());
        tvDetailBody.setText(Html.fromHtml(tweet.getBody()));
        tvDetailTweetStats.setText(Html.fromHtml(generateTweetStats()));
        tvDetailViewCreatedAt.setText(getFormartedDate());
        setupReplyTweet();
        Picasso.with(this).load(tweet.getUser().getProfileImageUrl()).placeholder(R.drawable.placeholder).into(ivDetailProfileImage);
    }

    protected void setupReplyTweet() {
        ibReplyDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                ComposeDialog dialog = ComposeDialog.newInstance(signedInUser);
                dialog.show(fm, "compose_retweet");
            }
        });
    }

    protected String getFormartedDate() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        String detailTweetFormat = "h:mm aa  dd MMM yy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long dateMillis = sf.parse(tweet.getCreatedAt()).getTime();
            return new SimpleDateFormat(detailTweetFormat, Locale.ENGLISH).format(dateMillis);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    protected String generateTweetStats() {
        String retweetStat = "<bold>" + tweet.getRetweetCount() + "</bold>";
        String favStat = "<bold>" + tweet.getFavoriteCount() + "</bold>";
        return retweetStat + " " + getResources().getString(R.string.detail_retweet) + " " +
                favStat + " " + getResources().getString(R.string.detail_favorites);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_tweet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void sendTweet(String tweet) {
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
                Log.i("DEUBG", "Failed to send tweet. Got error code " + Integer.toString(statusCode) + " Response " + responseString);
            }

        }, tweet, this.tweet.getUid());
    }
}
