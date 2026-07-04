package com.example.ykrin.sportisrael;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * Membership operations on the "groups" collection.
 *
 * All mutations run in transactions that rewrite the membership maps and the
 * denormalized memberCount together, because the bundled Firestore SDK (15)
 * has neither FieldValue.increment nor array operations.
 */
public class GroupActions {

    public static final String GROUPS = "groups";
    public static final String JOIN_REQUESTS = "joinRequests";
    public static final String INVITES = "invites";

    public interface Callback {
        void onResult(boolean success);
    }

    public static void join(final String groupId, final String uid, final String userName,
                            final Callback callback) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference ref = db.collection(GROUPS).document(groupId);
        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(ref);
                Group group = snapshot.toObject(Group.class);
                if (group == null || group.isMember(uid)) {
                    return null;
                }
                group.getMemberIds().put(uid, true);
                group.getMemberNames().put(uid, userName);
                group.setMemberCount(group.getMemberIds().size());
                transaction.set(ref, group);
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onResult(task.isSuccessful());
            }
        });
    }

    public static void leave(final String groupId, final String uid, final Callback callback) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference ref = db.collection(GROUPS).document(groupId);
        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(ref);
                Group group = snapshot.toObject(Group.class);
                if (group == null || !group.isMember(uid) || group.isOwner(uid)) {
                    // Owners must transfer or delete instead of leaving.
                    return null;
                }
                group.getMemberIds().remove(uid);
                group.getMemberNames().remove(uid);
                if (group.getAdminIds() != null) {
                    group.getAdminIds().remove(uid);
                }
                group.setMemberCount(group.getMemberIds().size());
                transaction.set(ref, group);
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onResult(task.isSuccessful());
            }
        });
    }

    public static void requestJoin(String groupId, String uid, String userName,
                                   final Callback callback) {
        Map<String, Object> request = new HashMap<>();
        request.put("userName", userName);
        request.put("requestedAt", System.currentTimeMillis());
        FirebaseFirestore.getInstance()
                .collection(GROUPS).document(groupId)
                .collection(JOIN_REQUESTS).document(uid)
                .set(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        callback.onResult(task.isSuccessful());
                    }
                });
    }

    /** Approve = join + delete the request document. */
    public static void approveRequest(final String groupId, final String requesterUid,
                                      final String requesterName, final Callback callback) {
        join(groupId, requesterUid, requesterName, new Callback() {
            @Override
            public void onResult(boolean success) {
                if (success) {
                    deleteRequest(groupId, requesterUid);
                }
                callback.onResult(success);
            }
        });
    }

    public static void deleteRequest(String groupId, String requesterUid) {
        FirebaseFirestore.getInstance()
                .collection(GROUPS).document(groupId)
                .collection(JOIN_REQUESTS).document(requesterUid)
                .delete();
    }

    public static void sendInvite(String invitedUid, Invitation invitation,
                                  final Callback callback) {
        FirebaseFirestore.getInstance()
                .collection(UserDirectory.COLLECTION).document(invitedUid)
                .collection(INVITES).document(invitation.getGroupId())
                .set(invitation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        callback.onResult(task.isSuccessful());
                    }
                });
    }

    public static void deleteInvite(String uid, String groupId) {
        FirebaseFirestore.getInstance()
                .collection(UserDirectory.COLLECTION).document(uid)
                .collection(INVITES).document(groupId)
                .delete();
    }
}
