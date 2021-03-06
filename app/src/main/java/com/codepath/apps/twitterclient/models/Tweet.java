package com.codepath.apps.twitterclient.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by litacho on 9/30/15.
 */
@Table(name = "Tweets")
public class Tweet extends Model implements Parcelable {
    @Column(name = "body")
    private String body;
    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;
    @Column(name = "user", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User user;
    @Column(name = "createdAt")
    private String createdAt;
    @Column(name = "retweetCount")
    private String retweetCount;
    @Column(name = "favoriteCount")
    private String favoriteCount;
    @Column(name = "inReplyToScreenName")
    private String inReplyToScreenName;
    @Column(name="mediaUrl")
    private String mediaUrl;
    @Column(name="retweet")
    private boolean retweet;
    @Column(name="retweetUser")
    private User retweetUser;

    public boolean isRetweet() {
        return retweet;
    }

    public User getRetweetUser() {
        return retweetUser;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public String getInReplyToScreenNameForView() {
        if (inReplyToScreenName != null) {
            return "@" + inReplyToScreenName;
        }
        return "";
    }

    public String getRetweetCount() {
        return retweetCount;
    }

    public String getFavoriteCount() {
        return favoriteCount;
    }

    public User getUser() { return user; }

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public static String processCaptionsToHTML(String caption) {
        String[] splitCaption = caption.split(" ");

        String htmlString = "";
        for (String str : splitCaption) {
            String result;
            if (str.startsWith("#") || str.startsWith("@")) {
                result = "<font color='#55ACEE'>" + str + "</font> ";
            } else {
                result = str + " ";
            }
            htmlString += result;
        }

        return htmlString;
    }

    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.retweet = jsonObject.getBoolean("retweeted");
            if (tweet.retweet) {
                tweet.retweetUser =  User.fromJSON(jsonObject.getJSONObject("user"));
                jsonObject = jsonObject.getJSONObject("retweeted_status");
            }
            tweet.body = processCaptionsToHTML(jsonObject.getString("text"));
            tweet.uid = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
            tweet.favoriteCount = jsonObject.getString("favorite_count");
            tweet.retweetCount = jsonObject.getString("retweet_count");
            JSONObject entities = jsonObject.getJSONObject("entities");
            if (entities.has("media")) {
                tweet.mediaUrl= entities.getJSONArray("media").getJSONObject(0).getString("media_url");
            }
            if (jsonObject.getString("in_reply_to_screen_name") != "null") {
                tweet.inReplyToScreenName = jsonObject.getString("in_reply_to_screen_name");
            }
            tweet.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = Tweet.fromJSON(tweetJson);
                if (tweet != null) {
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return tweets;
    }

    public String getRelativeCreatedAt() {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(this.createdAt).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();

            relativeDate = relativeDate.replace(" hour ago", "h");
            relativeDate = relativeDate.replace(" hours ago", "h");
            relativeDate = relativeDate.replace(" minute ago", "m");
            relativeDate = relativeDate.replace(" minutes ago", "m");
            relativeDate = relativeDate.replace(" week ago", "w");
            relativeDate = relativeDate.replace(" weeks ago", "w");
            relativeDate = relativeDate.replace(" minutes", "m");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public Tweet() {
    }

    public void incrementFavoriteCount() {
        int favCount = Integer.getInteger(favoriteCount);
        favoriteCount = Integer.toString(favCount + 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.body);
        dest.writeLong(this.uid);
        dest.writeParcelable(this.user, 0);
        dest.writeString(this.createdAt);
        dest.writeString(this.retweetCount);
        dest.writeString(this.favoriteCount);
        dest.writeString(this.inReplyToScreenName);
        dest.writeString(this.mediaUrl);
        dest.writeByte(retweet ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.retweetUser, 0);
    }

    protected Tweet(Parcel in) {
        this.body = in.readString();
        this.uid = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.retweetCount = in.readString();
        this.favoriteCount = in.readString();
        this.inReplyToScreenName = in.readString();
        this.mediaUrl = in.readString();
        this.retweet = in.readByte() != 0;
        this.retweetUser = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
