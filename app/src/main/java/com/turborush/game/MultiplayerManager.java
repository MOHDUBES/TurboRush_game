package com.turborush.game;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MultiplayerManager {

    private static final String TAG = "MultiplayerManager";

    private DatabaseReference db;
    public String currentRoomCode;
    public String myPlayerId;
    
    public boolean isHost = false;
    public boolean matchStarted = false;
    public boolean matchFinished = false;
    public long trackSeed = 0;
    
    private ValueEventListener roomListener;
    private ValueEventListener chatListener;
    private DatabaseReference activeRoomRef;
    
    private Map<String, Long> localMessageTimestamps = new HashMap<>();
    
    public static class OpponentState {
        public String id = "";
        public String name = "";
        public float x = 0f;
        public float score = 0f;
        public boolean crashed = false;
        public int coins = 0;
    }
    
    public Map<String, OpponentState> opponents = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Chat
    public List<ChatMessage> chatMessages = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static class ChatMessage {
        public String sender;
        public String message;
        public long timestamp;
        
        public ChatMessage(String sender, String message, long timestamp) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    public interface OnKickedCallback {
        void onKicked();
    }
    public OnKickedCallback onKickedCallback;

    public MultiplayerManager(String playerId) {
        if (playerId == null || playerId.isEmpty()) {
            this.myPlayerId = "Guest_" + new Random().nextInt(100000);
        } else {
            this.myPlayerId = playerId + "_" + new Random().nextInt(100000);
        }
        try {
            db = FirebaseDatabase.getInstance().getReference("rooms");
        } catch (Exception e) {
            Log.e(TAG, "Firebase DB not initialized", e);
        }
    }

    public void createRoom(String roomCode, String myName, OnRoomJoinCallback callback) {
        if (db == null) { callback.onError("Database not connected"); return; }
        
        currentRoomCode = roomCode;
        trackSeed = System.currentTimeMillis();
        matchStarted = false;
        matchFinished = false;
        isHost = true;
        opponents.clear();
        
        DatabaseReference roomRef = db.child(roomCode);
        
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("host", myPlayerId);
        roomData.put("seed", trackSeed);
        roomData.put("status", "waiting");
        roomData.put("v", 2);
        
        Map<String, Object> hostData = new HashMap<>();
        hostData.put("name", myName);
        hostData.put("x", 0);
        hostData.put("score", 0);
        hostData.put("crashed", false);
        hostData.put("coins", 0);
        
        roomRef.setValue(roomData);
        roomRef.child("players").child(myPlayerId).setValue(hostData);
        
        // If the host disconnects, remove the entire room
        roomRef.onDisconnect().removeValue();
        
        listenToRoom(roomRef, myName);
        callback.onSuccess();
    }

    public void joinRoom(String roomCode, String myName, OnRoomJoinCallback callback) {
        if (db == null) { callback.onError("Database not connected"); return; }
        
        currentRoomCode = roomCode;
        isHost = false;
        opponents.clear();
        DatabaseReference roomRef = db.child(roomCode);
        
        roomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snap = task.getResult();
                if ("playing".equals(snap.child("status").getValue(String.class))) {
                    callback.onError("Room is already in a match!");
                    return;
                }
                
                long playerCount = snap.child("players").getChildrenCount();
                if (playerCount >= 4) {
                    callback.onError("Room is full! Maximum 4 players.");
                    return;
                }
                
                Long seed = snap.child("seed").getValue(Long.class);
                if (seed != null) trackSeed = seed;
                
                Map<String, Object> playerData = new HashMap<>();
                playerData.put("name", myName);
                playerData.put("x", 0);
                playerData.put("score", 0);
                playerData.put("crashed", false);
                
                roomRef.child("players").child(myPlayerId).setValue(playerData);
                
                // If a guest disconnects, remove only their player data
                roomRef.child("players").child(myPlayerId).onDisconnect().removeValue();
                
                if (playerCount == 3) {
                    // This is the 4th player joining, auto-start the match
                    roomRef.child("status").setValue("playing");
                }
                
                listenToRoom(roomRef, myName);
                callback.onSuccess();
            } else {
                callback.onError("Room not found!");
            }
        });
    }

    public void joinRandomRoom(String myName, OnRoomJoinCallback callback) {
        if (db == null) { callback.onError("Database not connected"); return; }
        
        // Find a waiting room by checking the last 50 rooms to avoid needing Firebase .indexOn rules
        db.limitToLast(50).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot roomSnap : task.getResult().getChildren()) {
                    if ("waiting".equals(roomSnap.child("status").getValue(String.class))) {
                        Integer v = roomSnap.child("v").getValue(Integer.class);
                        if (v != null && v >= 2) {
                            long playerCount = roomSnap.child("players").getChildrenCount();
                            if (playerCount < 4) {
                                joinRoom(roomSnap.getKey(), myName, callback);
                                return;
                            }
                        }
                    }
                }
            }
            // If no open room is found, create a new one
            String newRoomCode = String.valueOf(1000 + new Random().nextInt(9000));
            createRoom(newRoomCode, myName, callback);
        });
    }

    public void startGame() {
        if (db != null && currentRoomCode != null && isHost) {
            db.child(currentRoomCode).child("status").setValue("playing");
        }
    }

    public void kickPlayer(String targetPlayerId) {
        if (db != null && currentRoomCode != null && isHost) {
            db.child(currentRoomCode).child("players").child(targetPlayerId).removeValue();
        }
    }

    public void returnToLobby() {
        matchStarted = false;
        matchFinished = false;
        if (db != null && currentRoomCode != null && isHost) {
            db.child(currentRoomCode).child("status").setValue("waiting");
            db.child(currentRoomCode).child("seed").setValue(new Random().nextLong());
        }
    }

    public void resetPlayerState() {
        if (db == null || currentRoomCode == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("crashed", false);
        update.put("score", 0);
        db.child(currentRoomCode).child("players").child(myPlayerId).updateChildren(update);
    }
    
    private void listenToRoom(DatabaseReference roomRef, String myName) {
        activeRoomRef = roomRef;
        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                
                String status = snapshot.child("status").getValue(String.class);
                if ("playing".equals(status)) {
                    matchStarted = true;
                    matchFinished = false;
                } else if ("finished".equals(status)) {
                    matchStarted = false;
                    matchFinished = true;
                } else if ("waiting".equals(status)) {
                    matchStarted = false;
                    matchFinished = false;
                }
                
                DataSnapshot players = snapshot.child("players");
                
                if (!isHost && !players.hasChild(myPlayerId)) {
                    if (onKickedCallback != null) {
                        onKickedCallback.onKicked();
                    }
                    return;
                }
                
                opponents.clear();
                for (DataSnapshot p : players.getChildren()) {
                    if (!p.getKey().equals(myPlayerId)) {
                        OpponentState opp = new OpponentState();
                        opp.name = p.child("name").getValue(String.class);
                        Float x = p.child("x").getValue(Float.class);
                        Float score = p.child("score").getValue(Float.class);
                        Boolean crashed = p.child("crashed").getValue(Boolean.class);
                        Integer coins = p.child("coins").getValue(Integer.class);
                        
                        if (x != null) opp.x = x;
                        if (score != null) opp.score = score;
                        if (crashed != null) opp.crashed = crashed;
                        if (coins != null) opp.coins = coins;
                        opp.id = p.getKey();
                        
                        if (opp.name != null) opponents.put(p.getKey(), opp);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomRef.addValueEventListener(roomListener);
        
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                long now = System.currentTimeMillis();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    String sender = msgSnap.child("sender").getValue(String.class);
                    String text = msgSnap.child("text").getValue(String.class);
                    String key = msgSnap.getKey();
                    
                    if (sender != null && text != null && key != null) {
                        long localReceiveTime;
                        if (localMessageTimestamps.containsKey(key)) {
                            localReceiveTime = localMessageTimestamps.get(key);
                        } else {
                            localReceiveTime = System.currentTimeMillis();
                            localMessageTimestamps.put(key, localReceiveTime);
                        }
                        chatMessages.add(new ChatMessage(sender, text, localReceiveTime));
                    }
                }
                if (chatMessages.size() > 5) {
                    chatMessages = new ArrayList<>(chatMessages.subList(chatMessages.size() - 5, chatMessages.size()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomRef.child("chat").addValueEventListener(chatListener);
    }
    private long lastUpdateTime = 0;

    public void updateMyPosition(float x, float score, boolean crashed, int coins) {
        if (db == null || currentRoomCode == null) return;
        
        long now = System.currentTimeMillis();
        // Throttle updates to ~10 per second to avoid overwhelming Firebase and getting disconnected
        if (!crashed && now - lastUpdateTime < 100) return;
        lastUpdateTime = now;
        Map<String, Object> update = new HashMap<>();
        update.put("x", x);
        update.put("score", score);
        update.put("crashed", crashed);
        update.put("coins", coins);
        
        db.child(currentRoomCode).child("players").child(myPlayerId).updateChildren(update);
        
        if (crashed && isHost) {
            checkAllPlayersCrashed();
        }
    }
    
    public void checkAllPlayersCrashed() {
        // Deprecated, use isEveryoneCrashed in update loop instead
    }
    
    public boolean isEveryoneCrashed() {
        for (OpponentState opp : opponents.values()) {
            if (!opp.crashed) return false;
        }
        return true;
    }
    
    public void setMatchFinished() {
        if (db != null && currentRoomCode != null && isHost) {
            db.child(currentRoomCode).child("status").setValue("finished");
            matchFinished = true;
        }
    }
    
    public void sendChatMessage(String senderName, String text) {
        if (db == null || currentRoomCode == null) return;
        
        String key = db.child(currentRoomCode).child("chat").push().getKey();
        if (key != null) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("sender", senderName);
            msg.put("text", text);
            msg.put("time", System.currentTimeMillis());
            db.child(currentRoomCode).child("chat").child(key).setValue(msg);
        }
    }
    
    
    public void leaveRoom() {
        if (db != null && currentRoomCode != null) {
            db.child(currentRoomCode).child("players").child(myPlayerId).removeValue();
            
            if (activeRoomRef != null) {
                if (roomListener != null) activeRoomRef.removeEventListener(roomListener);
                if (chatListener != null) activeRoomRef.child("chat").removeEventListener(chatListener);
            }
            
            activeRoomRef = null;
            roomListener = null;
            chatListener = null;
            
            currentRoomCode = null;
            matchStarted = false;
            isHost = false;
        }
    }

    public interface OnRoomJoinCallback {
        void onSuccess();
        void onError(String error);
    }
}
