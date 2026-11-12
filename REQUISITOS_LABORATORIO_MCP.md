# Requisitos del laboratorio: servidor MCP para servicios bancarios

**Fuente:** `Guía de LaboratorioMCPTDEA.pdf` (TdeA, 2026)
**Fecha de entrega indicada:** 15 de noviembre de 2026
**Estado de este documento:** requisitos y estado de cumplimiento del proyecto.

## 1. Propósito del entregable

Desarrollar un servidor MCP con Spring Boot que se conecte a una base de datos y permita ejecutar transacciones de servicios bancarios. La guía propone como ejemplos transferencias entre cuentas, solicitudes de crédito y consignaciones. El servidor debe integrarse con Claude Desktop para que el usuario solicite las operaciones en lenguaje natural.

El laboratorio inicial de la guía usa una lista de compras solo para explicar Spring AI, herramientas MCP y la conexión con Claude. **La lista de compras no es el dominio final del ejercicio.**

## 2. Requisitos obligatorios extraídos

- [x] Implementar un servidor MCP con **Spring Boot** y **Spring AI MCP**.
- [x] Conectar el servidor a una base de datos. Se eligió H2 en memoria para la demostración local.
- [x] Exponer herramientas MCP para operaciones de servicios bancarios y generar transacciones. Las operaciones incluidas son:
  - transferencia de dinero entre cuentas;
  - creación de solicitudes de crédito;
  - consignación en cuentas.
- [x] Registrar siete herramientas para que un cliente MCP pueda descubrirlas e invocarlas.
- [x] Conectar el servidor con **Claude Desktop** en el entorno de entrega.
- [x] Verificar manualmente la integración desde Claude Desktop.
- [ ] Entregar el proyecto a más tardar el **15 de noviembre de 2026**.

## 3. Requisitos técnicos de referencia de la guía

La configuración base del laboratorio y los apéndices indican:

| Componente | Referencia de la guía |
|---|---|
| Java | 17 o superior |
| Maven | 3.6 o superior |
| Spring Boot | 3.5.3 |
| Spring AI | 1.1.4 recomendado en este proyecto para alinearse con Spring Boot 3.5.x |
| Transporte para Claude Desktop | Stdio local |
| Cliente/host MCP | Claude Desktop actualizado |

La guía también incluye un procedimiento alternativo de prueba mediante **Streamable HTTP** y Postman/MCP Inspector. Para esa variante indica Spring AI 1.1.4, `spring-ai-starter-mcp-server-webmvc`, endpoint `/mcp` y puerto 8080; especifica no agregar simultáneamente los dos starters de servidor. Esta sección es una adaptación de transporte para pruebas, no reemplaza el requisito de integrar el servidor con Claude Desktop.

## 4. Arquitectura y comportamiento esperados

Flujo esperado:

1. El usuario pide una operación bancaria desde Claude Desktop.
2. Claude selecciona una herramienta MCP según su nombre, descripción y parámetros.
3. El cliente de Claude invoca la herramienta en el servidor MCP, mediante el transporte configurado.
4. El servidor valida la solicitud y ejecuta la operación usando la base de datos.
5. La herramienta devuelve un resultado que Claude presenta al usuario.

Responsabilidades mínimas:

- **Claude Desktop:** host/interfaz desde la cual se invocan las herramientas.
- **Servidor MCP Spring AI:** publica herramientas con `@Tool` y sus descripciones/parámetros.
- **Servicios de dominio:** aplican las reglas de las operaciones bancarias.
- **Persistencia:** almacena las entidades y los resultados de las transacciones en la base de datos elegida.

Los nombres y descripciones de herramientas deben comunicar claramente qué operación realizan y qué datos requieren. El resultado debe permitir confirmar el éxito o explicar el rechazo de la operación.

## 5. Criterios de aceptación propuestos

Estos criterios concretan cómo verificar los requisitos; la guía no prescribe herramientas o entidades con nombres específicos.

- [x] El proyecto compila y arranca con la configuración documentada.
- [x] La conexión a la base de datos funciona y las operaciones persisten durante la ejecución del servidor (H2 en memoria se reinicia al cerrar el proceso).
- [x] Claude Desktop reconoce el servidor y lista las herramientas MCP.
- [x] Se prueba una operación bancaria de extremo a extremo, desde Claude hasta la base de datos.
- [x] Se comprueban operaciones exitosas y casos inválidos/rechazados, incluido saldo insuficiente.
- [x] Los resultados de las herramientas reflejan el estado real de la operación y no confirman una operación fallida.
- [x] Se documentan la configuración, la ejecución, la conexión desde Claude Desktop y las pruebas.

