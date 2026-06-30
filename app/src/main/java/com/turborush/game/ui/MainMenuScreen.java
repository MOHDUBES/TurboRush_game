package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import com.turborush.game.models.PlayerProgress;

public class MainMenuScreen {

    private Paint pBg, pTitle, pSub, pBtnBg, pBtnText, pIcon, pFooter;
    
    public RectF btnStart, btnGarage, btnTracks, btnLeaderboard, btnMultiplayer, btnSettings, btnProfile;
    
    private float w, h;

    public MainMenuScreen() {
        pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
        pSub = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBtnBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBtnText = new Paint(Paint.ANTI_ALIAS_FLAG);
        pIcon = new Paint(Paint.ANTI_ALIAS_FLAG);
        pFooter = new Paint(Paint.ANTI_ALIAS_FLAG);

        btnStart = new RectF();
        btnGarage = new RectF();
        btnTracks = new RectF();
        btnLeaderboard = new RectF();
        btnMultiplayer = new RectF();
        btnSettings = new RectF();
        btnProfile = new RectF();

        pSub.setColor(Color.parseColor("#FF6B6B"));
        pSub.setTextSize(40f); // ~14sp
        pSub.setLetterSpacing(0.2f); // tracking 4dp
        pSub.setTextAlign(Paint.Align.CENTER);
        pSub.setFakeBoldText(true);

        pTitle.setTextSize(160f); // ~64sp
        pTitle.setTextAlign(Paint.Align.CENTER);
        pTitle.setFakeBoldText(true);

        pBtnText.setColor(Color.WHITE);
        pBtnText.setTextSize(50f);
        pBtnText.setFakeBoldText(true);
        pBtnText.setTextAlign(Paint.Align.LEFT);

        pIcon.setTextSize(60f);

        pFooter.setColor(Color.parseColor("#9CA3AF"));
        pFooter.setTextSize(30f); // 11sp
        pFooter.setTextAlign(Paint.Align.CENTER);
    }

    public void init(float w, float h) {
        this.w = w;
        this.h = h;

        pBg.setShader(new LinearGradient(0, 0, 0, h, 
            Color.parseColor("#0D0D1A"), Color.parseColor("#1A1A2E"), Shader.TileMode.CLAMP));

        pTitle.setShader(new LinearGradient(0, h*0.2f, 0, h*0.35f, 
            Color.parseColor("#FFD700"), Color.parseColor("#FFA000"), Shader.TileMode.CLAMP));

        float btnW = w - 160f; // screenW - 64dp
        float btnH = 130f;
        float spacing = 35f; // 14dp
        float cx = w / 2f;
        
        float startY = h * 0.42f;
        
        btnStart.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
        startY += btnH + spacing;
        
        btnGarage.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
        startY += btnH + spacing;

        btnTracks.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
        startY += btnH + spacing;

        btnLeaderboard.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
        startY += btnH + spacing;

        btnMultiplayer.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);

        // Settings gear
        btnSettings.set(w - 180f, 60f, w - 40f, 200f); // 56dp x 56dp
        
