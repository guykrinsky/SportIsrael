package com.example.ykrin.sportisrael;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * A sports group stored in the "groups" Firestore collection.
 *
 * Membership is modeled as maps (uid -> true) rather than arrays because the
 * bundled Firestore SDK (15.0.0) predates arrayUnion/whereArrayContains;
 * map fields can be queried with whereEqualTo("memberIds.<uid>", true).
 */
public class Group {
    private String name;
    private String nameLower;
    private String description;
    private String sport;
    private boolean isPrivate;
    private String ownerId;
    private String ownerName;
    private Map<String, Boolean> adminIds;
    private Map<String, Boolean> memberIds;
    private Map<String, String> memberNames;
    private int memberCount;
    private GeoPoint location;
    private String skillLevel;
    private long createdAt;

    // Document id, populated after reads; not stored inside the document.
    private String id;

    public Group() {
    }

    public Group(String name, String description, String sport, boolean isPrivate,
                 String ownerId, String ownerName, String skillLevel) {
        this.name = name;
        this.nameLower = name.toLowerCase();
        this.description = description;
        this.sport = sport;
        this.isPrivate = isPrivate;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.skillLevel = skillLevel;
        this.createdAt = System.currentTimeMillis();

        this.adminIds = new HashMap<>();
        this.adminIds.put(ownerId, true);
        this.memberIds = new HashMap<>();
        this.memberIds.put(ownerId, true);
        this.memberNames = new HashMap<>();
        this.memberNames.put(ownerId, ownerName);
        this.memberCount = 1;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public boolean isMember(String uid) {
        return uid != null && memberIds != null && Boolean.TRUE.equals(memberIds.get(uid));
    }

    @Exclude
    public boolean isAdmin(String uid) {
        return uid != null && adminIds != null && Boolean.TRUE.equals(adminIds.get(uid));
    }

    @Exclude
    public boolean isOwner(String uid) {
        return uid != null && uid.equals(ownerId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLower() {
        return nameLower;
    }

    public void setNameLower(String nameLower) {
        this.nameLower = nameLower;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Map<String, Boolean> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(Map<String, Boolean> adminIds) {
        this.adminIds = adminIds;
    }

    public Map<String, Boolean> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(Map<String, Boolean> memberIds) {
        this.memberIds = memberIds;
    }

    public Map<String, String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(Map<String, String> memberNames) {
        this.memberNames = memberNames;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
