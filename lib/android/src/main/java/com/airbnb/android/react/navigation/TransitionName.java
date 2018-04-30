package com.airbnb.android.react.navigation;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Helper to create higher fidelity transition names. <p> This can be used by shared element
 * transitions to understand what a particular element actually corresponds to. For example, a
 * listing card may use listing|1337|photo|0 which would have the exact same transition name in a
 * listing marquee and image viewer. However, if the user scrolls to photo 5 within the Listing
 * Marquee and then goes back to a page with the listing card, it could be understood that
 * listing|1337|photo|5 can be transitioned to listing|1337|photo|0 with a crossfade.
 */
public class TransitionName {

    private static final char DELIMETER = '|';

    private final String type;

    private final long id;

    private final String subtype;

    private final long subId;

    private TransitionName(String type, long id, String subtype, long subId) {
        this.type = type;
        this.id = id;
        this.subtype = subtype;
        this.subId = subId;
    }

    public static TransitionName create(@NonNull String type) {
        return create(type, 0);
    }

    public static TransitionName create(@NonNull String type, long id) {
        return create(type, id, "");
    }

    public static TransitionName create(@NonNull String type, long id, @NonNull String subtype) {
        return create(type, id, subtype, 0);
    }

    /**
     * Use empty string instead of null of subtype is not applicabale.
     */
    public static TransitionName create(
            @NonNull String type, long id, @NonNull String subtype, long subId) {
        return new TransitionName(type, id, subtype, subId);
    }

    public static String toString(@NonNull String type) {
        return toString(type, 0);
    }

    public static String toString(@NonNull String type, long id) {
        return toString(type, id, "");
    }


    public static String toString(@NonNull String type, long id, @NonNull String subtype) {
        return toString(type, id, subtype, 0);
    }

    /**
     * Use empty string instead of null of subtype is not applicabale.
     */
    public static String toString(
            @NonNull String type, long id, @NonNull String subtype, long subId) {
        if (type.indexOf(DELIMETER) != -1) {
            throw new IllegalArgumentException("Invalid type " + type + ". Delimeter is " + DELIMETER);
        } else if (subtype.indexOf(DELIMETER) != -1) {
            throw new IllegalArgumentException(
                    "Invalid subtype " + subtype + ". Delimeter is " + DELIMETER);
        }

        return type + DELIMETER + id + DELIMETER + subtype + DELIMETER + subId;
    }

    public static TransitionName parse(String transitionNameString) {
        if (TextUtils.isEmpty(transitionNameString)) {
            return new TransitionName("", 0, "", 0);
        }

        String[] parsed = transitionNameString.split("[" + DELIMETER + "]");
        switch (parsed.length) {
            case 1:
                return new TransitionName(parsed[0], 0, "", 0);
            case 2:
                return new TransitionName(parsed[0], Long.parseLong(parsed[1]), "", 0);
            case 3:
                return new TransitionName(parsed[0], Long.parseLong(parsed[1]), parsed[2], 0);
            case 4:
                return new TransitionName(parsed[0], Long.parseLong(parsed[1]), parsed[2], Long
                        .parseLong(parsed[3]));
            default:
                throw new IllegalArgumentException(
                        "Invalid transition name " + transitionNameString + ". Split into " + parsed.length +
                                ". Should be less than 4.");
        }
    }

    /**
     * Returns whether at least the type, id, and subtype match. This could be useful when there are 2
     * elements that represent the same thing (like a listing photo) but they are just different
     * indices. With this knowledge, we can safely maintain the shared element mapping and crossfade
     * them instead.
     */
    public boolean partialEquals(TransitionName other) {
        return type.equals(other.type) && id == other.id && subtype.equals(other.subtype);
    }

    public long subId() {
        return subId;
    }

    public long id() {
        return id;
    }

    public String type() {
        return type;
    }
}
