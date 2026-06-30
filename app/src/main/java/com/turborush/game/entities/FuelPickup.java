package com.turborush.game.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * FuelPickup — A glowing fuel canister that restores the player's fuel tank.
 */
public class FuelPickup {

    public float   x, y;
    public boolean active    = false;
    public boolean collected = false;
    private float  animTimer    = 0f;
    private float  collectTimer = 0f;

    private static final float WIDTH  = 26f;
    private static final float HEIGHT = 38f;

    private final Paint pCanister  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGlow      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pFlame     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pLabel     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCollect   = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FuelPickup() {
        pCanister.setStyle(Paint.Style.FILL);
        pGlow.setStyle(Paint.Style.FILL);
        pFlame.setStyle(Paint.Style.FILL);
        pLabel.setStyle(Paint.Style.FILL);
        pLabel.setTextAlign(Paint.Align.CENTER);
        pLabel.setFakeBoldText(true);
        pCollect.setStyle(Paint.Style.FILL);
    }

    public void spawn(float x, float y) {
        this.x = x; this.y = y;
        active    = true;
        collected = false;
        animTimer = (float)(Math.random() * 6.28); // random phase offset
        collectTimer = 0;
    }

    public void update(float delta, float roadScrollSpeed, float screenH) {
        animTimer += delta;
        y += roadScrollSpeed * delta;
        if (collected) {
            collectTimer += delta;
            if (collectTimer > 0.5f) active = false;
        }
        if (!collected && y > screenH + 60) active = false;
    }

    public void collect() { collected = true; }

    public void draw(Canvas canvas) {
        if (!active) return;

        if (collected) {
            float alpha = Math.max(0f, 1f - collectTimer / 0.5f);
            float scale = 1f + collectTimer * 3f;
            pCollect.setColor(Color.argb((int)(alpha * 180), 0, 230, 118));
            canvas.drawCircle(x, y, 30f * scale, pCollect);
            pCollect.setColor(Color.argb((int)(alpha * 255), 255, 255, 255));
            pCollect.setTextSize(20f);
            pCollect.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("FULL!", x, y - 30f * scale, pCollect);
            return;
        }

        // Bob animation
        float bobY = (float)Math.sin(animTimer * 3) * 4f;
        float glow = (float)(0.6 + 0.4 * Math.sin(animTimer * 4));

        // Outer glow
        pGlow.setShader(new RadialGradient(x, y + bobY, 30f,
                Color.argb((int)(glow * 80), 0, 230, 118),
                Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y + bobY, 30f, pGlow);
        pGlow.setShader(null);

        canvas.save();
        canvas.translate(x, y + bobY);

        // Canister body
        pCanister.setColor(Color.parseColor("#1B5E20"));
        canvas.drawRoundRect(new RectF(-WIDTH/2f, -HEIGHT/2f, WIDTH/2f, HEIGHT/2f), 6, 6, pCanister);

        // Canister cap
        pCanister.setColor(Color.parseColor("#FF6F00"));
        canvas.drawRoundRect(new RectF(-WIDTH/4f, -HEIGHT/2f - 8, WIDTH/4f, -HEIGHT/2f), 4, 4, pCanister);

        // Green stripe
        pFlame.setColor(Color.parseColor("#00E676"));
        pFlame.setAlpha((int)(glow * 200));
        canvas.drawRect(-WIDTH/2f + 4, -HEIGHT/2f + 10, WIDTH/2f - 4, -HEIGHT/2f + 14, pFlame);
        canvas.drawRect(-WIDTH/2f + 4, HEIGHT/2f - 14, WIDTH/2f - 4, HEIGHT/2f - 10, pFlame);

        // "FUEL" label
        pLabel.setColor(Color.parseColor("#00E676"));
        pLabel.setTextSize(10f);
        canvas.drawText("FUEL", 0, 4, pLabel);

        // Flame icon
        pFlame.setColor(Color.parseColor("#FF6F00"));
        pFlame.setAlpha((int)(glow * 255));
        canvas.drawCircle(0, -HEIGHT/2f - 3f, 5f, pFlame);

        canvas.restore();
    }

    public RectF getCollisionRect() {
        return new RectF(x - WIDTH, y - HEIGHT, x + WIDTH, y + HEIGHT);
    }
}
