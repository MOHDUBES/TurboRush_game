package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.turborush.game.models.PlayerProgress;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardScreen {

    private float screenW, screenH;
    private Paint pText, pBg, pPanel, pItemBg;
    public RectF btnBack;
    
    private List<LeaderboardEntry> entries = new ArrayList<>();
    private boolean isLoading = true;
    private String errorMessage = "";
    
    private static class LeaderboardEntry {
        String name;
        long score;
        int rank;
    }

    public void init(float w, float h) {
        screenW = w;
        screenH = h;

        pText = new Paint(Paint.ANTI_ALIAS_FLAG);
        pText.setColor(Color.WHITE);
        pText.setFakeBoldText(true);

        pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBg.setColor(Color.parseColor("#E6000000")); // Dark semi-transparent

        pPanel = new Paint(Paint.ANTI_ALIAS_FLAG);
        pPanel.setColor(Color.parseColor("#333333"));

        pItemBg = new Paint(Paint.ANTI_ALIAS_FLAG);

        float btnW = w * 0.4f;
        float btnH = h * 0.08f;
        btnBack = new RectF(w/2 - btnW/2, h * 0.85f, w/2 + btnW/2, h * 0.85f + btnH);
    }
    
    public void fetchLeaderboard() {
        isLoading = true;
        errorMessage = "";
        entries.clear();
        
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("leaderboard")
              .orderBy("bestScore", Query.Direction.DESCENDING)
              .limit(10)
              .get()
              .addOnCompleteListener(task -> {
                  isLoading = false;
                  if (task.isSuccessful() && task.getResult() != null) {
                      int rank = 1;
                      for (DocumentSnapshot doc : task.getResult()) {
                          LeaderboardEntry entry = new LeaderboardEntry();
                          entry.name = doc.getString("playerName");
                          if (entry.name == null) entry.name = "Unknown";
                          Long scoreObj = doc.getLong("bestScore");
                          entry.score = scoreObj != null ? scoreObj : 0;
                          entry.rank = rank++;
                          entries.add(entry);
                      }
                  } else {
                      errorMessage = "Failed to load leaderboard.";
                  }
              });
        } catch (Exception e) {
            isLoading = false;
            errorMessage = "Error connecting to server.";
        }
    }

    public void draw(Canvas canvas, PlayerProgress progress) {
        canvas.drawRect(0, 0, screenW, screenH, pBg);

        pText.setTextSize(screenW * 0.08f);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setColor(Color.parseColor("#FFD700")); // Gold
        canvas.drawText("GLOBAL LEADERBOARD", screenW / 2, screenH * 0.15f, pText);
        
        float panelY = screenH * 0.2f;
        
        if (isLoading) {
            pText.setTextSize(screenW * 0.05f);
            pText.setColor(Color.WHITE);
            canvas.drawText("Loading...", screenW / 2, screenH * 0.5f, pText);
        } else if (!errorMessage.isEmpty()) {
            pText.setTextSize(screenW * 0.05f);
            pText.setColor(Color.RED);
            canvas.drawText(errorMessage, screenW / 2, screenH * 0.5f, pText);
        } else if (entries.isEmpty()) {
            pText.setTextSize(screenW * 0.05f);
            pText.setColor(Color.WHITE);
            canvas.drawText("No scores found yet!", screenW / 2, screenH * 0.5f, pText);
        } else {
            // Draw list
            float itemH = screenH * 0.055f;
            for (int i = 0; i < entries.size(); i++) {
                LeaderboardEntry entry = entries.get(i);
                
                float itemY = panelY + (i * (itemH + 10f));
                RectF itemRect = new RectF(screenW * 0.1f, itemY, screenW * 0.9f, itemY + itemH);
                
                if (entry.rank == 1) pItemBg.setColor(Color.parseColor("#D4AF37")); // Gold
                else if (entry.rank == 2) pItemBg.setColor(Color.parseColor("#C0C0C0")); // Silver
                else if (entry.rank == 3) pItemBg.setColor(Color.parseColor("#CD7F32")); // Bronze
                else pItemBg.setColor(Color.parseColor("#444444")); // Normal
                
                canvas.drawRoundRect(itemRect, 10f, 10f, pItemBg);
                
                pText.setTextAlign(Paint.Align.LEFT);
                pText.setTextSize(itemH * 0.6f);
                pText.setColor(Color.WHITE);
                
                canvas.drawText("#" + entry.rank + "  " + entry.name, screenW * 0.15f, itemY + itemH * 0.7f, pText);
                
                pText.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.valueOf(entry.score), screenW * 0.85f, itemY + itemH * 0.7f, pText);
            }
        }
        
        if (!progress.isLoggedIn) {
            pText.setTextSize(screenW * 0.04f);
            pText.setColor(Color.LTGRAY);
            pText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Login to submit your score globally!", screenW / 2, screenH * 0.8f, pText);
        }

        // Draw Back button
        pPanel.setColor(Color.parseColor("#444444"));
        canvas.drawRoundRect(btnBack, 20f, 20f, pPanel);
        pText.setTextSize(screenW * 0.05f);
        pText.setColor(Color.WHITE);
        pText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("BACK", btnBack.centerX(), btnBack.centerY() + pText.getTextSize()/3, pText);
    }
}
