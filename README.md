# Servidor MCP bancario — TdeA

## Integrantes

- Wilson Tapias Posada
- Juan Jose Montoya Hernandez
- Sofia Ospina Gomez
- Juan Sebastian Zapata Munera
- Santiago Patiño Gomez

Servidor MCP local de demostración para operaciones bancarias simuladas. Está construido con Spring Boot, Spring AI, Spring Data JPA y H2, y se conecta a Claude Desktop mediante Stdio.

> **Uso educativo:** no se conecta con entidades financieras ni mueve dinero real. No incluye autenticación y no debe exponerse como un servicio bancario de producción.

## Estado

- Implementación, pruebas automatizadas y documentación técnica completadas.
- El usuario confirmó pruebas manuales con Claude Desktop.
- Falta agregar las capturas de evidencia en `evidencias/` antes de entregar.
- La base H2 es temporal y se borra al cerrar el proceso del servidor.

## Requisitos

- Java 17 o superior
- Maven 3.6 o superior (se incluye Maven Wrapper)
- Claude Desktop para la integración final
- MCP Inspector opcional para depuración

Versiones configuradas: Spring Boot 3.5.3 y Spring AI 1.1.4.

## Funcionalidades

| Herramienta MCP | Función |
|---|---|
| `createAccount` | Crea una cuenta activa con saldo inicial cero. |
| `getBalance` | Consulta saldo y estado. |
| `deposit` | Consigna un monto positivo y registra el movimiento. |
| `transfer` | Transfiere entre cuentas activas y registra el movimiento atómicamente. |
| `requestCredit` | Registra una solicitud en estado `PENDING`; no aprueba ni desembolsa crédito. |
| `getTransactionHistory` | Consulta consignaciones y transferencias asociadas a una cuenta. |
| `getCreditRequests` | Consulta solicitudes de crédito asociadas a una cuenta. |

## Arquitectura

```text
Claude Desktop (host MCP)
        │ Stdio / JSON-RPC
        ▼
Spring AI MCP Server ── @Tool / ToolCallbackProvider
        │
        ▼
BankingService (@Transactional, validaciones, bloqueos)
        │ Spring Data JPA
        ▼
H2 en memoria: accounts, bank_transactions, credit_requests
```

Las transferencias bloquean las cuentas en orden estable, verifican saldo y estado, actualizan ambas cuentas y guardan el movimiento dentro de una transacción. Los montos se validan con `BigDecimal`, positivos y con hasta dos decimales. Las solicitudes de crédito se registran pendientes y no alteran el saldo.

## Base de datos

Configuración: `jdbc:h2:mem:bankdb`; Hibernate crea/actualiza el esquema a partir de las entidades JPA.

- `Account`: número único, titular, saldo y estado.
- `BankTransaction`: consignaciones/transferencias, cuentas relacionadas, monto, referencia y fecha.
- `CreditRequest`: cuenta solicitante, monto, motivo, estado y fecha.

H2 en memoria significa que los datos **se mantienen durante la vida del proceso** y se pierden al cerrar o reiniciar Claude Desktop/servidor. No hay consola H2 web en el modo Stdio; el servidor no escucha en el puerto 8080.

## Compilar, probar y generar JavaDoc

