package com.example.ykrin.sportisrael;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InviteMembersActivity extends AppCompatActivity {

    private static final int SEARCH_LIMIT = 20;

    ImageButton back_button;
    EditText search_field;
    ProgressBar progress;
    TextView empty_text;
    RecyclerView results_list;

    FirebaseFirestore mDB;
    FirebaseUser current_user;
    String group_id;
    Group group;
    UserAdapter adapter;
    final Set<String> invited_uids = new HashSet<>();

    static class UserRow {
        String uid;
        String name;
        String email;

        UserRow(String uid, String name, String email) {
            this.uid = uid;
            this.name = name;
            this.email = email;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_members);

        mDB = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        group_id = getIntent().getStringExtra(GroupDetailActivity.EXTRA_GROUP_ID);

        back_button = findViewById(R.id.invite_back);
        search_field = findViewById(R.id.invite_search);
        progress = findViewById(R.id.invite_progress);
        empty_text = findViewById(R.id.invite_empty_text);
        results_list = findViewById(R.id.invite_results);

        adapter = new UserAdapter();
        results_list.setLayoutManager(new LinearLayoutManager(this));
        results_list.setAdapter(adapter);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search_field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString().trim().toLowerCase());
            }
        });

        loadGroup();
    }

    private void loadGroup() {
        if (group_id == null) {
            finish();
            return;
        }
        mDB.collection(GroupActions.GROUPS).document(group_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful() || !task.getResult().exists()) {
                            Toast.makeText(InviteMembersActivity.this,
                                    R.string.groups_error_loading, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        group = task.getResult().toObject(Group.class);
                        group.setId(task.getResult().getId());
                    }
                });
    }

    private void search(String query) {
        if (group == null || query.isEmpty()) {
            adapter.setUsers(new ArrayList<UserRow>());
            empty_text.setVisibility(View.GONE);
            return;
        }
        progress.setVisibility(View.VISIBLE);
        empty_text.setVisibility(View.GONE);

        mDB.collection(UserDirectory.COLLECTION)
                .orderBy("nameLower")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(SEARCH_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progress.setVisibility(View.GONE);
                        List<UserRow> rows = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                // Existing members can't be invited again.
                                if (group.isMember(doc.getId())) {
                                    continue;
                                }
                                rows.add(new UserRow(doc.getId(),
                                        doc.getString("name"), doc.getString("email")));
                            }
                        }
                        adapter.setUsers(rows);
                        empty_text.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void invite(final UserRow row, final Button button) {
        String inviterName = current_user == null ? null : current_user.getDisplayName();
        Invitation invitation = new Invitation(
                group.getId(), group.getName(), group.getSport(), inviterName);
        GroupActions.sendInvite(row.uid, invitation, new GroupActions.Callback() {
            @Override
            public void onResult(boolean success) {
                if (success) {
                    invited_uids.add(row.uid);
                    Toast.makeText(InviteMembersActivity.this,
                            R.string.groups_invite_sent, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InviteMembersActivity.this,
                            R.string.groups_join_failed, Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

        private final List<UserRow> users = new ArrayList<>();

        void setUsers(List<UserRow> rows) {
            users.clear();
            users.addAll(rows);
            notifyDataSetChanged();
        }

        @Override
        public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UserHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invite_user, parent, false));
        }

        @Override
        public void onBindViewHolder(UserHolder holder, int position) {
            holder.bind(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView email;
            Button invite_button;

            UserHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.invite_user_name);
                email = itemView.findViewById(R.id.invite_user_email);
                invite_button = itemView.findViewById(R.id.invite_user_button);
            }

            void bind(final UserRow row) {
                name.setText(row.name);
                email.setText(row.email);

                boolean already_invited = invited_uids.contains(row.uid);
                invite_button.setEnabled(!already_invited);
                if (already_invited) {
                    invite_button.setText(R.string.groups_invited);
                    invite_button.setBackgroundResource(R.drawable.btn_secondary);
                    invite_button.setTextColor(ContextCompat.getColor(
                            InviteMembersActivity.this, R.color.colorPrimary));
                } else {
                    invite_button.setText(R.string.groups_invite);
                    invite_button.setBackgroundResource(R.drawable.btn_primary);
                    invite_button.setTextColor(ContextCompat.getColor(
                            InviteMembersActivity.this, R.color.white));
                }
                invite_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        invite(row, invite_button);
                    }
                });
            }
        }
    }
}
