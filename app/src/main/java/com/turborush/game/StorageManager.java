package com.turborush.game;

import android.content.Context;
import android.content.SharedPreferences;

import com.turborush.game.models.PlayerProgress;
import com.turborush.game.models.Vehicle;
import com.turborush.game.models.Track;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * StorageManager — Handles all SharedPreferences persistence for TurboRush.
 */
public class StorageManager {

    private static final String PREFS_NAME           = "turborush_prefs";
    private static final String KEY_TOTAL_COINS      = "total_coins";
    private static final String KEY_BEST_SCORE       = "best_score";
    private static final String KEY_TOP_SCORES       = "top_scores";
    private static final String KEY_TOP_DATES        = "top_dates";
    private static final String KEY_OWNED_VEHICLES   = "owned_vehicles";
    private static final String KEY_SELECTED_VEHICLE = "selected_vehicle";
    private static final String KEY_OWNED_TRACKS     = "owned_tracks";
    private static final String KEY_SELECTED_TRACK   = "selected_track";
    private static final String KEY_VEHICLE_COLORS   = "vehicle_colors";
    private static final String KEY_UNLOCKED_COLORS  = "unlocked_colors";
    private static final String KEY_MUTED            = "muted";
    private static final String KEY_SFX_MUTED        = "sfx_muted";
    private static final String KEY_NIGHT_MODE       = "night_mode";

    private static final String KEY_PLAYER_NAME      = "player_name";
    private static final String KEY_AVATAR_ID        = "avatar_id";
    private static final String KEY_AVATAR_URI       = "avatar_uri";
    private static final String KEY_IS_LOGGED_IN     = "is_logged_in";
    private static final String KEY_LOGIN_PROVIDER   = "login_provider";
    private static final String KEY_PLAYER_LEVEL     = "player_level";
    private static final String KEY_PLAYER_XP        = "player_xp";
    private static final String KEY_TOTAL_RACES      = "total_races";
    private static final String KEY_TOTAL_DISTANCE   = "total_distance";
    private static final String KEY_TOP_SPEED        = "top_speed";

