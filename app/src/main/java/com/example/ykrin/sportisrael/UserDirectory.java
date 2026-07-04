package com.example.ykrin.sportisrael;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains the "users" collection so features like group invites can look
 * people up by name. Documents are keyed by auth uid.
 */
public class UserDirectory {

    public static final String COLLECTION = "users";

    /**
     * Writes (or refreshes) the caller's directory entry. Merge semantics make
     * this safe to call on every app entry, which also backfills accounts
     * created before the directory existed.
     */
    public static void ensureUserDocument(FirebaseUser user) {
        if (user == null || user.getDisplayName() == null) {
            return;
        }
        writeUserDocument(user.getUid(), user.getDisplayName(), user.getEmail());
    }

    /**
     * Explicit variant for registration, where the display name was just
     * submitted and may not be reflected on the FirebaseUser object yet.
     */
    public static void writeUserDocument(String uid, String name, String email) {
        if (uid == null || name == null) {
            return;
        }
        Map<String, Object> entry = new HashMap<>();
        entry.put("name", name);
        entry.put("nameLower", name.toLowerCase());
        entry.put("email", email);

        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(uid)
                .set(entry, SetOptions.merge());
    }
}
