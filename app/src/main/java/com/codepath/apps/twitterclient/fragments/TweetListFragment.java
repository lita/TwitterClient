package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.activeandroid.query.Select;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.listeners.EndlessScrollListener;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.codepath.apps.twitterclient.network.TwitterRestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by litacho on 10/9/15.
 */
public abstract class TweetListFragment extends Fragment {
    protected TweetsArrayAdapter aTweets;
    protected ArrayList<Tweet> tweets;
    protected ListView lvTweets;
    protected SwipeRefreshLayout swipeContainer;
    protected TwitterRestClient twitterClient;
    protected User signedInUser;
    protected ProgressBar pbProgressAction;

    public interface OnTweetItemListener {
        public void launchDetailTweetView(Tweet tweet);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweet_list, container, false);
        signedInUser = getArguments().getParcelable("user");

        lvTweets = (ListView) view.findViewById(R.id.lvTweets);
        View footer = inflater.inflate(
                R.layout.footer_progress, null);
        // Find the progressbar within footer
        pbProgressAction = (ProgressBar)
                footer.findViewById(R.id.pbFooterLoading);
        // Add footer to ListView before setting adapter
        lvTweets.addFooterView(footer);
        lvTweets.setAdapter(aTweets);
        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tweet tweet = get(position);
                OnTweetItemListener listener = (OnTweetItemListener) getActivity();
                listener.launchDetailTweetView(tweet);
            }
        });
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline(-1, true, false);
            }
        });

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                if (tweets.isEmpty()) {
                    return false;
                }
                Tweet lastTweet = tweets.get(tweets.size() - 1);
                populateTimeline(lastTweet.getUid(), false, false);
                return true;
            }
        });
        pbProgressAction.setVisibility(View.VISIBLE);
        populateTimeline(-1, false, true);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(), tweets);
        twitterClient = TwitterApplication.getRestClient();
    }

    public Tweet get(int position) {
        return tweets.get(position);
    }

    public void addAll(List<Tweet> tweets) {
        aTweets.addAll(tweets);
        aTweets.notifyDataSetChanged();

    }

    public void add(Tweet tweet) {
        aTweets.add(tweet);
        aTweets.notifyDataSetChanged();

    }

    public void insert(Tweet tweet, int position) {
        aTweets.insert(tweet, position);
        aTweets.notifyDataSetChanged();
    }

    public void clear() {
        aTweets.clear();
        if (swipeContainer != null) {
            swipeContainer.setRefreshing(false);
        }
        aTweets.notifyDataSetChanged();
    }

    protected void propagateFromDatabase() {
        List<Tweet> tweets = new Select().from(Tweet.class).limit(100).execute();
        addAll(tweets);
        swipeContainer.setRefreshing(false);
        pbProgressAction.setVisibility(View.GONE);
    }

    abstract public void populateTimeline(long lastId, final boolean clear, final boolean startup);
}
