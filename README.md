# Nexora API

**Nexora** es una API REST para que profesionales independientes y pequeños negocios gestionen clientes, servicios, turnos presenciales y online, y el seguimiento de pagos con comprobante.

El proyecto busca resolver necesidades reales de gestión mediante reglas de negocio claras, validaciones y trazabilidad de operaciones.

## Objetivo del proyecto

Desarrollar una API backend que permita centralizar la gestión de clientes, servicios, turnos y pagos, priorizando la consistencia de los datos y la organización de la lógica de negocio.

## Funcionalidades principales

- Gestión de clientes.
- Gestión de servicios.
- Creación y consulta de turnos.
- Cancelación y reprogramación de turnos.
- Control de superposición de horarios.
- Registro y seguimiento de pagos.
- Carga y revisión de comprobantes de pago.
- Historial de operaciones.

## Reglas de negocio destacadas

- No permitir turnos superpuestos para un mismo profesional.
- Establecer un plazo de 48 horas para realizar el pago.
- Mantener la trazabilidad de las reprogramaciones.
- Conservar el historial de pagos.
- Validar que los clientes y servicios estén activos.

## Tecnologías previstas

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Git y GitHub
- JUnit y Mockito
- Swagger / OpenAPI

## Estado del proyecto

En etapa de planificación y diseño técnico. La implementación se realizará progresivamente, comenzando por el modelo de datos y las reglas de negocio.

## Próximas etapas

1. Diseño del modelo de datos.
2. Definición de entidades y relaciones.
3. Implementación de la lógica de negocio.
4. Desarrollo de endpoints REST.
5. Persistencia con PostgreSQL.
6. Pruebas y documentación de la API.

---
*Proyecto personal de aprendizaje y portfolio Backend Java.*
