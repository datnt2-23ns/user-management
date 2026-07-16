package org.example.usermanagement.enums;

public enum UserStatus {

    ACTIVE((byte) 0),
    LOCKED((byte) 1),
    DELETED((byte) 2);

    private final byte code;

    UserStatus(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static UserStatus fromCode(Byte code) {
        if (code == null) {
            return null;
        }

        for (UserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Unknown UserStatus code: " + code
        );
    }
}