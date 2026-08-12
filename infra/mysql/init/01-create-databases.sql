-- Chỉ tạo database, chưa tạo bảng hoặc dữ liệu mẫu.
CREATE DATABASE IF NOT EXISTS identity_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS organization_customer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
       -- =========================================================
-- ORGANIZATION CUSTOMER SERVICE DATABASE
-- =========================================================

CREATE DATABASE IF NOT EXISTS organization_customer_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE organization_customer_db;


-- =========================================================
-- 1. ORGANIZATIONS - DOANH NGHIỆP
-- =========================================================

CREATE TABLE organizations (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               organization_code VARCHAR(50) NOT NULL,
                               organization_name VARCHAR(255) NOT NULL,

                               tax_code VARCHAR(50),
                               email VARCHAR(255),
                               phone VARCHAR(30),

                               address VARCHAR(500),

                               status ENUM(
        'ACTIVE',
        'INACTIVE',
        'SUSPENDED',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                               created_by BIGINT,
                               updated_by BIGINT,

                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,

                               deleted_at DATETIME NULL,

                               CONSTRAINT uq_organizations_code
                                   UNIQUE (organization_code),

                               CONSTRAINT uq_organizations_tax_code
                                   UNIQUE (tax_code)
);


-- =========================================================
-- 2. BRANCHES - CHI NHÁNH
-- =========================================================

CREATE TABLE branches (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          organization_id BIGINT NOT NULL,

                          branch_code VARCHAR(50) NOT NULL,
                          branch_name VARCHAR(255) NOT NULL,

                          email VARCHAR(255),
                          phone VARCHAR(30),

                          address VARCHAR(500),

                          status ENUM(
        'ACTIVE',
        'INACTIVE',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                          created_by BIGINT,
                          updated_by BIGINT,

                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                          deleted_at DATETIME NULL,

                          CONSTRAINT fk_branches_organization
                              FOREIGN KEY (organization_id)
                                  REFERENCES organizations(id),

                          CONSTRAINT uq_branch_code_per_organization
                              UNIQUE (organization_id, branch_code),

    -- phục vụ FK kép để bảo đảm đúng organization
                          CONSTRAINT uq_branch_id_organization
                              UNIQUE (id, organization_id)
);


-- =========================================================
-- 3. EMPLOYEES - NHÂN VIÊN
--
-- user_id chỉ tham chiếu LOGIC sang identity-service.
-- KHÔNG tạo foreign key tới identity_db.
-- =========================================================

CREATE TABLE employees (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,

                           organization_id BIGINT NOT NULL,

    -- ID user lấy từ identity-service nếu nhân viên có tài khoản
                           user_id BIGINT NULL,

                           employee_code VARCHAR(50) NOT NULL,

                           full_name VARCHAR(255) NOT NULL,

                           email VARCHAR(255),
                           phone VARCHAR(30),

                           job_title VARCHAR(100),

                           status ENUM(
        'ACTIVE',
        'INACTIVE',
        'RESIGNED',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                           hire_date DATE,

                           created_by BIGINT,
                           updated_by BIGINT,

                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,

                           deleted_at DATETIME NULL,

                           CONSTRAINT fk_employees_organization
                               FOREIGN KEY (organization_id)
                                   REFERENCES organizations(id),

                           CONSTRAINT uq_employee_code_per_organization
                               UNIQUE (organization_id, employee_code),

                           CONSTRAINT uq_employee_user_per_organization
                               UNIQUE (organization_id, user_id),

    -- phục vụ EmployeeBranchAssignment
                           CONSTRAINT uq_employee_id_organization
                               UNIQUE (id, organization_id)
);


-- =========================================================
-- 4. EMPLOYEE BRANCH ASSIGNMENTS
-- GÁN NHÂN VIÊN VÀO CHI NHÁNH
--
-- Đây là bảng RIÊNG theo đúng yêu cầu.
-- Không nhét branch trực tiếp vào employee.
-- =========================================================

CREATE TABLE employee_branch_assignments (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                             organization_id BIGINT NOT NULL,
                                             employee_id BIGINT NOT NULL,
                                             branch_id BIGINT NOT NULL,

                                             is_primary BOOLEAN NOT NULL DEFAULT FALSE,

                                             assigned_from DATE,
                                             assigned_to DATE,

                                             status ENUM(
        'ACTIVE',
        'INACTIVE'
    ) NOT NULL DEFAULT 'ACTIVE',

                                             created_by BIGINT,
                                             updated_by BIGINT,

                                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                 ON UPDATE CURRENT_TIMESTAMP,

                                             CONSTRAINT fk_assignment_organization
                                                 FOREIGN KEY (organization_id)
                                                     REFERENCES organizations(id),

    -- employee phải thuộc đúng organization
                                             CONSTRAINT fk_assignment_employee
                                                 FOREIGN KEY (employee_id, organization_id)
                                                     REFERENCES employees(id, organization_id),

    -- branch phải thuộc đúng organization
                                             CONSTRAINT fk_assignment_branch
                                                 FOREIGN KEY (branch_id, organization_id)
                                                     REFERENCES branches(id, organization_id),

                                             CONSTRAINT uq_employee_branch
                                                 UNIQUE (employee_id, branch_id)
);


-- =========================================================
-- 5. CUSTOMERS - KHÁCH HÀNG
--
-- Dùng chung 1 bảng cho:
-- + INDIVIDUAL = khách hàng cá nhân
-- + BUSINESS   = khách hàng doanh nghiệp
--
-- owner_user_id hỗ trợ kiểm tra scope OWN.
-- =========================================================

CREATE TABLE customers (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,

                           organization_id BIGINT NOT NULL,

    -- Có thể NULL nếu khách hàng không thuộc riêng một chi nhánh
                           branch_id BIGINT NULL,

    -- user chịu trách nhiệm quản lý customer này.
    -- Chỉ tham chiếu logic tới identity-service.
                           owner_user_id BIGINT NULL,

                           customer_code VARCHAR(50) NOT NULL,

                           customer_type ENUM(
        'INDIVIDUAL',
        'BUSINESS'
    ) NOT NULL,

    -- =====================================================
    -- Thông tin chung
    -- =====================================================

                           display_name VARCHAR(255) NOT NULL,

                           email VARCHAR(255),
                           phone VARCHAR(30),

                           address VARCHAR(500),

    -- =====================================================
    -- Thông tin khách hàng cá nhân
    -- =====================================================

                           full_name VARCHAR(255),
                           date_of_birth DATE,

                           identity_number VARCHAR(100),

    -- =====================================================
    -- Thông tin khách hàng doanh nghiệp
    -- =====================================================

                           company_name VARCHAR(255),
                           tax_code VARCHAR(50),

                           representative_name VARCHAR(255),
                           representative_phone VARCHAR(30),
                           representative_email VARCHAR(255),

    -- =====================================================

                           status ENUM(
        'ACTIVE',
        'INACTIVE',
        'BLOCKED',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                           note TEXT,

                           created_by BIGINT,
                           updated_by BIGINT,

                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,

                           deleted_at DATETIME NULL,

                           CONSTRAINT fk_customers_organization
                               FOREIGN KEY (organization_id)
                                   REFERENCES organizations(id),

                           CONSTRAINT fk_customers_branch
                               FOREIGN KEY (branch_id, organization_id)
                                   REFERENCES branches(id, organization_id),

                           CONSTRAINT uq_customer_code_per_organization
                               UNIQUE (organization_id, customer_code),

                           CONSTRAINT uq_customer_id_organization
                               UNIQUE (id, organization_id)
);


-- =========================================================
-- 6. CUSTOMER GROUPS - NHÓM KHÁCH HÀNG
-- =========================================================

CREATE TABLE customer_groups (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                 organization_id BIGINT NOT NULL,

                                 group_code VARCHAR(50) NOT NULL,
                                 group_name VARCHAR(255) NOT NULL,

                                 description TEXT,

                                 status ENUM(
        'ACTIVE',
        'INACTIVE',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE',

                                 created_by BIGINT,
                                 updated_by BIGINT,

                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,

                                 deleted_at DATETIME NULL,

                                 CONSTRAINT fk_customer_groups_organization
                                     FOREIGN KEY (organization_id)
                                         REFERENCES organizations(id),

                                 CONSTRAINT uq_customer_group_code
                                     UNIQUE (organization_id, group_code),

                                 CONSTRAINT uq_customer_group_id_organization
                                     UNIQUE (id, organization_id)
);


-- =========================================================
-- 7. CUSTOMER GROUP MEMBERS
--
-- Quan hệ nhiều-nhiều:
-- Customer <-> CustomerGroup
-- =========================================================

CREATE TABLE customer_group_members (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                        organization_id BIGINT NOT NULL,

                                        customer_group_id BIGINT NOT NULL,
                                        customer_id BIGINT NOT NULL,

                                        created_by BIGINT,

                                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT fk_group_member_organization
                                            FOREIGN KEY (organization_id)
                                                REFERENCES organizations(id),

                                        CONSTRAINT fk_group_member_group
                                            FOREIGN KEY (customer_group_id, organization_id)
                                                REFERENCES customer_groups(id, organization_id),

                                        CONSTRAINT fk_group_member_customer
                                            FOREIGN KEY (customer_id, organization_id)
                                                REFERENCES customers(id, organization_id),

                                        CONSTRAINT uq_customer_group_member
                                            UNIQUE (customer_group_id, customer_id)
);


-- =========================================================
-- 8. RESTRICTED CUSTOMERS
-- DANH SÁCH KHÁCH HÀNG HẠN CHẾ
--
-- Đây là bảng riêng theo đúng yêu cầu.
-- Không chỉ dùng một boolean trong bảng customers.
-- =========================================================

CREATE TABLE restricted_customers (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                      organization_id BIGINT NOT NULL,
                                      customer_id BIGINT NOT NULL,

                                      restriction_type ENUM(
        'RENTAL_BLOCK',
        'PAYMENT_RISK',
        'LATE_RETURN',
        'DAMAGED_EQUIPMENT',
        'FRAUD_RISK',
        'OTHER'
    ) NOT NULL,

                                      reason VARCHAR(1000) NOT NULL,

                                      status ENUM(
        'ACTIVE',
        'EXPIRED',
        'REMOVED'
    ) NOT NULL DEFAULT 'ACTIVE',

                                      restricted_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      restricted_until DATETIME NULL,

    -- user từ identity-service thực hiện hạn chế
                                      restricted_by_user_id BIGINT NOT NULL,

                                      removed_at DATETIME NULL,
                                      removed_by_user_id BIGINT NULL,
                                      removed_reason VARCHAR(1000),

                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_restricted_organization
                                          FOREIGN KEY (organization_id)
                                              REFERENCES organizations(id),

                                      CONSTRAINT fk_restricted_customer
                                          FOREIGN KEY (customer_id, organization_id)
                                              REFERENCES customers(id, organization_id)
);


-- =========================================================
-- INDEX
-- =========================================================

CREATE INDEX idx_branches_organization
    ON branches(organization_id);

CREATE INDEX idx_employees_organization
    ON employees(organization_id);

CREATE INDEX idx_employees_user
    ON employees(user_id);

CREATE INDEX idx_assignment_employee
    ON employee_branch_assignments(employee_id);

CREATE INDEX idx_assignment_branch
    ON employee_branch_assignments(branch_id);

CREATE INDEX idx_customers_organization
    ON customers(organization_id);

CREATE INDEX idx_customers_branch
    ON customers(branch_id);

CREATE INDEX idx_customers_owner
    ON customers(owner_user_id);

CREATE INDEX idx_customers_email
    ON customers(email);

CREATE INDEX idx_customers_phone
    ON customers(phone);

CREATE INDEX idx_customer_groups_organization
    ON customer_groups(organization_id);

CREATE INDEX idx_restricted_customer
    ON restricted_customers(customer_id);

CREATE INDEX idx_restricted_status
    ON restricted_customers(organization_id, status);
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS rental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS logistics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS billing_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS maintenance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
