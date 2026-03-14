package com.bank.domain.src;

import java.util.Objects;

public record AuditMetadata(int id, String createdAt, String updatedAt) {
    public AuditMetadata(int id, String createdAt, String updatedAt) {
        this.id = id;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditMetadata(int id1, String at, String updated))) return false;
        return id == id1
                && Objects.equals(createdAt, at)
                && Objects.equals(updatedAt, updated);
    }

    @Override
    public String toString() {
        return "AuditMetadata{id=" + id
                + ", createdAt='" + createdAt + '\''
                + ", updatedAt='" + updatedAt + "'}";
    }
}