package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.helpers.LinkifiedTextView;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.codepath.apps.twitterclient.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by litacho on 9/30/15.
 */
public class TweetsArrayAdapter extends ArrayAdapter<Tweet> {
    TwitterRestClient twitterClient;
    public interface OnTweetActionsListener {
        public void onReply(Tweet tweet);
        public void onProfileClick(User user);

    }
    public static class TweetViewHolder {
        ImageView ivProfileImage;
        ImageButton ibReply;
        ImageButton ibRetweet;
        ImageButton ibStar;
        TextView tvUserName;
        LinkifiedTextView tvBody;
        TextView tvFullName;
        TextView tvCreatedAt;
        TextView tvRetweetCount;
        TextView tvFavoriteCount;
        TextView tvReplyTo;
        ImageView ivMedia;
    }
    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Tweet tweet = getItem(position);
        TweetViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
            viewHolder = new TweetViewHolder();
            viewHolder.ivProfileImage = (ImageView) convertView.findViewById(R.id.rivProfileImageProfileView);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.tvBody = (LinkifiedTextView) convertView.findViewById(R.id.tvBody);
            viewHolder.tvFullName = (TextView) convertView.findViewById(R.id.tvFullName);
            viewHolder.tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
            viewHolder.tvFavoriteCount = (TextView) convertView.findViewById(R.id.tvFavoriteCount);
            viewHolder.tvRetweetCount = (TextView) convertView.findViewById(R.id.tvRetweetCount);
            viewHolder.tvReplyTo = (TextView) convertView.findViewById(R.id.tvReplyTo);
            viewHolder.ivMedia = (ImageView) convertView.findViewById(R.id.ivMedia);
            viewHolder.ibReply = (ImageButton) convertView.findViewById(R.id.ibReply);
            viewHolder.ibRetweet = (ImageButton) convertView.findViewById(R.id.ibRetweet);
            viewHolder.ibStar = (ImageButton) convertView.findViewById(R.id.ibStar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TweetViewHolder) convertView.getTag();
        }
        twitterClient = TwitterApplication.getRestClient();
        viewHolder.tvUserName.setText(tweet.getUser().getScreenNameForView());
        viewHolder.tvFullName.setText(tweet.getUser().getName());
        viewHolder.tvBody.setText(Html.fromHtml(tweet.getBody()));
        viewHolder.ivProfileImage.setImageResource(android.R.color.transparent);
        viewHolder.tvCreatedAt.setText(tweet.getRelativeCreatedAt());
        viewHolder.tvRetweetCount.setText(tweet.getRetweetCount());
        viewHolder.tvFavoriteCount.setText(tweet.getFavoriteCount());
        viewHolder.ivProfileImage.setImageResource(0);
        viewHolder.ivMedia.setImageResource(0);


        viewHolder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tweet tweet = getItem(position);
                User tweetUser = tweet.getUser();
                OnTweetActionsListener listener = (OnTweetActionsListener) getContext();
                listener.onProfileClick(tweetUser);
            }
        });

        viewHolder.ibReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tweet tweet = getItem(position);
                OnTweetActionsListener listener = (OnTweetActionsListener) getContext();
                listener.onReply(tweet);
            }
        });

        viewHolder.ibRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tweet tweet = getItem(position);
                twitterClient.postRetweet(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Tweet resultTweet = Tweet.fromJSON(response);
                        insert(resultTweet, 0);
                    }
                }, tweet.getUid());
            }
        });

        if (tweet.getInReplyToScreenName() != null) {
            String replyText = getContext().getResources().getString(R.string.reply);
            viewHolder.tvReplyTo.setText(replyText + " " + tweet.getInReplyToScreenNameForView());
            viewHolder.tvReplyTo.setTextSize(12);
        } else {
            viewHolder.tvReplyTo.setTextSize(0);
        }

        Picasso.with(getContext())
                .load(tweet.getMediaUrl())
                .resize(0, 400)
                .into(viewHolder.ivMedia);

        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.ivProfileImage);
        return convertView;
    }
}
