package com.example.ykrin.sportisrael;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the Groups tab feed: optional section headers, pending invitations
 * (My Groups tab) and group cards.
 */
public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_INVITATION = 1;
    private static final int TYPE_GROUP = 2;

    public interface Listener {
        void onGroupClick(Group group);

        void onGroupAction(Group group);

        void onInvitationAccept(Invitation invitation);

        void onInvitationDecline(Invitation invitation);
    }

    private final List<Object> rows = new ArrayList<>();
    private final Listener listener;
    private final String currentUid;

    public GroupAdapter(Listener listener, String currentUid) {
        this.listener = listener;
        this.currentUid = currentUid;
    }

    public void setRows(List<Invitation> invitations, List<Group> groups,
                        String invitationsHeader, String groupsHeader) {
        rows.clear();
        if (invitations != null && !invitations.isEmpty()) {
            rows.add(invitationsHeader);
            rows.addAll(invitations);
            if (!groups.isEmpty()) {
                rows.add(groupsHeader);
            }
        }
        rows.addAll(groups);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public int getItemViewType(int position) {
        Object row = rows.get(position);
        if (row instanceof String) {
            return TYPE_HEADER;
        }
        if (row instanceof Invitation) {
            return TYPE_INVITATION;
        }
        return TYPE_GROUP;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderHolder(inflater.inflate(R.layout.item_section_header, parent, false));
            case TYPE_INVITATION:
                return new InvitationHolder(inflater.inflate(R.layout.item_invitation, parent, false));
            default:
                return new GroupHolder(inflater.inflate(R.layout.item_group_card, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((String) row);
        } else if (holder instanceof InvitationHolder) {
            ((InvitationHolder) holder).bind((Invitation) row);
        } else {
            ((GroupHolder) holder).bind((Group) row);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View itemView) {
            super(itemView);
        }

        void bind(String title) {
            ((TextView) itemView).setText(title);
        }
    }

    class InvitationHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView meta;
        Button accept;
        Button decline;

        InvitationHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.invitation_group_name);
            meta = itemView.findViewById(R.id.invitation_meta);
            accept = itemView.findViewById(R.id.invitation_accept);
            decline = itemView.findViewById(R.id.invitation_decline);
        }

        void bind(final Invitation invitation) {
            groupName.setText(invitation.getGroupName());
            SportType sport = SportType.fromValue(invitation.getSport());
            meta.setText(sport.getDisplayName() + " · invited by " + invitation.getInvitedByName());
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onInvitationAccept(invitation);
                }
            });
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onInvitationDecline(invitation);
                }
            });
        }
    }

    class GroupHolder extends RecyclerView.ViewHolder {
        FrameLayout cover;
        TextView coverSport;
        ImageView privateBadge;
        TextView name;
        TextView meta;
        Button action;

        GroupHolder(View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.group_cover);
            coverSport = itemView.findViewById(R.id.group_cover_sport);
            privateBadge = itemView.findViewById(R.id.group_private_badge);
            name = itemView.findViewById(R.id.group_name);
            meta = itemView.findViewById(R.id.group_meta);
            action = itemView.findViewById(R.id.group_action_button);
        }

        void bind(final Group group) {
            SportType sport = SportType.fromValue(group.getSport());
            cover.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), sport.getColorRes()));
            coverSport.setText(sport.getDisplayName());
            privateBadge.setVisibility(group.getIsPrivate() ? View.VISIBLE : View.GONE);
            name.setText(group.getName());

            String metaText = itemView.getContext()
                    .getString(R.string.groups_members_count, group.getMemberCount());
            SkillLevel skill = SkillLevel.fromValue(group.getSkillLevel());
            if (skill != SkillLevel.ANY) {
                metaText += " · " + skill.getDisplayName();
            }
            meta.setText(metaText);

            boolean isMember = group.isMember(currentUid);
            if (isMember) {
                action.setText(R.string.groups_joined);
                action.setBackgroundResource(R.drawable.btn_secondary);
                action.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
            } else {
                action.setText(group.getIsPrivate() ? R.string.groups_request_join : R.string.groups_join);
                action.setBackgroundResource(R.drawable.btn_primary);
                action.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            }

            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onGroupAction(group);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onGroupClick(group);
                }
            });
        }
    }
}
