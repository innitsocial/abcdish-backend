SET search_path TO abcdish;

ALTER TABLE IF EXISTS otp_codes
    DROP CONSTRAINT IF EXISTS otp_codes_purpose_check;

ALTER TABLE IF EXISTS otp_codes
    ADD CONSTRAINT otp_codes_purpose_check
    CHECK (purpose IN (
        'LOGIN',
        'REGISTER_EMAIL',
        'VERIFY_EMAIL',
        'VERIFY_MOBILE',
        'RESET_PASSWORD'
    ));
