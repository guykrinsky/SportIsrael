package com.example.ykrin.sportisrael;

/**
 * An invitation to join a group, stored at users/{uid}/invites/{groupId}
 * so a user's pending invites are a single subcollection read.
 */
public class Invitation {
    private String groupId;
    private String groupName;
    private String sport;
    private String invitedByName;
    private long invitedAt;

    public Invitation() {
    }

    public Invitation(String groupId, String groupName, String sport, String invitedByName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.sport = sport;
        this.invitedByName = invitedByName;
        this.invitedAt = System.currentTimeMillis();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getInvitedByName() {
        return invitedByName;
    }

    public void setInvitedByName(String invitedByName) {
        this.invitedByName = invitedByName;
    }

    public long getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(long invitedAt) {
        this.invitedAt = invitedAt;
    }
}
