-- 초기 관리자 계정 (비밀번호: admin1234 BCrypt 해시)
INSERT INTO users (username, password, name, role, status)
VALUES (
    'admin',
    '$2b$10$HR8tD6AKhc2/Z6U67FpdBe05uDgsQilmjvV6ynl7ZG46o/VCbNz7i',
    '관리자',
    'ADMIN',
    'ACTIVE'
);
