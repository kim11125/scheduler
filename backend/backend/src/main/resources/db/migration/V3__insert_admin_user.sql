-- 초기 관리자 계정 (비밀번호: admin1234 BCrypt 해시)
INSERT INTO users (username, password, name, role, status)
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyGyR.pOm',
    '관리자',
    'ADMIN',
    'ACTIVE'
);
