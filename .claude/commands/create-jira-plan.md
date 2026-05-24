---
description: Draft and review an implementation plan for a Jira task before saving
argument-hint: <JIRA-TASK-ID>
allowed-tools: Read, Grep, Glob, Bash, Write, WebFetch
---

## Task

Draft a complete implementation plan for the Jira task **$1**, review it with the
user, and only then save it to `.claude/plans/plan-$1.md`.

If `$1` is empty, ask the user for the Jira task ID and stop.

**Idioma:** redacta el documento del plan y todas las respuestas al usuario en
español.

## Steps

1. **Read project context.** Read `AGENTS.md` to understand the architecture
   (api/ OpenAPI contract, backend/ Kotlin + Spring Modulith, frontend/ Angular 21).

2. **Fetch the Jira task.** Retrieve the issue **$1** from Jira using the
   available Atlassian integration (`getJiraIssue`). Capture: summary,
   description, type, status, acceptance criteria, and any linked issues or
   comments with technical detail. If the task cannot be fetched, report the
   error and stop — do not invent requirements.

3. **Explore the affected code.** Based on the task scope, locate the concrete
   files and symbols that will need to change across the three projects:
   - `api/openapi-rest.yaml` and its referenced YAML files (contract changes)
   - `backend/` — the relevant bounded module(s): `auth`, `users`, `groups`,
     `security`, `shared`, etc. Identify use cases, domain aggregates, controllers,
     mappers, persistence, and any needed Flyway migration.
   - `frontend/` — the relevant `features/`, `core/services`, guards,
     interceptors, i18n files (`en`, `es`, `cat`).
   Note whether OpenAPI regeneration is required on either side.

4. **Present the draft for review — do NOT write the file yet.** Show the full
   plan (using the structure below) inline in the chat. Then ask the user:
   "¿Quieres ajustar algo del plan o lo guardo así?" Be explicit that nothing
   has been saved yet.

5. **Iterate on feedback.** If the user gives comments or corrections,
   incorporate them and present the revised plan again. Repeat this loop as many
   times as needed. Do not proceed until the user explicitly approves.

6. **Save the approved plan.** Once the user approves, write the final plan to
   `.claude/plans/plan-$1.md`, then report the saved path and a 2-3 line summary.

Steps must be concrete: reference real file paths and follow the existing
project patterns (hexagonal modules, `open/` facades, generated OpenAPI
interfaces, `record-` selector prefix, path aliases).

## Plan document structure

```markdown
# Plan: $1 — <resumen de la tarea>

## Tarea de Jira
- **ID:** $1
- **Tipo:** <tipo>
- **Estado:** <estado>
- **Resumen:** <resumen>

## Descripción
<descripción de Jira>

## Criterios de aceptación
- <criterios>

## Análisis de alcance
- **Contrato API:** <cambios necesarios, o "ninguno">
- **Módulos de backend afectados:** <módulos + por qué>
- **Features de frontend afectadas:** <features + por qué>
- **Regeneración OpenAPI:** <backend / frontend / ambos / ninguno>

## Pasos de implementación
1. <paso concreto y ordenado — incluye rutas de archivo>
   ...

## Testing
- <tests de backend a añadir/actualizar — `./gradlew test`>
- <tests de frontend a añadir/actualizar — `pnpm test`>

## Riesgos y preguntas abiertas
- <cualquier ambigüedad en la tarea de Jira o algo que requiera confirmación>
```
