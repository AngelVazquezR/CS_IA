# Requisito funcional: RESOLVE_SERVER_ACTIVITIES

| Campo | Valor |
|---|---|
| Estado | Borrador funcional para validación |
| Fecha | 14 de septiembre de 2026 |
| Tecnología | Por definir |

## 1. Propósito

RESOLVE_SERVER_ACTIVITIES determina la lista ordenada de actividades aplicables a un servidor usando información enriquecida y parametrización externa. Cada actividad es el nombre de una SCA, por ejemplo: SCA - Linux Operating System Upgrade.

## 2. Alcance

Incluye validación de entradas, enriquecimiento del servidor, normalización del sistema operativo, selección de actividades, reglas para Windows y parcheo recursivo, ordenación, cálculo interno de CHECK_SCA y registro de errores.

No incluye la obtención de la lista inicial, la orquestación masiva, la resolución de URL, el carácter * como petición genérica ni la elección de tecnología. Para N servidores, un orquestador externo realizará N invocaciones independientes.

## 3. Convenciones

- Tablas paramétricas: prefijo PARAM_.
- Tablas de datos: prefijo TAB_.
- Si no se indica el tipo de una tabla, se considera de datos.

## 4. Interfaz

~~~text
RESOLVE_SERVER_ACTIVITIES(
    SERVER,
    SR_TYPE = vacío,
    SR_SUBTYPE = vacío
)

DEVUELVE:
    ACTIVITIES
    REQUIRES_REVIEW
~~~

| Entrada | Obligatoria | Descripción |
|---|---:|---|
| SERVER | Sí | Identificador del servidor. |
| SR_TYPE | No | Tipo de solicitud. |
| SR_SUBTYPE | No | Subtipo; exige SR_TYPE. |

| Salida | Tipo | Descripción |
|---|---|---|
| ACTIVITIES | Lista de texto | Nombres ordenados por Execution Order. |
| REQUIRES_REVIEW | Booleano | Verdadero si se necesita revisión manual. |

Una lista vacía con REQUIRES_REVIEW falso es un resultado válido sin actividades. Con REQUIRES_REVIEW verdadero indica error y revisión manual. CHECK_SCA se calcula internamente, pero no se devuelve ni filtra el resultado.

## 5. Tablas

### 5.1 TAB_SERVER_INFO

Servidor es clave única.

| Campo | Uso |
|---|---|
| Servidor | Clave de búsqueda. |
| Aplicación | Contexto; no interviene actualmente. |
| Entorno | Contexto; no interviene actualmente. |
| Sistema Operativo | Valor original para normalizar. |
| Clase | Contexto; no interviene actualmente. |
| Tag_Type | Parte de la tripleta base. |
| Role | Parte de la tripleta base. |
| Patching Security Group | Estado del parcheo recursivo. |

Sistema Operativo, Tag_Type y Role son obligatorios para resolver actividades.

### 5.2 PARAM_OS_NORMALIZED

| Campo | Uso |
|---|---|
| Operative System | Valor original y único. |
| OS | Identificador normalizado. |
| Type | Solo Windows o Linux. |

Ejemplos:

| Operative System | OS | Type |
|---|---|---|
| Microsoft Windows Server 2008 | win2k08 | Windows |
| Microsoft Windows Server 2012 R2 Standard | win2k12 | Windows |
| Microsoft Windows Server 2016 Datacenter | win2k16 | Windows |
| Microsoft Windows Server 2019 Datacenter | win2k19 | Windows |
| Microsoft Windows Server 2022 Datacenter | win2k22 | Windows |
| Microsoft Windows Server 2025 Datacenter | win2k25 | Windows |
| openSUSE Leap 15.3 | opensuse15.* | Linux |

opensuse15.* es literal, no un patrón.

### 5.3 PARAM_ACTIVITY_RELATION

| Campo | Uso |
|---|---|
| OS | Sistema operativo normalizado. |
| Tag_Type | Categoría. |
| Role | Rol. |
| SR Type | Tipo. |
| SR Subtype | Subtipo. |
| Activity | Nombre de SCA. |
| Execution Order | Prioridad numérica ascendente. |

