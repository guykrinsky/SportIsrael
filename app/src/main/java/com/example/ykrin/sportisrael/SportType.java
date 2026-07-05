package com.example.ykrin.sportisrael;

public enum SportType {
    BASKETBALL("basketball", "Basketball", R.color.sport_basketball),
    SOCCER("soccer", "Soccer", R.color.sport_soccer),
    TENNIS("tennis", "Tennis", R.color.sport_tennis),
    VOLLEYBALL("volleyball", "Volleyball", R.color.sport_volleyball),
    RUNNING("running", "Running", R.color.sport_running),
    CYCLING("cycling", "Cycling", R.color.sport_cycling),
    OTHER("other", "Other", R.color.sport_other);

    private final String value;
    private final String displayName;
    private final int colorRes;

    SportType(String value, String displayName, int colorRes) {
        this.value = value;
        this.displayName = displayName;
        this.colorRes = colorRes;
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
