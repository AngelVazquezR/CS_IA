# Plan de tareas - Data Model v2

## Objetivo
Adaptar la aplicación al nuevo modelo relacional basado en STUDENTS, TEACHERS, USERS y ASSIGNMENTS, manteniendo SQLite/MySQL y evitando regresiones durante la migración.

## Fase 1 - Esquema y dominio
- [x] Crear rama `feature/data-model-v2` desde `main`.
- [x] Normalizar nombres de columnas del esquema SQLite v2.
- [x] Añadir representación de dominio para asignaciones profesor-alumno.
- [x] Adaptar `Persona`, `Alumno` y `Profesor` de forma compatible con la UI existente.
- [x] Preparar el dominio para IDs numéricos autoincrementales manteniendo compatibilidad temporal con IDs de texto.
- [x] Añadir entidad `Usuario` para `USER_ID`, `USERNAME` y `PASSWORD_HASH` manteniendo temporalmente `Users` para la UI antigua.
- [x] Añadir pruebas unitarias del dominio v2.
- [x] Añadir prueba de integración del esquema SQLite v2.

## Fase 2 - Persistencia
- [ ] Incorporar la infraestructura SQLite/JDBC ya validada de `feature/sqlite-support` de forma controlada.
- [ ] Adaptar `AlumnoRepository` a `STUDENTS`.
- [ ] Adaptar `ProfesorRepository` a `TEACHERS`.
- [ ] Adaptar `UsuarioRepository` a `USERS`.
- [ ] Crear `AsignacionRepository` para `ASSIGNMENTS`.
- [ ] Eliminar generación de IDs basada en `COUNT(*)`.
- [ ] Garantizar `PreparedStatement` en todas las operaciones CRUD.
- [ ] Añadir pruebas de integridad referencial y CRUD para las cuatro tablas.

## Fase 3 - TableModels y controladores
- [ ] Adaptar `AlumnoTableModel` para mostrar email y eliminar profesor directo.
- [ ] Adaptar `ProfesorTableModel` para mostrar asignatura y email.
- [ ] Eliminar `AlumnoTableModelOld` cuando no existan referencias.
- [ ] Adaptar controladores y servicios que dependan de columnas antiguas.

## Fase 4 - UI
- [ ] Añadir email al alta/modificación de alumnos.
- [ ] Sustituir fecha alta/baja de profesor por asignatura y email.
- [ ] Rediseñar la asignación profesor-alumno para crear registros en `ASSIGNMENTS`.
- [ ] Añadir día de semana, hora de inicio, fecha inicial y fecha final a la asignación.
- [ ] Adaptar vistas de alumnos y profesores al nuevo modelo.
- [ ] Revisar validaciones de formularios y mensajes de error.

## Fase 5 - Seguridad y autenticación
- [ ] Migrar consultas de `USUARIOS/CONTRASEÑA` a `USERS/PASSWORD_HASH`.
- [ ] Evitar almacenamiento y logs de contraseñas en texto plano.
- [ ] Mantener compatibilidad temporal solo mientras se migra la UI de autenticación.

## Fase 6 - Validación
- [ ] Ejecutar `mvn clean test` con los nuevos tests del modelo v2.
- [ ] Ejecutar `mvn clean package`.
- [ ] Prueba manual de alta/modificación/baja de alumnos.
- [ ] Prueba manual de alta/modificación/baja de profesores.
- [ ] Prueba manual de asignaciones y persistencia tras reinicio.
- [ ] Prueba manual de login/registro.
- [x] Añadir comprobación automatizada del esquema SQLite esperado.

## Criterio de integración
La rama solo se propondrá para merge a `main` cuando el modelo, repositorios, UI y pruebas estén adaptados y no queden consultas activas contra las tablas antiguas `ALUMNOS`, `PROFESORES` o `USUARIOS`.
