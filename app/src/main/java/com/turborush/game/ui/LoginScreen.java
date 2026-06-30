package com.turborush.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.turborush.game.models.PlayerProgress;

public class LoginScreen {

    private float screenW, screenH;

    public RectF btnBack = new RectF();
    public RectF btnGoogle = new RectF();
    public RectF btnEmail = new RectF();
    public RectF btnConnect = new RectF();
    
    public boolean showOptions = false;

    private final Paint pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCard = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBtn = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LoginScreen() {
        pBg.setStyle(Paint.Style.FILL);
        pCard.setColor(Color.parseColor("#151821"));
        pCard.setStyle(Paint.Style.FILL);
        pText.setStyle(Paint.Style.FILL);
        pText.setTextAlign(Paint.Align.CENTER);
        pTitle.setStyle(Paint.Style.FILL);
        pTitle.setTextAlign(Paint.Align.CENTER);
        pTitle.setFakeBoldText(true);
        pBtn.setStyle(Paint.Style.FILL);
    }

    public void init(float w, float h) {
        screenW = w;
        screenH = h;
        // Make back button smaller and more square-like based on width
        btnBack.set(w * 0.05f, h * 0.04f, w * 0.18f, h * 0.04f + w * 0.13f);
        
        float cx = w / 2f;
        float btnW = w * 0.7f;
        float btnH = h * 0.08f;
        float startY = h * 0.40f;
        float spacing = btnH + 40f;
        
        btnConnect.set(cx - btnW/2, startY, cx + btnW/2, startY + btnH);
        
        btnGoogle.set(cx - btnW/2, startY, cx + btnW/2, startY + btnH);
        btnEmail.set(cx - btnW/2, startY + spacing, cx + btnW/2, startY + spacing + btnH);
    }

    public void update(float dt) {}

    public void draw(Canvas canvas, PlayerProgress prog, String loginStatus) {
        pBg.setShader(new LinearGradient(0, 0, 0, screenH, Color.parseColor("#1A1A2E"), Color.parseColor("#0F0F1A"), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, screenW, screenH, pBg);
        pBg.setShader(null);

        pTitle.setColor(Color.WHITE);
        pTitle.setTextSize(screenW * 0.07f); // Slightly smaller
        canvas.drawText("CLOUD ACCOUNT", screenW / 2f, screenH * 0.15f, pTitle);

        pText.setColor(Color.parseColor("#FFD700"));
        pText.setTextSize(screenW * 0.055f);
        pText.setFakeBoldText(true);
        canvas.drawText("Total Coins: " + prog.totalCoins, screenW / 2f, screenH * 0.22f, pText);
        pText.setFakeBoldText(false);

        drawBtn(canvas, btnBack, "◀", Color.parseColor("#33FFFFFF"));

        // Status Text
        if (loginStatus != null && !loginStatus.isEmpty()) {
            pText.setColor(Color.YELLOW);
            pText.setTextSize(screenW * 0.04f);
            canvas.drawText(loginStatus, screenW / 2f, screenH * 0.35f, pText);
        }

        // Options
        if (!showOptions) {
            drawBtn(canvas, btnConnect, "🔗 Connect Account", Color.parseColor("#374151"));
            pText.setColor(Color.LTGRAY);
            pText.setTextSize(screenW * 0.04f);
            canvas.drawText("Connect to save coins to the Cloud!", screenW / 2f, btnConnect.bottom + 100f, pText);
        } else {
            drawBtn(canvas, btnGoogle, "Continue with Google", Color.parseColor("#DB4437"));
            drawBtn(canvas, btnEmail, "Continue with Email", Color.parseColor("#4B5563"));
            
            pText.setColor(Color.LTGRAY);
            pText.setTextSize(screenW * 0.035f);
            canvas.drawText("Select a provider to continue", screenW / 2f, btnEmail.bottom + 80f, pText);
        }
    }

    private void drawBtn(Canvas c, RectF rect, String text, int color) {
        pBtn.setColor(color);
        c.drawRoundRect(rect, 20f, 20f, pBtn);
        pText.setColor(Color.WHITE);
        // Dynamically adjust text size based on length so it doesn't overflow
        float maxTextW = rect.width() * 0.85f;
        float tSize = rect.height() * 0.4f;
        pText.setTextSize(tSize);
        while (pText.measureText(text) > maxTextW && tSize > 10f) {
            tSize -= 1f;
            pText.setTextSize(tSize);
        }
        c.drawText(text, rect.centerX(), rect.centerY() + tSize * 0.35f, pText);
    }
}
