# RMBT Statistics Server — Architecture 


>
> If you have read `architecture.md` in **open-rmbt-control**, this is its companion: the Control
> Server is the *write* side (it records measurements); the Statistics Server is the *read* side
> (it queries, aggregates, and exports them). They share the same PostgreSQL database.

---

## 1. What this server is

**RMBT** ("RTR Multithreaded Broadband Test") is the engine behind **RTR-Netztest**, the public
internet-quality measurement service of RTR-GmbH. End users run broadband measurements; the
**Control Server** stores each one as a `test` row (plus sub-tables for pings, speeds,
geolocation, signals, QoS, …).

The **Statistics Server** is the public, read-oriented face of that data. It does four things:

1. **Open-test search & detail** — query the corpus of measurements with rich filters and return
   results as JSON (the "recent tests" / open-data browser), and render a single result by uuid.
2. **Statistics & aggregates** — country/operator/technology statistics, histograms, intraday
   curves, choice lists for filter UIs.
3. **Exports** — bulk **open-data** dumps (CSV / XLSX / ZIP), per-result **PDF** certificates, and
   **PNG share images** (social-media thumbnails, forum banners).
4. **Coverage / misc** — coverage lookups, version, an admin usage report.

Crucially:

- **It is read-only.** It does **not** own or migrate the schema (no Flyway) and does not
  register measurements. It reads the database that the Control Server populates.
- **It is a public API with no authentication.** Unlike the Control Server, there is **no Spring
  Security** here — these endpoints serve open data and public result pages. The only inbound
  policy is **CORS**. (This makes the SQL-safety discipline in §5 non-negotiable.)

Where this server sits among the RMBT components:

| Server | Repo | Role |
|--------|------|------|
| Control Server | `open-rmbt-control` | Writes: registers tests, stores results. |
| **Statistics Server** | `open-rmbt-statistics` (this repo) | **Reads: search, statistics, exports.** |
| Map Server | `open-rmbt-map` | Public map / tiles of aggregated results. |
| QoS Server | `open-rmbt-qos` | Runs active QoS probes. |
| Measurement Server(s) | (separate) | The peers clients exchange bytes with. |

---

## 2. Technology stack

- **Language / runtime:** Java 17.
- **Framework:** Spring Boot 3.x (Spring MVC, Spring Data JPA, Spring Cache). **No Spring
  Security.**
- **Packaging / hosting:** a **WAR** (`<packaging>war</packaging>`) deployed into external
  **Tomcat 10**. `StatisticServerApplication` extends `SpringBootServletInitializer`; it also has a
  `main()` so you can run/debug it standalone from the IDE.
- **Database:** PostgreSQL + **PostGIS** (read access to the shared RMBT DB). Hibernate 6 via Spring
  Data JPA, plus a lot of **hand-written native SQL** in `repository/impl` for the analytical
  queries. **hibernate-spatial** for geometry. No Flyway — the schema is owned by the Control
  Server side.
- **Caching:** **Redis** (`spring-data-redis` + Jedis) behind Spring Cache (`@Cacheable`). Expensive
  statistics/search results are cached with a short TTL.
- **Templating / exports:**
  - **Handlebars** (`com.github.jknack`) renders HTML for PDF documents.
  - An external HTML→PDF converter (**Prince** or **WeasyPrint**) turns that HTML into a PDF.
  - **Jackson CSV** + **xlsx-lite** produce open-data CSV/XLSX; results are zipped for download.
  - Custom Java2D image generators produce PNG share images.
- **Other:** MapStruct (DTO mapping), Guava, commons-lang3/io, geojson-jackson, **JavaMelody**
  (monitoring), Thymeleaf, logstash-logback-encoder (structured logging).

One-line mental model: **a read-only analytical/reporting API over the RMBT database, fronted by a
Redis cache, with CSV/XLSX/PDF/PNG export side-channels.**

---

## 3. High-level architecture

Same layered shape as the Control Server, but the "write a domain object" flow is replaced by
"build a query, aggregate, render/export".

