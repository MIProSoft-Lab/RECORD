-- Promueve publication_authors de colección embebida (PK compuesta por user_id)
-- a una tabla con identidad propia que admite autores internos (user_id) y
-- externos (first_name/last_name de texto plano, sin cuenta en la plataforma).
-- user_id es una referencia débil a la tabla de usuarios: sin foreign key, ya que
-- no se enlazan entidades de dominios distintos en base de datos.

ALTER TABLE publication_authors DROP CONSTRAINT publication_authors_pkey;

ALTER TABLE publication_authors ADD COLUMN id UUID NOT NULL DEFAULT gen_random_uuid();

ALTER TABLE publication_authors ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE publication_authors ADD COLUMN first_name VARCHAR(255);
ALTER TABLE publication_authors ADD COLUMN last_name VARCHAR(255);
ALTER TABLE publication_authors ADD COLUMN position INTEGER NOT NULL DEFAULT 0;

ALTER TABLE publication_authors ADD CONSTRAINT publication_authors_pkey PRIMARY KEY (id);

-- Un autor es interno (user_id presente, sin nombres) o externo (nombres presentes,
-- sin user_id), nunca ambos ni ninguno.
ALTER TABLE publication_authors
    ADD CONSTRAINT chk_publication_authors_internal_xor_external CHECK (
        (user_id IS NOT NULL AND first_name IS NULL AND last_name IS NULL)
        OR
        (user_id IS NULL AND first_name IS NOT NULL AND last_name IS NOT NULL)
    );

-- Evita autores internos duplicados dentro de una misma publicación. Los autores
-- externos tienen user_id NULL: al ser los NULL distintos entre sí, se permiten
-- varios externos por publicación.
ALTER TABLE publication_authors
    ADD CONSTRAINT uq_publication_authors_internal UNIQUE (publication_id, user_id);
