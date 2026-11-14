# Respaldo de MongoDB — Pollería Paredes

## 1. Descripción

Este directorio contiene una copia de seguridad del catálogo de productos de Pollería Paredes, almacenado en MongoDB.

El respaldo permite recuperar la colección de productos si fuera necesario reconstruir la base de datos.

## 2. Información del respaldo

- **Base de datos original:** `PolleriaParedes_Catalogo`
- **Colección:** `Catalogo_Platos`
- **Cantidad de documentos:** 40
- **Fecha del respaldo:** 09/11/2026
- **Formato:** archivo comprimido `.archive.gz`
- **Herramienta:** MongoDB Database Tools (`mongodump`)
- **Archivo:** `PolleriaParedes_Catalogo_2026-11-09.archive.gz`

El respaldo incluye los documentos del catálogo, sus precios y los índices correspondientes.

## 3. Crear un nuevo respaldo

Desde la raíz del repositorio, ejecutar:

```powershell
mongodump --uri="mongodb://127.0.0.1:27017" --db="PolleriaParedes_Catalogo" --collection="Catalogo_Platos" --archive=".\database\mongodb\backups\PolleriaParedes_Catalogo_2026-11-09.archive.gz" --gzip
```

Para futuros respaldos, cambiar la fecha del nombre del archivo y evitar sobrescribir copias anteriores.

## 4. Restaurar en una base de verificación

Para comprobar el respaldo sin modificar la base principal:

```powershell
mongorestore --uri="mongodb://127.0.0.1:27017" --gzip --archive=".\database\mongodb\backups\PolleriaParedes_Catalogo_2026-11-09.archive.gz" --nsFrom="PolleriaParedes_Catalogo.Catalogo_Platos" --nsTo="PolleriaParedes_Catalogo_VerificacionBackup.Catalogo_Platos"
```

Este procedimiento está pensado para una base de verificación vacía. No se debe ejecutar repetidamente sobre una colección que ya contenga los documentos restaurados.

## 5. Verificación

La restauración fue comprobada mediante MongoDB Compass.

Se verificó la presencia de la colección `Catalogo_Platos` con 40 documentos y 2 índices.

## 6. Consideraciones

- Este respaldo representa una copia del catálogo en una fecha determinada.
- No se actualiza automáticamente cuando cambian los productos.
- Para recuperar cambios posteriores será necesario generar otro respaldo.
- No restaurar sobre la base principal sin verificar previamente el destino.
- El respaldo está destinado al catálogo de productos del proyecto académico.
