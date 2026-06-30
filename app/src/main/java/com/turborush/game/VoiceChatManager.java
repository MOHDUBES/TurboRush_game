package com.turborush.game;

import android.content.Context;
import android.util.Log;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.Constants;

public class VoiceChatManager {

    private static final String APP_ID = "1aeeff6eb050496399abd324727616c0";
    private RtcEngine mRtcEngine;
    private boolean isJoined = false;
    private boolean isMuted = false;
    private Context mContext;

    public VoiceChatManager(Context context) {
        this.mContext = context;
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d("VoiceChatManager", "Join channel success, uid: " + uid);
            isJoined = true;
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            Log.d("VoiceChatManager", "User joined, uid: " + uid);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.d("VoiceChatManager", "User offline, uid: " + uid);
        }
    };

    public void initEngineIfNeeded() {
        if (mRtcEngine != null) return;
        try {
            io.agora.rtc2.RtcEngineConfig config = new io.agora.rtc2.RtcEngineConfig();
            config.mContext = mContext.getApplicationContext();
            config.mAppId = APP_ID;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
            
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.enableAudio();
            mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        } catch (Exception e) {
            Log.e("VoiceChatManager", "Error initializing Agora RTC Engine", e);
        }
    }

    private String currentChannel = null;

    public String getCurrentChannel() {
        return currentChannel;
    }

    public void joinChannel(String channelName) {
        initEngineIfNeeded();
        if (mRtcEngine != null && !isJoined && !channelName.equals(currentChannel)) {
            currentChannel = channelName;
            
            // Re-enable local audio to recover if permission was just granted
            mRtcEngine.enableLocalAudio(true);
            mRtcEngine.muteLocalAudioStream(isMuted); // Ensure mute state is applied
            mRtcEngine.setEnableSpeakerphone(true);
            
            io.agora.rtc2.ChannelMediaOptions options = new io.agora.rtc2.ChannelMediaOptions();
            options.autoSubscribeAudio = true;
            options.publishMicrophoneTrack = true;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            int result = mRtcEngine.joinChannel(null, channelName, 0, options);
            if (result != 0) {
                Log.e("VoiceChatManager", "joinChannel failed with error code: " + result);
            } else {
                Log.d("VoiceChatManager", "joinChannel initiated successfully");
            }
        }
    }

    public void leaveChannel() {
        if (mRtcEngine != null && (isJoined || currentChannel != null)) {
            mRtcEngine.leaveChannel();
            isJoined = false;
            currentChannel = null;
        }
    }

    public void enableLocalAudio() {
        if (mRtcEngine != null) {
            mRtcEngine.enableLocalAudio(true);
            mRtcEngine.muteLocalAudioStream(isMuted);
            if (isJoined && currentChannel != null) {
                io.agora.rtc2.ChannelMediaOptions options = new io.agora.rtc2.ChannelMediaOptions();
                options.publishMicrophoneTrack = true;
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
                mRtcEngine.updateChannelMediaOptions(options);
            }
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (mRtcEngine != null) {
            mRtcEngine.muteLocalAudioStream(isMuted);
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    private boolean isSpeakerMuted = false;

    public void toggleSpeaker() {
        isSpeakerMuted = !isSpeakerMuted;
        if (mRtcEngine != null) {
            mRtcEngine.muteAllRemoteAudioStreams(isSpeakerMuted);
        }
    }

    public boolean isSpeakerMuted() {
        return isSpeakerMuted;
    }

    public void destroy() {
        if (mRtcEngine != null) {
            leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}
