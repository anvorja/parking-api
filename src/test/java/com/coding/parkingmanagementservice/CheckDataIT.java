package com.coding.parkingmanagementservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class CheckDataIT {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @org.springframework.test.annotation.Rollback(false)
    void repairDatabase() {
        System.out.println("\n=== DATABASE REPAIR START ===");
        
        try {
            // 1. Create refresh_token table if missing
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.refresh_token (" +
                "id_refresh_token BIGSERIAL PRIMARY KEY, " +
                "token VARCHAR(64) NOT NULL UNIQUE, " +
                "id_usuario BIGINT NOT NULL, " +
                "fecha_expiracion TIMESTAMPTZ NOT NULL, " +
                "revocado BOOLEAN NOT NULL DEFAULT FALSE, " +
                "fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario (id_usuario) ON DELETE CASCADE" +
                ")");
            System.out.println("Refresh Token table ensured.");

            // 2. Ensure basic data
            jdbcTemplate.execute("INSERT INTO public.rol (nombre) VALUES ('ADMINISTRADOR'), ('AUXILIAR') ON CONFLICT (nombre) DO NOTHING");
            jdbcTemplate.execute("INSERT INTO public.estado_usuario (nombre) VALUES ('ACTIVO'), ('INACTIVO') ON CONFLICT (nombre) DO NOTHING");
            jdbcTemplate.execute("INSERT INTO public.tipo_vehiculo (nombre) VALUES ('CARRO'), ('MOTO') ON CONFLICT (nombre) DO NOTHING");
            jdbcTemplate.execute("INSERT INTO public.estado_ubicacion (nombre) VALUES ('DISPONIBLE'), ('OCUPADO'), ('INACTIVO') ON CONFLICT (nombre) DO NOTHING");
            jdbcTemplate.execute("INSERT INTO public.estado_ingreso (nombre) VALUES ('INGRESADO'), ('ENTREGADO') ON CONFLICT (nombre) DO NOTHING");
            jdbcTemplate.execute("INSERT INTO public.tipo_codigo_ticket (nombre) VALUES ('QR'), ('BARRAS') ON CONFLICT (nombre) DO NOTHING");
            
            // 3. Ensure Admin exists
            String passHash = passwordEncoder.encode("1234");
            jdbcTemplate.execute("INSERT INTO public.usuario (nombre_completo, nombre_usuario, contrasena_hash, id_rol, id_estado_usuario, fecha_creacion) " +
                "VALUES ('Administrador', 'admin', '" + passHash + "', " +
                "(SELECT id_rol FROM public.rol WHERE nombre='ADMINISTRADOR'), " +
                "(SELECT id_estado_usuario FROM public.estado_usuario WHERE nombre='ACTIVO'), CURRENT_TIMESTAMP) " +
                "ON CONFLICT (nombre_usuario) DO UPDATE SET contrasena_hash = '" + passHash + "'");
            
            // 4. Seeding Ubicaciones: Drop and recreate to ensure schema match
            Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM public.ubicacion", Long.class);
            if (count < 90) {
                System.out.println("Dropping and recreating tables to fix schema...");
                jdbcTemplate.execute("DROP TABLE IF EXISTS public.ticket CASCADE");
                jdbcTemplate.execute("DROP TABLE IF EXISTS public.ingreso_vehiculo CASCADE");
                jdbcTemplate.execute("DROP TABLE IF EXISTS public.ubicacion CASCADE");
                
                // Re-create ubicacion
                jdbcTemplate.execute("CREATE TABLE public.ubicacion (" +
                    "id_ubicacion BIGSERIAL PRIMARY KEY, " +
                    "nombre VARCHAR(50) NOT NULL UNIQUE, " +
                    "id_tipo_vehiculo_nativo BIGINT NOT NULL, " +
                    "capacidad INTEGER NOT NULL DEFAULT 1, " +
                    "id_estado_ubicacion BIGINT NOT NULL, " +
                    "fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_ubicacion_tipo_vehiculo_nativo FOREIGN KEY (id_tipo_vehiculo_nativo) REFERENCES public.tipo_vehiculo (id_tipo_vehiculo), " +
                    "CONSTRAINT fk_ubicacion_estado FOREIGN KEY (id_estado_ubicacion) REFERENCES public.estado_ubicacion (id_estado_ubicacion))");

                // Re-create ingreso_vehiculo
                jdbcTemplate.execute("CREATE TABLE public.ingreso_vehiculo (" +
                    "id_ingreso BIGSERIAL PRIMARY KEY, placa VARCHAR(10) NOT NULL, id_tipo_vehiculo BIGINT NOT NULL, " +
                    "id_ubicacion BIGINT NOT NULL, id_estado_ingreso BIGINT NOT NULL, fecha_hora_ingreso TIMESTAMPTZ NOT NULL, " +
                    "fecha_hora_salida TIMESTAMPTZ NULL, valor_cobrado NUMERIC(12,2) NULL, id_usuario_registro BIGINT NOT NULL, " +
                    "id_usuario_entrega BIGINT NULL, fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_ingreso_tipo_vehiculo FOREIGN KEY (id_tipo_vehiculo) REFERENCES public.tipo_vehiculo (id_tipo_vehiculo), " +
                    "CONSTRAINT fk_ingreso_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES public.ubicacion (id_ubicacion), " +
                    "CONSTRAINT fk_ingreso_estado FOREIGN KEY (id_estado_ingreso) REFERENCES public.estado_ingreso (id_estado_ingreso), " +
                    "CONSTRAINT fk_ingreso_usuario_registro FOREIGN KEY (id_usuario_registro) REFERENCES public.usuario (id_usuario), " +
                    "CONSTRAINT fk_ingreso_usuario_entrega FOREIGN KEY (id_usuario_entrega) REFERENCES public.usuario (id_usuario))");

                // Re-create ticket
                jdbcTemplate.execute("CREATE TABLE public.ticket (" +
                    "id_ticket BIGSERIAL PRIMARY KEY, id_ingreso BIGINT NOT NULL, numero_ticket VARCHAR(40) NOT NULL UNIQUE, " +
                    "id_tipo_codigo_ticket BIGINT NOT NULL, codigo_valor VARCHAR(255) NOT NULL, fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_ticket_ingreso FOREIGN KEY (id_ingreso) REFERENCES public.ingreso_vehiculo (id_ingreso), " +
                    "CONSTRAINT fk_ticket_tipo_codigo FOREIGN KEY (id_tipo_codigo_ticket) REFERENCES public.tipo_codigo_ticket (id_tipo_codigo_ticket), " +
                    "CONSTRAINT uq_ticket_codigo_valor UNIQUE (codigo_valor))");

                // Seed ubicaciones
                jdbcTemplate.execute("INSERT INTO public.ubicacion (nombre, id_tipo_vehiculo_nativo, capacidad, id_estado_ubicacion, fecha_creacion) " +
                    "SELECT 'A' || LPAD(n::TEXT, 2, '0'), (SELECT id_tipo_vehiculo FROM public.tipo_vehiculo WHERE nombre = 'CARRO'), 1, (SELECT id_estado_ubicacion FROM public.estado_ubicacion WHERE nombre = 'DISPONIBLE'), CURRENT_TIMESTAMP FROM generate_series(1, 50) AS n");
                jdbcTemplate.execute("INSERT INTO public.ubicacion (nombre, id_tipo_vehiculo_nativo, capacidad, id_estado_ubicacion, fecha_creacion) " +
                    "SELECT 'M' || LPAD(n::TEXT, 2, '0'), (SELECT id_tipo_vehiculo FROM public.tipo_vehiculo WHERE nombre = 'MOTO'), 1, (SELECT id_estado_ubicacion FROM public.estado_ubicacion WHERE nombre = 'DISPONIBLE'), CURRENT_TIMESTAMP FROM generate_series(1, 40) AS n");
                
                System.out.println("Locations and dependent tables recreated and seeded.");
            }

            System.out.println("Repair completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during repair: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== DATABASE REPAIR END ===\n");
    }
}
