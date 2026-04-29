package com.nullvoid;

public class MilestoneManager {

    private static final int   MILESTONE_INTERVAL = 100;
    private static final int   TIERS_PER_ZONE     = 5;
    private static final float BANNER_DURATION     = 2.2f;

    private static final float SPEED_BUMP_PER_TIER    = 15f;
    private static final float ALIEN_REDUCE_PER_TIER  = 0.18f;
    private static final float ROCK_REDUCE_PER_TIER   = 0.12f;
    private static final float CEIL_REDUCE_PER_TIER   = 0.25f;
    private static final float SPAWN_SEP_REDUCE_TIER  = 0.08f;

    private static final float MIN_SPAWN_SEP  = 0.6f;
    private static final float MIN_ALIEN_INT  = 1.2f;
    private static final float MIN_ROCK_INT   = 1.0f;
    private static final float MIN_CEIL_INT   = 2.5f;

    private static final float BASE_ALIEN_INT = 5.0f;
    private static final float BASE_ROCK_INT  = 3.5f;
    private static final float BASE_CEIL_INT  = 8.0f;
    private static final float BASE_SPAWN_SEP = 1.8f;

    private int   lastMilestoneTier = 0;
    private int   currentTier       = 0;

    private boolean bannerActive  = false;
    private float   bannerTimer   = 0f;
    private String  bannerText    = "";
    private boolean justTriggered = false;

    public void reset() {
        lastMilestoneTier = 0;
        currentTier       = 0;
        bannerActive      = false;
        bannerTimer       = 0f;
        bannerText        = "";
        justTriggered     = false;
    }

    public void update(float delta, int distanceMetres) {
        justTriggered = false;

        int tier = distanceMetres / MILESTONE_INTERVAL;
        if (tier > lastMilestoneTier) {
            lastMilestoneTier = tier;
            currentTier       = tier;
            justTriggered     = true;
            bannerActive      = true;
            bannerTimer       = BANNER_DURATION;
            // BUG 6 FIX: single space between the distance and REACHED
            int milestone = distanceMetres - (distanceMetres % MILESTONE_INTERVAL);
            bannerText = milestone + "m REACHED!";
        }

        if (bannerActive) {
            bannerTimer -= delta;
            if (bannerTimer <= 0f) bannerActive = false;
        }
    }

    public float getAlienInterval() {
        return Math.max(MIN_ALIEN_INT, BASE_ALIEN_INT - currentTier * ALIEN_REDUCE_PER_TIER);
    }

    public float getRockInterval() {
        return Math.max(MIN_ROCK_INT, BASE_ROCK_INT - currentTier * ROCK_REDUCE_PER_TIER);
    }

    public float getCeilInterval() {
        return Math.max(MIN_CEIL_INT, BASE_CEIL_INT - currentTier * CEIL_REDUCE_PER_TIER);
    }

    public float getSpawnSeparation() {
        return Math.max(MIN_SPAWN_SEP, BASE_SPAWN_SEP - currentTier * SPAWN_SEP_REDUCE_TIER);
    }

    public float getSpeedBonus() {
        return currentTier * SPEED_BUMP_PER_TIER;
    }

    public int getZone() { return currentTier / TIERS_PER_ZONE; }
    public int getTier() { return currentTier; }

    public boolean isBannerActive()  { return bannerActive;  }
    public float   getBannerTimer()  { return bannerTimer;   }
    public float   getBannerMax()    { return BANNER_DURATION; }
    public String  getBannerText()   { return bannerText;    }
    public boolean justHitMilestone(){ return justTriggered; }
}