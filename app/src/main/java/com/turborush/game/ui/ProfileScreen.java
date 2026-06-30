package com.turborush.game.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.BitmapShader;

import com.turborush.game.models.PlayerProgress;

public class ProfileScreen {

    private float screenW, screenH;

    public RectF btnBack = new RectF();
    public RectF btnEditName = new RectF();
    public RectF btnEditAvatar = new RectF();
    public RectF btnLogin = new RectF();
    public RectF btnLogout = new RectF();

    public Bitmap customAvatar = null;
    public java.util.List<String> friends = new java.util.ArrayList<>();
    
    public RectF btnFriendsCard = new RectF();
    public boolean isFriendsExpanded = false;

    private final Paint pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCard = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBtn = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    public static final String[] AVATARS = {"👦", "👧", "🤖", "👽", "🏎️", "🐱", "🐶"};

    public ProfileScreen() {
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
        btnBack.set(w * 0.05f, h * 0.05f, w * 0.05f + h * 0.07f, h * 0.05f + h * 0.07f);
        
        float cx = w / 2f;
        float cy = h * 0.35f;
        
        // Edit Avatar Button
        btnEditAvatar.set(cx + 120f, cy - 150f, cx + 220f, cy - 50f);
        
        // Edit Name Button
        btnEditName.set(cx + 200f, cy + 50f, cx + 300f, cy + 150f);
        
        // Login Button
        btnLogin.set(w - 400f, 60f, w - 40f, 150f);
        
        // Logout Button
        btnLogout.set(w - 200f, 60f, w - 40f, 150f);
    }

    public void update(float dt) {}

