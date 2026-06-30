package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class PauseScreen {

    private Paint pOverlay, pTitle, pBtnBg, pBtnText;

    public RectF btnResume, btnRestart, btnGarage, btnMainMenu;

    public PauseScreen() {
        pOverlay = new Paint(Paint.ANTI_ALIAS_FLAG);
        pOverlay.setColor(Color.argb(178, 0, 0, 0)); // 70% alpha black

        pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
        pTitle.setColor(Color.WHITE);
        pTitle.setTextAlign(Paint.Align.CENTER);
        pTitle.setFakeBoldText(true);
        pTitle.setShadowLayer(4f, 2f, 2f, Color.BLACK);

        pBtnBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        pBtnText = new Paint(Paint.ANTI_ALIAS_FLAG);
        pBtnText.setColor(Color.WHITE);
        pBtnText.setTextAlign(Paint.Align.CENTER);
        pBtnText.setFakeBoldText(true);

        btnResume = new RectF();
        btnRestart = new RectF();
        btnGarage = new RectF();
        btnMainMenu = new RectF();
    }

    public void init(float w, float h) {
        float cx = w / 2f;
        float cy = h / 2f;
        float btnW = w * 0.65f; 
        float btnH = h * 0.08f; 
        float spacing = h * 0.03f;

        // Position buttons vertically centered
        float startY = cy - (btnH * 2f + spacing * 1.5f);

        btnResume.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
        startY += btnH + spacing;
        
        btnRestart.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
        startY += btnH + spacing;

        btnGarage.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
        startY += btnH + spacing;

        btnMainMenu.set(cx - btnW/2f, startY, cx + btnW/2f, startY + btnH);
    }

    public void draw(Canvas canvas, float w, float h) {
        // Dark overlay
        canvas.drawRect(0, 0, w, h, pOverlay);

        // Title
        pTitle.setTextSize(w * 0.12f);
        canvas.drawText("PAUSED", w / 2f, btnResume.top - h * 0.08f, pTitle);

        pBtnText.setTextSize(Math.min(50f, w * 0.05f));
        float textYOffset = pBtnText.getTextSize() * 0.35f;

        // Resume
        pBtnBg.setColor(Color.parseColor("#16A34A"));
        canvas.drawRoundRect(btnResume, 35f, 35f, pBtnBg);
        canvas.drawText("▶ RESUME", btnResume.centerX(), btnResume.centerY() + textYOffset, pBtnText);

        // Restart
        pBtnBg.setColor(Color.parseColor("#2563EB"));
        canvas.drawRoundRect(btnRestart, 35f, 35f, pBtnBg);
        canvas.drawText("↺ RESTART", btnRestart.centerX(), btnRestart.centerY() + textYOffset, pBtnText);

        // Garage
        pBtnBg.setColor(Color.parseColor("#7C3AED"));
        canvas.drawRoundRect(btnGarage, 35f, 35f, pBtnBg);
        canvas.drawText("🔧 GARAGE", btnGarage.centerX(), btnGarage.centerY() + textYOffset, pBtnText);

        // Main Menu
        pBtnBg.setColor(Color.parseColor("#374151"));
        canvas.drawRoundRect(btnMainMenu, 35f, 35f, pBtnBg);
        canvas.drawText("🏠 MAIN MENU", btnMainMenu.centerX(), btnMainMenu.centerY() + textYOffset, pBtnText);
    }
}
