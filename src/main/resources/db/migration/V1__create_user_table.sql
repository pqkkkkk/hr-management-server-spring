-- Create department table first (parent table)
CREATE TABLE department_table (
    department_id VARCHAR(36) PRIMARY KEY,
    department_name VARCHAR(255) NOT NULL
);

-- Create user table with foreign key reference to department
CREATE TABLE user_table (
    user_id VARCHAR(36) PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255),
    role VARCHAR(50),
    position VARCHAR(255),
    join_date DATE,
    identity_card_number VARCHAR(50),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    bank_account_number VARCHAR(50),
    bank_name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    department_id VARCHAR(36),
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES department_table(department_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_user_email ON user_table(email);
CREATE INDEX idx_user_role ON user_table(role);
CREATE INDEX idx_user_department ON user_table(department_id);
CREATE INDEX idx_department_name ON department_table(department_name);