```
                         HTTP (JSON / CSV / XLSX / PDF / PNG)
                                     │
            ┌─────────────────────────────────────────────────┐
            │  Servlet filters: ApiLoggingFilter, JavaMelody  │   (no security filter chain)
            │  CORS                                           │
            └─────────────────────────────────────────────────┘
                                     │
                      ┌──────────────────────────────┐
                      │   Controllers (controller/)  │  thin; bind URL → method
                      └──────────────────────────────┘
                                     │
              ┌────────────────────────────────────────────┐
              │  Services (service/, service/impl/)        │  query building, aggregation,
              │  + export services (service/export/…)      │  classification, caching
              └────────────────────────────────────────────┘
                    │                │                 │
          ┌──────────────┐  ┌────────────────┐  ┌────────────────────┐
          │ QueryParser  │  │ Repositories   │  │ Exporters /        │
          │ (safe SQL)   │  │ (repo + impl,  │  │ renderers          │
          │              │  │ native SQL)    │  │ (CSV/XLSX/PDF/PNG) │
          └──────────────┘  └────────────────┘  └────────────────────┘
                    │                │                 │
                    └────────► PostgreSQL + PostGIS (read)   Redis (cache)   filesystem (PDF cache)
```

Cross-cutting concerns (identical in spirit to the Control Server):

- **`advice/`** — a global exception handler turning errors into clean JSON.
- **`filter/ApiLoggingFilter`** — per-request logging (method, path, body, headers; binary-safe).
- **`config/`** — all wiring: Redis cache, CORS/MVC, Jackson, OpenAPI, logging, clock.
- **`onstartup/OnStartUpRunner`** — startup + scheduled housekeeping (PDF/file cache cleanup).

---

## 4. Request families (the public surface)

All paths are constants in **`constant/URIConstants`** — your index. By controller:

- **`OpenTestController`** (the big one — the search/statistics engine):
  - `GET|POST /opentests`, `/opentests/search` — filtered search over measurements (JSON).
  - `GET /opentests/O{open_test_uuid}` — a single result by its public uuid.
  - `GET /opentests/choices` — distinct values for filter dropdowns.
  - `GET /opentests/statistics`, `/opentests/intraday`, `/opentests/histogram` — aggregates.
  - `GET /{lang}/{open_test_uuid}/{size}.png` — a rendered **share image** for a result.
- **`StatisticController`** — `POST /statistics` — the headline statistics payload (cached).
- **`ExportController`** — open-data bulk exports:
  - `/export/netztest-opendata-{year}-{month}.{format}`,
  - `/export/netztest-opendata_hours-{hours}.{format}`,
  - `/export/netztest-opendata.{format}` (recent) — `{format}` ∈ csv/xlsx/zip.
- **`PdfExportController`** — `POST /export/pdf[/{lang}]` and
  `GET /export/pdf[/{lang}]/{fileName}.pdf` — generate / fetch a result PDF certificate.
- **`CoverageController`** — `GET|POST /coverage` — coverage lookups.
- **`AdminUsageController`** — `GET /admin/usageJSON` — usage report (eventually 
  restrict at the proxy; there is no app-level auth).
- **`ApplicationVersionController`** — `GET /version`.

Swagger UI (springdoc) documents these from the controller annotations.

---

## 5. The OpenTest search engine (read this — it is the core *and* the security boundary)

The single most important subsystem is the open-test search, because it builds **dynamic SQL from
client-supplied filters** and runs it against the live database with **no authentication in front
of it**. It is designed to be safe; you must keep it that way.

The flow (`OpenTestController` → `OpenTestServiceImpl` → `QueryParser` → `repository/impl`):

1. **`QueryParser` (`utils/QueryParser`)** receives the request parameters and turns them into a
   SQL `WHERE` clause. It is the security-critical class. Two invariants make it injection-safe:
   - **Whitelist of fields.** Only parameters whose names are in a server-defined set of allowed
     fields are considered; everything else is ignored. Column names therefore come from the
     server, never from the user.
   - **Parameterised values.** The generated clause uses `?` placeholders; the actual user values
     are bound through `PreparedStatement` (`fillInWhereClause`), never concatenated into the SQL
     string. Comparators are from a fixed set.
2. **`OpenTestServiceImpl`** composes the final query (select list + whitelisted `WHERE` + ordering
   + a **capped** `max_results` limit) and executes it via the repository. The `max_results` value
   is bounded by a hard upper limit so a client cannot ask for an unbounded result set.
3. **`repository/impl/*` (native SQL)** holds the actual `SELECT`s, including spatial (`ST_*`) and
   aggregate queries (`OpenTestRepositoryImpl`, `HistogramRepositoryImpl`, `ChoicesRepositoryImpl`,
   `GeoAnalyticsRepositoryImpl`, `HourlyStatisticRepositoryImpl`, …). Where a query interpolates a
   "field" into the SQL text (e.g. histograms over `speed_download` / `speed_upload`), that field is
   itself drawn from a **regex/whitelist-validated** measurement name — not raw user input.
