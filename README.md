# Nexora API

**Nexora** es una API REST diseñada para que profesionales independientes y pequeños negocios puedan gestionar clientes, servicios, turnos presenciales y online, y el seguimiento de pagos con comprobantes.

El proyecto busca resolver necesidades reales de gestión mediante reglas de negocio claras, validaciones y trazabilidad de operaciones.

## Objetivo del proyecto

Desarrollar una aplicación backend que centralice la gestión de clientes, servicios, turnos y pagos, priorizando la consistencia de los datos, la organización del código y la aplicación de reglas de negocio.

## Funcionalidades implementadas

- Gestión de clientes (CRUD).
- Gestión de servicios (CRUD).
- Consulta de clientes y servicios individuales.
- Actualización y desactivación de registros.
- Validación de datos de entrada.
- Prevención de emails duplicados.
- Manejo centralizado de excepciones.
- Respuestas de error consistentes mediante códigos HTTP.
- Persistencia de datos en PostgreSQL.
- Versionado de base de datos con Flyway.

## Funcionalidades pendientes

- Gestión de turnos presenciales y online.
- Cancelación y reprogramación de turnos.
- Prevención de superposición de horarios.
- Trazabilidad del historial de reprogramaciones.
- Registro y seguimiento de pagos.
- Carga y revisión de comprobantes.
- Plazo de 48 horas para realizar los pagos.
- Historial de operaciones.

## Reglas de negocio

El sistema contempla las siguientes reglas:

- No permitir turnos superpuestos para un mismo profesional.
- Establecer un plazo de 48 horas para realizar los pagos.
- Mantener la trazabilidad de las reprogramaciones.
- Conservar el historial de pagos.
- Validar que los clientes y servicios estén activos al crear un turno.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven
- Git y GitHub
- JUnit

## Arquitectura del proyecto

La aplicación utiliza una arquitectura por capas para separar responsabilidades:

- `controller`: endpoints REST y recepción de solicitudes.
- `service`: lógica de negocio.
- `repository`: acceso y persistencia de datos.
- `entity`: entidades del modelo de datos.
- `dto`: objetos para transferencia y validación de datos.
- `exception`: manejo centralizado de excepciones.
- `resources/db/migration`: scripts de migración de base de datos.

## Estado del proyecto

**En desarrollo.**

Los módulos de clientes y servicios están implementados y fueron probados mediante Postman.

La próxima etapa contempla el desarrollo del módulo de turnos y sus correspondientes reglas de negocio.

## Hoja de ruta

1. Gestión de clientes y servicios — completada.
2. Gestión de turnos — pendiente.
3. Validaciones y reglas de negocio de turnos — pendiente.
4. Gestión de pagos y comprobantes — pendiente.
5. Pruebas automatizadas y documentación de la API — pendiente.
6. Configuración y despliegue — pendiente.

---

*Proyecto personal de aprendizaje y portfolio profesional orientado al desarrollo Backend Java.*