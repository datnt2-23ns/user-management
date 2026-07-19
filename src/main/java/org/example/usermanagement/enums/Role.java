package org.example.usermanagement.enums;

public enum Role {

    USER((byte) 0),
    ADMIN((byte) 1);

    private final byte code;

    Role(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static Role fromCode(Byte code) {
        if (code == null) {
            return null;
        }

        for (Role role : values()) {
            if (role.code == code) {
                return role;
            }
        }

        throw new IllegalArgumentException(
                "Unknown Role code: " + code);
    }
}