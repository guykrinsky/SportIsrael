package com.example.ykrin.sportisrael;

public enum SportType {
    BASKETBALL("basketball", "Basketball", R.color.sport_basketball, R.drawable.ic_sport_basketball),
    SOCCER("soccer", "Soccer", R.color.sport_soccer, R.drawable.ic_sport_soccer),
    TENNIS("tennis", "Tennis", R.color.sport_tennis, R.drawable.ic_sport_tennis),
    VOLLEYBALL("volleyball", "Volleyball", R.color.sport_volleyball, R.drawable.ic_sport_volleyball),
    RUNNING("running", "Running", R.color.sport_running, R.drawable.ic_sport_running),
    CYCLING("cycling", "Cycling", R.color.sport_cycling, R.drawable.ic_sport_cycling),
    OTHER("other", "Other", R.color.sport_other, R.drawable.ic_sport_other);

    private final String value;
    private final String displayName;
    private final int colorRes;
    private final int iconRes;

    SportType(String value, String displayName, int colorRes, int iconRes) {
        this.value = value;
        this.displayName = displayName;
        this.colorRes = colorRes;
        this.iconRes = iconRes;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorRes() {
        return colorRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    public static SportType fromValue(String value) {
        if (value == null) {
            return OTHER;
        }
        for (SportType sport : SportType.values()) {
            if (sport.value.equals(value)) {
                return sport;
            }
        }
        return OTHER;
    }
}
