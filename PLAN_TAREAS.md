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

En el entorno de implementación Maven Central no era accesible, por lo que
la suite Maven completa no pudo resolver sus plugins. Sí se verificaron la
compilación con JDK 21 y las operaciones nuevas sobre SQLite real.