4. **Caching.** Hot read paths (`OpenTestServiceImpl`, `StatisticGeneratorServiceImpl`) are
   `@Cacheable` in **Redis** with a short TTL, so repeated identical queries don't re-hit Postgres.

> **Rule for any change here:** never concatenate a user-supplied string into SQL. Column/field
> names must come from a whitelist; values must be bound parameters. If you add a filter, add it to
> the allowed-field set and bind its value — do not build SQL by string interpolation.

---

## 6. Package-by-package guide

Everything is under `at.rtr.rmbt`.

### `controller/`
Thin HTTP adapters: bind a `URIConstants` path, parse query/body, delegate to a service, return a
DTO (or a streamed CSV/XLSX/PDF/PNG). `OpenTestController` is large because it hosts the whole
search/statistics family; the rest are small.

### `service/` + `service/impl/`
Business logic, interface + `…Impl` convention. Highlights:
- **`OpenTestServiceImpl`** — orchestrates search/detail/choices/intraday/histogram using
  `QueryParser` and the repositories; `@Cacheable`.
- **`StatisticServiceImpl` / `StatisticGeneratorServiceImpl`** — the `/statistics` payload
  (cached).
- **`ExportServiceImpl`** + **`service/export/opendata/*`** (`CsvExportService`, `XlsxExportService`,
  `ZipExportService`, `AbstractExportService`) — bulk open-data exports.
- **`service/export/pdf/*`** (`PdfExportService`, `PdfGenerator`, impls) — PDF certificate
  generation (Handlebars → HTML → external converter).
- **`ImageExportServiceImpl`** — PNG share images (delegates to `utils/image/generator/*`).
- **`CoverageServiceImpl`, `LocationServiceImpl`, `PingServiceImpl`, `RadioSignalServiceImpl`,
  `QoeClassificationServiceImpl`, `FencesServiceImpl`, `AdminUsageServiceImpl`,
  `ApplicationVersionServiceImpl`** — focused read services.
- **`FileServiceImpl`** — manages the on-disk PDF/file cache (used by the scheduled cleanup).

### `repository/` + `repository/impl/`
Spring Data JPA repositories plus **hand-written native-SQL implementations** for the analytical
queries (`*RepositoryImpl`). This is the read side of the domain; most heavy lifting is SQL here.

### `utils/`
Stateless helpers, several important:
- **`QueryParser`** — safe dynamic `WHERE` builder (see §5).
- **`SqlUtils`, `ControllerUtils`, `ConvertUtils`, `ClassificationUtils`, `SignificantFormat`,
  `BandCalculationUtil`** — formatting, classification thresholds, radio-band math.
- **`ExtendedHandlebars`, `JacksonAwareSnakeCaseJavaBeanResolver`** — Handlebars setup for PDF
  templates (snake_case field resolution).
- **`utils/export/`** — `PdfConverter` abstraction with `PrincePdfConverter` /
  `WeasyprintPdfConverter` (external HTML→PDF tools).
- **`utils/image/generator/`** — `ShareImageGenerator`, `FacebookThumbnailGenerator`,
  `ForumBannerGenerator`, `ForumBannerSmallGenerator` (Java2D PNG rendering).
- **`utils/smoothing/`** — smoothing functions for curves/graphs.

### `response/`, `request/`, `dto/`, `model/`, `mapper/`
- **`response/`** — outbound JSON DTOs (the bulk of the wire contract; this is a read API).
- **`request/`** — the few inbound bodies (e.g. statistics/export request shapes).
- **`dto/`** — internal value objects used while assembling results.
- **`model/`** — the handful of JPA entities the server maps (it reads far more via native SQL than
  it maps as entities, so this package is small).
- **`mapper/`** — MapStruct DTO↔entity mapping.

### `enums/`, `constant/`
Domain enums and constants. **`constant/URIConstants`** = endpoint index; **`constant/Constants`** =
tuning values (limits, cache names, classification constants).

### `config/`
- **`RedisConfig`** — Redis connection (Jedis), cache manager, TTL, JSON value serialization.
- **`WebMvcConfig`** — CORS mappings and a CORS `Filter` (no security chain).
- **`ApiLoggingFilterConfig`** — registers the request-logging filter.
- **`LoggingConfigurer`** — runtime logging setup (identical pattern to the Control Server;
  `app_name = statistic-service`). See §10.
