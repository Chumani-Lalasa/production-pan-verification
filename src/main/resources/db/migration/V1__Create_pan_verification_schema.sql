CREATE TABLE pan_verification_records (
    id BIGSERIAL PRIMARY KEY,
    pan_number VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    aadhaar_linked BOOLEAN,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pan_verification_pan_number ON pan_verification_records(pan_number);
CREATE INDEX idx_pan_verification_reference_number ON pan_verification_records(reference_number);