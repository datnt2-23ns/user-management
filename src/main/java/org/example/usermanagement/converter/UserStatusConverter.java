package org.example.usermanagement.converter;

import org.example.usermanagement.enums.UserStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserStatusConverter
        implements AttributeConverter<UserStatus, Byte> {

    @Override
    public Byte convertToDatabaseColumn(UserStatus status) {
        return status == null ? null : status.getCode();
    }

    @Override
    public UserStatus convertToEntityAttribute(Byte code) {
        return UserStatus.fromCode(code);
    }
}