- **`OpenApiConfiguration`, `ClockConfiguration`, `ApplicationProperties`** — Swagger, injectable
  `Clock`, typed `app.*` config (file-cache paths, expiration, job rate, languages, …).

### `onstartup/`
**`OnStartUpRunner`** — an `ApplicationRunner` that also carries a `@Scheduled` job to **clear the
PDF/file cache** directory on a fixed rate (and any startup priming). This is why exports can be
cached on disk without growing unbounded.

### `filter/`, `advice/`, `exception/`
- **`filter/ApiLoggingFilter`** — per-request logging (same design as Control: request id in MDC,
  binary-body-safe, friendly on client disconnects).
- **`advice/`** — global `@RestControllerAdvice` mapping exceptions to clean JSON error responses.
- **`exception/`** — the custom exceptions it maps.

---

## 7. Persistence (read-only over a shared database)

- The server reads the **same PostgreSQL+PostGIS database** the Control Server writes (`test` and
  its sub-tables). It treats it as **read-only**: there is **no Flyway** here and no schema
  ownership. Don't add migrations to this repo.
- Most analytical access is **native SQL** in `repository/impl` (search, histograms, choices, geo
  aggregates, hourly stats). A small set of JPA `@Entity` classes in `model/` cover the cases that
  benefit from object mapping.
- Spatial columns use **PostGIS** (`ST_*` functions, hibernate-spatial). Geo aggregation (e.g.
  snapping points to a grid for heat data) happens in SQL.
- **Connection role / privileges:** the DB user this server connects as only needs `SELECT`. Keep
  it that way — it's a useful safety property given there's no auth in front.

---

## 8. Caching (Redis)

- `RedisConfig` wires a Jedis connection and a Spring `CacheManager` whose values are JSON-encoded
  with a short **TTL** (seconds). Cache keys derive from the method arguments.
- `@Cacheable` sits on the hot, expensive read paths (`OpenTestServiceImpl`,
  `StatisticGeneratorServiceImpl`). The point is to absorb bursts of identical public requests
  (e.g. the same statistics page being hammered) without re-querying Postgres each time.
- Because the TTL is short, the data stays near-real-time; Redis is a buffer, not a source of
  truth. If Redis is unavailable, treat it as a performance degradation, not a correctness issue.

---

## 9. The exports subsystem

Three independent export channels, all triggered from controllers:

1. **Open data (CSV / XLSX / ZIP)** — `ExportController` → `ExportServiceImpl` →
   `service/export/opendata/*`. `AbstractExportService` defines the shared pipeline; `CsvExportService`
   and `XlsxExportService` render rows (Jackson CSV / xlsx-lite); `ZipExportService` packages them.
   These stream large datasets, so mind memory and streaming.
2. **PDF certificates** — `PdfExportController` → `service/export/pdf/*`. A result is rendered to
   HTML via **Handlebars** templates (`ExtendedHandlebars` + snake_case resolver), then converted to
   PDF by an **external tool** (`PrincePdfConverter` or `WeasyprintPdfConverter` under
   `utils/export/`). Generated PDFs are cached on disk (under `app.fileCache.pdfPath`) and served by
   filename; `OnStartUpRunner` periodically purges expired files. **Note the external dependency:**
   the PDF converter binary must be installed on the host.
3. **PNG share images** — `OpenTestController` (`…/{size}.png`) → `ImageExportServiceImpl` →
   `utils/image/generator/*`. Java2D draws social/forum images for a result.

---

## 10. Cross-cutting concerns

### No authentication — CORS only
There is **no Spring Security** dependency. Inbound requests are gated only by **CORS**
(`WebMvcConfig`). Endpoints under `/admin/*` are "admin" by convention and might be protected at the
**reverse proxy / network** layer, not by the application. Treat every endpoint as publicly
reachable when reasoning about safety — which is exactly why §5 (SQL whitelisting + bound
parameters) matters so much.

### Error handling
A global `@RestControllerAdvice` in `advice/` maps exceptions to clean JSON error responses, the
same contract as the Control Server. Throw a mapped exception rather than leaking a 500 + stack
trace.

### Logging (`config/LoggingConfigurer` + `logback.xml`)
Identical pattern to the Control Server: `logback.xml` is a **console-only baseline that always
loads**; at `ApplicationReadyEvent`, `LoggingConfigurer` reads the deployment context
(Tomcat `conf/context.xml` `<Parameter>`, JVM `-D`, or env) and reconfigures:
1. `LOGGING_CONFIG_FILE_STATISTIC` → load that logback file verbatim;
2. else `LOG_HOST` set → ship INFO to **Logstash** (`app_name = statistic-service`), console
   limited to ERROR;