## 6. Decisiones que la guía deja abiertas

Deben definirse antes o durante el diseño; no son valores impuestos por el PDF:

1. Motor de base de datos y estrategia de configuración de credenciales. **Decisión tomada:** H2 en memoria para desarrollo y demostración local; los datos se reinician al cerrar la aplicación.
2. Operaciones bancarias que tendrá el alcance mínimo del proyecto.
3. Entidades, relaciones, reglas de negocio y estados de las transacciones.
4. Política de identificación/autorización del usuario y de las cuentas.
5. Transporte final: Stdio es el flujo explicado para Claude Desktop; HTTP se describe como alternativa de prueba.
6. Evidencias de entrega (capturas, pruebas automatizadas, manual de ejecución, etc.).

Por tratarse de operaciones bancarias, conviene diseñar con validación estricta, montos decimales (`BigDecimal` en Java), consistencia/transacciones de base de datos, controles de autorización y registro de operaciones. Son recomendaciones de diseño seguro, no detalles especificados por la guía.

## 7. Inconsistencias y precauciones detectadas en el PDF

- El cuerpo recomienda Spring Boot 3.5.3 y Spring AI 1.0.0, mientras que la sección opcional de HTTP usa Spring AI 1.1.4. La documentación de Spring AI 1.0.0 no declara soporte para Boot 3.5.x; la línea 1.1.x sí declara compatibilidad con Boot 3.5.x. Por eso el `pom.xml` usa Spring AI 1.1.4, manteniendo el starter Stdio y sin agregar el starter WebMVC.
- La guía enseña primero una lista de compras en memoria, pero el ejercicio final exige **base de datos y dominio bancario**. No basta con reproducir literalmente `ShoppingCart`.
- Hay referencias cruzadas de apéndices que no coinciden en algunos pasos; el código y la sección de apéndices deben leerse directamente.
- El ejemplo de ruta de logs contiene carpetas de un servidor meteorológico (`weather/starter-stdio-server`); es una ruta de ejemplo copiada y no debe adoptarse como ruta del proyecto sin revisarla.
- Los ejemplos de paquetes y nombres de proyecto varían (`com.tdea`, `com.udea`, nombres de artefacto distintos). Deben alinearse con el proyecto real.
- La dirección HTTP `/mcp` corresponde a la variante Streamable HTTP; no debe confundirse con la comunicación Stdio local.

## 8. Estado actual de implementación (validación)

La revisión inicial encontró un esqueleto sin MCP ni persistencia. En el primer hito ya se actualizó la configuración:

- `pom.xml` usa Spring Boot **3.5.3**, Java 17 y Spring AI **1.1.4**, con el starter MCP Stdio, Spring Data JPA y H2.
- `application.properties` configura H2 en memoria y JPA.
- Hay una prueba automatizada de conexión a la base de datos.
- Se modelaron `Account`, `BankTransaction` y `CreditRequest`, sus estados y repositorios JPA.
- Las pruebas de persistencia verifican cuentas, referencias de movimientos y solicitudes asociadas a una cuenta.
- `ProyectMcpApplication` inicia Spring Boot y registra las herramientas MCP mediante `ToolCallbackProvider`.
- Se implementaron reglas transaccionales para consignaciones y transferencias, además de registro y consulta de solicitudes de crédito.
- Se registraron siete herramientas MCP con `ToolCallbackProvider`; pruebas automatizadas e Inspector verificaron su publicación y ejecución.
- Se agregó JavaDoc y un `README.md` con configuración, ejecución y escenarios manuales para Claude Desktop.
- El usuario confirmó la conexión y pruebas manuales desde Claude Desktop; falta adjuntar las capturas de evidencia al informe.
- `MCP_LOG_FILE` permite escribir el archivo de logs en una ruta absoluta, evitando que Claude resuelva el log relativo desde `C:\Windows\System32`.

La implementación funcional y la integración manual con Claude Desktop fueron validadas. Antes de entregar, agregar las capturas en `evidencias/` y revisar que el JAR y las instrucciones sean los definitivos.

## 9. Alcance para la siguiente etapa

El siguiente paso de entrega es completar las evidencias visuales y empaquetar el código, los documentos y los resultados de pruebas.
