BEGIN;

CREATE TABLE public.cuenta_empleado (
    id_empleado VARCHAR(10) PRIMARY KEY
        REFERENCES public.empleado (id_empleado),
    contrasena_hash TEXT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

COMMIT;