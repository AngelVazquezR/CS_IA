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
- [x] Realizar prueba manual de las tres pantallas migradas.

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
- [x] Validar la limpieza con `mvn clean test` (21 tests correctos).
- [x] Completar configuración inicial MySQL/SQLite con selector de motor y valores JDBC predeterminados.
- [x] Permitir SQLite sin usuario/contraseña y generar URL desde el nombre del fichero añadiendo `.db`.
- [x] Crear automáticamente el directorio/fichero SQLite e inicializar el esquema v2 de forma idempotente.
- [x] Añadir pruebas automatizadas de configuración SQLite, creación desde cero y reapertura sin pérdida de datos.
- [x] Validar los nuevos tests con `mvn clean test` (25 tests, 0 fallos, 0 errores).
- [x] Ejecutar `mvn clean package` correctamente y generar el JAR sombreado.
- [x] Prueba manual de configuración inicial SQLite desde cero, creación de BD y alta obligatoria del primer usuario.
- [x] Prueba manual de login con el primer usuario creado.
- [x] Prueba manual de alta/modificación/baja de alumnos y detección de DNI duplicado.
- [x] Prueba manual de alta/modificación/baja de profesores y detección de DNI duplicado.
- [x] Prueba manual de asignaciones.
- [x] Prueba manual de persistencia de alumnos, profesores y asignaciones tras reinicio.
- [x] Añadir comprobación automatizada del esquema SQLite esperado.

## Evidencias de validación manual final
- Alta de profesor: correcta.
- Alta de alumno: correcta.
- Modificación de profesor: correcta.
- Modificación de alumno: correcta.
- Alta duplicada de profesor: rechazada como se esperaba.
- Alta duplicada de alumno: rechazada como se esperaba.
- Persistencia tras cierre y nuevo arranque: correcta.
- Alta de nuevos profesor y alumno tras reinicio: correcta.
- Eliminación de profesor existente: correcta.
- Eliminación de alumno existente: correcta.
- Segunda comprobación de persistencia tras reinicio: correcta.

## Criterio de integración
[x] Cumplido. El modelo, repositories, UI, autenticación y pruebas están adaptados; no quedan consultas activas contra `ALUMNOS`, `PROFESORES` o `USUARIOS`; `mvn clean package` es correcto y las pruebas manuales críticas han finalizado con el resultado esperado. La rama `feature/data-model-v2` queda preparada para revisión y fusión a `main`.
