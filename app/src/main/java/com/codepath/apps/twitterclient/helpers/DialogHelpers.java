package com.codepath.apps.twitterclient.helpers;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.twitterclient.R;

/**
 * Created by litacho on 10/4/15.
 */
public class DialogHelpers {
    static public void showAlert(Context context, String title, String content) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .titleColor(ContextCompat.getColor(context, R.color.primary_twitter_bar))
                .widgetColor(ContextCompat.getColor(context, R.color.primary_twitter_bar))
                .neutralColor(ContextCompat.getColorStateList(context, R.color.button_text))
                .neutralText(R.string.neutral)
                .buttonRippleColor(ContextCompat.getColor(context, R.color.primary_twitter_accent))
                .show();
    }
}
