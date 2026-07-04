package com.example.ykrin.sportisrael;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "group_id";
    private static final int MEMBERS_PREVIEW_LIMIT = 5;

    ImageButton back_button;
    TextView header_title;
    ProgressBar progress;
    ScrollView content;
    FrameLayout cover;
    TextView cover_sport;
    ImageView private_badge;
    TextView group_name;
    TextView group_meta;
    Button action_button;
    LinearLayout requests_card;
    LinearLayout requests_container;
    TextView description;
    TextView members_preview;
    Button view_members_button;
    Button invite_button;
    Button delete_button;

    FirebaseFirestore mDB;
    FirebaseUser current_user;
    String group_id;
    Group group;
    boolean has_pending_request = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        mDB = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        group_id = getIntent().getStringExtra(EXTRA_GROUP_ID);

        back_button = findViewById(R.id.detail_back);
        header_title = findViewById(R.id.detail_header_title);
        progress = findViewById(R.id.detail_progress);
        content = findViewById(R.id.detail_content);
        cover = findViewById(R.id.detail_cover);
        cover_sport = findViewById(R.id.detail_cover_sport);
        private_badge = findViewById(R.id.detail_private_badge);
        group_name = findViewById(R.id.detail_name);
        group_meta = findViewById(R.id.detail_meta);
        action_button = findViewById(R.id.detail_action_button);
        requests_card = findViewById(R.id.detail_requests_card);
        requests_container = findViewById(R.id.detail_requests_container);
        description = findViewById(R.id.detail_description);
        members_preview = findViewById(R.id.detail_members_preview);
        view_members_button = findViewById(R.id.detail_view_members);
        invite_button = findViewById(R.id.detail_invite_button);
        delete_button = findViewById(R.id.detail_delete_button);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroup();
    }

    private void loadGroup() {
        if (group_id == null) {
            finish();
            return;
        }
        progress.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);

        mDB.collection(GroupActions.GROUPS).document(group_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progress.setVisibility(View.GONE);
                        if (!task.isSuccessful() || !task.getResult().exists()) {
                            Toast.makeText(GroupDetailActivity.this,
                                    R.string.groups_error_loading, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        group = task.getResult().toObject(Group.class);
                        group.setId(task.getResult().getId());
                        content.setVisibility(View.VISIBLE);
                        bindGroup();
                        loadSecondaryState();
                    }
                });
    }

    private void bindGroup() {
        SportType sport = SportType.fromValue(group.getSport());
        cover.setBackgroundColor(ContextCompat.getColor(this, sport.getColorRes()));
        cover_sport.setText(sport.getDisplayName());
        private_badge.setVisibility(group.getIsPrivate() ? View.VISIBLE : View.GONE);

        header_title.setText(group.getName());
        group_name.setText(group.getName());

        String meta = getString(R.string.groups_members_count, group.getMemberCount());
        SkillLevel skill = SkillLevel.fromValue(group.getSkillLevel());
        if (skill != SkillLevel.ANY) {
            meta += " · " + skill.getDisplayName();
        }
        if (group.getIsPrivate()) {
            meta += " · " + getString(R.string.groups_private_label);
        }
        group_meta.setText(meta);

        description.setText(group.getDescription());

        // Members preview: first few names, comma separated.
        List<String> names = new ArrayList<>();
        Map<String, String> memberNames = group.getMemberNames();
        if (memberNames != null) {
            for (String name : memberNames.values()) {
                names.add(name);
                if (names.size() == MEMBERS_PREVIEW_LIMIT) {
                    break;
                }
            }
        }
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                preview.append(", ");
            }
            preview.append(names.get(i));
        }
        if (group.getMemberCount() > MEMBERS_PREVIEW_LIMIT) {
            preview.append(" +").append(group.getMemberCount() - MEMBERS_PREVIEW_LIMIT);
        }
        members_preview.setText(preview.toString());

        view_members_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this, GroupMembersActivity.class);
                intent.putExtra(EXTRA_GROUP_ID, group_id);
                startActivity(intent);
            }
        });

        String uid = current_user == null ? null : current_user.getUid();
        boolean member = group.isMember(uid);

        invite_button.setVisibility(member ? View.VISIBLE : View.GONE);
        invite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this, InviteMembersActivity.class);
                intent.putExtra(EXTRA_GROUP_ID, group_id);
                startActivity(intent);
            }
        });

        delete_button.setVisibility(group.isOwner(uid) ? View.VISIBLE : View.GONE);
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        updateActionButton();
    }

    private void updateActionButton() {
        String uid = current_user == null ? null : current_user.getUid();

        if (group.isOwner(uid)) {
            action_button.setText(R.string.groups_manage);
            styleActionSecondary();
            action_button.setEnabled(false);
            return;
        }
        if (group.isMember(uid)) {
            action_button.setText(R.string.groups_leave);
            styleActionSecondary();
            action_button.setEnabled(true);
            action_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmLeave();
                }
            });
            return;
        }
        if (group.getIsPrivate()) {
            if (has_pending_request) {
                action_button.setText(R.string.groups_requested);
                styleActionSecondary();
                action_button.setEnabled(false);
            } else {
                action_button.setText(R.string.groups_request_join);
                styleActionPrimary();
                action_button.setEnabled(true);
                action_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendJoinRequest();
                    }
                });
            }
            return;
        }
        action_button.setText(R.string.groups_join);
        styleActionPrimary();
        action_button.setEnabled(true);
        action_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });
    }

    private void styleActionPrimary() {
        action_button.setBackgroundResource(R.drawable.btn_primary);
        action_button.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void styleActionSecondary() {
        action_button.setBackgroundResource(R.drawable.btn_secondary);
        action_button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    /** Loads state that needs extra reads: my pending request + admin request list. */
    private void loadSecondaryState() {
        final String uid = current_user == null ? null : current_user.getUid();
        if (uid == null) {
            return;
        }

        if (group.getIsPrivate() && !group.isMember(uid)) {
            mDB.collection(GroupActions.GROUPS).document(group_id)
                    .collection(GroupActions.JOIN_REQUESTS).document(uid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            has_pending_request = task.isSuccessful()
                                    && task.getResult().exists();
                            updateActionButton();
                        }
                    });
        }

        if (group.isAdmin(uid) || group.isOwner(uid)) {
            mDB.collection(GroupActions.GROUPS).document(group_id)
                    .collection(GroupActions.JOIN_REQUESTS).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                bindRequests(task.getResult());
                            }
                        }
                    });
        }
    }

    private void bindRequests(QuerySnapshot requests) {
        requests_container.removeAllViews();
        if (requests.isEmpty()) {
            requests_card.setVisibility(View.GONE);
            return;
        }
        requests_card.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (QueryDocumentSnapshot doc : requests) {
            final String requesterUid = doc.getId();
            final String requesterName = doc.getString("userName");

            View row = inflater.inflate(R.layout.item_join_request, requests_container, false);
            TextView name = row.findViewById(R.id.request_user_name);
            Button approve = row.findViewById(R.id.request_approve);
            Button decline = row.findViewById(R.id.request_decline);

            name.setText(requesterName);
            approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupActions.approveRequest(group_id, requesterUid, requesterName,
                            new GroupActions.Callback() {
                                @Override
                                public void onResult(boolean success) {
                                    if (!success) {
                                        Toast.makeText(GroupDetailActivity.this,
                                                R.string.groups_join_failed, Toast.LENGTH_SHORT).show();
                                    }
                                    loadGroup();
                                }
                            });
                }
            });
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupActions.deleteRequest(group_id, requesterUid);
                    loadGroup();
                }
            });
            requests_container.addView(row);
        }
    }

    private void joinGroup() {
        GroupActions.join(group_id, current_user.getUid(), current_user.getDisplayName(),
                new GroupActions.Callback() {
                    @Override
                    public void onResult(boolean success) {
                        if (!success) {
                            Toast.makeText(GroupDetailActivity.this,
                                    R.string.groups_join_failed, Toast.LENGTH_SHORT).show();
                        }
                        loadGroup();
                    }
                });
    }

    private void sendJoinRequest() {
        GroupActions.requestJoin(group_id, current_user.getUid(), current_user.getDisplayName(),
                new GroupActions.Callback() {
                    @Override
                    public void onResult(boolean success) {
                        if (success) {
                            has_pending_request = true;
                            Toast.makeText(GroupDetailActivity.this,
                                    R.string.groups_request_sent, Toast.LENGTH_SHORT).show();
                            updateActionButton();
                        } else {
                            Toast.makeText(GroupDetailActivity.this,
                                    R.string.groups_join_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void confirmLeave() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.groups_leave_confirm)
                .setPositiveButton(R.string.groups_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GroupActions.leave(group_id, current_user.getUid(),
                                new GroupActions.Callback() {
                                    @Override
                                    public void onResult(boolean success) {
                                        if (!success) {
                                            Toast.makeText(GroupDetailActivity.this,
                                                    R.string.groups_join_failed,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        loadGroup();
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.groups_delete_confirm)
                .setPositiveButton(R.string.groups_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDB.collection(GroupActions.GROUPS).document(group_id).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