3. else → console only.
Per-request logging is `ApiLoggingFilter`.

### Monitoring & docs
**JavaMelody** for runtime monitoring; **springdoc** for the OpenAPI/Swagger UI.

### Scheduling
`@EnableScheduling` (via the startup runner) drives the periodic file-cache cleanup.

---

## 11. Configuration & deployment

- **Profiles** (`application.yaml`): a default block plus `dev`, `test`, `prod` documents
  (`spring.config.activate.on-profile`). Activate via `spring.profiles.active=prod` in Tomcat's
  `catalina.properties`.
- **Externalised settings** (env / Tomcat `context.xml` `<Parameter>`):
  - DB: `STATISTIC_DB_HOST` / `_PORT` / `_NAME` (+ user/password).
  - Redis: `redis.host` / `redis.port`.
  - File cache: `app.fileCache.path` / `.pdfPath` / `.expirationTerm` / `.cleaningJobRate`.
  - CORS origin; logging (`LOG_HOST`, `LOG_PORT`, `LOGGING_HOST`,
    `LOGGING_CONFIG_FILE_STATISTIC`).
- **External runtime dependencies:** a reachable PostgreSQL+PostGIS (read), a Redis instance, and
  the **PDF converter** (Prince or WeasyPrint) installed on the host for PDF export.
- **Build / deploy:** `mvn clean package` → WAR → Tomcat 10 (`webapps/RMBTStatisticServer.war`),
  Java 17.
- **Local run/debug:** run `StatisticServerApplication.main()`; point it at a local DB (with data)
  and a local Redis. PDF export needs the converter binary; CSV/XLSX/search do not.

---

## 12. Conventions you should follow

- **SQL safety is the prime directive** (§5): whitelist column/field names, bind values as
  parameters, never string-concatenate user input into SQL. There is no auth to fall back on.
- **Read-only mindset:** this server must not write the measurement schema and must not depend on
  Flyway. Keep DB privileges at `SELECT`.
- **Bound the work:** large result sets and exports must be capped/streamed (`max_results` limit,
  streaming CSV/XLSX) so a single request can't exhaust memory or the DB.
- **Cache hot reads, keep TTL short** (`@Cacheable`) — correctness must not depend on the cache.
- **DTOs out, not entities:** serialize `response/*` DTOs, not `model/` entities.
- **Mapped exceptions:** throw what `advice/` knows about, for clean JSON errors.

---

## 13. Where to start reading (suggested path)

1. **`constant/URIConstants`** — the endpoint catalogue.
2. **`controller/OpenTestController`** + **`service/impl/OpenTestServiceImpl`** — the search/
   statistics core.
3. **`utils/QueryParser`** — *the* class to understand; the safe dynamic-SQL boundary.
4. **`repository/impl/OpenTestRepositoryImpl` / `HistogramRepositoryImpl`** — the native analytical
   SQL the parser feeds.
5. **`config/RedisConfig`** + the `@Cacheable` sites — how reads are cached.
6. **`service/export/…`** + **`PdfExportController`** + **`utils/export/`** — the export channels
   (incl. the external PDF converter).
7. **`config/LoggingConfigurer`** + **`advice/`** — logging and the error contract (shared with the
   Control Server).

---

## 14. Glossary

- **Open test** — a single measurement record, viewed/searched publicly (anonymised) via
  `open_test_uuid`.
- **`open_test_uuid`** — the public identifier of a measurement (used in result URLs, exports,
  share images).
- **Choices** — the distinct filter values (operators, technologies, …) used to build search UIs.
- **Open data** — the bulk, anonymised export of measurements (CSV/XLSX/ZIP) under `/export`.
- **Histogram / intraday** — aggregate distributions / time-of-day curves of a measurement
  metric.
- **Classification thresholds** — the good/medium/bad cut-offs used to colour metrics (shared
  vocabulary with the Control Server's QoE classification).
- **Share image** — a PNG (Facebook thumbnail / forum banner) rendered for a result.
- **PDF certificate** — a printable per-result document rendered via Handlebars + an external
  HTML→PDF converter.

---

*This describes the structure and intent of the code as it stands. The code is the source of
truth — start from the reading path in §13 and follow the calls. For the write side of the same
database, see `architecture.md` in `open-rmbt-control`.*