Ejecutar desde PowerShell en la raíz del repositorio:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
.\mvnw.cmd javadoc:javadoc
```

Las pruebas cubren arranque, conexión H2, persistencia y relaciones, consignaciones, transferencias, saldos, historiales, créditos, validaciones, rechazos por saldo insuficiente y registro de las siete herramientas MCP. El resultado de validación registrado para esta entrega fue **12 pruebas, 0 fallos, 0 errores**. El JAR queda en `target/proyect-mcp-0.0.1-SNAPSHOT.jar`; el JavaDoc generado queda en `target/reports/apidocs/index.html`.

## Configurar Claude Desktop en Windows

1. Compila el JAR con `mvnw.cmd package` (o el comando anterior).
2. En Claude Desktop abre **Configuración → Desarrollador → Editar configuración**. Usa el archivo que abre Claude; algunas instalaciones almacenan esta configuración en `%LOCALAPPDATA%\Claude-Data\...`, no en `%APPDATA%\Claude`.
3. Conserva las demás propiedades del archivo y agrega `mcpServers` al nivel raíz. Ajusta las rutas a las de tu equipo:

```json
{
  "mcpServers": {
    "tdea-banking": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\ruta\\absoluta\\project-mcp\\target\\proyect-mcp-0.0.1-SNAPSHOT.jar"
      ],
      "env": {
        "MCP_LOG_FILE": "C:/ruta/absoluta/project-mcp/target/proyect-mcp.log"
      }
    }
  }
}
```

`MCP_LOG_FILE` evita que el log relativo se resuelva desde `C:\Windows\System32`, que puede ser el directorio de trabajo de Claude. Si el archivo ya contiene preferencias de Claude, **no las reemplaces**: incorpora solo la propiedad `mcpServers` y conserva las demás.

4. Guarda el JSON, cierra Claude Desktop completamente (incluida la bandeja del sistema) y vuelve a abrirlo.
5. En **Configuración → Desarrollador → Servidores MCP locales**, confirma que `tdea-banking` esté **En ejecución**. En el chat, el botón `+` permite ver/activar conectores y herramientas disponibles.

No uses **Agregar conector personalizado** para este servidor: esa opción es para servidores MCP remotos con URL. Este proyecto corre localmente por Stdio.

## Secuencia de prueba manual

Ejecuta en una sola sesión, aprobando las llamadas a herramientas cuando Claude lo solicite:

1. “Crea una cuenta de demostración `30000001` a nombre de Ana Pérez.”
2. “Crea una cuenta de demostración `30000002` a nombre de Luis Díaz.”
3. “Consigna `1000.00` en la cuenta `30000001`, motivo prueba.”
4. “Transfiere `250.00` desde `30000001` hacia `30000002`, motivo prueba.”
5. Consulta saldos: deben ser **750.00** y **250.00** respectivamente.
6. Consulta historial para ambas cuentas: debe aparecer la consignación y transferencia.
7. Registra crédito por `5000.00` para `30000001`, motivo educativo; verifica que quede `PENDING` y no cambie el saldo.
8. Intenta transferir `900.00` desde `30000001`; debe rechazarse por saldo insuficiente y no alterar ninguno de los saldos.

Las cantidades son unidades de demostración, no una moneda real.

## MCP Inspector (alternativa de depuración)

Para abrir su interfaz web en Windows:

```powershell
npx.cmd --yes @modelcontextprotocol/inspector java -jar "C:\ruta\absoluta\project-mcp\target\proyect-mcp-0.0.1-SNAPSHOT.jar"
```

Usa **STDIO**, conecta y revisa la pestaña **Tools**. Mantén la misma sesión abierta para conservar los datos H2. Inspector valida el servidor MCP, pero no sustituye la evidencia de Claude Desktop si la entrega la solicita explícitamente.

## Registros y auditoría

La aplicación envía logs técnicos a `stderr` y al archivo configurado por `MCP_LOG_FILE` (en las instrucciones, `target/proyect-mcp.log`). Los movimientos financieros y solicitudes quedan registrados en H2 durante la ejecución del proceso. El archivo de log cubre eventos técnicos del servidor; el historial funcional de consignaciones y transferencias se consulta con `getTransactionHistory`.

## Evidencias de Claude Desktop

Pega las capturas en `evidencias/` y usa estos nombres para enlazarlas en el informe:

- `01-servidor-en-ejecucion.png` — servidor MCP en estado **En ejecución**.
- `02-consignacion.png` — resultado y referencia de la consignación.
- `03-transferencia.png` — transferencia invocada mediante la integración MCP.
- `04-saldos.png` — saldo destino y conciliación.
- `05-historial.png` — movimientos y referencias.
- `06-solicitud-credito.png` — solicitud `PENDING`.
- `07-rechazo-saldo-insuficiente.png` — operación rechazada y saldos conservados.

## Archivos de documentación

- `INFORME_LABORATORIO_MCP.md`: informe técnico y espacios para evidencias.
- `REQUISITOS_LABORATORIO_MCP.md`: análisis de requisitos del PDF y estado de cumplimiento.