    public void draw(Canvas canvas, PlayerProgress prog) {
        pBg.setShader(new LinearGradient(0, 0, 0, screenH, Color.parseColor("#1A1A2E"), Color.parseColor("#0F0F1A"), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, screenW, screenH, pBg);
        pBg.setShader(null);

        pTitle.setColor(Color.WHITE);
        pTitle.setTextSize(screenW * 0.08f);
        canvas.drawText("PLAYER PROFILE", screenW / 2f, screenH * 0.12f, pTitle);

        drawBtn(canvas, btnBack, "◀", Color.parseColor("#33FFFFFF"));

        float cx = screenW / 2f;
        float cy = screenH * 0.35f;

        // Avatar
        pBg.setColor(Color.parseColor("#2A2E3D"));
        canvas.drawCircle(cx, cy - 100f, 120f, pBg);
        
        if (customAvatar != null) {
            Paint bp = new Paint(Paint.ANTI_ALIAS_FLAG);
            BitmapShader shader = new BitmapShader(customAvatar, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            android.graphics.Matrix m = new android.graphics.Matrix();
            float scale = 240f / Math.max(customAvatar.getWidth(), customAvatar.getHeight());
            m.setScale(scale, scale);
            m.postTranslate(cx - (customAvatar.getWidth()*scale)/2f, (cy - 100f) - (customAvatar.getHeight()*scale)/2f);
            shader.setLocalMatrix(m);
            bp.setShader(shader);
            canvas.drawCircle(cx, cy - 100f, 120f, bp);
        } else {
            pText.setTextSize(140f);
            canvas.drawText(AVATARS[prog.avatarId % AVATARS.length], cx, cy - 50f, pText);
        }
        
        // Edit Avatar Btn
        drawBtn(canvas, btnEditAvatar, "🔄", Color.parseColor("#44FFFFFF"));

        // Name
        pText.setColor(Color.parseColor("#FFD700"));
        pText.setTextSize(screenW * 0.09f);
        pText.setFakeBoldText(true);
        canvas.drawText(prog.playerName, cx, cy + 110f, pText);
        pText.setFakeBoldText(false);
        
        // Edit Name Btn
        drawBtn(canvas, btnEditName, "✏️", Color.parseColor("#44FFFFFF"));

        // Level & XP
        float startY = cy + 250f;
        pText.setColor(Color.WHITE);
        pText.setTextSize(screenW * 0.05f);
        canvas.drawText("LEVEL " + prog.playerLevel, cx, startY, pText);
        
        float xpBarW = screenW * 0.6f;
        float xpBarH = 20f;
        float xpBarY = startY + 40f;
        
        pBg.setColor(Color.parseColor("#374151"));
        canvas.drawRoundRect(new RectF(cx - xpBarW/2, xpBarY, cx + xpBarW/2, xpBarY + xpBarH), 10, 10, pBg);
        
        long xpNeeded = prog.playerLevel * 1000L;
        float progress = Math.min(1f, (float)prog.playerXp / xpNeeded);
        pBg.setColor(Color.parseColor("#4CAF50"));
        canvas.drawRoundRect(new RectF(cx - xpBarW/2, xpBarY, cx - xpBarW/2 + (xpBarW * progress), xpBarY + xpBarH), 10, 10, pBg);
        
        pText.setTextSize(screenW * 0.035f);
        pText.setColor(Color.LTGRAY);
        canvas.drawText(prog.playerXp + " / " + xpNeeded + " XP", cx, xpBarY + 60f, pText);

        // Login Status
        if (prog.isLoggedIn) {
            pText.setColor(Color.parseColor("#4CAF50"));
            pText.setTextSize(screenW * 0.04f);
            canvas.drawText("Connected", screenW - 320f, 115f, pText);
            drawBtn(canvas, btnLogout, "LOGOUT", Color.parseColor("#E53935"));
        } else {
            drawBtn(canvas, btnLogin, "LOGIN / REGISTER", Color.parseColor("#10B981"));
        }

        // Stats Card
        float cardY = xpBarY + 150f;
        float cardH = screenH * 0.25f;
        canvas.drawRoundRect(new RectF(cx - xpBarW/2 - 40f, cardY, cx + xpBarW/2 + 40f, cardY + cardH), 24, 24, pCard);
        
        pText.setColor(Color.WHITE);
        pText.setTextAlign(Paint.Align.LEFT);
        pText.setTextSize(screenW * 0.045f);
        
        float textX = cx - xpBarW/2;
        float textY = cardY + 90f;
        float spacing = 80f;
        
        canvas.drawText("🏁 Total Races:", textX, textY, pText);
        canvas.drawText("🛣️ Total Distance:", textX, textY + spacing, pText);
        canvas.drawText("⚡ Top Speed:", textX, textY + spacing * 2, pText);
        
        pText.setColor(Color.parseColor("#FFD700"));
        pText.setTextAlign(Paint.Align.RIGHT);
        float valX = cx + xpBarW/2;
        
        canvas.drawText(String.valueOf(prog.totalRaces), valX, textY, pText);
        canvas.drawText(prog.totalDistance + "m", valX, textY + spacing, pText);
        canvas.drawText((int)prog.topSpeedReached + " km/h", valX, textY + spacing * 2, pText);
        pText.setTextAlign(Paint.Align.CENTER);
        
        // Friends Card
        float friendY = cardY + cardH + 40f;
        float friendH = isFriendsExpanded ? Math.max(screenH * 0.15f, 100f + (friends.size() * 60f)) : 100f;
        
        btnFriendsCard.set(cx - xpBarW/2 - 40f, friendY, cx + xpBarW/2 + 40f, friendY + friendH);
        canvas.drawRoundRect(btnFriendsCard, 24, 24, pCard);
        
        pText.setColor(Color.parseColor("#10B981")); // Emerald
        pText.setTextSize(screenW * 0.045f);
        pText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(isFriendsExpanded ? "▼ FRIENDS" : "▶ FRIENDS", cx, friendY + 65f, pText);
        
        if (isFriendsExpanded) {
            pText.setColor(Color.WHITE);
            pText.setTextSize(screenW * 0.04f);
            if (friends.isEmpty()) {
                pText.setColor(Color.GRAY);
                canvas.drawText("No friends added yet.", cx, friendY + 130f, pText);
            } else {
                for (int i = 0; i < friends.size(); i++) {
                    canvas.drawText(friends.get(i), cx, friendY + 130f + (i * 60f), pText);
                }
            }
        }
    }

    private void drawBtn(Canvas c, RectF rect, String text, int color) {
        pBtn.setColor(color);
        c.drawRoundRect(rect, 20f, 20f, pBtn);
        pText.setColor(Color.WHITE);
        pText.setTextSize(rect.height() * 0.4f);
        c.drawText(text, rect.centerX(), rect.centerY() + rect.height() * 0.15f, pText);
    }
}
