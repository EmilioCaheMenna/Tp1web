INSERT INTO usuarios (email, password_hash, rol)
SELECT 'sistema@demo.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiZ6T8W8N9W1uJ5vX7x6Y2z3A4b5C6d', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'sistema@demo.local');

ALTER TABLE listas ADD COLUMN usuario_id BIGINT REFERENCES usuarios(id);

UPDATE listas SET usuario_id = (SELECT id FROM usuarios WHERE email = 'sistema@demo.local' LIMIT 1) WHERE usuario_id IS NULL;

ALTER TABLE listas ALTER COLUMN usuario_id SET NOT NULL;
