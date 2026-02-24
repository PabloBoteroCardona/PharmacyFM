-- =============================================
--   PharmacyFM - Schema de base de datos
--   SQLite
-- =============================================

PRAGMA foreign_keys = ON;

-- =============================================
--   TABLAS
-- =============================================

CREATE TABLE IF NOT EXISTS usuarios (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    email    TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nombre   TEXT NOT NULL,
    telefono TEXT,
    rol      TEXT NOT NULL  -- 'admin' o 'paciente'
);

CREATE TABLE IF NOT EXISTS pacientes (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    nombre     TEXT NOT NULL,
    telefono   TEXT,
    email      TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS formulas (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre      TEXT NOT NULL,
    descripcion TEXT,
    precio      REAL
);

CREATE TABLE IF NOT EXISTS pedidos (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente          INTEGER NOT NULL,
    id_formula           INTEGER,           -- NULL si es fórmula personalizada
    formula_personalizada TEXT,             -- NULL si es fórmula del catálogo
    cantidad             INTEGER NOT NULL,
    observaciones        TEXT,
    fecha                TEXT NOT NULL,
    estado               TEXT NOT NULL,     -- pendiente, en preparación, listo, entregado, cancelado
    FOREIGN KEY (id_paciente) REFERENCES pacientes(id),
    FOREIGN KEY (id_formula)  REFERENCES formulas(id)
);

-- =============================================
--   DATOS DE EJEMPLO
--   NOTA: Las contraseñas están hasheadas con BCrypt.
--   La contraseña real de todos los usuarios es "password123"
--   La contraseña del admin es "admin"
-- =============================================

-- Usuarios
-- Admin: email=admin / password=admin
INSERT OR IGNORE INTO usuarios (id, email, password, nombre, telefono, rol)
VALUES (1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Administrador', '', 'admin');

-- Pacientes de ejemplo (password: "password123")
INSERT OR IGNORE INTO usuarios (id, email, password, nombre, telefono, rol)
VALUES (2, 'maria.garcia@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'María García López', '612345678', 'paciente');

INSERT OR IGNORE INTO usuarios (id, email, password, nombre, telefono, rol)
VALUES (3, 'carlos.martin@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Carlos Martín Ruiz', '698765432', 'paciente');

INSERT OR IGNORE INTO usuarios (id, email, password, nombre, telefono, rol)
VALUES (4, 'ana.lopez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ana López Fernández', '677123456', 'paciente');

-- Pacientes
INSERT OR IGNORE INTO pacientes (id, id_usuario, nombre, telefono, email)
VALUES (1, 2, 'María García López', '612345678', 'maria.garcia@email.com');

INSERT OR IGNORE INTO pacientes (id, id_usuario, nombre, telefono, email)
VALUES (2, 3, 'Carlos Martín Ruiz', '698765432', 'carlos.martin@email.com');

INSERT OR IGNORE INTO pacientes (id, id_usuario, nombre, telefono, email)
VALUES (3, 4, 'Ana López Fernández', '677123456', 'ana.lopez@email.com');

-- Fórmulas magistrales
INSERT OR IGNORE INTO formulas (id, nombre, descripcion, precio)
VALUES (1, 'Crema hidratante base', 'Crema emoliente con urea al 10% para piel seca y atópica.', 18.50);

INSERT OR IGNORE INTO formulas (id, nombre, descripcion, precio)
VALUES (2, 'Solución antifúngica', 'Solución tópica con clotrimazol al 1% para infecciones por hongos.', 12.00);

INSERT OR IGNORE INTO formulas (id, nombre, descripcion, precio)
VALUES (3, 'Gel antiinflamatorio', 'Gel con ibuprofeno al 5% para alivio de dolor muscular localizado.', 15.75);

INSERT OR IGNORE INTO formulas (id, nombre, descripcion, precio)
VALUES (4, 'Colirio lubricante', 'Solución oftálmica con ácido hialurónico al 0.2% para ojos secos.', 22.00);

INSERT OR IGNORE INTO formulas (id, nombre, descripcion, precio)
VALUES (5, 'Suspensión pediátrica', 'Suspensión oral de paracetamol 250mg/5ml con sabor a fresa.', 9.90);

-- Pedidos de ejemplo
INSERT OR IGNORE INTO pedidos (id, id_paciente, id_formula, formula_personalizada, cantidad, observaciones, fecha, estado)
VALUES (1, 1, 1, NULL, 2, 'Indicado por dermatólogo. Aplicar dos veces al día.', '2026-02-10 10:30:00', 'Entregado');

INSERT OR IGNORE INTO pedidos (id, id_paciente, id_formula, formula_personalizada, cantidad, observaciones, fecha, estado)
VALUES (2, 2, 3, NULL, 1, 'Para zona lumbar. Sin fragancia si es posible.', '2026-02-15 11:00:00', 'Listo');

INSERT OR IGNORE INTO pedidos (id, id_paciente, id_formula, formula_personalizada, cantidad, observaciones, fecha, estado)
VALUES (3, 3, NULL, 'Crema con vitamina C al 15%', 1, 'Prescripción médica adjunta en consulta. Sin parabenos.', '2026-02-18 09:15:00', 'En preparación');

INSERT OR IGNORE INTO pedidos (id, id_paciente, id_formula, formula_personalizada, cantidad, observaciones, fecha, estado)
VALUES (4, 1, 4, NULL, 1, NULL, '2026-02-20 16:45:00', 'Pendiente');

INSERT OR IGNORE INTO pedidos (id, id_paciente, id_formula, formula_personalizada, cantidad, observaciones, fecha, estado)
VALUES (5, 2, NULL, 'Pomada con óxido de zinc 20% y nistatina', 2, 'Para uso pediátrico. Bebé de 8 meses.', '2026-02-21 08:30:00', 'Pendiente');