package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class HUD {

    private Paint pText, pScoreVal, pLevelVal, pDistVal;
    private Paint pCoinInner, pCoinOuter, pCoinDot;
    private Paint pFuelBarBg, pFuelBarFg;
    private Paint pControlBg, pControlIcon, pMuteBg;

    public RectF btnLeft, btnRight, btnPause;
    public RectF btnMute, btnPauseTop, btnTilt;
    
    private float screenW, screenH;
    
    // Animation
    private float pulseTimer = 0f;

    public HUD() {
        pText = new Paint(Paint.ANTI_ALIAS_FLAG);
        pText.setColor(Color.WHITE);
        pText.setShadowLayer(4f, 1f, 1f, Color.argb(180, 0, 0, 0));

        pScoreVal = new Paint(pText);
        pScoreVal.setColor(Color.parseColor("#FFD700"));
        pScoreVal.setTextSize(90f); // ~36sp
        pScoreVal.setFakeBoldText(true);

        pLevelVal = new Paint(pText);
        pLevelVal.setTextSize(70f); // ~28sp
        pLevelVal.setFakeBoldText(true);
        pLevelVal.setTextAlign(Paint.Align.CENTER);

        pDistVal = new Paint(pText);
        pDistVal.setTextSize(55f); // ~22sp

        pCoinOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        pCoinOuter.setColor(Color.parseColor("#FFD700"));
        pCoinInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        pCoinInner.setColor(Color.parseColor("#FFF176"));
        pCoinDot = new Paint(Paint.ANTI_ALIAS_FLAG);
        pCoinDot.setColor(Color.WHITE);

        pFuelBarBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pFuelBarBg.setColor(Color.parseColor("#374151"));
        pFuelBarFg = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        pControlBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pControlBg.setColor(Color.parseColor("#1A2A4A"));
        pControlBg.setAlpha(204); // 80%

        pControlIcon = new Paint(pText);
        pControlIcon.setTextSize(80f);
        pControlIcon.setTextAlign(Paint.Align.CENTER);

        pMuteBg = new Paint(Paint.ANTI_ALIAS_FLAG);

        btnLeft = new RectF();
        btnRight = new RectF();
        btnPause = new RectF();
        btnMute = new RectF();
        btnPauseTop = new RectF();
        btnTilt = new RectF();
    }

    public void init(float w, float h) {
        this.screenW = w;
        this.screenH = h;

        float ctrlW = 250f; // ~100dp
        float ctrlH = 200f; // ~80dp
        float margin = 40f; // ~16dp

        btnLeft.set(margin, h - margin - ctrlH, margin + ctrlW, h - margin);
        btnRight.set(w - margin - ctrlW, h - margin - ctrlH, w - margin, h - margin);

        float pauseW = 175f; // ~70dp
        btnPause.set(w / 2f - pauseW/2f, h - margin - pauseW, w / 2f + pauseW/2f, h - margin);

        // Icon row (Top Right)
        float iconS = 110f; // ~44dp
        float iconM = 20f;
        btnMute.set(w - margin - iconS, margin, w - margin, margin + iconS);
        btnPauseTop.set(w - margin - iconS, margin + iconS + iconM, w - margin, margin + iconS * 2 + iconM);
        btnTilt.set(w - margin - iconS, margin + iconS * 2 + iconM * 2, w - margin, margin + iconS * 3 + iconM * 2);
    }

    public void update(float dt) {
        pulseTimer += dt;
    }

    public void draw(Canvas c, int score, int level, int coins, float distance, float fuel, boolean isMuted, boolean tiltOn) {
        float topY = 80f; // statusBar + 8dp
        float margin = 40f;

        // --- TOP BAR ---
        
        // SCORE (Left)
        pText.setTextSize(30f); // 12sp
        pText.setTextAlign(Paint.Align.LEFT);
        c.drawText("SCORE", margin, topY, pText);
        c.drawText(String.valueOf(score), margin, topY + 80f, pScoreVal);

        // LEVEL (Center)
        float levelX = screenW * 0.45f;
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText("LEVEL", levelX, topY, pText);
        c.drawText(String.valueOf(level), levelX, topY + 70f, pLevelVal);
        
        // XP Bar
        float xpW = screenW * 0.2f;
        float xpH = 10f; // 4dp
        c.drawRoundRect(new RectF(levelX - xpW/2f, topY + 90f, levelX + xpW/2f, topY + 90f + xpH), 5, 5, pFuelBarBg);
        float progress = (distance % 1000f) / 1000f; // Mock progress
        pFuelBarFg.setColor(Color.parseColor("#4CAF50"));
        c.drawRoundRect(new RectF(levelX - xpW/2f, topY + 90f, levelX - xpW/2f + (xpW * progress), topY + 90f + xpH), 5, 5, pFuelBarFg);

        // COINS (Right)
        float coinX = screenW * 0.70f;
        c.drawCircle(coinX, topY + 30f, 22f, pCoinOuter);
        c.drawCircle(coinX, topY + 30f, 16f, pCoinInner);
        c.drawCircle(coinX - 6f, topY + 24f, 4f, pCoinDot);
        pText.setTextSize(75f); // 30sp
        pText.setColor(Color.parseColor("#FFD700"));
        pText.setTextAlign(Paint.Align.LEFT);
        c.drawText(String.valueOf(coins), coinX + 35f, topY + 55f, pText);
        pText.setColor(Color.WHITE);

        // --- SECOND ROW ---
        float row2Y = topY + 130f; // +52dp

        // DIST (Left)
        pText.setTextSize(28f); // 11sp
        pText.setTextAlign(Paint.Align.LEFT);
        c.drawText("DIST", margin, row2Y, pText);
        c.drawText((int)distance + "m", margin, row2Y + 50f, pDistVal);

        // FUEL (Right)
        float fuelX = screenW * 0.45f;
        pText.setTextAlign(Paint.Align.LEFT);
        c.drawText("FUEL", fuelX, row2Y, pText);
        
        float fuelW = screenW * 0.25f; // 25% of screen
        float fuelH = 35f; // 14dp
        RectF fuelRect = new RectF(fuelX, row2Y + 15f, fuelX + fuelW, row2Y + 15f + fuelH);
        
        // Red glow if low
        if (fuel < 20f) {
            float glow = (float)(0.5 + 0.5 * Math.sin(pulseTimer * 10f));
            pFuelBarBg.setShadowLayer(15f, 0, 0, Color.argb((int)(glow * 255), 244, 67, 54));
        } else {
            pFuelBarBg.clearShadowLayer();
        }
        
        c.drawRoundRect(fuelRect, 8, 8, pFuelBarBg);
        
        if (fuel > 50f) pFuelBarFg.setColor(Color.parseColor("#4CAF50"));
        else if (fuel > 20f) pFuelBarFg.setColor(Color.parseColor("#FFC107"));
        else pFuelBarFg.setColor(Color.parseColor("#F44336"));
        
        c.drawRoundRect(new RectF(fuelX, row2Y + 15f, fuelX + (fuelW * (fuel / 100f)), row2Y + 15f + fuelH), 8, 8, pFuelBarFg);
        pFuelBarBg.clearShadowLayer();

        // --- ICON ROW ---
        drawIconButton(c, btnMute, isMuted ? "🔇" : "🔊");
        drawIconButton(c, btnPauseTop, "⏸");
        drawIconButton(c, btnTilt, tiltOn ? "📱" : "🖐");

        // --- BOTTOM CONTROLS ---
        c.drawRoundRect(btnLeft, 30f, 30f, pControlBg);
        c.drawText("◄", btnLeft.centerX(), btnLeft.centerY() + 25f, pControlIcon);

        c.drawRoundRect(btnRight, 30f, 30f, pControlBg);
        c.drawText("►", btnRight.centerX(), btnRight.centerY() + 25f, pControlIcon);

        c.drawRoundRect(btnPause, 30f, 30f, pControlBg);
        c.drawText("⏸", btnPause.centerX(), btnPause.centerY() + 25f, pControlIcon);
    }

    private void drawIconButton(Canvas c, RectF rect, String icon) {
        pMuteBg.setColor(Color.parseColor("#1A2A4A"));
        pMuteBg.setAlpha(150);
        c.drawRoundRect(rect, 16f, 16f, pMuteBg);
        pText.setTextSize(50f);
        pText.setTextAlign(Paint.Align.CENTER);
        c.drawText(icon, rect.centerX(), rect.centerY() + 18f, pText);
        pText.setTextAlign(Paint.Align.LEFT);
    }
}
