package com.example.ykrin.sportisrael;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GroupMembersActivity extends AppCompatActivity {

    ImageButton back_button;
    TextView header_title;
    ProgressBar progress;
    RecyclerView members_list;

    FirebaseFirestore mDB;
    FirebaseUser current_user;
    String group_id;
    Group group;
    MemberAdapter adapter;

    static class MemberRow {
        String uid;
        String name;
        String role;

        MemberRow(String uid, String name, String role) {
            this.uid = uid;
            this.name = name;
            this.role = role;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        mDB = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        group_id = getIntent().getStringExtra(GroupDetailActivity.EXTRA_GROUP_ID);

        back_button = findViewById(R.id.members_back);
        header_title = findViewById(R.id.members_header_title);
        progress = findViewById(R.id.members_progress);
        members_list = findViewById(R.id.members_list);

        adapter = new MemberAdapter();
        members_list.setLayoutManager(new LinearLayoutManager(this));
        members_list.setAdapter(adapter);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadMembers();
    }

    private void loadMembers() {
        if (group_id == null) {
            finish();
            return;
        }
        progress.setVisibility(View.VISIBLE);
        members_list.setVisibility(View.GONE);

        mDB.collection(GroupActions.GROUPS).document(group_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progress.setVisibility(View.GONE);
                        if (!task.isSuccessful() || !task.getResult().exists()) {
                            Toast.makeText(GroupMembersActivity.this,
                                    R.string.groups_error_loading, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        group = task.getResult().toObject(Group.class);
                        group.setId(task.getResult().getId());
                        members_list.setVisibility(View.VISIBLE);
                        header_title.setText(getString(R.string.groups_members)
                                + " · " + group.getMemberCount());
                        adapter.setMembers(buildRows());
                    }
                });
    }

    private List<MemberRow> buildRows() {
        List<MemberRow> rows = new ArrayList<>();
        Map<String, String> names = group.getMemberNames();
        if (names == null) {
            return rows;
        }
        for (Map.Entry<String, String> entry : names.entrySet()) {
            String uid = entry.getKey();
            String role;
            if (group.isOwner(uid)) {
                role = getString(R.string.groups_role_owner);
            } else if (group.isAdmin(uid)) {
                role = getString(R.string.groups_role_admin);
            } else {
                role = getString(R.string.groups_role_member);
            }
            rows.add(new MemberRow(uid, entry.getValue(), role));
        }
        // Owner first, then admins, then members, alphabetical within role.
        Collections.sort(rows, new Comparator<MemberRow>() {
            @Override
            public int compare(MemberRow a, MemberRow b) {
                int rankA = rank(a.uid);
                int rankB = rank(b.uid);
                if (rankA != rankB) {
                    return rankA - rankB;
                }
                String an = a.name == null ? "" : a.name;
                String bn = b.name == null ? "" : b.name;
                return an.compareToIgnoreCase(bn);
            }

            private int rank(String uid) {
                if (group.isOwner(uid)) {
                    return 0;
                }
                if (group.isAdmin(uid)) {
                    return 1;
                }
                return 2;
            }
        });
        return rows;
    }

    private void makeAdmin(final MemberRow row) {
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot =
                        transaction.get(mDB.collection(GroupActions.GROUPS).document(group_id));
                Group fresh = snapshot.toObject(Group.class);
                if (fresh == null || !fresh.isMember(row.uid)) {
                    return null;
                }
                fresh.getAdminIds().put(row.uid, true);
                transaction.set(mDB.collection(GroupActions.GROUPS).document(group_id), fresh);
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadMembers();
            }
        });
    }

    private void confirmRemove(final MemberRow row) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.groups_remove_member) + " " + row.name + "?")
                .setPositiveButton(R.string.groups_remove_member,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GroupActions.leave(group_id, row.uid, new GroupActions.Callback() {
                                    @Override
                                    public void onResult(boolean success) {
                                        loadMembers();
                                    }
                                });
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberHolder> {

        private final List<MemberRow> members = new ArrayList<>();

        void setMembers(List<MemberRow> rows) {
            members.clear();
            members.addAll(rows);
            notifyDataSetChanged();
        }

        @Override
        public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MemberHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member, parent, false));
        }

        @Override
        public void onBindViewHolder(MemberHolder holder, int position) {
            holder.bind(members.get(position));
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class MemberHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView role;
            Button make_admin;
            Button remove;

            MemberHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.member_name);
                role = itemView.findViewById(R.id.member_role);
                make_admin = itemView.findViewById(R.id.member_make_admin);
                remove = itemView.findViewById(R.id.member_remove);
            }

            void bind(final MemberRow row) {
                name.setText(row.name);
                role.setText(row.role);

                String uid = current_user == null ? null : current_user.getUid();
                boolean owner_viewing = group.isOwner(uid);
                boolean row_is_self = row.uid.equals(uid);
                boolean row_is_owner = group.isOwner(row.uid);
                boolean row_is_admin = group.isAdmin(row.uid);

                boolean can_promote = owner_viewing && !row_is_owner && !row_is_admin;
                boolean can_remove = owner_viewing && !row_is_owner && !row_is_self;

                make_admin.setVisibility(can_promote ? View.VISIBLE : View.GONE);
                remove.setVisibility(can_remove ? View.VISIBLE : View.GONE);

                make_admin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeAdmin(row);
                    }
                });
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmRemove(row);
                    }
                });
            }
        }
    }
}
