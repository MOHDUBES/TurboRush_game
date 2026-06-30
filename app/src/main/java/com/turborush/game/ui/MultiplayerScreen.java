package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.turborush.game.MultiplayerManager;

import java.util.HashMap;
import java.util.Map;

public class MultiplayerScreen {
    private float screenW, screenH;
    private Paint pText, pBg, pPanel, pBtn;

    public RectF btnCreate, btnJoin, btnRandom, btnBack, btnStartGame;
    public boolean isWaitingForPlayer = false;
    public String currentRoomCode = "";
    public String errorMessage = "";
    
    public Map<String, RectF> kickButtons = new HashMap<>();
    
    public java.util.List<com.turborush.game.StorageManager.Friend> friends = new java.util.ArrayList<>();
    public Map<String, RectF> inviteButtons = new HashMap<>();
    
    // We pass the manager to draw current players in the lobby
    public MultiplayerManager multiplayerManager;

    public void init(float w, float h) {
        screenW = w;
        screenH = h;

        pText = new Paint(Paint.ANTI_ALIAS_FLAG);
        pText.setFakeBoldText(true);

        pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBg.setColor(Color.parseColor("#E6000000"));

        pPanel = new Paint(Paint.ANTI_ALIAS_FLAG);
        pPanel.setColor(Color.parseColor("#222222"));
        
        pBtn = new Paint(Paint.ANTI_ALIAS_FLAG);

        float btnW = w * 0.6f;
        float btnH = h * 0.1f;
        
        btnCreate = new RectF(w/2 - btnW/2, h * 0.25f, w/2 + btnW/2, h * 0.25f + btnH);
        btnJoin = new RectF(w/2 - btnW/2, h * 0.40f, w/2 + btnW/2, h * 0.40f + btnH);
        btnRandom = new RectF(w/2 - btnW/2, h * 0.55f, w/2 + btnW/2, h * 0.55f + btnH);
        
        float backW = w * 0.4f;
        float backH = h * 0.08f;
        btnBack = new RectF(w/2 - backW/2, h * 0.85f, w/2 + backW/2, h * 0.85f + backH);
        
        btnStartGame = new RectF(w/2 - btnW/2, h * 0.70f, w/2 + btnW/2, h * 0.70f + btnH);
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(0, 0, screenW, screenH, pBg);

        pText.setTextSize(screenW * 0.08f);
        pText.setColor(Color.parseColor("#00FFFF")); // Cyan
        pText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("MULTIPLAYER LOBBY", screenW / 2, screenH * 0.12f, pText);

        if (isWaitingForPlayer) {
            pText.setTextSize(screenW * 0.06f);
            pText.setColor(Color.WHITE);
            canvas.drawText("Room Created!", screenW / 2, screenH * 0.22f, pText);
            
            pText.setTextSize(screenW * 0.08f);
            pText.setColor(Color.YELLOW);
            canvas.drawText("CODE: " + currentRoomCode, screenW / 2, screenH * 0.30f, pText);
            
            // Draw list of joined players
            pText.setTextSize(screenW * 0.04f);
            pText.setColor(Color.LTGRAY);
            canvas.drawText("Players in Room:", screenW * 0.30f, screenH * 0.38f, pText);
            
            pText.setColor(Color.WHITE);
            canvas.drawText("1. You", screenW * 0.30f, screenH * 0.43f, pText);
            
            int pIndex = 2;
            kickButtons.clear();
            if (multiplayerManager != null && !multiplayerManager.opponents.isEmpty()) {
                for (Map.Entry<String, MultiplayerManager.OpponentState> entry : multiplayerManager.opponents.entrySet()) {
                    float yPos = screenH * 0.43f + (pIndex - 1) * screenH * 0.05f;
                    canvas.drawText(pIndex + ". " + entry.getValue().name, screenW * 0.30f, yPos, pText);
                    
                    if (multiplayerManager.isHost) {
                        float textWidth = pText.measureText(pIndex + ". " + entry.getValue().name);
                        float btnX = screenW * 0.30f + textWidth / 2 + 50f;
                        
                        RectF kickBtn = new RectF(btnX - 30f, yPos - 30f, btnX + 50f, yPos + 10f);
                        kickButtons.put(entry.getKey(), kickBtn);
                        
                        pBtn.setColor(Color.RED);
                        canvas.drawRoundRect(kickBtn, 10f, 10f, pBtn);
                        
                        Paint pSmallText = new Paint(Paint.ANTI_ALIAS_FLAG);
                        pSmallText.setFakeBoldText(true);
                        pSmallText.setColor(Color.WHITE);
                        pSmallText.setTextSize(screenW * 0.03f);
                        pSmallText.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText("KICK", kickBtn.centerX(), kickBtn.centerY() + pSmallText.getTextSize()/3, pSmallText);
                    }
                    
                    pIndex++;
                }
            }
            
            // Draw Friends List to invite
            pText.setColor(Color.parseColor("#10B981")); // Emerald
            canvas.drawText("Invite Friends:", screenW * 0.75f, screenH * 0.38f, pText);
            inviteButtons.clear();
            if (friends.isEmpty()) {
                pText.setColor(Color.GRAY);
                canvas.drawText("No friends yet", screenW * 0.75f, screenH * 0.43f, pText);
            } else {
                for (int i = 0; i < friends.size(); i++) {
                    float yPos = screenH * 0.43f + i * screenH * 0.05f;
                    pText.setColor(Color.WHITE);
                    canvas.drawText(friends.get(i).name, screenW * 0.75f, yPos, pText);
                    
                    if (multiplayerManager != null && multiplayerManager.isHost) {
                        float btnX = screenW * 0.75f; // Centered
                        RectF invBtn = new RectF(btnX - 50f, yPos + 10f, btnX + 50f, yPos + 40f);
                        inviteButtons.put(friends.get(i).id, invBtn);
                        
                        pBtn.setColor(Color.parseColor("#0088FF"));
                        canvas.drawRoundRect(invBtn, 10f, 10f, pBtn);
                        
                        Paint pSmallText = new Paint(Paint.ANTI_ALIAS_FLAG);
                        pSmallText.setFakeBoldText(true);
                        pSmallText.setColor(Color.WHITE);
                        pSmallText.setTextSize(screenW * 0.025f);
                        pSmallText.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText("INVITE", invBtn.centerX(), invBtn.centerY() + pSmallText.getTextSize()/3, pSmallText);
                    }
                }
            }
            
            pText.setColor(Color.LTGRAY);
            canvas.drawText("Waiting for others... (" + (pIndex - 1) + "/4)", screenW / 2, screenH * 0.65f, pText);
            
            // If I am the host and there is at least 1 other player, show START GAME
            if (multiplayerManager != null && multiplayerManager.isHost && multiplayerManager.opponents.size() > 0) {
                pBtn.setColor(Color.parseColor("#FF5500"));
                canvas.drawRoundRect(btnStartGame, 20f, 20f, pBtn);
                pText.setTextSize(screenW * 0.06f);
                pText.setColor(Color.WHITE);
                canvas.drawText("START GAME", btnStartGame.centerX(), btnStartGame.centerY() + pText.getTextSize()/3, pText);
            }
        } else {
            // Draw Create Button
            pBtn.setColor(Color.parseColor("#0088FF"));
            canvas.drawRoundRect(btnCreate, 20f, 20f, pBtn);
            pText.setTextSize(screenW * 0.06f);
            pText.setColor(Color.WHITE);
            canvas.drawText("CREATE ROOM", btnCreate.centerX(), btnCreate.centerY() + pText.getTextSize()/3, pText);
            
            // Draw Join Button
            pBtn.setColor(Color.parseColor("#00AA00"));
            canvas.drawRoundRect(btnJoin, 20f, 20f, pBtn);
            canvas.drawText("JOIN ROOM", btnJoin.centerX(), btnJoin.centerY() + pText.getTextSize()/3, pText);
            
            // Draw Random Button
            pBtn.setColor(Color.parseColor("#8800FF"));
            canvas.drawRoundRect(btnRandom, 20f, 20f, pBtn);
            canvas.drawText("RANDOM MATCH", btnRandom.centerX(), btnRandom.centerY() + pText.getTextSize()/3, pText);
            
            if (!errorMessage.isEmpty()) {
                pText.setTextSize(screenW * 0.04f);
                pText.setColor(Color.RED);
                canvas.drawText(errorMessage, screenW / 2, screenH * 0.80f, pText);
            }
        }

        // Draw Back
        pBtn.setColor(Color.parseColor("#555555"));
        canvas.drawRoundRect(btnBack, 20f, 20f, pBtn);
        pText.setTextSize(screenW * 0.05f);
        pText.setColor(Color.WHITE);
        canvas.drawText(isWaitingForPlayer ? "CANCEL" : "BACK", btnBack.centerX(), btnBack.centerY() + pText.getTextSize()/3, pText);
    }
}
