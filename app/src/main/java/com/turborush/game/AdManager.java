package com.turborush.game;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdManager {
    private static final String TAG = "AdManager";
    private static final String AD_UNIT_ID = "ca-app-pub-1648653896183802/1488098214"; // Real Ad ID
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1648653896183802/7909202181"; // Real Interstitial ID
    
    private RewardedAd rewardedAd;
    private InterstitialAd interstitialAd;
    
    private Activity activity;
    private boolean isRewardedLoading = false;
    private boolean isInterstitialLoading = false;

    public AdManager(Activity activity) {
        this.activity = activity;
        MobileAds.initialize(activity, initializationStatus -> {
            loadRewardedAd();
            loadInterstitialAd();
        });
    }

    private void loadRewardedAd() {
        if (rewardedAd == null && !isRewardedLoading) {
            isRewardedLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(activity, AD_UNIT_ID, adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, loadAdError.toString());
                        rewardedAd = null;
                        isRewardedLoading = false;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        Log.d(TAG, "Ad was loaded.");
                        rewardedAd = ad;
                        isRewardedLoading = false;
                        
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad was dismissed.");
                                rewardedAd = null;
                                loadRewardedAd(); // Load the next one
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                Log.d(TAG, "Ad failed to show.");
                                rewardedAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });
                    }
                });
        }
    }

    private void loadInterstitialAd() {
        if (interstitialAd == null && !isInterstitialLoading) {
            isInterstitialLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(activity, INTERSTITIAL_AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, loadAdError.toString());
                        interstitialAd = null;
                        isInterstitialLoading = false;
                    }

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        Log.d(TAG, "Interstitial Ad was loaded.");
                        interstitialAd = ad;
                        isInterstitialLoading = false;
                        
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                interstitialAd = null;
                                loadInterstitialAd();
                            }
                            
                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                interstitialAd = null;
                            }
                        });
                    }
                });
        }
    }

    public boolean isAdReady() {
        return rewardedAd != null;
    }

    public void showRewardedAd(OnUserEarnedRewardListener listener) {
        if (rewardedAd != null) {
            rewardedAd.show(activity, listener);
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
            android.widget.Toast.makeText(activity, "Ad is not ready yet. Please try again in a few seconds.", android.widget.Toast.LENGTH_SHORT).show();
            // Try loading again in case it failed previously
            loadRewardedAd();
        }
    }

    public void showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd.show(activity);
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.");
            loadInterstitialAd();
        }
    }
}