Los tipos y pares válidos se obtienen dinámicamente de esta tabla. Varias actividades pueden compartir orden y serán intercambiables dentro de esa prioridad.

| Execution Order | SR Subtype |
|---:|---|
| 1 | CONVERSION |
| 2 | OS |
| 3 | RECURSIVE |
| 4 | DB |
| 5 | JAVA |

### 5.4 TAB_ACTIVITY_RESOLUTION_LOG

Inicialmente registra solo errores.

| Campo |
|---|
| Fecha y hora |
| Servidor |
| SR Type recibido |
| SR Subtype recibido |
| Etapa del proceso |
| Código de error, catálogo por definir |
| Descripción detallada |
| Datos relevantes |

Debe conservar toda la información razonablemente posible para facilitar la actuación manual.

### 5.5 PARAM_EXECUTION_ENDPOINT_RELATION

Proceso posterior fuera de alcance: SR Type, SR Subtype, Activity y URL. Se documentará separadamente.

## 6. Normalización

Antes de comparar texto:

1. Eliminar espacios iniciales y finales.
2. Convertir a minúsculas.
3. Tratar la cadena vacía como no informada.

Las coincidencias son exactas tras normalizar. No se admiten patrones.

## 7. Reglas funcionales

### RF-01. Servidor

SERVER es obligatorio. Debe existir exactamente una fila en TAB_SERVER_INFO. Cero o varias filas producen lista vacía, REQUIRES_REVIEW verdadero y log.

### RF-02. Enriquecimiento

La ausencia de Sistema Operativo, Tag_Type o Role produce error. Patching Security Group vacío es válido.

### RF-03. Sistema operativo

Buscar exactamente Sistema Operativo en PARAM_OS_NORMALIZED. Cero o varias filas producen error. Recuperar OS y Type. Un Type distinto de Windows o Linux produce error.

### RF-04. Filtros opcionales

| SR_TYPE | SR_SUBTYPE | Resultado |
|---|---|---|
| Vacío | Vacío | Todas las actividades aplicables. |
| Informado | Vacío | Todas las aplicables de ese tipo. |
| Informado | Informado | Las del par exacto. |
| Vacío | Informado | Error. |

Con solo SR_TYPE, el tipo debe existir globalmente. Con ambos, el par debe existir globalmente. La validación se realiza contra PARAM_ACTIVITY_RELATION.

### RF-05. Orden de resolución

1. Validar globalmente los filtros.
2. Buscar por OS normalizado + Tag_Type + Role.
3. Aplicar reglas de negocio.
4. Aplicar filtros opcionales.

Si la tripleta base no tiene filas, devolver lista vacía sin error. Un tipo o par válido globalmente pero no aplicable al servidor también devuelve lista vacía sin error.

### RF-06. Windows

Si Type es Windows, excluir todas las actividades con SR Type igual a OS. La exclusión no es error. Las filas OS parametrizadas para Windows se ignoran sin registrarse por ahora como inconsistencia. Sin filtros pueden devolverse actividades no OS.

### RF-07. Parcheo recursivo

| Patching Security Group | Estado |
|---|---|
| Vacío | No activo |
| 0 | No activo |
| Otro valor | Activo |

Si está activo, excluir OS / RECURSIVE sin error y conservar las demás actividades.

### RF-08. Integridad y orden

Activity debe estar informado y Execution Order debe ser numérico. Los nombres duplicados producen error; no se eliminan silenciosamente. Ordenar por Execution Order ascendente. Los empates son válidos.

### RF-09. CHECK_SCA

Se calcula sobre todas las actividades efectivamente aplicables antes de los filtros opcionales:

| Condición | CHECK_SCA |
|---|---|
| Windows | Upgrade/Patching |
| No Windows con actividades | Upgrade/Patching |
| No Windows sin actividades | Rebuild |

Es una regla provisional y no forma parte de la salida.

### RF-10. Error

Ante cualquier error, no devolver lista parcial:

~~~text
ACTIVITIES = []
REQUIRES_REVIEW = verdadero
~~~

