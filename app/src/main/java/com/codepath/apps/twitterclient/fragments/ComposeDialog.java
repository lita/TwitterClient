package com.codepath.apps.twitterclient.fragments;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

/**
 * Created by litacho on 10/2/15.
 */
public class ComposeDialog extends DialogFragment {

    final static int TWITTER_CHARACTER_LIMIT = 140;

    public interface ComposeTweetListener {
        void sendTweet(String tweet, @Nullable Tweet reTweet);
    }

    private EditText etCompose;
    private Button btTweet;
    private ImageButton ibtDimiss;
    private TextView tvCounter;
    private TextWatcher editTextListener;
    private RoundedImageView rivUserProfile;
    private Tweet retweet;

    public ComposeDialog() {

    }

    public static ComposeDialog newInstance(User user) {
        ComposeDialog dialog = new ComposeDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static ComposeDialog newInstance(User user, Tweet tweet) {
        ComposeDialog dialog = new ComposeDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        bundle.putParcelable("retweet", tweet);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_dialog, container);
    }

    protected boolean validateTweet(String tweet) {
        if (tweet.isEmpty() || tweet.length() > 140) {
            return false;
        }

        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rivUserProfile = (RoundedImageView) view.findViewById(R.id.rivUserProfile);
        btTweet = (Button) view.findViewById(R.id.btTweet);
        ibtDimiss = (ImageButton) view.findViewById(R.id.ibtDismiss);
        etCompose = (EditText) view.findViewById(R.id.etCompose);
        tvCounter = (TextView) view.findViewById(R.id.tvCounter);

        User user = getArguments().getParcelable("user");
        retweet = getArguments().getParcelable("retweet");
        if (retweet != null) {
            String username = retweet.getUser().getScreenNameForView();
            etCompose.setText(username);
            etCompose.setSelection(username.length());
        }
        Picasso.with(getActivity()).load(user.getProfileImageUrl()).into(rivUserProfile);

        btTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweet = etCompose.getText().toString();
                if (!validateTweet(tweet)) {
                    return;
                }

                ComposeTweetListener listener = (ComposeTweetListener) getActivity();
                listener.sendTweet(tweet, retweet);
                dismiss();
            }
        });

        ibtDimiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setUpEditView();
    }

    private void setUpEditView() {
        etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        editTextListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int totalCount = s.length();
                if (totalCount > ComposeDialog.TWITTER_CHARACTER_LIMIT) {
                    etCompose.removeTextChangedListener(editTextListener);
                    CharSequence withinLimit = s.subSequence(0, ComposeDialog.TWITTER_CHARACTER_LIMIT).toString();
                    CharSequence excessString = s.subSequence(ComposeDialog.TWITTER_CHARACTER_LIMIT, s.length());
                    String colorAdjustedString = "<font color='#ff0000'>" +  excessString.toString() + "</font>";
                    etCompose.setText(Html.fromHtml(withinLimit + colorAdjustedString));
                    etCompose.setSelection(totalCount);
                    etCompose.addTextChangedListener(editTextListener);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int count = ComposeDialog.TWITTER_CHARACTER_LIMIT - s.toString().length();
                tvCounter.setText(Integer.toString(count));

                if (count < 20 && count > 10) {
                    tvCounter.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_twitter_bar));
                } else if (count <= 10) {
                    tvCounter.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));
                } else {
                    tvCounter.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                }

                if (count < 0) {
                    btTweet.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.tweet_button_faded));
                } else {
                    btTweet.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.tweet_button));
                }
            }
        };

        etCompose.addTextChangedListener(editTextListener);
    }

    @Override
    public void onResume() {
        if (getDialog().getActionBar() != null) {
            getDialog().getActionBar().hide();
        }

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }
}
