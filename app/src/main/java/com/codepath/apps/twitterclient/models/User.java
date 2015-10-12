package com.codepath.apps.twitterclient.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by litacho on 9/30/15.
 */
@Table(name = "Users")
public class User extends Model implements Parcelable {
    @Column(name = "name")
    private String name;
    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;
    @Column(name = "screenName")
    private String screenName;
    @Column(name = "profileImageUrl")
    private String profileImageUrl;
    @Column(name = "tagLine")
    private String tagLine;
    @Column(name = "followersCount")
    private String followersCount;
    @Column(name = "followingCount")
    private String followingCount;
    @Column(name = "bannerImageUrl")
    private String bannerImageUrl;

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public static User fromJSON(JSONObject jsonObject) {
        User user = new User();
        try {
            user.uid = jsonObject.getLong("id");
            user.name = jsonObject.getString("name");
            user.screenName = jsonObject.getString("screen_name");
            user.profileImageUrl = jsonObject.getString("profile_image_url");
            user.tagLine = jsonObject.getString("description");
            user.followersCount = jsonObject.getString("followers_count");
            user.followingCount = jsonObject.getString("friends_count");

            user.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    public String getFollowingCount() {
        return followingCount;
    }

    public String getTagLine() {
        return tagLine;
    }

    public String  getFollowersCount() {
        return followersCount;
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getScreenNameForView() {
        return "@" + screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public User() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeLong(this.uid);
        dest.writeString(this.screenName);
        dest.writeString(this.profileImageUrl);
        dest.writeString(this.tagLine);
        dest.writeString(this.followersCount);
        dest.writeString(this.followingCount);
        dest.writeString(this.bannerImageUrl);
    }

    protected User(Parcel in) {
        this.name = in.readString();
        this.uid = in.readLong();
        this.screenName = in.readString();
        this.profileImageUrl = in.readString();
        this.tagLine = in.readString();
        this.followersCount = in.readString();
        this.followingCount = in.readString();
        this.bannerImageUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setBannerImage(JSONObject response) {
        try {
            this.bannerImageUrl = response.getJSONObject("sizes").getJSONObject("mobile").getString("url");
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }
}
