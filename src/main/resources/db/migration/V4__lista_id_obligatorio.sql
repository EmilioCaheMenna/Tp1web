INSERT INTO listas (nombre) VALUES ('Sin clasificar');
UPDATE favoritos SET lista_id = (SELECT id FROM listas WHERE nombre = 'Sin clasificar' LIMIT 1) WHERE lista_id IS NULL;
ALTER TABLE favoritos ALTER COLUMN lista_id SET NOT NULL;
