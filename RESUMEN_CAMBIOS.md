# Resumen de cambios para revisión

## Objetivo

Continuar la adaptación a SQLite reduciendo el SQL acoplado a la fachada
`ConectionSQL`, sin modificar las firmas usadas por la interfaz Swing.

## Situación de las ramas

El 11 de agosto de 2026 se actualizó la información remota y se comprobó que:

- `main` apunta a `d6794a44b206484f026b90e2b1bb4d49858ad9f0`, del 9 de agosto.
- `feature/sqlite-support` apunta a
  `0e054f4f0f5ac1d88cdd20cc0c47622c6eb6f2d7`.
- La rama de SQLite está 47 commits por delante y 0 por detrás de `main`.
- No había un commit nuevo en `main` ni un merge que realizar.

La implementación se hizo en `agent/sqlite-safe-improvements`, creada desde
`feature/sqlite-support`.

## Cambios funcionales

### Configuración inicial

- El diálogo muestra primero un selector entre MySQL y SQLite.
- Al cambiar de motor se rellenan el driver y la URL JDBC recomendados.
- SQLite se guarda ahora realmente con `tipo=sqlite`; antes el diálogo
  construía siempre la configuración como MySQL.
- Para SQLite se deshabilitan y vacían la base de datos, el usuario y la
  contraseña, ya que la ruta del fichero forma parte de la URL JDBC.
- Para MySQL se mantienen como obligatorios la base de datos y el usuario.
- Los errores de validación ya no reconstruyen el diálogo ni borran los datos
  introducidos.

### Profesores

- Nuevo `ProfesorRepository` para listar, crear, modificar, eliminar y
  comprobar DNI.
- Consultas parametrizadas compatibles con MySQL y SQLite.
- Generación de ID basada en el máximo existente para evitar colisiones
  después de borrar registros.

### Alumnos

- Nuevo `AlumnoRepository` para listar, crear, modificar, eliminar, asignar
  profesor y comprobar DNI.
- Consultas parametrizadas compatibles con MySQL y SQLite.
- Generación de ID con la misma protección frente a colisiones.

### Fachada existente

- `ConectionSQL` conserva sus métodos públicos para no obligar a cambiar las
  ventanas Swing.
- Las operaciones de alumnos y profesores se delegan ahora en los nuevos
  repositorios.
- `existeDNI` deja de consultar las tablas inexistentes `STUDENTS` y
  `TEACHERS`; usa `ALUMNOS` y `PROFESORES` mediante parámetros JDBC.

## Pruebas añadidas

- `ConfiguracionInicialPanelTest`: selección de motor, valores JDBC
  predeterminados, campos habilitados, validación condicional y construcción
  de configuraciones MySQL/SQLite.
- `ProfesorRepositoryTest`: CRUD, continuidad de IDs e inyección SQL.
- `AlumnoRepositoryTest`: CRUD, asignación, continuidad de IDs e inyección
  SQL.

## Validación realizada

- `git diff --check`: correcto.
- Compilación de todo `src/main/java` con JDK 21: correcta.
- Prueba automatizada *headless* del formulario MySQL/SQLite: correcta.
- Prueba funcional con el driver SQLite 3.53.2.1: correcta.
- Se verificaron creación automática del esquema, CRUD de ambos repositorios,
  asignación profesor–alumno, IDs e intentos de inyección.
- Se compilaron y ejecutaron los 47 casos JUnit: 47 correctos y 0 fallos.
  Son 40 anteriores y 7 nuevos para esta mejora de configuración.

La ejecución mediante `mvn test` quedó bloqueada porque el resolvedor de Maven
no pudo acceder a Maven Central por DNS. Las mismas fuentes se compilaron y
probaron correctamente mediante JDK 21 y JUnit Platform. El revisor debe
ejecutar `mvn clean test` y `mvn package` con JDK 21 antes de fusionar.

## Riesgos no incluidos

Se evitó modificar la interfaz Swing, los modelos de tabla, el estado estático
global y el formato de contraseñas. Son áreas con impacto transversal que
requieren tareas y pruebas específicas.
