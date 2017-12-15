package com.mps.esteban.activities.youtube;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.mps.esteban.R;
import com.mps.esteban.application.MyApplication;
import com.mps.esteban.mvp.BaseActivity;
import com.mps.esteban.mvp.BaseActivityForYoutube;
import com.mps.esteban.mvp.BasePresenter;
import com.mps.esteban.utils.IntentManager;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by cosmin on 13.12.2017.
 */

public class YoutubeActivity extends BaseActivityForYoutube<Contract.ContractPresenter> implements Contract.ContractView, YouTubePlayer.OnInitializedListener {

    @BindView(R.id.youtube_view) YouTubePlayerView youTubePlayerView;

    @Inject IntentManager intentManager;

    @Override
    public void setupToolbar() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        youTubePlayerView.initialize(getString(R.string.youtube_api_key), this);

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_youtube;
    }

    @Override
    public Contract.ContractPresenter bindPresenter() {
        return new YoutubePresenter(this);
    }

    @Override
    public void injectDependencies() {
        MyApplication.getAppComponent().inject(this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            youTubePlayer.cueVideo("fhWaJi1Hsfo"); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, IntentManager.RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), youTubeInitializationResult.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentManager.RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTubePlayerView.initialize(getString(R.string.youtube_api_key), this);
        }
    }

}
