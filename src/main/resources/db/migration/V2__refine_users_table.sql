ALTER TABLE users
    ADD COLUMN first_name VARCHAR(100) NULL
        AFTER id,
    ADD COLUMN last_name VARCHAR(150) NULL
        AFTER first_name,
    ADD COLUMN date_of_birth DATE NULL
        AFTER password,
    ADD COLUMN gender VARCHAR(20) NULL
        AFTER date_of_birth,
    ADD COLUMN address VARCHAR(255) NULL
        AFTER gender,
    ADD COLUMN role_code TINYINT NULL
        AFTER avatar_url,
    ADD COLUMN status_code TINYINT NULL
        AFTER role_code,
    ADD COLUMN updated_by BIGINT NULL
        AFTER status_code,
    ADD COLUMN locked_by BIGINT NULL
        AFTER updated_by,
    ADD COLUMN locked_at DATETIME(6) NULL
        AFTER locked_by,
    ADD COLUMN deleted_by BIGINT NULL
        AFTER locked_at,
    ADD COLUMN deleted_at DATETIME(6) NULL
        AFTER deleted_by;


UPDATE users
SET first_name =
        SUBSTRING_INDEX(TRIM(full_name), ' ', -1),

    last_name =
        CASE
            WHEN LOCATE(' ', TRIM(full_name)) = 0
                THEN ''
            ELSE TRIM(
                    LEFT(
                         TRIM(full_name),
                         CHAR_LENGTH(TRIM(full_name))
                             - CHAR_LENGTH(
                                 SUBSTRING_INDEX(
                                         TRIM(full_name),
                                         ' ',
                                         -1
                                 )
                               )
                        )
                 )
            END;


UPDATE users
SET date_of_birth = '2000-01-01',
    gender = 'OTHER',
    address = 'System',
    phone = CASE
                WHEN phone IS NULL OR TRIM(phone) = ''
                    THEN '0000000000'
                ELSE phone
        END;

UPDATE users
SET role_code =
        CASE
            WHEN role = 'ADMIN' THEN 1
            ELSE 0
            END,

    status_code =
        CASE
            WHEN status = 'LOCKED' THEN 1
            WHEN status = 'DELETED' THEN 2
            ELSE 0
            END;

ALTER TABLE users
DROP COLUMN full_name,
    DROP COLUMN role,
    DROP COLUMN status,

    CHANGE COLUMN role_code role
        TINYINT NOT NULL DEFAULT 0,

    CHANGE COLUMN status_code status
        TINYINT NOT NULL DEFAULT 0,

    MODIFY COLUMN first_name
        VARCHAR(100) NOT NULL,

    MODIFY COLUMN last_name
        VARCHAR(150) NOT NULL,

    MODIFY COLUMN email
        VARCHAR(320) NOT NULL,

    MODIFY COLUMN password
        VARCHAR(255) NOT NULL,

    MODIFY COLUMN date_of_birth
        DATE NOT NULL,

    MODIFY COLUMN gender
        VARCHAR(20) NOT NULL,

    MODIFY COLUMN address
        VARCHAR(255) NOT NULL,

    MODIFY COLUMN phone
        VARCHAR(20) NOT NULL,

    MODIFY COLUMN avatar_url
        VARCHAR(255) NULL;



ALTER TABLE users
    ADD CONSTRAINT fk_users_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users (id)
            ON DELETE SET NULL,

    ADD CONSTRAINT fk_users_locked_by
        FOREIGN KEY (locked_by)
        REFERENCES users (id)
        ON DELETE SET NULL,

    ADD CONSTRAINT fk_users_deleted_by
        FOREIGN KEY (deleted_by)
        REFERENCES users (id)
        ON DELETE SET NULL;


CREATE INDEX idx_users_name
    ON users (first_name, last_name);

CREATE INDEX idx_users_role_status
    ON users (role, status);