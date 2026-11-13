BEGIN;

-- Evitar inserciones mientras se inicializa la numeración.
LOCK TABLE public.cliente, public.factura IN SHARE ROW EXCLUSIVE MODE;

DO $$
BEGIN
    -- Inicializar cada secuencia solo al crearla.
    -- Si ya existe, conserva su posición actual.
    IF to_regclass('public.seq_cliente') IS NULL THEN
        CREATE SEQUENCE public.seq_cliente MAXVALUE 999999999;

        PERFORM setval(
            'public.seq_cliente',
            COALESCE(MAX(substring(id_cliente FROM '^C([0-9]+)$')::bigint), 0) + 1,
            false
        )
        FROM public.cliente;
    END IF;

    IF to_regclass('public.seq_factura') IS NULL THEN
        CREATE SEQUENCE public.seq_factura MAXVALUE 999999999;

        PERFORM setval(
            'public.seq_factura',
            COALESCE(MAX(substring(id_factura FROM '^F([0-9]+)$')::bigint), 0) + 1,
            false
        )
        FROM public.factura;
    END IF;

    IF to_regclass('public.seq_numero_comprobante') IS NULL THEN
        CREATE SEQUENCE public.seq_numero_comprobante AS integer;

        PERFORM setval(
            'public.seq_numero_comprobante',
            GREATEST(COALESCE(MAX(numero_comprobante)::bigint, 0) + 1, 1),
            false
        )
        FROM public.factura;
    END IF;
END
$$;

-- Aplicar la misma regla de duplicados que usa ClientePostgresDAO.
CREATE UNIQUE INDEX IF NOT EXISTS ux_cliente_ficha
ON public.cliente (
    LOWER(BTRIM(nombre)),
    LOWER(BTRIM(apellido)),
    BTRIM(telefono),
    LOWER(BTRIM(direccion))
);

COMMIT;