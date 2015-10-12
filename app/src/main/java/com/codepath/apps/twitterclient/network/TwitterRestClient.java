package com.codepath.apps.twitterclient.network;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterRestClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1/"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "mIWpebvttIpzD5PzIZ9i8mEfJ";       // Change this
	public static final String REST_CONSUMER_SECRET = "HKt8jtaZt8EcVXsfYw7lkFtnld6rYPEaQyvFYXnfEQ0gDx558C"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://litatwitterclient"; // Change this (here and in manifest)

	public TwitterRestClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	public void getHomeTimeline(AsyncHttpResponseHandler handler, long lastId) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 100);
		params.put("since_id", 1);
        if (lastId > -1) {
            params.put("max_id", lastId);
        }
		// Execute the request
		getClient().get(apiUrl, params, handler);
	}

    public void getMentionsTimeline(AsyncHttpResponseHandler handler, long lastId) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", 100);
        if (lastId > -1) {
            params.put("max_id", lastId);
        }
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

	// COMPOSING A TWEET
	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */

    public void setComposeTweet(AsyncHttpResponseHandler handler, String tweet, long replyId) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", tweet);
        if (replyId > -1) {
            params.put("in_reply_to_status_id", replyId);
        }
        // Execute the request
        getClient().post(apiUrl, params, handler);
    }

    public void getUserTimeline(AsyncHttpResponseHandler handler, long lastId, String screenName) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count", 100);
        if (lastId > -1) {
            params.put("max_id", lastId);
        }

        params.put("screen_name", screenName);

        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    public void getSignedInUserData(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("account/verify_credentials.json");
		getClient().get(apiUrl, handler);
	}

    public void getUserBanner(AsyncHttpResponseHandler handler, String screenName) {
        String apiUrl = getApiUrl("users/profile_banner.json");
        RequestParams params = new RequestParams();
        //params.put("screen_name", screenName);
        getClient().get(apiUrl, params, handler);
    }

    public void postRetweet(AsyncHttpResponseHandler handler, long tweetId) {
        String apiUrl = getApiUrl("statuses/retweet/" + Long.toString(tweetId) + ".json");
        RequestParams params = new RequestParams();
        getClient().post(apiUrl, params, handler);
    }

    public void postFavorite(AsyncHttpResponseHandler handler, long tweetId) {
        String apiUrl = getApiUrl("statuses/retweet/favorites/create.json");
        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        getClient().post(apiUrl, params, handler);
    }
}