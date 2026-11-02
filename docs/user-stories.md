# Nexora API — User Stories

## 1. Gestión de clientes

**US-01 — Registrar clientes**

Como profesional, quiero registrar clientes para mantener organizada su información y poder asociarlos a sus turnos.

**US-02 — Consultar y actualizar clientes**

Como profesional, quiero consultar y modificar los datos de mis clientes para mantener la información actualizada.

**US-03 — Desactivar clientes**

Como profesional, quiero desactivar clientes sin eliminar su historial para conservar el registro de sus operaciones anteriores.

## 2. Gestión de servicios

**US-04 — Administrar servicios**

Como profesional, quiero crear, consultar, actualizar y desactivar servicios para mantener mi oferta organizada, incluyendo su duración y precio.

## 3. Gestión de turnos

**US-05 — Crear turnos**

Como profesional, quiero asignar turnos a mis clientes según los servicios disponibles, su modalidad y los horarios libres.

**US-06 — Evitar superposición de turnos**

Como profesional, quiero que el sistema impida reservar dos turnos en el mismo horario para evitar conflictos en mi agenda.

**US-07 — Cancelar y reprogramar turnos**

Como profesional, quiero cancelar o reprogramar turnos y conservar su historial para mantener la trazabilidad de los cambios.

## 4. Gestión de pagos

**US-08 — Registrar pagos y comprobantes**

Como profesional, quiero registrar pagos asociados a los turnos y recibir comprobantes para controlar las operaciones.

**US-09 — Revisar pagos**

Como profesional, quiero aprobar o rechazar comprobantes de pago para mantener actualizado el estado de cada operación.

## 5. Reglas generales

- Los turnos pendientes de pago tienen un plazo de 48 horas para completar el pago.
- Los clientes y servicios desactivados no pueden utilizarse para crear nuevos turnos.
- El historial de turnos y pagos debe conservarse.
- Cada operación debe respetar las reglas de negocio definidas por el sistema.
