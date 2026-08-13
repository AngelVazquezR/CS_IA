# Plan de tareas - Data Model v2

## Objetivo
Adaptar la aplicación al nuevo modelo relacional basado en STUDENTS, TEACHERS, USERS y ASSIGNMENTS, manteniendo SQLite/MySQL y evitando regresiones durante la migración.

## Fase 1 - Esquema y dominio
- [x] Crear rama `feature/data-model-v2` desde `main`.
- [x] Normalizar nombres de columnas del esquema SQLite v2.
- [x] Añadir representación de dominio para asignaciones profesor-alumno.
- [x] Adaptar `Persona`, `Alumno` y `Profesor` de forma compatible con la UI existente.
- [x] Preparar el dominio para IDs numéricos autoincrementales manteniendo compatibilidad temporal con IDs de texto.
- [x] Añadir entidad `Usuario` para `USER_ID`, `USERNAME` y `PASSWORD_HASH`.
- [x] Añadir pruebas unitarias del dominio v2.
- [x] Añadir prueba de integración del esquema SQLite v2.

## Fase 2 - Persistencia
- [x] Incorporar la infraestructura SQLite/JDBC necesaria de forma controlada (`DatabaseType`, `ConfigDB`, `DatabaseConnectionFactory`).
- [x] Adaptar `AlumnoRepository` a `STUDENTS`.
- [x] Adaptar `ProfesorRepository` a `TEACHERS`.
- [x] Adaptar `UsuarioRepository` a `USERS`.
- [x] Crear `AsignacionRepository` para `ASSIGNMENTS`.
- [x] Eliminar generación de IDs basada en `COUNT(*)` de los nuevos repositories.
- [x] Garantizar `PreparedStatement` en las operaciones CRUD de los nuevos repositories.
- [x] Añadir pruebas de integración CRUD e integridad referencial para las cuatro tablas.
- [x] Validar la fase completa con `mvn clean test` (12 tests correctos).

## Fase 3 - TableModels y controladores
- [x] Adaptar `AlumnoTableModel` para mostrar ID, nombre, apellido, DNI y email, eliminando profesor directo.
- [x] Adaptar `ProfesorTableModel` para mostrar ID, nombre, apellido, DNI, asignatura y email.
- [x] Añadir `setData` y actualización tipada para cargas desde repositories.
- [x] Añadir `PersonasTableController` para desacoplar la carga de tablas del SQL directo en las ventanas.
- [x] Añadir pruebas unitarias de los TableModels v2.
- [x] Eliminar `AlumnoTableModelOld` tras migrar las vistas.

## Fase 4 - UI
- [x] Conservar la configuración de ejecución en `Main` para compartirla con las ventanas.
- [x] Detectar el tipo MYSQL/SQLITE al leer configuraciones existentes.
- [x] Añadir email al alta/modificación de alumnos en `VisualizarAlumnos`.
- [x] Sustituir fecha alta/baja de profesor por asignatura y email en `VisualizarProfesores`.
- [x] Conectar vistas de alumnos/profesores con `PersonasTableController` y repositories v2.
- [x] Implementar alta, modificación y baja mediante repositories v2, sin SQL directo en esas vistas.
- [x] Rediseñar `AsignarTab` para crear registros en `ASSIGNMENTS`.
- [x] Añadir selección única de día de semana, hora de inicio, fecha inicial y fecha final a la asignación.
- [x] Cargar profesores y alumnos de la asignación por ID desde sus repositories.
- [x] Añadir validaciones básicas y mensajes de error en las pantallas migradas.
- [x] Validar compilación y tests después del cableado de UI (16 tests correctos).
- [x] Retirar `GestionarTab` y pantallas auxiliares experimentales que duplicaban funcionalidad.
- [ ] Realizar prueba manual de las tres pantallas migradas.

## Fase 5 - Seguridad y autenticación
- [x] Migrar `LoginPage` y `RegistarTab` de `USUARIOS/CONTRASEÑA` a `UsuarioRepository` sobre `USERS/PASSWORD_HASH`.
- [x] Añadir `AuthService` para desacoplar autenticación y registro de Swing.
- [x] Sustituir SHA-256 directo por PBKDF2-HMAC-SHA256 con sal aleatoria para las nuevas credenciales.
- [x] Usar `JPasswordField` y limpiar arrays/campos de contraseña después de utilizarlos.
- [x] Evitar logs de contraseñas o hashes en el flujo nuevo.
- [x] Retirar la inicialización de `ConectionSQL` del arranque.
- [x] Añadir pruebas unitarias del hasher y pruebas de integración de autenticación sobre SQLite.
- [x] Validar la fase con `mvn clean test` (21 tests correctos).
- [x] Eliminar `IDandPasswords`, `Users` y la firma de login basada en el mapa legado.

## Fase 6 - Limpieza y validación final
- [x] Eliminar `ConectionSQL` y las consultas activas contra `ALUMNOS`, `PROFESORES` y `USUARIOS`.
- [x] Eliminar `GestionarTab`, `AlumnoTableModelOld` y pantallas experimentales obsoletas.
- [x] Simplificar `Main` y `WelcomePage` para retirar rutas legadas.
- [ ] Validar la limpieza con `mvn clean test`.
- [ ] Ejecutar `mvn clean package`.
- [ ] Completar configuración inicial MySQL/SQLite portable y arranque SQLite desde cero.
- [ ] Prueba manual de alta/modificación/baja de alumnos.
- [ ] Prueba manual de alta/modificación/baja de profesores.
- [ ] Prueba manual de asignaciones y persistencia tras reinicio.
- [ ] Prueba manual de login/registro.
- [x] Añadir comprobación automatizada del esquema SQLite esperado.

## Criterio de integración
La rama solo se propondrá para merge a `main` cuando el modelo, repositories, UI y pruebas estén adaptados, no queden consultas activas contra las tablas antiguas `ALUMNOS`, `PROFESORES` o `USUARIOS`, `mvn clean package` sea correcto y se hayan completado las pruebas manuales críticas.
