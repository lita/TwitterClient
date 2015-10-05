package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.helpers.LinkifiedTextView;
import com.codepath.apps.twitterclient.models.Tweet;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by litacho on 9/30/15.
 */
public class TweetsArrayAdapter extends ArrayAdapter<Tweet> {
    public static class TweetViewHolder {
        ImageView ivProfileImage;
        TextView tvUserName;
        LinkifiedTextView tvBody;
        TextView tvFullName;
        TextView tvCreatedAt;
        TextView tvRetweetCount;
        TextView tvFavoriteCount;
    }
    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tweet tweet = getItem(position);
        TweetViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
            viewHolder = new TweetViewHolder();
            viewHolder.ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.tvBody = (LinkifiedTextView) convertView.findViewById(R.id.tvBody);
            viewHolder.tvFullName = (TextView) convertView.findViewById(R.id.tvFullName);
            viewHolder.tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
            viewHolder.tvFavoriteCount = (TextView) convertView.findViewById(R.id.tvFavoriteCount);
            viewHolder.tvRetweetCount = (TextView) convertView.findViewById(R.id.tvRetweetCount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TweetViewHolder) convertView.getTag();
        }
        viewHolder.tvUserName.setText(tweet.getUser().getScreenNameForView());
        viewHolder.tvFullName.setText(tweet.getUser().getName());
        viewHolder.tvBody.setText(Html.fromHtml(tweet.getBody()));
        viewHolder.ivProfileImage.setImageResource(android.R.color.transparent);
        viewHolder.tvCreatedAt.setText(tweet.getRelativeCreatedAt());
        viewHolder.tvRetweetCount.setText(tweet.getRetweetCount());
        viewHolder.tvFavoriteCount.setText(tweet.getFavoriteCount());
        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.ivProfileImage);
        return convertView;
    }
}
