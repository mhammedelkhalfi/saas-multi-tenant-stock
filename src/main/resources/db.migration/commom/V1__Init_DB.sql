CREATE TABLE public.tenants
(
    id              VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP(6) NOT NULL,
    created_by      VARCHAR(255) NOT NULL,
    deleted         BOOLEAN      NOT NULL,
    updated_at      TIMESTAMP(6),
    updated_by      VARCHAR(255),
    company_name    VARCHAR(255) NOT NULL,
    company_code    VARCHAR(255) NOT NULL
        CONSTRAINT tenants_company_code_unique_constraint UNIQUE,
    email           VARCHAR(255) NOT NULL
        CONSTRAINT tenants_email_unique_constraint UNIQUE,
    status          VARCHAR(255) NOT NULL
        CONSTRAINT tenants_status_check
            CHECK ((status)::TEXT = ANY (ARRAY ['PENDING'::CHARACTER VARYING, 'ACTIVE'::CHARACTER VARYING, 'SUSPENDED'::CHARACTER VARYING, 'INACTIVE'::CHARACTER VARYING]::TEXT[])),
    admin_full_name VARCHAR(255) NOT NULL,
    admin_email     VARCHAR(255) NOT NULL
        CONSTRAINT tenants_admin_email_unique_constraint UNIQUE,
    admin_username  VARCHAR(255) NOT NULL
        CONSTRAINT tenants_admin_username_unique_constraint UNIQUE,
    admin_password  VARCHAR(255) NOT NULL
);

CREATE TABLE public.users
(
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    deleted    BOOLEAN      NOT NULL,
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    tenant_id  VARCHAR(255)
        CONSTRAINT fk_user_tenant_id REFERENCES public.tenants (id),
    username   VARCHAR(255) NOT NULL
        CONSTRAINT users_username_unique_constraint UNIQUE,
    email      VARCHAR(255) NOT NULL
        CONSTRAINT users_email_unique_constraint UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL
        CONSTRAINT users_role_check
            CHECK ((role)::TEXT = ANY (ARRAY [
                'ROLE_PLATFORM_ADMIN'::CHARACTER VARYING,
                'ROLE_COMPANY_ADMIN'::CHARACTER VARYING,
                'ROLE_ADMINISTRATOR'::CHARACTER VARYING,
                'ROLE_USER'::CHARACTER VARYING,
                'ROLE_SALES_OPERATOR'::CHARACTER VARYING
            ]::TEXT[])),
    enabled    BOOLEAN
);

CREATE TABLE public.categories
(
    id          VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at  TIMESTAMP(6) NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    deleted     BOOLEAN      NOT NULL,
    updated_at  TIMESTAMP(6),
    updated_by  VARCHAR(255),
    description TEXT,
    name        VARCHAR(255) NOT NULL
        CONSTRAINT category_name_unique_constraint UNIQUE
);

CREATE TABLE public.products
(
    id              VARCHAR(255)   NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP(6)   NOT NULL,
    created_by      VARCHAR(255)   NOT NULL,
    deleted         BOOLEAN        NOT NULL,
    updated_at      TIMESTAMP(6),
    updated_by      VARCHAR(255),
    alert_threshold INTEGER        NOT NULL,
    description     TEXT,
    name            VARCHAR(255)   NOT NULL,
    price           NUMERIC(38, 2) NOT NULL,
    reference       VARCHAR(255)   NOT NULL
        CONSTRAINT products_reference_unique_constraint UNIQUE,
    category_id     VARCHAR(255)
        CONSTRAINT fk_category_id REFERENCES public.categories (id)
);

CREATE TABLE public.stock_mvts
(
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    deleted    BOOLEAN      NOT NULL,
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    comment    TEXT,
    date_mvt   DATE         NOT NULL,
    quantity   INTEGER      NOT NULL,
    type_mvt   VARCHAR(255) NOT NULL
        CONSTRAINT stock_mvts_type_mvt_check
            CHECK ((type_mvt)::TEXT = ANY (ARRAY ['IN'::CHARACTER VARYING, 'OUT'::CHARACTER VARYING]::TEXT[])),
    product_id VARCHAR(255)
        CONSTRAINT fk_product_id REFERENCES public.products (id)
);
