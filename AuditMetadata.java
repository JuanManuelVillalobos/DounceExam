package com.bank.domain;

import java.util.Objects;

public final class AuditMetadata {
    private final int id;
    private final String createdAt;
    private final String updatedAt;

    public AuditMetadata(int id, String createdAt, String updatedAt) {
        this.id        = id;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public int    getId()        { return id; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditMetadata)) return false;
        AuditMetadata other = (AuditMetadata) o;
        return id == other.id
            && Objects.equals(createdAt, other.createdAt)
            && Objects.equals(updatedAt, other.updatedAt);
    }

    @Override public int hashCode() { return Objects.hash(id, createdAt, updatedAt); }

    @Override
    public String toString() {
        return "AuditMetadata{id=" + id
            + ", createdAt='" + createdAt + '\''
            + ", updatedAt='" + updatedAt + "'}";
    }
}