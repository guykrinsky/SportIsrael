package com.example.ykrin.sportisrael;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupsActivity extends AppCompatActivity implements GroupAdapter.Listener {

    private static final int TAB_MY_GROUPS = 0;
    private static final int TAB_DISCOVER = 1;

    BottomNavigationView menu_bar_view;
    TabLayout tabs;
    EditText search_field;
    LinearLayout chips_container;
    RecyclerView groups_list;
    LinearLayout empty_state;
    TextView empty_text;
    Button empty_action;
    ProgressBar progress;
    FloatingActionButton create_fab;

    GroupAdapter adapter;
    FirebaseFirestore mDB;
    FirebaseUser current_user;

    // Cached fetch results; chips and search re-filter these client-side.
    private final List<Group> fetched_groups = new ArrayList<>();
    private final List<Invitation> fetched_invitations = new ArrayList<>();
    private SportType selected_sport = null; // null == all sports
    private boolean load_failed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        mDB = FirebaseFirestore.getInstance();
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        UserDirectory.ensureUserDocument(current_user);

        menu_bar_view = (BottomNavigationView) findViewById(R.id.navigation_bar);
        NavigationBar.attach(this, menu_bar_view);

        tabs = findViewById(R.id.groups_tabs);
        search_field = findViewById(R.id.groups_search);
        chips_container = findViewById(R.id.sport_chips_container);
        groups_list = findViewById(R.id.groups_list);
        empty_state = findViewById(R.id.groups_empty_state);
        empty_text = findViewById(R.id.groups_empty_text);
        empty_action = findViewById(R.id.groups_empty_action);
        progress = findViewById(R.id.groups_progress);
        create_fab = findViewById(R.id.groups_fab);

        adapter = new GroupAdapter(this, current_user == null ? null : current_user.getUid());
        groups_list.setLayoutManager(new LinearLayoutManager(this));
        groups_list.setAdapter(adapter);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
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
                render();
            }
        });

        buildSportChips();

        create_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupsActivity.this, CreateGroupActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void buildSportChips() {
        chips_container.removeAllViews();
        addChip(null);
        for (SportType sport : SportType.values()) {
            addChip(sport);
        }
        updateChipSelection();
    }

    private void addChip(final SportType sport) {
        TextView chip = new TextView(this);
        chip.setText(sport == null ? "All" : sport.getDisplayName());
        chip.setTextSize(14);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_color));
        chip.setBackgroundResource(R.drawable.chip_background);
        int padH = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        int padV = getResources().getDimensionPixelSize(R.dimen.spacing_s);
        chip.setPadding(padH, padV, padH, padV);
        chip.setTag(sport);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.spacing_s);
        chip.setLayoutParams(params);

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_sport = sport;
                updateChipSelection();
                render();
            }
        });
        chips_container.addView(chip);
    }

    private void updateChipSelection() {
        for (int i = 0; i < chips_container.getChildCount(); i++) {
            View chip = chips_container.getChildAt(i);
            chip.setSelected(chip.getTag() == selected_sport);
        }
    }

    private void loadData() {
        load_failed = false;
        progress.setVisibility(View.VISIBLE);
        empty_state.setVisibility(View.GONE);

        if (tabs.getSelectedTabPosition() == TAB_MY_GROUPS) {
            if (current_user == null) {
                fetched_groups.clear();
                fetched_invitations.clear();
                progress.setVisibility(View.GONE);
                render();
                return;
            }
            mDB.collection(GroupActions.GROUPS)
                    .whereEqualTo(FieldPath.of("memberIds", current_user.getUid()), true)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            onGroupsFetched(task);
                        }
                    });
            mDB.collection(UserDirectory.COLLECTION)
                    .document(current_user.getUid())
                    .collection(GroupActions.INVITES)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            fetched_invitations.clear();
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    fetched_invitations.add(doc.toObject(Invitation.class));
                                }
                            }
                            render();
                        }
                    });
        } else {
            fetched_invitations.clear();
            mDB.collection(GroupActions.GROUPS)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            onGroupsFetched(task);
                        }
                    });
        }
    }

    private void onGroupsFetched(Task<QuerySnapshot> task) {
        progress.setVisibility(View.GONE);
        fetched_groups.clear();
        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot doc : task.getResult()) {
                Group group = doc.toObject(Group.class);
                group.setId(doc.getId());
                fetched_groups.add(group);
            }
        } else {
            load_failed = true;
        }
        render();
    }

    private void render() {
        boolean discover = tabs.getSelectedTabPosition() == TAB_DISCOVER;
        String query = search_field.getText().toString().trim().toLowerCase();

        List<Group> visible = new ArrayList<>();
        for (Group group : fetched_groups) {
            if (selected_sport != null
                    && SportType.fromValue(group.getSport()) != selected_sport) {
                continue;
            }
            if (!query.isEmpty() && (group.getNameLower() == null
                    || !group.getNameLower().contains(query))) {
                continue;
            }
            visible.add(group);
        }

        if (discover) {
            Collections.sort(visible, new Comparator<Group>() {
                @Override
                public int compare(Group a, Group b) {
                    return b.getMemberCount() - a.getMemberCount();
                }
            });
        } else {
            Collections.sort(visible, new Comparator<Group>() {
                @Override
                public int compare(Group a, Group b) {
                    String an = a.getName() == null ? "" : a.getName();
                    String bn = b.getName() == null ? "" : b.getName();
                    return an.compareToIgnoreCase(bn);
                }
            });
        }

        List<Invitation> invitations = discover ? new ArrayList<Invitation>() : fetched_invitations;
        adapter.setRows(invitations, visible,
                getString(R.string.groups_invitations), getString(R.string.groups_tab_my));

        if (adapter.isEmpty()) {
            empty_state.setVisibility(View.VISIBLE);
            if (load_failed) {
                empty_text.setText(R.string.groups_error_loading);
                empty_action.setText(R.string.groups_retry);
                empty_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadData();
                    }
                });
            } else if (discover) {
                empty_text.setText(R.string.groups_empty_discover);
                empty_action.setText(R.string.groups_create_title);
                empty_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(GroupsActivity.this, CreateGroupActivity.class));
                    }
                });
            } else {
                empty_text.setText(R.string.groups_empty_mine);
                empty_action.setText(R.string.groups_tab_discover);
                empty_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TabLayout.Tab discoverTab = tabs.getTabAt(TAB_DISCOVER);
                        if (discoverTab != null) {
                            discoverTab.select();
                        }
                    }
                });
            }
        } else {
            empty_state.setVisibility(View.GONE);
        }
    }

    private void openDetail(Group group) {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, group.getId());
        startActivity(intent);
    }

    @Override
    public void onGroupClick(Group group) {
        openDetail(group);
    }

    @Override
    public void onGroupAction(Group group) {
        if (current_user == null) {
            Toast.makeText(this, R.string.user_null, Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = current_user.getUid();
        if (group.isMember(uid)) {
            openDetail(group);
            return;
        }
        if (group.getIsPrivate()) {
            // The detail screen owns the request flow and its pending state.
            openDetail(group);
            return;
        }
        GroupActions.join(group.getId(), uid, current_user.getDisplayName(),
                new GroupActions.Callback() {
                    @Override
                    public void onResult(boolean success) {
                        if (!success) {
                            Toast.makeText(GroupsActivity.this,
                                    R.string.groups_join_failed, Toast.LENGTH_SHORT).show();
                        }
                        loadData();
                    }
                });
    }

    @Override
    public void onInvitationAccept(final Invitation invitation) {
        if (current_user == null) {
            return;
        }
        final String uid = current_user.getUid();
        GroupActions.join(invitation.getGroupId(), uid, current_user.getDisplayName(),
                new GroupActions.Callback() {
                    @Override
                    public void onResult(boolean success) {
                        if (success) {
                            GroupActions.deleteInvite(uid, invitation.getGroupId());
                        } else {
                            Toast.makeText(GroupsActivity.this,
                                    R.string.groups_join_failed, Toast.LENGTH_SHORT).show();
                        }
                        loadData();
                    }
                });
    }

    @Override
    public void onInvitationDecline(Invitation invitation) {
        if (current_user == null) {
            return;
        }
        GroupActions.deleteInvite(current_user.getUid(), invitation.getGroupId());
        loadData();
    }
}