Registrar el detalle en TAB_ACTIVITY_RESOLUTION_LOG. Un error de un servidor no afecta a otras invocaciones.

## 8. Secuencia lógica

1. Recibir y normalizar SERVER, SR_TYPE y SR_SUBTYPE.
2. Validar SERVER y la combinación de filtros.
3. Validar globalmente el tipo o par informado.
4. Recuperar exactamente un registro de TAB_SERVER_INFO.
5. Validar los datos mínimos.
6. Recuperar OS y Type desde PARAM_OS_NORMALIZED.
7. Buscar actividades por OS + Tag_Type + Role.
8. Excluir actividades OS para Windows.
9. Excluir OS / RECURSIVE si el parcheo recursivo está activo.
10. Calcular CHECK_SCA.
11. Aplicar filtros opcionales.
12. Validar Activity, Execution Order y duplicados.
13. Ordenar y devolver los nombres.
14. Ante error, registrar y devolver lista vacía con revisión manual.

## 9. Pseudocódigo

~~~text
FUNCIÓN RESOLVE_SERVER_ACTIVITIES(SERVER, SR_TYPE opcional, SR_SUBTYPE opcional)

    normalizar entradas
    SI SERVER vacío:
        error_y_revisión

    SI SR_TYPE vacío Y SR_SUBTYPE informado:
        error_y_revisión

    validar globalmente SR_TYPE o el par, si se informaron
    SI no son válidos:
        error_y_revisión

    filas_servidor ← buscar SERVER en TAB_SERVER_INFO
    SI cantidad ≠ 1:
        error_y_revisión

    validar Sistema Operativo, Tag_Type y Role
    normalización ← buscar Sistema Operativo en PARAM_OS_NORMALIZED
    SI cantidad ≠ 1 O Type no es Windows ni Linux:
        error_y_revisión

    aplicables ← PARAM_ACTIVITY_RELATION donde:
        OS = normalización.OS
        Y Tag_Type = servidor.Tag_Type
        Y Role = servidor.Role

    SI Type = Windows:
        excluir SR Type = OS

    SI Patching Security Group no está vacío Y es distinto de 0:
        excluir SR Type = OS Y SR Subtype = RECURSIVE

    SI Type = Windows O aplicables no está vacío:
        CHECK_SCA ← Upgrade/Patching
    SINO:
        CHECK_SCA ← Rebuild

    seleccionadas ← aplicables
    SI SR_TYPE informado:
        filtrar por SR Type
    SI SR_SUBTYPE informado:
        filtrar por SR Subtype

    SI Activity vacío, Execution Order inválido
       O Activity duplicado:
        error_y_revisión

    ordenar por Execution Order ascendente
    ACTIVITIES ← nombres de Activity
    DEVOLVER ACTIVITIES, FALSO

error_y_revisión:
    registrar detalle en TAB_ACTIVITY_RESOLUTION_LOG
    DEVOLVER [], VERDADERO
~~~

## 10. Criterios de aceptación

- SERVER vacío, inexistente o duplicado produce error y log.
- Datos enriquecidos mínimos ausentes producen error.
- Normalización ausente, duplicada o con Type inválido produce error.
- Sin filtros se devuelven todas las actividades efectivas.
- Solo SR_TYPE filtra por tipo; ambos parámetros filtran por el par.
- SR_SUBTYPE sin SR_TYPE produce error.
- Un tipo o par inexistente globalmente produce error.
- Un tipo o par válido pero no aplicable produce lista vacía sin error.
- Una tripleta sin filas produce lista vacía sin error.
- Windows no devuelve actividades OS.
- Parcheo recursivo activo excluye OS / RECURSIVE.
- Las actividades se ordenan por Execution Order; los empates son válidos.
- Activity vacío, orden inválido o duplicado produce error y log.
- CHECK_SCA se calcula antes de los filtros y no se devuelve.
- Los errores se aíslan por servidor.

## 11. Pendientes

- Elegir tecnología y arquitectura.
- Definir catálogo de códigos de error.
- Definir la obtención previa de servidores.
- Documentar el proceso posterior de URL.
- Valorar el carácter *.
- Revisar la regla definitiva de CHECK_SCA.
