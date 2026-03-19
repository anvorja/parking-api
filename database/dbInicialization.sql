CREATE SCHEMA IF NOT EXISTS public;

-- =========================
-- TABLAS CATÁLOGO
-- =========================

CREATE TABLE IF NOT EXISTS public.rol (
    id_rol BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.estado_usuario (
    id_estado_usuario BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.estado_ubicacion (
    id_estado_ubicacion BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.estado_ingreso (
    id_estado_ingreso BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.tipo_vehiculo (
    id_tipo_vehiculo BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.unidad_tarifa (
    id_unidad_tarifa BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.tipo_codigo_ticket (
    id_tipo_codigo_ticket BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

-- =========================
-- TABLAS PRINCIPALES
-- =========================

CREATE TABLE IF NOT EXISTS public.usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(120) NOT NULL,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash TEXT NOT NULL,
    id_rol BIGINT NOT NULL,
    id_estado_usuario BIGINT NOT NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol)
        REFERENCES public.rol (id_rol),

    CONSTRAINT fk_usuario_estado_usuario
        FOREIGN KEY (id_estado_usuario)
        REFERENCES public.estado_usuario (id_estado_usuario)
);


-- =========================
-- TABLA REFRESH TOKEN
-- =========================
-- Almacena los refresh tokens de sesión.
-- El access token (JWT) dura 15 min; el refresh token dura 7 días.
-- ON DELETE CASCADE: si se elimina un usuario, sus tokens desaparecen también.
-- =========================

CREATE TABLE IF NOT EXISTS public.refresh_token (
    id_refresh_token  BIGSERIAL    PRIMARY KEY,
    token             VARCHAR(64)  NOT NULL UNIQUE,
    id_usuario        BIGINT       NOT NULL,
    fecha_expiracion  TIMESTAMPTZ  NOT NULL,
    revocado          BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES public.usuario (id_usuario)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_token
    ON public.refresh_token (token);

CREATE INDEX IF NOT EXISTS idx_refresh_token_usuario
    ON public.refresh_token (id_usuario);

-- =========================
-- TABLA UBICACION (extendida)
-- =========================
-- tipo_vehiculo_nativo: indica para qué tipo de vehículo fue diseñado el espacio.
--   CARRO (id=1): espacio físico de automóvil.
--   MOTO  (id=2): espacio físico exclusivo de moto.
--
-- capacidad: cuántos vehículos del tipo admitido caben en este espacio físico.
--   Espacio de carro normal      → capacidad = 1 (1 carro, o hasta 4 motos si se adapta)
--   Espacio de moto              → capacidad = 1 (siempre 1 moto)
--
-- Regla de negocio:
--   - Un espacio de tipo CARRO con capacidad=1 admite:
--       • exactamente 1 carro   (sin motos al mismo tiempo), O
--       • hasta 4 motos         (sin carro al mismo tiempo).
--   - Un espacio de tipo MOTO con capacidad=1 admite:
--       • exactamente 1 moto    (nunca un carro).
--   - El conteo de ingresos activos en ese espacio se compara con la capacidad
--     para determinar disponibilidad (lógica en IngresoVehiculoServiceImpl).
-- =========================

CREATE TABLE IF NOT EXISTS public.ubicacion (
    id_ubicacion           BIGSERIAL PRIMARY KEY,
    nombre                 VARCHAR(50)  NOT NULL UNIQUE,
    id_tipo_vehiculo_nativo BIGINT      NOT NULL,
    capacidad              INTEGER      NOT NULL DEFAULT 1
                               CONSTRAINT chk_ubicacion_capacidad CHECK (capacidad >= 1),
    id_estado_ubicacion    BIGINT       NOT NULL,
    fecha_creacion         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ubicacion_tipo_vehiculo_nativo
        FOREIGN KEY (id_tipo_vehiculo_nativo)
        REFERENCES public.tipo_vehiculo (id_tipo_vehiculo),

    CONSTRAINT fk_ubicacion_estado
        FOREIGN KEY (id_estado_ubicacion)
        REFERENCES public.estado_ubicacion (id_estado_ubicacion)
);

CREATE TABLE IF NOT EXISTS public.tarifa (
    id_tarifa BIGSERIAL PRIMARY KEY,
    id_tipo_vehiculo BIGINT NOT NULL,
    id_unidad_tarifa BIGINT NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tarifa_tipo_vehiculo
        FOREIGN KEY (id_tipo_vehiculo)
        REFERENCES public.tipo_vehiculo (id_tipo_vehiculo),

    CONSTRAINT fk_tarifa_unidad_tarifa
        FOREIGN KEY (id_unidad_tarifa)
        REFERENCES public.unidad_tarifa (id_unidad_tarifa),

    CONSTRAINT chk_tarifa_valor_positivo
        CHECK (valor >= 0)
);

CREATE TABLE IF NOT EXISTS public.ingreso_vehiculo (
    id_ingreso BIGSERIAL PRIMARY KEY,
    placa VARCHAR(10) NOT NULL,
    id_tipo_vehiculo BIGINT NOT NULL,
    id_ubicacion BIGINT NOT NULL,
    id_estado_ingreso BIGINT NOT NULL,
    fecha_hora_ingreso TIMESTAMPTZ NOT NULL,
    fecha_hora_salida TIMESTAMPTZ NULL,
    valor_cobrado NUMERIC(12,2) NULL,
    id_usuario_registro BIGINT NOT NULL,
    id_usuario_entrega BIGINT NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ingreso_tipo_vehiculo
        FOREIGN KEY (id_tipo_vehiculo)
        REFERENCES public.tipo_vehiculo (id_tipo_vehiculo),

    CONSTRAINT fk_ingreso_ubicacion
        FOREIGN KEY (id_ubicacion)
        REFERENCES public.ubicacion (id_ubicacion),

    CONSTRAINT fk_ingreso_estado
        FOREIGN KEY (id_estado_ingreso)
        REFERENCES public.estado_ingreso (id_estado_ingreso),

    CONSTRAINT fk_ingreso_usuario_registro
        FOREIGN KEY (id_usuario_registro)
        REFERENCES public.usuario (id_usuario),

    CONSTRAINT fk_ingreso_usuario_entrega
        FOREIGN KEY (id_usuario_entrega)
        REFERENCES public.usuario (id_usuario),

    CONSTRAINT chk_ingreso_valor_cobrado_positivo
        CHECK (valor_cobrado IS NULL OR valor_cobrado >= 0),

    CONSTRAINT chk_ingreso_fechas
        CHECK (
            fecha_hora_salida IS NULL
            OR fecha_hora_salida >= fecha_hora_ingreso
        )
);

CREATE TABLE IF NOT EXISTS public.ticket (
    id_ticket BIGSERIAL PRIMARY KEY,
    id_ingreso BIGINT NOT NULL,
    numero_ticket VARCHAR(40) NOT NULL UNIQUE,
    id_tipo_codigo_ticket BIGINT NOT NULL,
    codigo_valor VARCHAR(255) NOT NULL,
    fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_ingreso
        FOREIGN KEY (id_ingreso)
        REFERENCES public.ingreso_vehiculo (id_ingreso),

    CONSTRAINT fk_ticket_tipo_codigo
        FOREIGN KEY (id_tipo_codigo_ticket)
        REFERENCES public.tipo_codigo_ticket (id_tipo_codigo_ticket),

    CONSTRAINT uq_ticket_codigo_valor
        UNIQUE (codigo_valor)
);

-- =========================
-- DATOS CATÁLOGO
-- =========================

INSERT INTO public.rol (nombre) VALUES
('ADMINISTRADOR'),
('AUXILIAR')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO public.estado_usuario (nombre) VALUES
('ACTIVO'),
('INACTIVO')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO public.estado_ubicacion (nombre) VALUES
('DISPONIBLE'),
('OCUPADO'),
('INACTIVO')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO public.estado_ingreso (nombre) VALUES
('INGRESADO'),
('ENTREGADO')
ON CONFLICT (nombre) DO NOTHING;

-- id=1 → CARRO, id=2 → MOTO  (orden importa: lo referencia el seed de ubicaciones)
INSERT INTO public.tipo_vehiculo (nombre) VALUES
('CARRO'),
('MOTO')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO public.unidad_tarifa (nombre) VALUES
('HORA'),
('DIA')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO public.tipo_codigo_ticket (nombre) VALUES
('QR'),
('BARRAS')
ON CONFLICT (nombre) DO NOTHING;

-- =========================
-- SEED UBICACIONES
-- =========================
-- Espacios de CARRO: A01 – A50
--   tipo_vehiculo_nativo = 1 (CARRO)
--   capacidad            = 1 (1 carro, o hasta 4 motos — validado en backend)
--   estado               = DISPONIBLE
--
-- Espacios de MOTO: M01 – M40
--   tipo_vehiculo_nativo = 2 (MOTO)
--   capacidad            = 1 (siempre 1 moto)
--   estado               = DISPONIBLE
-- =========================

INSERT INTO public.ubicacion (nombre, id_tipo_vehiculo_nativo, capacidad, id_estado_ubicacion, fecha_creacion)
SELECT
    'A' || LPAD(n::TEXT, 2, '0'),   -- A01, A02 … A50
    (SELECT id_tipo_vehiculo FROM public.tipo_vehiculo WHERE nombre = 'CARRO'),
    1,
    (SELECT id_estado_ubicacion FROM public.estado_ubicacion WHERE nombre = 'DISPONIBLE'),
    CURRENT_TIMESTAMP
FROM generate_series(1, 50) AS n
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO public.ubicacion (nombre, id_tipo_vehiculo_nativo, capacidad, id_estado_ubicacion, fecha_creacion)
SELECT
    'M' || LPAD(n::TEXT, 2, '0'),   -- M01, M02 … M40
    (SELECT id_tipo_vehiculo FROM public.tipo_vehiculo WHERE nombre = 'MOTO'),
    1,
    (SELECT id_estado_ubicacion FROM public.estado_ubicacion WHERE nombre = 'DISPONIBLE'),
    CURRENT_TIMESTAMP
FROM generate_series(1, 40) AS n
ON CONFLICT (nombre) DO NOTHING;

-- =========================
-- SEED TARIFAS (ejemplo base)
-- Ajustar valores según política del parqueadero
-- =========================

INSERT INTO public.tarifa (id_tipo_vehiculo, id_unidad_tarifa, valor, activa, fecha_creacion)
SELECT
    (SELECT id_tipo_vehiculo FROM public.tipo_vehiculo WHERE nombre = 'CARRO'),
    (SELECT id_unidad_tarifa FROM public.unidad_tarifa WHERE nombre = 'HORA'),
    3000.00,
    TRUE,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM public.tarifa t
    JOIN public.tipo_vehiculo tv ON tv.id_tipo_vehiculo = t.id_tipo_vehiculo
    JOIN public.unidad_tarifa ut ON ut.id_unidad_tarifa = t.id_unidad_tarifa
    WHERE tv.nombre = 'CARRO' AND ut.nombre = 'HORA' AND t.activa = TRUE
);

INSERT INTO public.tarifa (id_tipo_vehiculo, id_unidad_tarifa, valor, activa, fecha_creacion)
SELECT
    (SELECT id_tipo_vehiculo FROM public.tipo_vehiculo WHERE nombre = 'MOTO'),
    (SELECT id_unidad_tarifa FROM public.unidad_tarifa WHERE nombre = 'HORA'),
    2000.00,
    TRUE,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM public.tarifa t
    JOIN public.tipo_vehiculo tv ON tv.id_tipo_vehiculo = t.id_tipo_vehiculo
    JOIN public.unidad_tarifa ut ON ut.id_unidad_tarifa = t.id_unidad_tarifa
    WHERE tv.nombre = 'MOTO' AND ut.nombre = 'HORA' AND t.activa = TRUE
);