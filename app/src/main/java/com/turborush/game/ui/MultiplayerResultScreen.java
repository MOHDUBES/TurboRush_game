package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.turborush.game.MultiplayerManager;
import com.turborush.game.models.PlayerProgress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MultiplayerResultScreen {

    private float screenW, screenH;
    private Paint pText, pBg, pPanel, pBtn;

    public RectF btnCancel, btnStartAgain;
    
    public List<RectF> btnAddFriends = new ArrayList<>();
    public List<String> btnAddFriendIds = new ArrayList<>();
    public List<String> btnAddFriendNames = new ArrayList<>();

    public void init(float w, float h) {
        screenW = w;
        screenH = h;

        pText = new Paint(Paint.ANTI_ALIAS_FLAG);
        pText.setFakeBoldText(true);

        pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBg.setColor(Color.parseColor("#E6000000")); // dark overlay

        pPanel = new Paint(Paint.ANTI_ALIAS_FLAG);
        pPanel.setColor(Color.parseColor("#222222"));

        pBtn = new Paint(Paint.ANTI_ALIAS_FLAG);

        float btnW = w * 0.4f;
        float btnH = h * 0.08f;
        
        float gap = w * 0.05f;

        btnCancel = new RectF(w/2 - btnW - gap/2, h * 0.85f, w/2 - gap/2, h * 0.85f + btnH);
        btnStartAgain = new RectF(w/2 + gap/2, h * 0.85f, w/2 + btnW + gap/2, h * 0.85f + btnH);
    }

    private static class PlayerResult {
        String id;
        String name;
        float score;
        int coins;
        boolean isMe;
        public PlayerResult(String id, String name, float score, int coins, boolean isMe) {
            this.id = id;
            this.name = name;
            this.score = score;
            this.coins = coins;
            this.isMe = isMe;
        }
    }

    public void draw(Canvas canvas, PlayerProgress progress, float myScore, int myCoins, MultiplayerManager multiplayerManager) {
        // Draw Dark Background
        canvas.drawRect(0, 0, screenW, screenH, pBg);

        // Draw Panel
        float panelMargin = 40f;
        RectF panel = new RectF(panelMargin, screenH * 0.15f, screenW - panelMargin, screenH * 0.8f);
        canvas.drawRoundRect(panel, 30f, 30f, pPanel);

        // Title
        pText.setColor(Color.parseColor("#FFD700")); // Gold
        pText.setTextSize(screenW * 0.1f);
        pText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("RACE RESULTS", screenW / 2, screenH * 0.25f, pText);

        // Build list of all players
        List<PlayerResult> results = new ArrayList<>();
        results.add(new PlayerResult(multiplayerManager != null ? multiplayerManager.myPlayerId : "", progress.playerName + " (You)", myScore, myCoins, true));

        if (multiplayerManager != null && multiplayerManager.opponents != null) {
            for (Map.Entry<String, MultiplayerManager.OpponentState> entry : multiplayerManager.opponents.entrySet()) {
                MultiplayerManager.OpponentState opp = entry.getValue();
                results.add(new PlayerResult(opp.id, opp.name, opp.score, opp.coins, false));
            }
        }
        
        btnAddFriends.clear();
        btnAddFriendIds.clear();
        btnAddFriendNames.clear();

        // Sort descending by score
        Collections.sort(results, new Comparator<PlayerResult>() {
            @Override
            public int compare(PlayerResult o1, PlayerResult o2) {
                return Float.compare(o2.score, o1.score);
            }
        });

        // Draw Leaderboard
        pText.setTextAlign(Paint.Align.LEFT);
        float startY = screenH * 0.35f;
        float rowHeight = 90f;
        
        // Table Headers
        pText.setTextSize(40f);
        pText.setColor(Color.GRAY);
        canvas.drawText("RANK", panelMargin + 40f, startY, pText);
        canvas.drawText("NAME", panelMargin + 170f, startY, pText);
        
        pText.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("COINS", screenW - panelMargin - 220f, startY, pText);
        canvas.drawText("DIST", screenW - panelMargin - 40f, startY, pText);
        pText.setTextAlign(Paint.Align.LEFT);
        
        startY += 60f; // Shift down for rows
        
        for (int i = 0; i < results.size(); i++) {
            PlayerResult r = results.get(i);
            
            // Rank
            pText.setTextSize(55f);
            if (i == 0) pText.setColor(Color.parseColor("#FFD700")); // Gold
            else if (i == 1) pText.setColor(Color.parseColor("#C0C0C0")); // Silver
            else if (i == 2) pText.setColor(Color.parseColor("#CD7F32")); // Bronze
            else pText.setColor(Color.WHITE);
            
            canvas.drawText((i + 1) + ".", panelMargin + 50f, startY + (i * rowHeight), pText);
            
            // Name
            pText.setTextSize(45f);
            pText.setColor(Color.WHITE);
            // truncate name if too long
            String dName = r.name;
            if (dName.length() > 12) dName = dName.substring(0, 10) + "..";
            canvas.drawText(dName, panelMargin + 170f, startY + (i * rowHeight), pText);
            
            // Coins & Score
            pText.setTextAlign(Paint.Align.RIGHT);
            pText.setColor(Color.parseColor("#FFD700")); // Gold for coins
            canvas.drawText(String.valueOf(r.coins), screenW - panelMargin - 220f, startY + (i * rowHeight), pText);
            
            pText.setColor(Color.parseColor("#00FF00")); // Green for distance
            canvas.drawText((int)r.score + "m", screenW - panelMargin - 40f, startY + (i * rowHeight), pText);
            
            pText.setTextAlign(Paint.Align.LEFT); // reset
            
            if (!r.isMe && r.id != null && !r.id.isEmpty()) {
                RectF addFriendRect = new RectF(screenW - panelMargin - 350f, startY + (i * rowHeight) - 40f, screenW - panelMargin - 280f, startY + (i * rowHeight) + 10f);
                btnAddFriends.add(addFriendRect);
                btnAddFriendIds.add(r.id);
                btnAddFriendNames.add(r.name);
                
                pBtn.setColor(Color.parseColor("#2196F3")); // Blue
                canvas.drawRoundRect(addFriendRect, 10f, 10f, pBtn);
                pText.setColor(Color.WHITE);
                pText.setTextSize(30f);
                pText.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("+", addFriendRect.centerX(), addFriendRect.centerY() + 10f, pText);
                pText.setTextAlign(Paint.Align.LEFT); // reset
            }
        }

        // Cancel Button
        pBtn.setColor(Color.parseColor("#FF3B30"));
        canvas.drawRoundRect(btnCancel, 20f, 20f, pBtn);
        pText.setColor(Color.WHITE);
        pText.setTextSize(screenW * 0.05f);
        pText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("CANCEL", btnCancel.centerX(), btnCancel.centerY() + 15, pText);

        // Start Again Button
        pBtn.setColor(Color.parseColor("#4CAF50")); // Green
        canvas.drawRoundRect(btnStartAgain, 20f, 20f, pBtn);
        pText.setColor(Color.WHITE);
        pText.setTextSize(screenW * 0.05f);
        pText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("START AGAIN", btnStartAgain.centerX(), btnStartAgain.centerY() + 15, pText);
    }
}
