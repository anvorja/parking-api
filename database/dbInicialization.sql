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

CREATE TABLE IF NOT EXISTS public.ubicacion (
    id_ubicacion BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    id_estado_ubicacion BIGINT NOT NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

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

-- ROLES
INSERT INTO public.rol (nombre) VALUES
('ADMINISTRADOR'),
('AUXILIAR')
ON CONFLICT (nombre) DO NOTHING;

-- ESTADO USUARIO
INSERT INTO public.estado_usuario (nombre) VALUES
('ACTIVO'),
('INACTIVO')
ON CONFLICT (nombre) DO NOTHING;

-- ESTADO UBICACION
INSERT INTO public.estado_ubicacion (nombre) VALUES
('DISPONIBLE'),
('OCUPADO')
ON CONFLICT (nombre) DO NOTHING;

-- ESTADO INGRESO
INSERT INTO public.estado_ingreso (nombre) VALUES
('INGRESADO'),
('ENTREGADO')
ON CONFLICT (nombre) DO NOTHING;

-- TIPO VEHICULO
INSERT INTO public.tipo_vehiculo (nombre) VALUES
('CARRO'),
('MOTO')
ON CONFLICT (nombre) DO NOTHING;

-- UNIDAD TARIFA
INSERT INTO public.unidad_tarifa (nombre) VALUES
('HORA'),
('DIA')
ON CONFLICT (nombre) DO NOTHING;

-- TIPO CODIGO TICKET
INSERT INTO public.tipo_codigo_ticket (nombre) VALUES
('QR'),
('BARRAS')
ON CONFLICT (nombre) DO NOTHING;