        // Profile icon
        btnProfile.set(w - 340f, 60f, w - 200f, 200f);
    }

    public void update(float dt) {
        // Animation logic here if needed
    }

    public void draw(Canvas c, PlayerProgress progress) {
        c.drawRect(0, 0, w, h, pBg);

        // Stats (Top Left)
        pFooter.setColor(Color.parseColor("#FFD700"));
        pFooter.setTextAlign(Paint.Align.LEFT);
        pFooter.setTextSize(45f);
        pFooter.setFakeBoldText(true);
        c.drawText("💰 " + progress.totalCoins, 40f, 90f, pFooter);
        
        pFooter.setColor(Color.WHITE);
        pFooter.setTextSize(35f);
        c.drawText("🏆 Best: " + progress.bestScore, 40f, 150f, pFooter);
        pFooter.setFakeBoldText(false);

        // Titles
        c.drawText("STREET RACING", w / 2f, h * 0.22f, pSub);
        c.drawText("TURBO", w / 2f, h * 0.3f, pTitle);
        c.drawText("RUSH", w / 2f, h * 0.38f, pTitle);

        // Settings
        pBtnBg.setShader(null);
        pBtnBg.setColor(Color.WHITE);
        c.drawRoundRect(btnSettings, 35f, 35f, pBtnBg);
        pIcon.setColor(Color.parseColor("#6B7280"));
        pIcon.setTextAlign(Paint.Align.CENTER);
        c.drawText("⚙", btnSettings.centerX(), btnSettings.centerY() + 20f, pIcon);

        // Profile
        c.drawRoundRect(btnProfile, 35f, 35f, pBtnBg);
        c.drawText("👤", btnProfile.centerX(), btnProfile.centerY() + 20f, pIcon);

        // Buttons
        // Start
        pBtnBg.setShader(new LinearGradient(btnStart.left, 0, btnStart.right, 0, 
            Color.parseColor("#FF4458"), Color.parseColor("#FF6B35"), Shader.TileMode.CLAMP));
        c.drawRoundRect(btnStart, 25f, 25f, pBtnBg);
        pIcon.setColor(Color.WHITE);
        pIcon.setTextAlign(Paint.Align.LEFT);
        c.drawText("▶", btnStart.left + 50f, btnStart.centerY() + 20f, pIcon);
        c.drawText("START RACE", btnStart.left + 150f, btnStart.centerY() + 18f, pBtnText);

        // Garage
        pBtnBg.setShader(new LinearGradient(btnGarage.left, 0, btnGarage.right, 0, 
            Color.parseColor("#1E3A5F"), Color.parseColor("#2D4F7F"), Shader.TileMode.CLAMP));
        c.drawRoundRect(btnGarage, 25f, 25f, pBtnBg);
        c.drawText("🔧", btnGarage.left + 50f, btnGarage.centerY() + 20f, pIcon);
        c.drawText("GARAGE", btnGarage.left + 150f, btnGarage.centerY() + 18f, pBtnText);

        // Tracks
        pBtnBg.setShader(new LinearGradient(btnTracks.left, 0, btnTracks.right, 0, 
            Color.parseColor("#1E3A5F"), Color.parseColor("#2D4F7F"), Shader.TileMode.CLAMP));
        c.drawRoundRect(btnTracks, 25f, 25f, pBtnBg);
        c.drawText("🗺", btnTracks.left + 50f, btnTracks.centerY() + 20f, pIcon);
        c.drawText("TRACKS", btnTracks.left + 150f, btnTracks.centerY() + 18f, pBtnText);

        // Leaderboard
        pBtnBg.setShader(new LinearGradient(btnLeaderboard.left, 0, btnLeaderboard.right, 0, 
            Color.parseColor("#1E3A5F"), Color.parseColor("#2D4F7F"), Shader.TileMode.CLAMP));
        c.drawRoundRect(btnLeaderboard, 25f, 25f, pBtnBg);
        c.drawText("🏆", btnLeaderboard.left + 50f, btnLeaderboard.centerY() + 20f, pIcon);
        c.drawText("LEADERBOARD", btnLeaderboard.left + 150f, btnLeaderboard.centerY() + 18f, pBtnText);

        // Multiplayer
        pBtnBg.setShader(new LinearGradient(btnMultiplayer.left, 0, btnMultiplayer.right, 0, 
            Color.parseColor("#8E24AA"), Color.parseColor("#4A148C"), Shader.TileMode.CLAMP));
        c.drawRoundRect(btnMultiplayer, 25f, 25f, pBtnBg);
        c.drawText("🌐", btnMultiplayer.left + 50f, btnMultiplayer.centerY() + 20f, pIcon);
        c.drawText("MULTIPLAYER", btnMultiplayer.left + 150f, btnMultiplayer.centerY() + 18f, pBtnText);

        // Footer removed as requested
    }
}
