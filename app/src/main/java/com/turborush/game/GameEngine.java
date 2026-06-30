package com.turborush.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.turborush.game.entities.AICar;
import com.turborush.game.entities.Coin;
import com.turborush.game.entities.FuelPickup;
import com.turborush.game.entities.ParticleSystem;
import com.turborush.game.entities.PlayerCar;
import com.turborush.game.entities.Road;
import com.turborush.game.models.GameState;
import com.turborush.game.models.PlayerProgress;
import com.turborush.game.models.Vehicle;
import com.turborush.game.ui.GarageScreen;
import com.turborush.game.ui.HUD;
import com.turborush.game.ui.MainMenuScreen;
import com.turborush.game.ui.PauseScreen;
import com.turborush.game.ui.SettingsScreen;
import com.turborush.game.ui.TrackSelectionScreen;
import com.turborush.game.ui.ProfileScreen;
import com.turborush.game.ui.LoginScreen;
import com.turborush.game.ui.LeaderboardScreen;
import com.turborush.game.ui.MultiplayerScreen;
import com.turborush.game.MultiplayerManager;
import com.turborush.game.models.Track;
import android.net.Uri;
import java.util.Map;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import java.io.IOException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    private final Context context;
    private float screenW, screenH;

    // Subsystems
    private GameState currentState = GameState.MAIN_MENU;
    private GameState previousState = null;
    private int crashesSinceLastAd = 0;
    
    private StorageManager storage;
    private GameAudioManager audio;

    // Entities
    private Road road;
    private PlayerCar playerCar;
    private ParticleSystem particles;

    private List<AICar> aiCars = new ArrayList<>();
    private List<Coin> coins = new ArrayList<>();
    private List<FuelPickup> fuelPickups = new ArrayList<>();

    // UI
    private MainMenuScreen mainMenu;
    private GarageScreen garage;
    private SettingsScreen settingsUi;
    private PauseScreen pauseScreen;
    private TrackSelectionScreen trackSelectionUi;
    private ProfileScreen profileScreen;
    private LoginScreen loginScreen;
    private LeaderboardScreen leaderboardScreen;
    private MultiplayerScreen multiplayerScreen;
    private com.turborush.game.ui.MultiplayerResultScreen multiplayerResultScreen;
    private MultiplayerManager multiplayerManager;
    private String loginStatusMessage = "";
    private com.turborush.game.StorageManager.InviteInfo pendingInvite;
    private boolean wasLoggedIn = false;
    
    // Invite UI Rects
    private RectF btnInviteJoin = new RectF();
    private RectF btnInviteDecline = new RectF();
    
    // AdMob 
    private RectF btnWatchAd = new RectF();
    private boolean hasDoubledCoins = false;

    private List<FloatingText> floatingTexts = new ArrayList<>();

    private class FloatingText {
        float x, y, timer;
        String text;
        int color;
    }
    private HUD hud;

    // Game variables
    private float roadScrollSpeed = 500f;
    private float baseRoadSpeed = 500f;
    private float exactScore = 0f;
    private long currentScore = 0;
    private int runCoins = 0;
    private float spawnTimer = 0f;
    private float coinSpawnTimer = 0f;
    private float fuelSpawnTimer = 0f;
    private float animTimer = 0f;

    // Vehicle Data
    private List<Vehicle> allVehicles = new ArrayList<>();
    private PlayerProgress progress;
    private int garageIdx = 0;
    
    // Track Data
    private List<Track> allTracks = new ArrayList<>();
    private int trackIdx = 0;
    
    // World themes
    private GameState.WorldTheme currentWorld = GameState.WorldTheme.CITY;
    private int currentLevel = 1;
    private float transitionTimer = 0f;
    private String transitionText = "";
    private Paint pTransition = new Paint(Paint.ANTI_ALIAS_FLAG);

    public VoiceChatManager voiceChatManager;
    private Random rand = new Random();

    public GameEngine(Context context) {
        this.context = context;
        storage = new StorageManager(context);
        audio = new GameAudioManager(context);
        pTransition.setTextAlign(Paint.Align.CENTER);
        pTransition.setFakeBoldText(true);

        initVehicles();
        initTracks();
        progress = storage.loadProgress();

        road = new Road();
        playerCar = new PlayerCar();
        audio.setMuted(progress.isMuted);
        audio.setSfxMuted(progress.isSfxMuted);
        
        particles = new ParticleSystem();

        mainMenu = new MainMenuScreen();
        garage = new GarageScreen();
        settingsUi = new SettingsScreen();
        trackSelectionUi = new TrackSelectionScreen();
        profileScreen = new ProfileScreen();
        leaderboardScreen = new LeaderboardScreen();
        multiplayerResultScreen = new com.turborush.game.ui.MultiplayerResultScreen();
        
        pauseScreen = new PauseScreen();
        loginScreen = new LoginScreen();
        multiplayerScreen = new MultiplayerScreen();
        hud = new HUD();

        voiceChatManager = new VoiceChatManager(context);
        
        multiplayerManager = new MultiplayerManager(progress.playerName);
        applySettings();
    }

    private void initVehicles() {
        allVehicles.add(new Vehicle(0, "Viper X", Vehicle.TYPE_SPORTS_CAR, "D", 0, 3, 3, 3, 3));
        allVehicles.add(new Vehicle(1, "Thunder V8", Vehicle.TYPE_MUSCLE_CAR, "C", 1000, 5, 4, 3, 5));
        allVehicles.add(new Vehicle(2, "Road King", Vehicle.TYPE_SUV, "C", 2500, 4, 5, 4, 6));
        allVehicles.add(new Vehicle(3, "Titan Hauler", Vehicle.TYPE_TRUCK, "B", 4000, 3, 3, 6, 9));
        allVehicles.add(new Vehicle(4, "Phantom R", Vehicle.TYPE_MOTORCYCLE, "A", 6000, 8, 9, 5, 2));
        allVehicles.add(new Vehicle(5, "Ghost S", Vehicle.TYPE_HYPERCAR, "S", 10000, 10, 10, 7, 4));
    }

    private void initTracks() {
        allTracks.add(new Track(0, "City Night", 0, GameState.WorldTheme.CITY));
        allTracks.add(new Track(1, "Mountain Pass", 500, GameState.WorldTheme.MOUNTAIN));
        allTracks.add(new Track(2, "Coastal Drive", 800, GameState.WorldTheme.OCEAN));
        allTracks.add(new Track(3, "Desert Run", 1000, GameState.WorldTheme.DESERT));
        allTracks.add(new Track(4, "Snowy Peaks", 1500, GameState.WorldTheme.SNOW));
        allTracks.add(new Track(5, "Forest Trail", 2000, GameState.WorldTheme.FOREST));
        allTracks.add(new Track(6, "Canyon Edge", 2500, GameState.WorldTheme.CANYON));
        allTracks.add(new Track(7, "Neon Grid", 3000, GameState.WorldTheme.NEON_CITY));
        allTracks.add(new Track(8, "Synthwave", 3500, GameState.WorldTheme.RETRO));
        allTracks.add(new Track(9, "Village Path", 4000, GameState.WorldTheme.VILLAGE));
        allTracks.add(new Track(10, "Cyber District", 4500, GameState.WorldTheme.CYBERPUNK));
        allTracks.add(new Track(11, "Volcano Core", 5000, GameState.WorldTheme.VOLCANO));
    }

    public void onSurfaceChanged(int width, int height) {
        screenW = width;
        screenH = height;
        
        baseRoadSpeed = screenH * 0.35f; // Scale base speed dynamically
        roadScrollSpeed = baseRoadSpeed;
        
        road.init(width, height);
        particles.init(width, height);
        mainMenu.init(width, height);
        garage.init(width, height);
        settingsUi.init(width, height);
        pauseScreen.init(width, height);
        trackSelectionUi.init(width, height);
        profileScreen.init(width, height);
        loginScreen.init(width, height);
        leaderboardScreen.init(width, height);
        multiplayerScreen.init(width, height);
        multiplayerResultScreen.init(width, height);
        hud.init(width, height);

        applySelectedVehicle();
        if (progress.avatarUri != null && !progress.avatarUri.isEmpty()) {
            loadAvatarBitmap(Uri.parse(progress.avatarUri));
        }
        currentState = GameState.MAIN_MENU;
    }
    
    private void applySettings() {
        // Load settings from storage in reality, simplified here
        settingsUi.tiltOn = storage.getTiltPreference(context);
        settingsUi.musicOn = !progress.isMuted;
        settingsUi.nightMode = progress.isNightMode;
    }

    private void applySelectedVehicle() {
        Vehicle v = null;
        for (Vehicle vehicle : allVehicles) {
            if (vehicle.id == progress.selectedVehicleId) {
                v = vehicle;
                break;
            }
        }
        if (v == null) v = allVehicles.get(0);
        
        v.selectedColorIndex = storage.getVehicleColorIndex(progress, v.id);

        Track t = null;
        for (Track track : allTracks) {
            if (track.id == progress.selectedTrackId) {
                t = track;
                break;
            }
        }
        if (t == null) t = allTracks.get(0);
        currentWorld = t.theme;

        // Apply Stats
        playerCar.init(screenW, screenH, road.roadLeft, road.roadRight);
        playerCar.setVehicleType(v.id);
        playerCar.setBodyColor(v.getSelectedColor());
        playerCar.vehicleStats = v;
        playerCar.lerpSpeed = 4f + (v.handling * 0.6f);
        playerCar.fuelDrainRate = 1.0f - (v.fuel * 0.06f);
    }

    public void update(float dt) {
        animTimer += dt;
        
        if (previousState != currentState) {
            onStateChanged(previousState, currentState);
            previousState = currentState;
        }

        GameState state = currentState;
        
        if (progress != null && progress.isLoggedIn && !wasLoggedIn) {
            wasLoggedIn = true;
            storage.listenForInvites(invite -> {
                pendingInvite = invite;
            });
        }
        
        if (state != GameState.RACING && state != GameState.MULTIPLAYER_RACING && state != GameState.CRASHING && state != GameState.PAUSED) {
            audio.stopEngine();
        }

        if (state == GameState.MAIN_MENU) mainMenu.update(dt);
        else if (state == GameState.GARAGE) garage.update(dt);
        else if (state == GameState.TRACK_SELECTION) trackSelectionUi.update(dt);
        else if (state == GameState.PROFILE) profileScreen.update(dt);
        else if (state == GameState.LOGIN) loginScreen.update(dt);
        else if (state == GameState.LEADERBOARD) { /* static UI */ }
        else if (state == GameState.MULTIPLAYER_LOBBY) {
            if (multiplayerManager != null && multiplayerManager.matchStarted) {
                startGame(multiplayerManager.trackSeed);
                currentState = GameState.MULTIPLAYER_RACING;
            }
        }
        else if (state == GameState.RACING) updateRacing(dt);
        else if (state == GameState.MULTIPLAYER_RACING) updateMultiplayerRacing(dt);
        else if (state == GameState.MULTIPLAYER_CRASHED) updateMultiplayerCrashed(dt);
        else if (state == GameState.CRASHING) updateCrashing(dt);
        else if (state == GameState.GAME_OVER) particles.update(dt, currentWorld);

        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.timer += dt;
            ft.y -= 50f * dt;
            if (ft.timer > 1.0f) floatingTexts.remove(i);
        }
    }

    private void onStateChanged(GameState oldState, GameState newState) {
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            
            // Manage Banner Ad
            if (newState == GameState.MAIN_MENU || newState == GameState.GARAGE || newState == GameState.TRACK_SELECTION) {
                mainActivity.showBannerAd();
            } else {
                mainActivity.hideBannerAd();
            }
            
            // Manage Interstitial Ad (Crash)
            if (newState == GameState.GAME_OVER || newState == GameState.MULTIPLAYER_RESULTS) {
                if (oldState != GameState.GAME_OVER && oldState != GameState.MULTIPLAYER_RESULTS) {
                    crashesSinceLastAd++;
                    if (crashesSinceLastAd >= 3) {
                        crashesSinceLastAd = 0;
                        mainActivity.showInterstitialAd();
                    }
                }
            }
        }
    }

    private void updateRacing(float dt) {
        roadScrollSpeed = baseRoadSpeed + (currentScore / 50f);
        road.update(roadScrollSpeed, dt);
        playerCar.update(dt, roadScrollSpeed);
        particles.update(dt, currentWorld);
        hud.update(dt);
        
        // World / Level progression
        int newLevel = (int)(currentScore / 1000) + 1;
        if (newLevel != currentLevel) {
            currentLevel = newLevel;
            GameState.WorldTheme newTheme;
            if (currentLevel <= 4) newTheme = GameState.WorldTheme.CITY;
            else if (currentLevel <= 8) newTheme = GameState.WorldTheme.VILLAGE;
            else if (currentLevel <= 13) newTheme = GameState.WorldTheme.MOUNTAIN;
            else newTheme = GameState.WorldTheme.OCEAN;
            
            if (newTheme != currentWorld) {
                currentWorld = newTheme;
                transitionTimer = 3f;
                transitionText = "ENTERING " + currentWorld.name();
            }
        }
        if (transitionTimer > 0) transitionTimer -= dt;

        // Update score independently of screen resolution
        float speedMultiplier = roadScrollSpeed / baseRoadSpeed;
        exactScore += 50f * speedMultiplier * dt; // 50 points per sec at base speed
        currentScore = (long) exactScore;
        
        audio.updateEnginePitch(speedMultiplier - 1.0f);
        
        // Track Top Speed
        float currentKmh = speedMultiplier * 100f; // 100kmh at base speed
        if (currentKmh > progress.topSpeedReached) {
            progress.topSpeedReached = currentKmh;
        }

        spawnEntities(dt);
        updateEntities(dt);
        checkCollisions();

        if (playerCar.fuel <= 0) {
            audio.playCrash();
            currentState = GameState.GAME_OVER;
            storage.submitScore(progress, currentScore, runCoins);
        }
        if (animTimer > 0.05f) {
            animTimer = 0;
        }
    }

    private void updateMultiplayerRacing(float dt) {
        updateRacing(dt);
        if (multiplayerManager != null) {
            multiplayerManager.updateMyPosition(playerCar.x, exactScore, currentState == GameState.CRASHING || currentState == GameState.GAME_OVER, runCoins);
        }
    }

    private void updateMultiplayerCrashed(float dt) {
        particles.update(dt, currentWorld);
        if (multiplayerManager != null) {
            if (multiplayerManager.isHost && multiplayerManager.isEveryoneCrashed() && !multiplayerManager.matchFinished) {
                multiplayerManager.setMatchFinished();
            }
            if (multiplayerManager.matchFinished) {
                currentState = GameState.MULTIPLAYER_RESULTS;
            }
        }
    }

    private void updateCrashing(float dt) {
        roadScrollSpeed *= 0.9f; // Slow down
        road.update(roadScrollSpeed, dt);
        particles.update(dt, currentWorld);
        hud.update(dt);
        if (roadScrollSpeed < 10f) {
            currentState = GameState.GAME_OVER;
            audio.stopEngine();
            
            // Finalize Lifetime Stats
            progress.totalRaces++;
            progress.totalDistance += (long)(currentScore / 10f);
            progress.playerXp += currentScore;
            
            // Level Up Check (1000 XP per level)
            while (progress.playerXp >= progress.playerLevel * 1000L) {
                progress.playerLevel++;
            }
            
            storage.submitScore(progress, currentScore, runCoins); // this also calls saveProgress
            
            if (multiplayerManager != null && multiplayerManager.currentRoomCode != null) {
                currentState = GameState.MULTIPLAYER_CRASHED;
                multiplayerManager.updateMyPosition(playerCar.x, exactScore, true, runCoins);
            } else {
                currentState = GameState.GAME_OVER;
            }
        }
    }

    private void showEditNameDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Enter Player Name");
        final android.widget.EditText input = new android.widget.EditText(context);
        input.setText(progress.playerName);
        builder.setView(input);
        builder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                progress.playerName = input.getText().toString().trim();
                if(progress.playerName.isEmpty()) progress.playerName = "Racer";
                storage.saveProgress(progress);
            }
        });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showAuthDialog(final boolean isRegister) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(isRegister ? "Register New Account" : "Login");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        
        final android.widget.EditText emailInput = new android.widget.EditText(context);
        emailInput.setHint("Email Address");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        
        final android.widget.EditText passwordInput = new android.widget.EditText(context);
        passwordInput.setHint("Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        
        layout.addView(emailInput);
        layout.addView(passwordInput);
        builder.setView(layout);
        
        builder.setPositiveButton(isRegister ? "Register" : "Login", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                String email = emailInput.getText().toString().trim();
                String pass = passwordInput.getText().toString().trim();
                if(email.isEmpty() || pass.isEmpty()) {
                    loginStatusMessage = "Email and Password required!";
                    return;
                }
                
                loginStatusMessage = "Authenticating...";
                FirebaseAuth auth = FirebaseAuth.getInstance();
                
                if (isRegister) {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loginStatusMessage = "Registration Successful!";
                            progress.isLoggedIn = true;
                            progress.loginProvider = "Email";
                            storage.saveProgress(progress);
                        } else {
                            loginStatusMessage = "Error: " + task.getException().getMessage();
                        }
                    });
                } else {
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loginStatusMessage = "Login Successful!";
                            progress.isLoggedIn = true;
                            progress.loginProvider = "Email";
                            storage.saveProgress(progress);
                        } else {
                            loginStatusMessage = "Error: " + task.getException().getMessage();
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void spawnEntities(float dt) {
        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnAICar();
            spawnTimer = 1.0f + rand.nextFloat() * 1.5f;
        }

        coinSpawnTimer -= dt;
        if (coinSpawnTimer <= 0) {
            spawnCoin();
            coinSpawnTimer = 2.0f + rand.nextFloat() * 2.0f;
        }

        fuelSpawnTimer -= dt;
        if (fuelSpawnTimer <= 0) {
            spawnFuel();
            fuelSpawnTimer = 8.0f + rand.nextFloat() * 5.0f;
        }
    }

    private void spawnAICar() {
        int lane = rand.nextInt(3);
        boolean oncoming = rand.nextFloat() < 0.7f;
        
        float spawnY = oncoming ? -100f : road.roadBottom + 100f;
        
        // Prevent overlap check
        for(AICar c : aiCars) {
            if(c.lane == lane && Math.abs(c.y - spawnY) < 300f) return;
        }

        float x = road.roadLeft + (lane * road.laneWidth) + (road.laneWidth / 2f);
        aiCars.add(new AICar(x, spawnY, lane, oncoming ? AICar.Direction.ONCOMING : AICar.Direction.SAME_LANE));
    }

    private void spawnCoin() {
        float minX = road.roadLeft + 20f;
        float maxX = road.roadRight - 20f;
        float x = minX + rand.nextFloat() * (maxX - minX);
        float y = -100f;
        Coin coin = new Coin();
        coin.spawn(x, y);
        coins.add(coin);
    }

    private void spawnFuel() {
        float minX = road.roadLeft + 20f;
        float maxX = road.roadRight - 20f;
        float x = minX + rand.nextFloat() * (maxX - minX);
        float y = -100f;
        FuelPickup fp = new FuelPickup();
        fp.spawn(x, y);
        fuelPickups.add(fp);
    }

    private void updateEntities(float dt) {
        for (int i = aiCars.size() - 1; i >= 0; i--) {
            AICar car = aiCars.get(i);
            car.update(roadScrollSpeed, dt);
            if (car.direction == AICar.Direction.ONCOMING && car.y > screenH + 100) aiCars.remove(i);
            else if (car.direction == AICar.Direction.SAME_LANE && car.y < -100) aiCars.remove(i);
        }

        for (int i = coins.size() - 1; i >= 0; i--) {
            Coin coin = coins.get(i);
            coin.update(roadScrollSpeed, dt);
            if (coin.y > screenH + 50 || coin.collected) coins.remove(i);
        }

        for (int i = fuelPickups.size() - 1; i >= 0; i--) {
            FuelPickup fuel = fuelPickups.get(i);
            fuel.update(dt, roadScrollSpeed, screenH);
            if (!fuel.active) fuelPickups.remove(i);
        }
    }

    private void checkCollisions() {
        RectF pRect = new RectF(playerCar.x - playerCar.width/2, playerCar.y - playerCar.height/2, playerCar.x + playerCar.width/2, playerCar.y + playerCar.height/2);

        // Soft / Hard Collisions with AI
        for (AICar car : aiCars) {
            if (!car.isActive) continue;
            RectF cRect = new RectF(car.x - car.width/2 + 10, car.y - car.height/2 + 10, car.x + car.width/2 - 10, car.y + car.height/2 - 10);
            
            if (RectF.intersects(pRect, cRect)) {
                if (car.direction == AICar.Direction.ONCOMING) {
                    // Hard collision
                    audio.playCrash();
                    particles.spawnExplosion(playerCar.x, playerCar.y);
                    
                    if (currentState == GameState.MULTIPLAYER_RACING) {
                        currentState = GameState.MULTIPLAYER_CRASHED;
                        audio.stopEngine();
                        if (multiplayerManager != null && multiplayerManager.currentRoomCode != null) {
                            multiplayerManager.updateMyPosition(playerCar.x, exactScore, true, runCoins); // Crashed
                        }
                    } else {
                        currentState = GameState.CRASHING;
                    }
                    return;
                } else {
                    // Soft collision
                    audio.playCrash(); // Or a bump sound
                    particles.spawnSparks(car.x, car.y);
                    currentScore = Math.max(0, currentScore - 5);
                    spawnFloatingText("-5", car.x, car.y, Color.RED);
                    car.isActive = false;
                    
                    // Apply slip based on durability
                    float slip = 1.0f - (playerCar.vehicleStats.durability * 0.07f);
                    playerCar.x += (playerCar.x > car.x ? 30f : -30f) * slip;
                }
            }
        }

        // Coins
        for (Coin c : coins) {
            if (!c.collected && RectF.intersects(pRect, new RectF(c.x - 24, c.y - 24, c.x + 24, c.y + 24))) {
                c.collected = true;
                runCoins++;
                exactScore += 10f;
                currentScore = (long) exactScore;
                audio.playCoin();
                spawnFloatingText("+10", c.x, c.y, Color.parseColor("#FFD700"));
            }
        }

        // Fuel
        for (FuelPickup f : fuelPickups) {
            if (!f.collected && RectF.intersects(pRect, f.getCollisionRect())) {
                f.collect();
                playerCar.fuel = Math.min(100f, playerCar.fuel + 30f);
                audio.playFuel();
            }
        }
    }

    private void spawnFloatingText(String text, float x, float y, int color) {
        FloatingText ft = new FloatingText();
        ft.text = text;
        ft.x = x;
        ft.y = y;
        ft.color = color;
        ft.timer = 0f;
        floatingTexts.add(ft);
    }

    public void draw(Canvas canvas) {
        GameState state = currentState;

        if (state == GameState.MAIN_MENU) mainMenu.draw(canvas, progress);
        else if (state == GameState.GARAGE) garage.draw(canvas, progress, storage, allVehicles, garageIdx);
        else if (state == GameState.TRACK_SELECTION) trackSelectionUi.draw(canvas, progress, allTracks, trackIdx);
        else if (state == GameState.SETTINGS) settingsUi.draw(canvas);
        else if (state == GameState.PROFILE) profileScreen.draw(canvas, progress);
        else if (state == GameState.LOGIN) loginScreen.draw(canvas, progress, loginStatusMessage);
        else if (state == GameState.LEADERBOARD) leaderboardScreen.draw(canvas, progress);
        else if (state == GameState.MULTIPLAYER_LOBBY) {
            multiplayerScreen.multiplayerManager = multiplayerManager;
            multiplayerScreen.draw(canvas);
        }
        else if (state == GameState.MULTIPLAYER_RESULTS) {
            multiplayerResultScreen.draw(canvas, progress, (int)exactScore, runCoins, multiplayerManager);
        }
        else {
            road.draw(canvas, currentWorld, progress.isNightMode);
            for (Coin c : coins) c.draw(canvas, animTimer, road.roadLeft, road.roadRight);
            for (FuelPickup f : fuelPickups) f.draw(canvas);
            for (AICar c : aiCars) c.draw(canvas);
            
            if (state != GameState.CRASHING && state != GameState.GAME_OVER && state != GameState.MULTIPLAYER_CRASHED) {
                playerCar.drawDetailed(canvas, new Paint(Paint.ANTI_ALIAS_FLAG), progress.isNightMode);
            }

            particles.draw(canvas, currentWorld);
            hud.draw(canvas, (int)currentScore, currentLevel, runCoins, currentScore / 10f, playerCar.fuel, progress.isMuted, settingsUi.tiltOn);

            for (FloatingText ft : floatingTexts) {
                pTransition.setColor(ft.color);
                pTransition.setTextSize(40f);
                pTransition.setAlpha((int)((1.0f - ft.timer) * 255));
                pTransition.setFakeBoldText(true);
                canvas.drawText(ft.text, ft.x, ft.y, pTransition);
                pTransition.setAlpha(255);
            }

            if (transitionTimer > 0) {
                // Flash overlay fading out
                float alpha = Math.min(1f, transitionTimer / 1.0f); // Fades in last 1 second
                pTransition.setColor(Color.argb((int)(alpha * 200), 255, 255, 255));
                canvas.drawRect(0, 0, screenW, screenH, pTransition);
                
                // Text for 2 seconds
                if (transitionTimer > 1f) {
                    pTransition.setColor(Color.BLACK);
                    pTransition.setTextSize(screenW * 0.08f);
                    canvas.drawText(transitionText, screenW/2, screenH/2, pTransition);
                }
            }

            if (state == GameState.GAME_OVER) {
                Paint p = new Paint();
                p.setColor(Color.argb(150, 0, 0, 0));
                canvas.drawRect(0, 0, screenW, screenH, p);
                p.setColor(Color.WHITE);
                p.setTextSize(screenW * 0.1f);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("CRASHED!", screenW / 2f, screenH * 0.4f, p);
                p.setTextSize(screenW * 0.05f);
                canvas.drawText("Score: " + currentScore, screenW / 2f, screenH * 0.5f, p);
                
                if (!hasDoubledCoins && runCoins > 0) {
                    float btnW = screenW * 0.8f;
                    float btnH = screenH * 0.08f;
                    float btnY = screenH * 0.58f;
                    btnWatchAd.set(screenW/2f - btnW/2, btnY, screenW/2f + btnW/2, btnY + btnH);
                    
                    p.setColor(Color.parseColor("#4CAF50")); // Green
                    canvas.drawRoundRect(btnWatchAd, 20f, 20f, p);
                    p.setColor(Color.WHITE);
                    p.setTextSize(btnH * 0.4f);
                    canvas.drawText("📺 WATCH AD (2X COINS!)", btnWatchAd.centerX(), btnWatchAd.centerY() + p.getTextSize()*0.38f, p);
                    
                    p.setColor(Color.WHITE);
                    p.setTextSize(screenW * 0.04f);
                    canvas.drawText("TAP ANYWHERE ELSE TO RESTART", screenW / 2f, screenH * 0.75f, p);
                } else {
                    canvas.drawText("TAP TO RESTART", screenW / 2, screenH / 2 + 50, p);
                }
            }
            
            if (state == GameState.MULTIPLAYER_CRASHED) {
                Paint p = new Paint();
                p.setColor(Color.argb(150, 0, 0, 0));
                canvas.drawRect(0, 0, screenW, screenH, p);
                p.setColor(Color.WHITE);
                p.setTextSize(screenW * 0.08f);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("CRASHED!", screenW / 2f, screenH * 0.4f, p);
                p.setTextSize(screenW * 0.05f);
                canvas.drawText("Waiting for other players to finish...", screenW / 2f, screenH * 0.5f, p);
            }
            
            if ((state == GameState.MULTIPLAYER_RACING || state == GameState.MULTIPLAYER_CRASHED) && multiplayerManager != null) {
                Paint textPaint = new Paint();
                textPaint.setAntiAlias(true);
                textPaint.setShadowLayer(4f, 1f, 1f, Color.argb(180, 0, 0, 0));
                
                // Draw Real-time Mini Leaderboard
                float topY = 80f; 
                float row3Y = topY + 230f; 
                float margin = 40f;
                
                textPaint.setTextSize(25f);
                textPaint.setColor(Color.parseColor("#FF9800"));
                textPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("LEADERBOARD:", margin, row3Y, textPaint);
                
                class LiveScore {
                    String name;
                    float score;
                    LiveScore(String n, float s) { name = n; score = s; }
                }
                
                List<LiveScore> liveScores = new ArrayList<>();
                liveScores.add(new LiveScore(progress.playerName + " (You)", (float)exactScore));
                
                if (multiplayerManager.opponents != null) {
                    for (Map.Entry<String, MultiplayerManager.OpponentState> entry : multiplayerManager.opponents.entrySet()) {
                        liveScores.add(new LiveScore(entry.getValue().name, entry.getValue().score));
                    }
                }
                
                java.util.Collections.sort(liveScores, new java.util.Comparator<LiveScore>() {
                    @Override
                    public int compare(LiveScore p1, LiveScore p2) {
                        return Float.compare(p2.score, p1.score);
                    }
                });

                for (int i = 0; i < liveScores.size(); i++) {
                    float oppRowY = row3Y + 40f + (i * 40f);
                    textPaint.setTextSize(25f);
                    textPaint.setColor(i == 0 ? Color.parseColor("#FFD700") : Color.WHITE);
                    textPaint.setFakeBoldText(true);
                    canvas.drawText((i+1) + ". " + liveScores.get(i).name + ": " + (int)liveScores.get(i).score + "m", margin, oppRowY, textPaint);
                }
                
                // Draw opponent ghosts
                int ghostIndex = 0;
                if (multiplayerManager.opponents != null) {
                    for (Map.Entry<String, MultiplayerManager.OpponentState> entry : multiplayerManager.opponents.entrySet()) {
                        MultiplayerManager.OpponentState opp = entry.getValue();
                        
                        // Calculate opponent Y based on score (distance) difference
                        float distDiff = opp.score - exactScore;
                        // Scale distance to pixels. 1 distance = baseRoadSpeed/50 pixels.
                        float pixelsPerDistance = baseRoadSpeed / 50f;
                        float renderY = playerCar.y - (distDiff * pixelsPerDistance);

                        // Draw opponent ghost if within screen bounds roughly
                        if (renderY > -200f && renderY < screenH + 200f) {
                            Paint ghostPaint = new Paint();
                            ghostPaint.setColor(Color.argb(128, 255, ghostIndex * 80, 255 - (ghostIndex * 80))); 
                            canvas.drawRect(opp.x - playerCar.width/2, renderY - playerCar.height/2, 
                                            opp.x + playerCar.width/2, renderY + playerCar.height/2, ghostPaint);
                            textPaint.setTextSize(30f);
                            textPaint.setColor(Color.WHITE);
                            textPaint.setTextAlign(Paint.Align.CENTER);
                            canvas.drawText(opp.name, opp.x, renderY - playerCar.height/2 - 10, textPaint);
                            textPaint.setTextAlign(Paint.Align.LEFT); // Reset text align
                        }
                        ghostIndex++;
                    }
                }
                textPaint.setFakeBoldText(false);
                
                // Draw Chat UI in center of screen
                textPaint.setTextSize(50f);
                textPaint.setTextAlign(Paint.Align.CENTER);
                long now = System.currentTimeMillis();
                
                int activeCount = 0;
                for (MultiplayerManager.ChatMessage msg : multiplayerManager.chatMessages) {
                    if (now - msg.timestamp <= 3000) activeCount++;
                }
                
                float chatY = screenH / 2f - (activeCount * 30f); // Center block vertically
                for (MultiplayerManager.ChatMessage msg : multiplayerManager.chatMessages) {
                    if (now - msg.timestamp > 3000) continue; // Only show for 3 seconds locally
                    textPaint.setColor(msg.sender.equals(progress.playerName) ? Color.GREEN : Color.parseColor("#FFEB3B")); // Yellow for opponent
                    canvas.drawText(msg.sender + ": " + msg.message, screenW / 2f, chatY, textPaint);
                    chatY += 60f;
                }
                
                // Draw Chat Button below Tilt button
                textPaint.setTextSize(30f);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setColor(Color.parseColor("#1A2A4A"));
                textPaint.setAlpha(150);
                canvas.drawCircle(screenW - 80f, 520f, 55f, textPaint); // Background
                textPaint.setColor(Color.WHITE);
                textPaint.setAlpha(255);
                canvas.drawText("CHAT", screenW - 80f, 530f, textPaint);
                
                // Draw Mic Mute Button below Chat button
                textPaint.setColor(Color.parseColor("#1A2A4A"));
                textPaint.setAlpha(150);
                canvas.drawCircle(screenW - 80f, 650f, 55f, textPaint); // Background
                textPaint.setColor(voiceChatManager != null && voiceChatManager.isMuted() ? Color.RED : Color.WHITE);
                textPaint.setAlpha(255);
                textPaint.setTextSize(50f);
                canvas.drawText(voiceChatManager != null && voiceChatManager.isMuted() ? "🔇" : "🎤", screenW - 80f, 668f, textPaint);
                
                // Draw Speaker Mute Button below Mic button
                textPaint.setColor(Color.parseColor("#1A2A4A"));
                textPaint.setAlpha(150);
                canvas.drawCircle(screenW - 80f, 780f, 55f, textPaint); // Background
                textPaint.setColor(voiceChatManager != null && voiceChatManager.isSpeakerMuted() ? Color.RED : Color.WHITE);
                textPaint.setAlpha(255);
                canvas.drawText(voiceChatManager != null && voiceChatManager.isSpeakerMuted() ? "🔈" : "🔊", screenW - 80f, 798f, textPaint);
                textPaint.setTextSize(30f);
            }

            if (state == GameState.PAUSED) {
                pauseScreen.draw(canvas, screenW, screenH);
            }
        }
        
        // Draw Invite Popup over everything (except during racing)
        if (pendingInvite != null && state != GameState.RACING && state != GameState.MULTIPLAYER_RACING && state != GameState.MULTIPLAYER_CRASHED) {
            Paint pOverlay = new Paint();
            pOverlay.setColor(Color.parseColor("#EE222222"));
            
            float popupW = screenW * 0.7f;
            float popupH = screenH * 0.25f;
            float px = screenW / 2 - popupW / 2;
            float py = screenH * 0.1f;
            
            canvas.drawRoundRect(new RectF(px, py, px + popupW, py + popupH), 30f, 30f, pOverlay);
            
            Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
            pText.setColor(Color.WHITE);
            pText.setTextSize(screenW * 0.05f);
            pText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(pendingInvite.fromName + " invited you to play!", screenW / 2, py + 80f, pText);
            
            // JOIN Button
            btnInviteJoin.set(px + 40f, py + popupH - 120f, screenW / 2 - 20f, py + popupH - 30f);
            pOverlay.setColor(Color.parseColor("#10B981"));
            canvas.drawRoundRect(btnInviteJoin, 20f, 20f, pOverlay);
            
            // DECLINE Button
            btnInviteDecline.set(screenW / 2 + 20f, py + popupH - 120f, px + popupW - 40f, py + popupH - 30f);
            pOverlay.setColor(Color.parseColor("#EF4444"));
            canvas.drawRoundRect(btnInviteDecline, 20f, 20f, pOverlay);
            
            pText.setTextSize(screenW * 0.04f);
            pText.setFakeBoldText(true);
            canvas.drawText("JOIN", btnInviteJoin.centerX(), btnInviteJoin.centerY() + 15f, pText);
            canvas.drawText("DECLINE", btnInviteDecline.centerX(), btnInviteDecline.centerY() + 15f, pText);
        }
    }

    public void handleTouchDown(float x, float y) {
        if (pendingInvite != null) {
            if (btnInviteJoin.contains(x, y)) {
                audio.playUi();
                String codeToJoin = pendingInvite.roomCode;
                pendingInvite = null;
                // Leave current room if already in one
                if (multiplayerManager != null) {
                    multiplayerManager.leaveRoom();
                    if (voiceChatManager != null) voiceChatManager.leaveChannel();
                } else {
                    multiplayerManager = new MultiplayerManager(progress.playerName);
                }
                
                currentState = GameState.MULTIPLAYER_LOBBY;
                multiplayerScreen.isWaitingForPlayer = false;
                multiplayerScreen.errorMessage = "Joining room...";
                storage.fetchFriendsData(friends -> {
                    multiplayerScreen.friends = friends;
                });
                
                multiplayerManager.joinRoom(codeToJoin, progress.playerName, new MultiplayerManager.OnRoomJoinCallback() {
                    @Override
                    public void onSuccess() {
                        multiplayerScreen.isWaitingForPlayer = true;
                        multiplayerScreen.currentRoomCode = multiplayerManager.currentRoomCode;
                        multiplayerScreen.errorMessage = "";
                        if (voiceChatManager != null) voiceChatManager.joinChannel(multiplayerManager.currentRoomCode);
                    }
                    @Override
                    public void onError(String error) { 
                        multiplayerScreen.isWaitingForPlayer = false;
                        multiplayerScreen.errorMessage = error; 
                    }
                });
                return;
            } else if (btnInviteDecline.contains(x, y)) {
                audio.playUi();
                pendingInvite = null;
                return;
            }
        }
        
        GameState state = currentState;

        if (state == GameState.MAIN_MENU) {
            if (mainMenu.btnStart.contains(x, y)) {
                startGame();
            } else if (mainMenu.btnGarage.contains(x, y)) {
                currentState = GameState.GARAGE;
                audio.playUi();
            } else if (mainMenu.btnTracks.contains(x, y)) {
                currentState = GameState.TRACK_SELECTION;
                audio.playUi();
            } else if (mainMenu.btnMultiplayer.contains(x, y)) {
                audio.playUi();
                if (!progress.isLoggedIn) {
                    spawnFloatingText("Please Login First!", x, y - 50, Color.RED);
                } else {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).requestVoicePermissions();
                    }
                    currentState = GameState.MULTIPLAYER_LOBBY;
                    storage.fetchFriendsData(friends -> {
                        multiplayerScreen.friends = friends;
                    });
                    if (multiplayerManager == null) {
                        multiplayerManager = new MultiplayerManager(progress.playerName);
                    }
                    multiplayerManager.onKickedCallback = new MultiplayerManager.OnKickedCallback() {
                        @Override
                        public void onKicked() {
                            if (currentState == GameState.MULTIPLAYER_LOBBY || currentState == GameState.MULTIPLAYER_RACING || currentState == GameState.MULTIPLAYER_CRASHED) {
                                multiplayerManager.leaveRoom();
                                if (voiceChatManager != null) voiceChatManager.leaveChannel();
                                multiplayerScreen.isWaitingForPlayer = false;
                                multiplayerScreen.errorMessage = "You were kicked by the host.";
                                currentState = GameState.MULTIPLAYER_LOBBY;
                            }
                        }
                    };
                }
            } else if (mainMenu.btnLeaderboard.contains(x, y)) {
                audio.playUi();
                currentState = GameState.LEADERBOARD;
                leaderboardScreen.fetchLeaderboard();
            } else if (mainMenu.btnSettings.contains(x, y)) {
                currentState = GameState.SETTINGS;
                audio.playUi();
            } else if (mainMenu.btnProfile.contains(x, y)) {
                currentState = GameState.PROFILE;
                audio.playUi();
                storage.fetchFriends(friendNames -> {
                    profileScreen.friends = friendNames;
                });
            }
        } else if (state == GameState.PROFILE) {
            if (profileScreen.btnBack.contains(x, y)) {
                currentState = GameState.MAIN_MENU;
                audio.playUi();
            } else if (profileScreen.btnFriendsCard.contains(x, y)) {
                profileScreen.isFriendsExpanded = !profileScreen.isFriendsExpanded;
                audio.playUi();
            } else if (profileScreen.btnEditAvatar.contains(x, y)) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).pickAvatarImage();
                } else {
                    progress.avatarId++;
                    storage.saveProgress(progress);
                }
                audio.playUi();
            } else if (profileScreen.btnEditName.contains(x, y)) {
                audio.playUi();
                showEditNameDialog();
            } else if (!progress.isLoggedIn && profileScreen.btnLogin.contains(x, y)) {
                currentState = GameState.LOGIN;
                audio.playUi();
            } else if (progress.isLoggedIn && profileScreen.btnLogout.contains(x, y)) {
                audio.playUi();
                progress.isLoggedIn = false;
                progress.playerName = "Racer";
                storage.saveProgress(progress);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).signOut();
                }
            }
        } else if (state == GameState.LOGIN) {
            if (loginScreen.btnBack.contains(x, y)) {
                audio.playUi();
                currentState = GameState.PROFILE;
                storage.fetchFriends(friendNames -> {
                    profileScreen.friends = friendNames;
                });
                loginStatusMessage = "";
                loginScreen.showOptions = false; // Reset state when leaving
            } else if (!loginScreen.showOptions && loginScreen.btnConnect.contains(x, y)) {
                audio.playUi();
                loginScreen.showOptions = true;
            } else if (loginScreen.showOptions) {
                if (loginScreen.btnGoogle.contains(x, y)) {
                    audio.playUi();
                    loginStatusMessage = "Starting Google Sign-In...";
                    // Proxy call to MainActivity via GameSurfaceView
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).startGoogleSignIn();
                    }
                } else if (loginScreen.btnEmail.contains(x, y)) {
                    audio.playUi();
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showEmailLoginDialog();
                    }
                }
            }
        } else if (state == GameState.LEADERBOARD) {
            if (leaderboardScreen.btnBack.contains(x, y)) {
                audio.playUi();
                currentState = GameState.MAIN_MENU;
            }
        } else if (state == GameState.MULTIPLAYER_LOBBY) {
            if (multiplayerScreen.btnBack.contains(x, y)) {
                audio.playUi();
                if (multiplayerManager != null) multiplayerManager.leaveRoom();
                if (voiceChatManager != null) voiceChatManager.leaveChannel();
                multiplayerScreen.isWaitingForPlayer = false;
                currentState = GameState.MAIN_MENU;
            } else if (!multiplayerScreen.isWaitingForPlayer && multiplayerScreen.btnCreate.contains(x, y)) {
                audio.playUi();
                String code = String.valueOf(1000 + (int)(Math.random() * 9000));
                multiplayerManager.createRoom(code, progress.playerName, new MultiplayerManager.OnRoomJoinCallback() {
                    @Override
                    public void onSuccess() {
                        multiplayerScreen.isWaitingForPlayer = true;
                        multiplayerScreen.currentRoomCode = code;
                        multiplayerScreen.errorMessage = "";
                        if (voiceChatManager != null) voiceChatManager.joinChannel(code);
                    }
                    @Override
                    public void onError(String error) { multiplayerScreen.errorMessage = error; }
                });
            } else if (!multiplayerScreen.isWaitingForPlayer && multiplayerScreen.btnJoin.contains(x, y)) {
                audio.playUi();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showJoinRoomDialog();
                }
            } else if (!multiplayerScreen.isWaitingForPlayer && multiplayerScreen.btnRandom.contains(x, y)) {
                audio.playUi();
                multiplayerManager.joinRandomRoom(progress.playerName, new MultiplayerManager.OnRoomJoinCallback() {
                    @Override
                    public void onSuccess() {
                        multiplayerScreen.isWaitingForPlayer = true;
                        multiplayerScreen.currentRoomCode = multiplayerManager.currentRoomCode;
                        multiplayerScreen.errorMessage = "";
                        if (voiceChatManager != null) voiceChatManager.joinChannel(multiplayerManager.currentRoomCode);
                    }
                    @Override
                    public void onError(String error) { multiplayerScreen.errorMessage = error; }
                });
            } else if (multiplayerScreen.isWaitingForPlayer && multiplayerManager.isHost && multiplayerManager.opponents.size() > 0 && multiplayerScreen.btnStartGame != null && multiplayerScreen.btnStartGame.contains(x, y)) {
                audio.playUi();
                multiplayerManager.startGame();
            } else if (multiplayerScreen.isWaitingForPlayer && multiplayerManager.isHost) {
                // Check if any invite button was pressed
                for (Map.Entry<String, RectF> entry : multiplayerScreen.inviteButtons.entrySet()) {
                    if (entry.getValue().contains(x, y)) {
                        audio.playUi();
                        storage.sendInvite(entry.getKey(), multiplayerScreen.currentRoomCode, progress.playerName);
                        if (context instanceof android.app.Activity) {
                            android.widget.Toast.makeText(context, "Invite sent!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        return; // return after sending invite to avoid further processing
                    }
                }
                
                // Check if any kick button was pressed
                for (Map.Entry<String, RectF> entry : multiplayerScreen.kickButtons.entrySet()) {
                    if (entry.getValue().contains(x, y)) {
                        audio.playUi();
                        multiplayerManager.kickPlayer(entry.getKey());
                        break;
                    }
                }
            }
        } else if (state == GameState.GARAGE) {
            handleGarageTouchDown(x, y);
        } else if (state == GameState.TRACK_SELECTION) {
            handleTrackTouchDown(x, y);
        } else if (state == GameState.SETTINGS) {
            handleSettingsTouchDown(x, y);
        } else if (state == GameState.RACING || state == GameState.MULTIPLAYER_RACING) {
            if (hud.btnPause.contains(x, y) || hud.btnPauseTop.contains(x, y)) {
                currentState = GameState.PAUSED;
                audio.stopEngine();
                audio.playUi();
            } else if (hud.btnMute.contains(x, y)) {
                settingsUi.musicOn = !settingsUi.musicOn;
                progress.isMuted = !settingsUi.musicOn;
                audio.setMuted(progress.isMuted);
                if (!progress.isMuted) audio.playEngine();
                storage.saveProgress(progress);
                audio.playUi();
            } else if (hud.btnTilt.contains(x, y)) {
                settingsUi.tiltOn = !settingsUi.tiltOn;
                audio.playUi();
            } else if (hud.btnLeft.contains(x, y) || x < screenW / 2) {
                playerCar.setSteeringLeft(true);
            } else if (hud.btnRight.contains(x, y) || x >= screenW / 2) {
                playerCar.setSteeringRight(true);
            }
            if (multiplayerManager != null && (state == GameState.MULTIPLAYER_RACING || state == GameState.MULTIPLAYER_CRASHED)) {
                // Check if tapped on CHAT button
                float dx = x - (screenW - 80f);
                float dy = y - 520f;
                if (dx*dx + dy*dy <= 60*60) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showChatInputDialog();
                    }
                    return;
                }
                
                // Check if tapped on MIC button
                float micDy = y - 650f;
                if (dx*dx + micDy*micDy <= 60*60) {
                    if (voiceChatManager != null) {
                        voiceChatManager.toggleMute();
                        audio.playUi();
                    }
                    return;
                }
                
                // Check if tapped on SPEAKER button
                float spkDy = y - 780f;
                if (dx*dx + spkDy*spkDy <= 60*60) {
                    if (voiceChatManager != null) {
                        voiceChatManager.toggleSpeaker();
                        audio.playUi();
                    }
                    return;
                }
            }
        } else if (state == GameState.MULTIPLAYER_CRASHED) {
            // Check if tapped on CHAT button
            if (multiplayerManager != null) {
                float dx = x - (screenW - 80f);
                float dy = y - 520f;
                if (dx*dx + dy*dy <= 60*60) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showChatInputDialog();
                    }
                    return;
                }
                
                // Check if tapped on MIC button
                float micDy = y - 650f;
                if (dx*dx + micDy*micDy <= 60*60) {
                    if (voiceChatManager != null) {
                        voiceChatManager.toggleMute();
                        audio.playUi();
                    }
                    return;
                }
                
                // Check if tapped on SPEAKER button
                float spkDy = y - 780f;
                if (dx*dx + spkDy*spkDy <= 60*60) {
                    if (voiceChatManager != null) {
                        voiceChatManager.toggleSpeaker();
                        audio.playUi();
                    }
                    return;
                }
            }
            
            // If they tap the HUD pause button, let them exit
            if (hud.btnPause.contains(x, y) || hud.btnPauseTop.contains(x, y)) {
                if (multiplayerManager != null) multiplayerManager.leaveRoom();
                if (voiceChatManager != null) voiceChatManager.leaveChannel();
                currentState = GameState.MAIN_MENU;
                audio.playUi();
            }
        } else if (state == GameState.PAUSED) {
            if (pauseScreen.btnResume.contains(x, y)) {
                currentState = GameState.RACING;
                audio.playEngine();
                audio.playUi();
            } else if (pauseScreen.btnRestart.contains(x, y)) {
                startGame();
            } else if (pauseScreen.btnGarage.contains(x, y)) {
                currentState = GameState.GARAGE;
                audio.playUi();
            } else if (pauseScreen.btnMainMenu.contains(x, y)) {
                if (multiplayerManager != null) multiplayerManager.leaveRoom();
                if (voiceChatManager != null) voiceChatManager.leaveChannel();
                currentState = GameState.MAIN_MENU;
                audio.playUi();
            }
        } else if (state == GameState.GAME_OVER) {
            if (!hasDoubledCoins && runCoins > 0 && btnWatchAd.contains(x, y)) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showRewardedAd(rewardItem -> {
                        hasDoubledCoins = true;
                        progress.totalCoins += runCoins;
                        storage.saveProgress(progress);
                        audio.playLevelUp(); // Play success sound
                    });
                }
            } else {
                currentState = GameState.MAIN_MENU;
                audio.playUi();
            }
        } else if (state == GameState.MULTIPLAYER_RESULTS) {
            if (multiplayerResultScreen.btnCancel != null && multiplayerResultScreen.btnCancel.contains(x, y)) {
                if (multiplayerManager != null) multiplayerManager.leaveRoom();
                if (voiceChatManager != null) voiceChatManager.leaveChannel();
                currentState = GameState.MAIN_MENU;
                audio.playUi();
            } else if (multiplayerResultScreen.btnStartAgain != null && multiplayerResultScreen.btnStartAgain.contains(x, y)) {
                if (multiplayerManager != null) {
                    multiplayerManager.resetPlayerState();
                    if (multiplayerManager.isHost) {
                        multiplayerManager.returnToLobby();
                    }
                }
                currentState = GameState.MULTIPLAYER_LOBBY;
                audio.playUi();
            } else if (multiplayerResultScreen.btnAddFriends != null) {
                for (int i = 0; i < multiplayerResultScreen.btnAddFriends.size(); i++) {
                    if (multiplayerResultScreen.btnAddFriends.get(i).contains(x, y)) {
                        String fId = multiplayerResultScreen.btnAddFriendIds.get(i);
                        String fName = multiplayerResultScreen.btnAddFriendNames.get(i);
                        storage.addFriend(fId, fName);
                        audio.playUi();
                        if (context instanceof android.app.Activity) {
                            android.widget.Toast.makeText(context, "Added " + fName + " to friends!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            }
        }
    }

    public void handleTouchEvent(android.view.MotionEvent event) {
        int action = event.getActionMasked();
        
        if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_POINTER_DOWN) {
            int pointerIndex = event.getActionIndex();
            handleTouchDown(event.getX(pointerIndex), event.getY(pointerIndex));
        }

        if (currentState == GameState.RACING || currentState == GameState.MULTIPLAYER_RACING) {
            boolean isLeft = false;
            boolean isRight = false;
            int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                if ((action == android.view.MotionEvent.ACTION_POINTER_UP || action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) && i == event.getActionIndex()) {
                    continue;
                }
                float px = event.getX(i);
                float py = event.getY(i);
                
                // Exclude the top right quadrant (Y < 300) where Pause/Mute buttons are.
                if (py > 300f) {
                    if (hud.btnLeft.contains(px, py) || px < screenW / 2) {
                        isLeft = true;
                    } else if (hud.btnRight.contains(px, py) || px >= screenW / 2) {
                        isRight = true;
                    }
                }
            }
            playerCar.setSteeringLeft(isLeft);
            playerCar.setSteeringRight(isRight);
        }
    }

    private void handleGarageTouchDown(float x, float y) {
        if (garage.showingColorConfirm) {
            if (garage.btnConfirmYes.contains(x, y)) {
                int COLOR_PRICE = 200;
                Vehicle v = allVehicles.get(garageIdx);
                if (progress.totalCoins >= COLOR_PRICE && garage.pendingColorIndex != -1) {
                    progress.totalCoins -= COLOR_PRICE;
                    storage.unlockColor(progress, v.id, garage.pendingColorIndex);
                    v.selectedColorIndex = garage.pendingColorIndex;
                    storage.saveVehicleColor(progress, v.id, garage.pendingColorIndex);
                    audio.playLevelUp();
                    android.widget.Toast.makeText(context, "Color Unlocked! (-" + COLOR_PRICE + " Coins)", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    audio.playCrash();
                    android.widget.Toast.makeText(context, "Not enough coins! Need " + COLOR_PRICE, android.widget.Toast.LENGTH_SHORT).show();
                }
                garage.showingColorConfirm = false;
                garage.pendingColorIndex = -1;
            } else if (garage.btnConfirmNo.contains(x, y)) {
                garage.showingColorConfirm = false;
                garage.pendingColorIndex = -1;
                audio.playUi();
            }
            return;
        }
        
        if(garage.btnBack.contains(x,y)) { currentState = GameState.MAIN_MENU; audio.playUi(); }
        else if(garage.btnNext.contains(x,y)) {
            garageIdx = (garageIdx+1)%allVehicles.size();
            audio.playUi();
        } else if(garage.btnPrev.contains(x,y)) {
            garageIdx = (garageIdx-1+allVehicles.size())%allVehicles.size();
            audio.playUi();
        } else if(garage.btnAction.contains(x,y)) {
            Vehicle v = allVehicles.get(garageIdx);
            if(storage.isVehicleOwned(progress, v.id)) {
                storage.selectVehicle(progress, v.id);
                applySelectedVehicle();
                audio.playUi();
            } else {
                if(storage.unlockVehicle(progress, v)) {
                    audio.playLevelUp();
                } else {
                    audio.playCrash();
                }
            }
        } else {
            // Check Color Buttons
            for(int i=0; i<8; i++) {
                if(garage.btnColors[i].contains(x,y)) {
                    Vehicle v = allVehicles.get(garageIdx);
                    if (storage.isColorUnlocked(progress, v.id, i)) {
                        v.selectedColorIndex = i;
                        storage.saveVehicleColor(progress, v.id, i);
                        audio.playUi();
                    } else {
                        garage.showingColorConfirm = true;
                        garage.pendingColorIndex = i;
                        audio.playUi();
                    }
                    return;
                }
            }
        }
    }

    private void handleTrackTouchDown(float x, float y) {
        if(trackSelectionUi.btnBack.contains(x,y)) { currentState = GameState.MAIN_MENU; audio.playUi(); }
        else if(trackSelectionUi.btnNext.contains(x,y)) {
            trackIdx = (trackIdx+1)%allTracks.size();
            audio.playUi();
        } else if(trackSelectionUi.btnPrev.contains(x,y)) {
            trackIdx = (trackIdx-1+allTracks.size())%allTracks.size();
            audio.playUi();
        } else if(trackSelectionUi.btnAction.contains(x,y)) {
            Track t = allTracks.get(trackIdx);
            if(storage.isTrackOwned(progress, t.id)) {
                storage.selectTrack(progress, t.id);
                applySelectedVehicle(); // Reloads both car and track
                audio.playUi();
            } else {
                if(storage.unlockTrack(progress, t)) {
                    audio.playLevelUp();
                } else {
                    audio.playCrash();
                }
            }
        }
    }
    
    private void handleSettingsTouchDown(float x, float y) {
        if (settingsUi.showingResetConfirm) {
            if (settingsUi.btnConfirmNo.contains(x, y)) {
                settingsUi.showingResetConfirm = false;
                audio.playUi();
            } else if (settingsUi.btnConfirmYes.contains(x, y)) {
                settingsUi.showingResetConfirm = false;
                progress = new PlayerProgress();
                storage.saveProgress(progress);
                if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                }
                currentState = GameState.MAIN_MENU;
                android.widget.Toast.makeText(context, "Progress Reset Successful!", android.widget.Toast.LENGTH_SHORT).show();
                audio.playUi(); 
            }
            return; // Block other interactions
        }

        if (settingsUi.btnBack.contains(x, y)) { settingsUi.showingResetConfirm = false; currentState = GameState.MAIN_MENU; audio.playUi(); }
        else if (settingsUi.rowRects[0].contains(x, y)) { 
            settingsUi.musicOn = !settingsUi.musicOn; 
            progress.isMuted = !settingsUi.musicOn;
            storage.saveMutePref(progress);
            audio.setMuted(progress.isMuted);
            if (currentState == GameState.RACING && !progress.isMuted) audio.playEngine();
            audio.playUi(); 
        }
        else if (settingsUi.rowRects[1].contains(x, y)) { 
            progress.isSfxMuted = !progress.isSfxMuted; 
            settingsUi.sfxOn = !progress.isSfxMuted;
            audio.setSfxMuted(progress.isSfxMuted);
            audio.playUi(); 
        }
        else if (settingsUi.rowRects[2].contains(x, y)) { 
            settingsUi.tiltOn = !settingsUi.tiltOn; 
            storage.saveTiltPreference(context, settingsUi.tiltOn);
            audio.playUi(); 
        }
        else if (settingsUi.rowRects[3].contains(x, y)) { 
            settingsUi.nightMode = !settingsUi.nightMode;
            progress.isNightMode = settingsUi.nightMode;
            storage.saveNightMode(progress);
            audio.playUi(); 
        }
        else if (settingsUi.langRects[0].contains(x, y)) { settingsUi.languageIdx = 0; audio.playUi(); }
        else if (settingsUi.langRects[1].contains(x, y)) { settingsUi.languageIdx = 1; audio.playUi(); }
        else if (settingsUi.langRects[2].contains(x, y)) { settingsUi.languageIdx = 2; audio.playUi(); }
        else if (settingsUi.sliderRect.contains(x, y) || settingsUi.rowRects[4].contains(x, y)) {
            float pct = (x - settingsUi.sliderRect.left) / settingsUi.sliderRect.width();
            settingsUi.tiltSens = Math.max(1, Math.min(10, (int)(pct * 10) + 1));
        }
        else if (settingsUi.btnReset.contains(x, y)) { 
            settingsUi.showingResetConfirm = true;
            audio.playUi(); 
        }
    }

    private void startGame() {
        exactScore = 0f;
        currentScore = 0;
        runCoins = 0;
        hasDoubledCoins = false;
        currentLevel = 1;
        applySelectedVehicle(); // Restores currentWorld
        aiCars.clear();
        coins.clear();
        fuelPickups.clear();
        playerCar.fuel = 100f;
        playerCar.x = screenW / 2f;
        applySelectedVehicle();
        currentState = GameState.RACING;
        audio.playEngine();
        audio.playLevelUp();
    }
    
    private void startGame(long seed) {
        rand = new java.util.Random(seed);
        startGame();
        if (voiceChatManager != null && multiplayerManager != null && multiplayerManager.currentRoomCode != null) {
            voiceChatManager.joinChannel(multiplayerManager.currentRoomCode);
        }
    }

    public void setTilt(float tiltX) {
        if (settingsUi != null && settingsUi.tiltOn && playerCar != null) {
            playerCar.setTilt(tiltX * (settingsUi.tiltSens / 5f));
        }
    }

    public void onResume() {}
    public void onPause() { 
        if(currentState == GameState.RACING) {
            currentState = GameState.PAUSED; 
            audio.stopEngine();
        }
    }
    public void onDestroy() {}
    public void pauseGame() { onPause(); }

    public boolean handleBackButton() {
        if (currentState == GameState.RACING) {
            currentState = GameState.PAUSED;
            return true;
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.RACING;
            return true;
        } else if (currentState == GameState.GARAGE || currentState == GameState.SETTINGS || currentState == GameState.PROFILE) {
            currentState = GameState.MAIN_MENU;
            return true;
        } else if (currentState == GameState.LOGIN) {
            currentState = GameState.PROFILE;
            return true;
        } else if (currentState == GameState.MULTIPLAYER_LOBBY) {
            currentState = GameState.MAIN_MENU;
            if (multiplayerManager != null) multiplayerManager.leaveRoom();
            if (voiceChatManager != null) voiceChatManager.leaveChannel();
            return true;
        } else if (currentState == GameState.MULTIPLAYER_RACING) {
            currentState = GameState.MAIN_MENU;
            if (multiplayerManager != null) multiplayerManager.leaveRoom();
            if (voiceChatManager != null) voiceChatManager.leaveChannel();
            return true;
        }
        return false;
    }

    public void setCustomAvatar(Uri uri) {
        progress.avatarUri = uri.toString();
        storage.saveProgress(progress);
        loadAvatarBitmap(uri);
    }
    
    private void loadAvatarBitmap(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            if (profileScreen != null) {
                profileScreen.customAvatar = bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void joinMultiplayerRoom(String code) {
        if (multiplayerManager != null) {
            multiplayerManager.joinRoom(code, progress.playerName, new MultiplayerManager.OnRoomJoinCallback() {
                @Override
                public void onSuccess() {
                    multiplayerScreen.isWaitingForPlayer = true;
                    multiplayerScreen.currentRoomCode = code;
                    multiplayerScreen.errorMessage = "";
                    if (voiceChatManager != null) voiceChatManager.joinChannel(code);
                }
                @Override
                public void onError(String error) {
                    multiplayerScreen.errorMessage = error;
                }
            });
        }
    }
    
    public void sendChatMessage(String msg) {
        if (multiplayerManager != null && progress != null) {
            multiplayerManager.sendChatMessage(progress.playerName, msg);
        }
    }
    
    public void onGoogleLoginSuccess(String name, String statusMsg) { progress.playerName = name; progress.isLoggedIn = true; loginStatusMessage = statusMsg; storage.saveProgress(progress); }
    public void onGoogleLoginFailure(String errorMsg) { loginStatusMessage = errorMsg; progress.isLoggedIn = false; storage.saveProgress(progress); }

    public void destroy() {
        if (voiceChatManager != null) {
            voiceChatManager.destroy();
        }
    }
}
