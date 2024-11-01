package com.pegalite.fourgonly;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.pegalite.fourgonly.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private InterstitialAd mInterstitialAd;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, initializationStatus -> {
        });

        // Window Styling
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        loadAd();

        new Handler().postDelayed(() -> {
            if (mInterstitialAd == null) {
                binding.mainContainer.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }
        }, 5000);
        binding.set4GOnly.setOnClickListener(view -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(this);
            } else {
                openPhoneInfo();
            }
        });
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.ad_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                binding.mainContainer.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                mInterstitialAd = interstitialAd;
                setupAdCallbacks();
                Log.i(TAG, "Ad loaded successfully");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "Ad failed to load: " + loadAdError);
                mInterstitialAd = null;
            }
        });
    }

    private void setupAdCallbacks() {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                Log.d(TAG, "Ad clicked");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                openPhoneInfo();
                Log.d(TAG, "Ad dismissed");
                mInterstitialAd = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                Log.e(TAG, "Ad failed to show: " + adError);
                mInterstitialAd = null;
            }

            @Override
            public void onAdImpression() {
                Log.d(TAG, "Ad impression recorded");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad shown");
            }
        });
    }

    private void openPhoneInfo() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                intent.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo");
            } else {
                intent.setClassName("com.android.settings", "com.android.settings.RadioInfo");
            }
            startActivity(intent);
        } catch (Exception e) {
            openAlternativePhoneInfo();
        }
    }

    private void openAlternativePhoneInfo() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:*#*#4636#*#*"));
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivity(intent);
            } catch (Exception e3) {
                Log.e(TAG, "Failed to open settings", e3);
            }
        }
    }
}
