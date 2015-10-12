package com.codepath.apps.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterApplication;
import com.codepath.apps.twitterclient.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by litacho on 10/10/15.
 */
public class HeaderFragment extends Fragment {
    User user;
    TwitterRestClient client;
    ImageView ivProfileBanner;
    View profileView;

    public static HeaderFragment newInstance(User user) {
        HeaderFragment frag = new HeaderFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        frag.setArguments(bundle);
        return frag;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");
        if (user == null) {
            return;
        }
        client = TwitterApplication.getRestClient();
        if (user.getBannerImageUrl() == null) {
            client.getUserBanner(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    user.setBannerImage(response);
                    Picasso.with(profileView.getContext()).load(user.getBannerImageUrl()).into(ivProfileBanner);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.i("DEUBG", errorResponse.toString());
                }
            }, user.getScreenName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileView = inflater.inflate(R.layout.fragment_header, container, false);
        if (user != null) {
            RoundedImageView rivProfileImage = (RoundedImageView) profileView.findViewById(R.id.rivProfileImageProfileView);
            ivProfileBanner = (ImageView) profileView.findViewById(R.id.ivProfileBanner);
            TextView tvFullName = (TextView) profileView.findViewById(R.id.tvFullName);
            TextView tvScreenName = (TextView) profileView.findViewById(R.id.tvScreenName);
            TextView tvTagLine = (TextView) profileView.findViewById(R.id.tvTagline);
            TextView tvFollowers = (TextView) profileView.findViewById(R.id.tvFollowersCount);
            TextView tvFollowing = (TextView) profileView.findViewById(R.id.tvFollowingCount);
            tvFullName.setText(user.getName());
            tvScreenName.setText(user.getScreenNameForView());
            tvTagLine.setText(user.getTagLine());
            tvFollowers.setText(user.getFollowersCount());
            tvFollowing.setText(user.getFollowingCount());
            Picasso.with(profileView.getContext()).load(user.getProfileImageUrl()).placeholder(R.drawable.placeholder).into(rivProfileImage);
        }
        return profileView;
    }
}
