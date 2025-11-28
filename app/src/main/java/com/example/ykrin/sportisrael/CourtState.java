package com.example.ykrin.sportisrael;

public enum CourtState {
    EMPTY("empty"),
    FULL("full"),
    SEARCHING("searching"),
    UNKNOWN("unknown");

    private final String value;

    CourtState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CourtState fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (CourtState state : CourtState.values()) {
            if (state.value.equals(value)) {
                return state;
            }
        }
        return UNKNOWN;
    }
}

