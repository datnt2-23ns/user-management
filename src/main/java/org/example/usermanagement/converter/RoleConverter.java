package org.example.usermanagement.converter;

import org.example.usermanagement.enums.Role;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter
        implements AttributeConverter<Role, Byte> {

    @Override
    public Byte convertToDatabaseColumn(Role role) {
        return role == null ? null : role.getCode();
    }

    @Override
    public Role convertToEntityAttribute(Byte code) {
        return Role.fromCode(code);
    }
}