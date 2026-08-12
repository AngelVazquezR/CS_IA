# Plan de tareas — soporte SQLite

Actualizado el 11 de agosto de 2026 sobre
`agent/sqlite-safe-improvements`, creada desde `feature/sqlite-support`.

## Estado de integración

- [x] Comprobar el estado remoto de `main`.
- [x] Comparar `main` con `feature/sqlite-support`.
- [x] Confirmar que no hay cambios pendientes de integrar: la rama de
  SQLite está 47 commits por delante y 0 por detrás.
- [x] Crear una rama nueva para continuar el trabajo sin modificar
  directamente `feature/sqlite-support`.

## Completado en etapas anteriores

- [x] Externalizar la configuración y excluir credenciales reales.
- [x] Incorporar una plantilla de configuración.
- [x] Centralizar la creación de conexiones JDBC.
- [x] Definir y validar el esquema equivalente de SQLite.
- [x] Inicializar y comprobar automáticamente la base SQLite.
- [x] Configurar claves externas e integridad al abrir SQLite.
- [x] Extraer y probar el repositorio de usuarios.

## Completado en esta rama

### Mejora: configuración inicial según el motor

- [x] Añadir como primera opción un selector entre MySQL y SQLite.
- [x] Rellenar automáticamente el driver y la URL predeterminados al cambiar
  de motor.
- [x] Generar `ConfigDB` con el tipo seleccionado en vez de forzar MySQL.
- [x] Hacer opcionales y deshabilitar usuario y contraseña cuando se
  selecciona SQLite.
- [x] Permitir elegir el nombre del fichero SQLite, añadir `.db` cuando no
  se indique y construir automáticamente la URL `jdbc:sqlite:data/<nombre>.db`.
- [x] Mantener la URL SQLite como campo calculado de solo lectura para evitar
  que el nombre y la ruta JDBC queden desincronizados.
- [x] Validar que el nombre SQLite sea obligatorio y no contenga rutas ni
  caracteres de fichero no válidos.
- [x] Mantener para MySQL la validación de base de datos y usuario.
- [x] Conservar los datos del formulario cuando falla una validación.
- [x] Añadir pruebas automatizadas del selector, valores predeterminados,
  nombre del fichero, URL generada, validación condicional y creación de la
  configuración.
- [ ] Validar manualmente el diálogo en Windows/Eclipse y el primer arranque
  del JAR con una base SQLite nueva.

### Repositorios JDBC

- [x] Extraer las operaciones de profesores a `ProfesorRepository`.
- [x] Extraer las operaciones de alumnos a `AlumnoRepository`.
- [x] Sustituir concatenaciones SQL por `PreparedStatement` en ambos
  dominios.
- [x] Corregir la comprobación de DNI para usar `PROFESORES` y `ALUMNOS`.
- [x] Evitar la reutilización o colisión de IDs después de eliminar filas.
- [x] Mantener las firmas públicas de `ConectionSQL` usadas por Swing.
- [x] Añadir pruebas SQLite de CRUD, asignación e intentos de inyección.
- [x] Compilar el código principal con JDK 21.
- [x] Ejecutar una prueba funcional de repositorios contra SQLite real.

## Siguientes tareas recomendadas

### Riesgo medio

- [ ] Hacer que la asignación profesor–alumno use un identificador único
  (ID o DNI) en vez del nombre, porque pueden existir nombres repetidos.
- [ ] Devolver resultados de operación a la UI en vez de limitarse a
  imprimir excepciones, para poder informar de errores al usuario.
- [ ] Añadir restricciones `NOT NULL` y unicidad para DNI mediante una
  migración versionada y compatible con bases existentes.
- [ ] Añadir pruebas de integración MySQL en un entorno aislado.

### Riesgo alto — pospuesto

- [ ] Eliminar el estado estático y las conexiones globales de
  `ConectionSQL`; requiere revisar todas las ventanas y su ciclo de vida.
- [ ] Rediseñar los modelos `Persona`, `Profesor` y `Alumno`; actualmente
  forman parte del contrato de los modelos de tabla Swing.
- [ ] Corregir en bloque el flujo de altas, modificaciones y borrados de la
  interfaz; debe abordarse con pruebas de UI para evitar regresiones.
- [ ] Migrar el almacenamiento de contraseñas a un algoritmo de hash moderno;
  exige estrategia compatible con usuarios ya creados.

## Validación pendiente del entorno de revisión

Ejecutar desde `CS_IA/` con JDK 21 y Maven 3.9:

```bash
mvn clean test
mvn package
```

En una validación anterior se compilaron el código principal y los tests con
JDK 21 y se ejecutaron 47 casos con JUnit Platform: 47 correctos, 0 fallos.
Esta ampliación añade 3 casos, por lo que quedan declarados 50. Deben
ejecutarse con Maven/JDK 21 en el entorno de revisión, ya que el entorno actual
no dispone de JDK 21 ni Maven. Como comprobación parcial, el formulario y sus
dependencias se compilaron con el compilador interno de Java 17 y el *smoke
test* `headless` de los nuevos recorridos terminó correctamente.