    private final SharedPreferences prefs;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public StorageManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveProgress(PlayerProgress progress) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TOTAL_COINS, progress.totalCoins);
        editor.putLong(KEY_BEST_SCORE, progress.bestScore);
        editor.putString(KEY_OWNED_VEHICLES, progress.ownedVehicleIds);
        editor.putInt(KEY_SELECTED_VEHICLE, progress.selectedVehicleId);
        editor.putString(KEY_OWNED_TRACKS, progress.ownedTrackIds);
        editor.putInt(KEY_SELECTED_TRACK, progress.selectedTrackId);
        editor.putString(KEY_VEHICLE_COLORS, progress.vehicleColorMap);
        editor.putString(KEY_UNLOCKED_COLORS, progress.unlockedColorsMap);
        editor.putBoolean(KEY_MUTED, progress.isMuted);
        editor.putBoolean(KEY_SFX_MUTED, progress.isSfxMuted);
        editor.putBoolean(KEY_NIGHT_MODE, progress.isNightMode);

        editor.putString(KEY_PLAYER_NAME, progress.playerName);
        editor.putInt(KEY_AVATAR_ID, progress.avatarId);
        editor.putString(KEY_AVATAR_URI, progress.avatarUri);
        editor.putBoolean(KEY_IS_LOGGED_IN, progress.isLoggedIn);
        editor.putString(KEY_LOGIN_PROVIDER, progress.loginProvider);
        editor.putInt(KEY_PLAYER_LEVEL, progress.playerLevel);
        editor.putLong(KEY_PLAYER_XP, progress.playerXp);
        editor.putInt(KEY_TOTAL_RACES, progress.totalRaces);
        editor.putLong(KEY_TOTAL_DISTANCE, progress.totalDistance);
        editor.putFloat(KEY_TOP_SPEED, progress.topSpeedReached);

        StringBuilder scoresSb = new StringBuilder();
        StringBuilder datesSb  = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i > 0) { scoresSb.append(","); datesSb.append(","); }
            scoresSb.append(progress.topScores[i]);
            datesSb.append(progress.topScoreDates[i] != null ? progress.topScoreDates[i] : "");
        }
        editor.putString(KEY_TOP_SCORES, scoresSb.toString());
        editor.putString(KEY_TOP_DATES,  datesSb.toString());
        editor.apply();
    }

    public PlayerProgress loadProgress() {
        PlayerProgress p = new PlayerProgress();
        p.totalCoins        = prefs.getInt(KEY_TOTAL_COINS, 0);
        p.bestScore         = prefs.getLong(KEY_BEST_SCORE, 0);
        p.ownedVehicleIds   = prefs.getString(KEY_OWNED_VEHICLES, "0");
        p.selectedVehicleId = prefs.getInt(KEY_SELECTED_VEHICLE, 0);
        p.ownedTrackIds     = prefs.getString(KEY_OWNED_TRACKS, "0");
        p.selectedTrackId   = prefs.getInt(KEY_SELECTED_TRACK, 0);
        p.vehicleColorMap = prefs.getString(KEY_VEHICLE_COLORS, "");
        p.unlockedColorsMap = prefs.getString(KEY_UNLOCKED_COLORS, "");
        p.isMuted = prefs.getBoolean(KEY_MUTED, false);
        p.isSfxMuted = prefs.getBoolean(KEY_SFX_MUTED, false);
        p.isNightMode = prefs.getBoolean(KEY_NIGHT_MODE, false);

        p.playerName        = prefs.getString(KEY_PLAYER_NAME, "Racer");
        p.avatarId          = prefs.getInt(KEY_AVATAR_ID, 0);
        p.avatarUri         = prefs.getString(KEY_AVATAR_URI, "");
        p.isLoggedIn        = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        p.loginProvider     = prefs.getString(KEY_LOGIN_PROVIDER, "");
        p.playerLevel       = prefs.getInt(KEY_PLAYER_LEVEL, 1);
        p.playerXp          = prefs.getLong(KEY_PLAYER_XP, 0);
        p.totalRaces        = prefs.getInt(KEY_TOTAL_RACES, 0);
        p.totalDistance     = prefs.getLong(KEY_TOTAL_DISTANCE, 0);
        p.topSpeedReached   = prefs.getFloat(KEY_TOP_SPEED, 0f);

        String scoresStr = prefs.getString(KEY_TOP_SCORES, "0,0,0,0,0");
        String datesStr  = prefs.getString(KEY_TOP_DATES,  ",,,,");
        String[] scoresArr = scoresStr.split(",", -1);
        String[] datesArr  = datesStr.split(",", -1);
        for (int i = 0; i < 5; i++) {
            try { p.topScores[i] = Long.parseLong(scoresArr[i]); } catch (Exception e) { p.topScores[i] = 0; }
            p.topScoreDates[i] = (datesArr.length > i) ? datesArr[i] : "";
        }
        return p;
    }

    public boolean submitScore(PlayerProgress progress, long runScore, int runCoins) {
        boolean isNewBest = false;
        progress.totalCoins += runCoins;
        if (runScore > progress.bestScore) {
            progress.bestScore = runScore;
            isNewBest = true;
        }
        insertScore(progress, runScore);
        saveProgress(progress);
        
        // Push to Global Leaderboard if logged in
        if (progress.isLoggedIn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> data = new HashMap<>();
                data.put("playerName", progress.playerName);
                data.put("bestScore", progress.bestScore);
                data.put("totalDistance", progress.totalDistance);
                data.put("avatarId", progress.avatarId);
                data.put("timestamp", System.currentTimeMillis());
                
                db.collection("leaderboard").document(user.getUid()).set(data);
            }
        }
        
        return isNewBest;
    }

    private void insertScore(PlayerProgress p, long score) {
        String today = dateFormat.format(new Date());
        for (int i = 0; i < 5; i++) {
            if (score > p.topScores[i]) {
                for (int j = 4; j > i; j--) {
                    p.topScores[j]     = p.topScores[j-1];
                    p.topScoreDates[j] = p.topScoreDates[j-1];
                }
                p.topScores[i]     = score;
                p.topScoreDates[i] = today;
                return;
            }
        }
    }

    public boolean unlockVehicle(PlayerProgress progress, Vehicle vehicle) {
        if (progress.totalCoins < vehicle.unlockCost) return false;
        progress.totalCoins -= vehicle.unlockCost;
        if (!isVehicleOwned(progress, vehicle.id)) {
            progress.ownedVehicleIds += "," + vehicle.id;
        }
        saveProgress(progress);
        return true;
    }

    public boolean isVehicleOwned(PlayerProgress progress, int vehicleId) {
        for (String p : progress.ownedVehicleIds.split(",")) {
            try { if (Integer.parseInt(p.trim()) == vehicleId) return true; }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }

    public void selectVehicle(PlayerProgress progress, int vehicleId) {
        progress.selectedVehicleId = vehicleId;
        saveProgress(progress);
    }

    public boolean unlockTrack(PlayerProgress progress, Track track) {
        if (progress.totalCoins < track.unlockCost) return false;
        progress.totalCoins -= track.unlockCost;
        if (!isTrackOwned(progress, track.id)) {
            progress.ownedTrackIds += "," + track.id;
        }
        saveProgress(progress);
        return true;
    }

    public boolean isTrackOwned(PlayerProgress progress, int trackId) {
        for (String p : progress.ownedTrackIds.split(",")) {
            try { if (Integer.parseInt(p.trim()) == trackId) return true; }
            catch (NumberFormatException ignored) {}
        }
        return false;
    }

    public void selectTrack(PlayerProgress progress, int trackId) {
        progress.selectedTrackId = trackId;
        saveProgress(progress);
    }

    public void saveVehicleColor(PlayerProgress progress, int vehicleId, int colorIdx) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        if (!progress.vehicleColorMap.isEmpty()) {
            for (String entry : progress.vehicleColorMap.split(",")) {
                String[] kv = entry.split(":");
                if (kv.length == 2) {
                    if (sb.length() > 0) sb.append(",");
                    if (kv[0].equals(String.valueOf(vehicleId))) {
                        sb.append(vehicleId).append(":").append(colorIdx);
                        found = true;
                    } else { sb.append(entry); }
                }
            }
        }
        if (!found) {
            if (sb.length() > 0) sb.append(",");
            sb.append(vehicleId).append(":").append(colorIdx);
        }
        progress.vehicleColorMap = sb.toString();
        saveProgress(progress);
    }

    public int getVehicleColorIndex(PlayerProgress progress, int vehicleId) {
        if (progress.vehicleColorMap == null || progress.vehicleColorMap.isEmpty()) return 0;
        for (String entry : progress.vehicleColorMap.split(",")) {
            String[] kv = entry.split(":");
            if (kv.length == 2 && kv[0].equals(String.valueOf(vehicleId))) {
                try { return Integer.parseInt(kv[1]); } catch (NumberFormatException e) { return 0; }
            }
        }
        return 0;
    }

    public boolean isColorUnlocked(PlayerProgress progress, int vehicleId, int colorIdx) {
        if (colorIdx == 0) return true; // Color 0 is always unlocked
        if (progress.unlockedColorsMap == null || progress.unlockedColorsMap.isEmpty()) return false;
        
        for (String entry : progress.unlockedColorsMap.split(",")) {
            String[] kv = entry.split(":");
            if (kv.length == 2 && kv[0].equals(String.valueOf(vehicleId))) {
                String[] colors = kv[1].split("_");
                for (String c : colors) {
                    try {
                        if (Integer.parseInt(c) == colorIdx) return true;
                    } catch (NumberFormatException ignored) {}
                }
                return false;
            }
        }
        return false;
    }

    public void unlockColor(PlayerProgress progress, int vehicleId, int colorIdx) {
        if (isColorUnlocked(progress, vehicleId, colorIdx)) return;
        
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        if (progress.unlockedColorsMap != null && !progress.unlockedColorsMap.isEmpty()) {
            for (String entry : progress.unlockedColorsMap.split(",")) {
                String[] kv = entry.split(":");
                if (kv.length == 2) {
                    if (sb.length() > 0) sb.append(",");
                    if (kv[0].equals(String.valueOf(vehicleId))) {
                        sb.append(vehicleId).append(":").append(kv[1]).append("_").append(colorIdx);
                        found = true;
                    } else {
                        sb.append(entry);
                    }
                }
            }
        }
        if (!found) {
            if (sb.length() > 0) sb.append(",");
            sb.append(vehicleId).append(":0_").append(colorIdx); // 0 is always unlocked implicitly, but we append it here
        }
        progress.unlockedColorsMap = sb.toString();
        saveProgress(progress);
    }

    public void saveTiltPreference(Context ctx, boolean enabled) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        p.edit().putBoolean("tilt_enabled", enabled).apply();
    }

    public boolean getTiltPreference(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return p.getBoolean("tilt_enabled", false);
    }

    public void saveMutePref(PlayerProgress progress) {
        saveProgress(progress);
    }

    public void saveNightMode(PlayerProgress progress) {
        saveProgress(progress);
    }

    public void addFriend(String friendId, String friendName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && friendId != null && !friendId.isEmpty()) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            Map<String, Object> friendData = new HashMap<>();
            friendData.put("name", friendName);
            db.child("users").child(user.getUid()).child("friends").child(friendId).setValue(friendData);
        }
    }

    public interface FriendsCallback {
        void onFriendsFetched(java.util.List<String> friendNames);
    }

    public void fetchFriends(FriendsCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            db.child("users").child(user.getUid()).child("friends").get().addOnCompleteListener(task -> {
                java.util.List<String> friends = new java.util.ArrayList<>();
                if (task.isSuccessful() && task.getResult().exists()) {
                    for (com.google.firebase.database.DataSnapshot snap : task.getResult().getChildren()) {
                        String name = snap.child("name").getValue(String.class);
                        if (name != null) friends.add(name);
                    }
                }
                if (callback != null) callback.onFriendsFetched(friends);
            });
        } else {
            if (callback != null) callback.onFriendsFetched(new java.util.ArrayList<>());
        }
    }
    
    public static class Friend {
        public String id;
        public String name;
        public Friend(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public interface FriendsDataCallback {
        void onFriendsFetched(java.util.List<Friend> friends);
    }

    public void fetchFriendsData(FriendsDataCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            db.child("users").child(user.getUid()).child("friends").get().addOnCompleteListener(task -> {
                java.util.List<Friend> friends = new java.util.ArrayList<>();
                if (task.isSuccessful() && task.getResult().exists()) {
                    for (com.google.firebase.database.DataSnapshot snap : task.getResult().getChildren()) {
                        String id = snap.getKey();
                        String name = snap.child("name").getValue(String.class);
                        if (name != null && id != null) friends.add(new Friend(id, name));
                    }
                }
                if (callback != null) callback.onFriendsFetched(friends);
            });
        } else {
            if (callback != null) callback.onFriendsFetched(new java.util.ArrayList<>());
        }
    }

    public void sendInvite(String friendId, String roomCode, String myName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && friendId != null) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            Map<String, Object> inviteData = new HashMap<>();
            inviteData.put("roomCode", roomCode);
            inviteData.put("fromName", myName);
            inviteData.put("timestamp", System.currentTimeMillis());
            db.child("users").child(friendId).child("invites").child(user.getUid()).setValue(inviteData);
        }
    }
    
    public static class InviteInfo {
        public String fromUid;
        public String fromName;
        public String roomCode;
    }

    public interface InviteCallback {
        void onInviteReceived(InviteInfo invite);
    }
    
    private com.google.firebase.database.ValueEventListener invitesListener;

    public void listenForInvites(InviteCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("invites");
            if (invitesListener != null) {
                invitesRef.removeEventListener(invitesListener);
            }
            invitesListener = new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            InviteInfo info = new InviteInfo();
                            info.fromUid = child.getKey();
                            info.fromName = child.child("fromName").getValue(String.class);
                            info.roomCode = child.child("roomCode").getValue(String.class);
                            
                            // Delete invite so we don't process it again next time
                            child.getRef().removeValue();
                            
                            if (callback != null && info.roomCode != null) {
                                callback.onInviteReceived(info);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            invitesRef.addValueEventListener(invitesListener);
        }
    }
}
