package com.codepath.apps.twitterclient.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.codepath.apps.twitterclient.fragments.HomeTimelineFragment;
import com.codepath.apps.twitterclient.fragments.MentionsTimelineFragment;
import com.codepath.apps.twitterclient.models.User;

// returns the order of the fragments
public class TweetsPagerAdapter extends SmartFragmentStatePagerAdapter {
    private String tabTitles[] = {"Home", "Mentions"};
    private User signedInUser;

    public TweetsPagerAdapter(FragmentManager fm, User user){
        super(fm);
        signedInUser = user;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return HomeTimelineFragment.newInstance(signedInUser);
        } else if (position == 1) {
            return MentionsTimelineFragment.newInstance(signedInUser);
        } else {
            return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}