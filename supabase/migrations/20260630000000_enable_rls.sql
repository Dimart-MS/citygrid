-- Habilitar Row Level Security (RLS) en todas las tablas
ALTER TABLE public.roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.contenedores ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.lecturasresiduos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tanques ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bombas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.lecturasagua ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.luminarias ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.lecturasluminaria ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.alertas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notificaciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.componentes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mantenimientos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bitacorasistema ENABLE ROW LEVEL SECURITY;

-- Como la app Android no usa Supabase Auth (usa tabla propia de 'usuarios'),
-- necesitamos permitir operaciones a nivel anon.
-- En un entorno de producción, esto debería cambiarse para usar
-- autenticación segura con JWT o restringir el acceso desde APIs.

DO $$ 
DECLARE
    t text;
BEGIN
    FOR t IN 
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'public' 
    LOOP
        -- Permitir lectura pública a todas las tablas
        EXECUTE format('
            CREATE POLICY "Allow anon read access on %I"
            ON %I FOR SELECT 
            TO anon
            USING (true);
        ', t, t);
        
        -- Permitir inserción pública para registros de telemetría, bitácoras y registros
        EXECUTE format('
            CREATE POLICY "Allow anon insert access on %I"
            ON %I FOR INSERT 
            TO anon
            WITH CHECK (true);
        ', t, t);
        
        -- Permitir actualización pública ÚNICAMENTE en 'alertas' (para marcarlas como atendidas)
        -- y en 'usuarios' (para migrar/hashear contraseñas en seedAdmin)
        IF t IN ('alertas', 'usuarios') THEN
            EXECUTE format('
                CREATE POLICY "Allow anon update access on %I"
                ON %I FOR UPDATE 
                TO anon
                USING (true)
                WITH CHECK (true);
            ', t, t);
        END IF;
        
        -- NO se crea ninguna política de DELETE para anon.
        -- Eliminar registros está completamente deshabilitado para el rol público 'anon'.
    END LOOP;
END $$;
