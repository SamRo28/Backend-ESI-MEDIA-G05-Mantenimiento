package com.example.usersbe.dto;

import java.time.Instant;

public class CreadorDTO {
    private String id;
    private String alias;
    private String nombre;
    private String email;
    private boolean blocked;
    private boolean deleted;
    private Instant createdAt;
    private String fotoUrl;
    private String bio;

    public String getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getBio() {
        return bio;
    }

    private CreadorDTO(Builder builder) {
        this.id = builder.id;
        this.alias = builder.alias;
        this.nombre = builder.nombre;
        this.email = builder.email;
        this.blocked = builder.blocked;
        this.deleted = builder.deleted;
        this.createdAt = builder.createdAt;
        this.fotoUrl = builder.fotoUrl;
        this.bio = builder.bio;
    }

    public static class Builder {
        private String id;
        private String alias;
        private String nombre;
        private String email;
        private boolean blocked;
        private boolean deleted;
        private Instant createdAt;
        private String fotoUrl;
        private String bio;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder blocked(boolean blocked) {
            this.blocked = blocked;
            return this;
        }

        public Builder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder fotoUrl(String fotoUrl) {
            this.fotoUrl = fotoUrl;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public CreadorDTO build() {
            return new CreadorDTO(this);
        }
    }
}
