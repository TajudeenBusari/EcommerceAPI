Your Apps → Log Files → Promtail (collects) → Loki (stores) → Grafana (visualizes)
↑              ↑                     ↑
Log to files  Runs on each        Central log
or stdout     host/container      storage

Promtail is a log collector and shipper.

It can read logs from:
/var/log/containers/*.log → container logs
/var/log/*.log → host logs
custom application log files → any path you specify (e.g., /var/log/inventory-service/*.log)

CASE1: WHEN SERVICE RUNS IN DOCKER CONTAINER
-----------------------------------
When services run in Docker container, Docker automatically writes that container
logs to: var/lib/docker/containers/[container-id]/[container-id]-json.log. Can be checked by:
docker inspect --format='{{.LogPath}}' inventory-service
Promtail mounted with:
- /var/lib/docker/containers:/var/lib/docker/containers:ro (replace var with mnt if you want to keep ro)
- That means Promtail can read all container logs files on the host system.
- Promtail has this section in its config to read all container logs:
  - job_name: docker
    static_configs:
    - targets:
        - localhost
      labels:
        job: dockerLogs
        __path__: /mnt/lib/docker/containers/*/*.log
So Promtail:
        - Scan that directory for all container log files.
        - Read each log file line by line.
        - Add labels to each log line (e.g., job=dockerLogs).
        - Push the log entries to Loki at http://loki:3100/loki/api/v1/push
Results: printed logs in the container console are automatically collected by Promtail,
sent to Loki, and can be queried and visualized in Grafana.

Example Flow:
inventory-service (container)
|
| logs → stdout/stderr
v
Docker Engine
|
| writes JSON logs to:
| /var/lib/docker/containers/<id>/<id>-json.log
v
Promtail container (mounted at /var/lib/docker/containers)
|
| reads and tails all *.log files
v
Loki (via http://loki:3100/loki/api/v1/push)
|
v
Grafana dashboards / Log Explorer

CASE2: WHEN SERVICE RUNS ON HOST (NOT IN CONTAINER)
-----------------------------------
When services run locally on the host (not in a container),
Promtail will not see them unless you also make them go to a file Promtail can read.
For example: in the application,yml of the service, add:

* logging:
   file:
     name: ./log/inventory-service/inventory-service.log

* Then create a Promtail config job to read that log file:
  - job_name: inventory-service
    static_configs:
    - targets:
        - localhost
      labels:
        job: inventoryServiceLogs
        __path__: /path/to/log/inventory-service/*.log
      

* has that directory mounted in the Promtail container:
    - /path/to/log/inventory-service:/path/to/log/inventory-service:ro

* update Promtail config:
 - __path_: /path/to/log/inventory-service/*.log

EXPLANATION OF VOLUME MOUNTS
-----------------------------------
For example, this: 
    **.logs/product-service:/mnt/log/product-service**
means:
- Host->Container
- On the host, there is a directory: /path/to/log/product-service which is mounted to 
  /mnt/log/product-service inside the Promtail container.
- The container sees whatever files are in the local .logs/product-service directory on the host.
- For logging, if an application writes logs to **/mnt/log/product-service** inside the container, the log 
  files will actually be created in **.logs/product-service** on the host so that Promtail can read them.

When service starts locally on the host
---------------------------------------------------------------------
 it creates log files in ./logs/product-service because the 
  application is configured to write logs to ./logs/product-service/app.log in the application.yml.
- Promtail running in docker can still collect if you have mounted the same folder into the Promtail container
  like this:
    - ./logs/product-service:/mnt/log/product-service:ro
    - that way, Promtail (inside Docker) can “see” the host’s logs.

When services starts inside its own container
-------------------------------------------------------------------
 the mount line:

    - ./logs/product-service:/mnt/log/product-service:ro

  still works the same way.
  - The service container writes logs to /mnt/log/product-service/app.log (inside its own container).
  - Docker maps that to ./logs/product-service/app.log on the host and log appears in that directory.
  - Promtail (in its own container) can read /mnt/log/product-service/app.log because of the same mount.
  - Therefore, Promtail is actually reading logs that exist on your host, even though it sees them at /mnt/log/... inside its container
  - Promtail’s /mnt/log/inventory-service is just a window looking into your host folder ./logs/inventory-service.

    Summary:
    product-service writes → /mnt/log/product-service/app.log
    ↳ Docker mounts it → ./logs/product-service/app.log (host)
    ↳ Promtail reads it from its own /mnt/log/product-service

 OBSERVABILITY STACK
 -------------------
1. Grafana: Visualization layer to create dashboards and explore logs.
    start with http://localhost:3000 (admin/admin)
    Add data source: Loki (http://loki:3100), Prometheus (http://prometheus:9090), etc.
    Then:
    start your new dashboard by adding visualization/import panel/import dashboard
    **Add visualization**: 
        use to create own panel from Loki logs for example.
        pick a data source (Loki), then write a query to get logs (e.g., {job="inventory-service"}).
        chose a chart type (e.g., logs, table, graph, stat etc.).
        This is the normal way to create a custom dashboard.
    **Import panel**:
        for example, you can export a panel from an existing dashboard and then import it here.
        Use this if:
        You already have a similar panel elsewhere (e.g. “Error Count”)
        You want to reuse its configuration quickly.
    Import dashboard:
        You can import a full dashboard JSON file 
        The grafana dashboard library(e.g., from Grafana.com/dashboard).
            For example, you can import Loki Logs, Spring boot Microservices Logs, PromtailLoki dashboards from Grafana.com.
        Then copy the dashboard ID (e.g., 1860) and paste it into the import dashboard field.

2. Loki: Log aggregation system to store and index logs.
        Grafana Loki is a log aggregation system - very similar to Elasticsearch but designed specifically for logs
        with Prometheus-style labels and queries.
        It indexes logs (collected by Promtail or another client) and exposes them via an 
        HTTP API-usually at http://localhost:3100.
        What you get from Loki:
        - Centralized log storage: All logs from different services and hosts are stored in one place
            You can think of Loki as your backend log store
            You can use its HTTP API or Grafana data source to access:
                Raw logs - Example of query in Grafana or Loki API: {job="inventory-service"} returns all logs with that label
                Filtered logs - Example: {job="inventory-service", level="error"} returns only error logs
                Log streams - Example: {job="inventory-service"} |~ "timeout" returns logs containing "timeout"
                Log counts - Example: count_over_time({job="inventory-service"}[5m]) returns number of logs in last 5 minutes
        - Structured Log fields: (JSON parsing)
            If your logs are in JSON format, Loki can parse them into structured fields
            Example: {"time":"2024-01-01T12:00:00Z", "level":"info", "msg":"Service started"}
            You can then query based on these fields:
                {job="inventory-service", level="info"}
                {job="inventory-service"} | json | level="info":
        - Log aggregation/Metrics:
            You can aggregate logs over time to create metrics
            Example: 
              count_over_time({job="inventory-service", level="error"}[1m]) gives error count per minute
              rate({job="inventory-service", level="error"}[5m]) gives error rate over 5 minutes
              These can be turned into graphs or alert in Grafana.
        - Metadata and Labels:
            Loki uses labels (key-value pairs) to organize logs
            Example labels: job, filename, host, container, level etc.
            You can query all labels: http://loki:3100/loki/api/v1/labels or label levels for a specific log stream: http://loki:3100/loki/api/v1/label/job/values
        - Query the Loki API directly:
            You can use curl or Postman to query Loki’s HTTP API
            Example:
            GET http://localhost:3100/ready
            GET http://loki:3100/loki/api/v1/query?query={job="inventory-service"}&limit=10
       - IN SUMMARY:
            Loki is your centralized log store
            It collects logs from Promtail (or other clients)
            It indexes them with labels and structured fields
            You can query raw logs, filtered logs, log counts, and aggregated metrics
            You can access it via Grafana or directly via its HTTP API.
            Parse JSON logs into structured data
            Feed Grafana dashboards and alerts
3. Promtail: Log collector and shipper to Loki. It is part of the Grafana Loki log aggregation stack. Its job is to
   read(tail) log files on the host or in containers, add labels to each log entry, and push them to Loki.
   Read more in promtail documentation: https://grafana.com/docs/loki/latest/send-data/promtail/?utm_source=chatgpt.com,
   https://grafana.com/docs/loki/latest/send-data/promtail/configuration/?utm_source=chatgpt.com

4. Prometheus: Metrics collection and monitoring system. It scrapes metrics endpoints (e.g., /actuator/prometheus)
   from your services and stores time-series data. Grafana can visualize these metrics.
   Read more in prometheus documentation: https://prometheus.io/docs/introduction/overview/?utm_source=chatgpt.com

   [Microservices] ──▶ [Promtail] ──▶ [Loki] ──▶ [Grafana: Logs Dashboard]
   │
   └──▶ [Prometheus] ──▶ [Grafana: Metrics Dashboard]

   While Loki handles logs, Prometheus handles metric(numeric time-series data).
   For example:
   - JVM memory usage over time
   - Request duration
   - Error rate
   - Service uptime
   - CPU7Network usage (if node exporter is used)
   Microservices typically expose a metric endpoint at: http://<service-host>:<port>/actuator/prometheus (spring boot app)
   - Prometheus scrapes these endpoints at regular intervals (e.g., every 15s) and stores the data.
   - Grafana can then visualize this metric data in dashboards and graphs.
   - So just like Loki, Prometheus is another backend data source for Grafana, but focused on metrics instead of logs.
   - To connect Prometheus to Grafana: 
       1. Connection→Data sources→Add data source→Prometheus.
       2. Set URL to: http://prometheus:9090
       3. Click Save & Test.
   - Once Grafana is connected to both Loki (Log dashboard) and Prometheus(metrics):
       1. You can create a custom dashboard:
           - Dashboard → New dashboard → Visualization
           - Choose data source (Loki or Prometheus)
           - Query example for Loki: {job="inventory-service"}
           - Query example for Prometheus: rate(http_server_requests_seconds_count{job="inventory-service"}[5m])
           - Choose visualization type (Graph, Table, Stat, etc.)
       2. Or import pre-built dashboards from Grafana.com/dashboard:
           - click import dashboard.
           - Enter dashboard ID (e.g., 1860 for Loki logs, 4701 for Spring Boot microservices metrics)
           - Select data source (Loki or Prometheus)
           - Click Import.
           - You will get a beautiful panel showing for example for Prometheus: CPU, Memory, HTTP requests, JVM metrics, thread count, Request latency, JVM GC activity etc.

   5. Jaeger (optional): Distributed tracing system to trace requests across microservices.
      It helps visualize request flows, latencies, and errors across services.
      Read more in Jaeger documentation: https://www.jaegertracing.io/docs/1.41/?utm_source=chatgpt.com

      In summary, the observability stack consists of:
      - Promtail: Collects logs from services and containers
      - Loki: Centralized log storage and indexing
      - Prometheus: Metrics collection and storage
      - Grafana: Visualization layer for logs and metrics
      - Jaeger (optional): Distributed tracing for request flows

      Together, these tools provide comprehensive observability into microservices applications,
      allowing developers to monitor performance, troubleshoot issues, and gain insights into system behavior.
      To confirm if jaeger port is reachable by the apps:
       * docker exec -it <service-name> sh
       * inside the terminal of the container, run:
       * curl -v http://ecommerce-jaeger:4318/v1/traces
       * you get a response like:
         Host ecommerce-jaeger:4318 was resolved.
         IPv6: (none)
         IPv4: 172.18.0.5
         Trying 172.18.0.5:4318...
         Connected to ecommerce-jaeger (172.18.0.5) port 4318
         GET /v1/traces HTTP/1.1
         Host: ecommerce-jaeger:4318 
         User-Agent: curl/8.5.0
         Accept: */*
         HTTP/1.1 405 Method Not Allowed
         Content-Type: text/plain
         Vary: Origin
         Date: Tue, 21 Oct 2025 14:10:25 GMT
         Content-Length: 41
       
         Connection #0 to host ecommerce-jaeger left intact
         405 method not allowed, supported: [POST]#

EXTRA:
---------------------------------------------------------------------------------
   When spring boot app collects tracing data (e.g., using Micrometer Tracing + OpenTelemetry), it needs a way to
   send that data somewhere for storage and visualization---> such as Jaeger,Tempo, Zipkin or Grafana Cloud.
   OTLP defines the format and transport for sending that data. 
   TWO OPTIONS:
   **TRANSPORT**                 **PORT**             **DESCRIPTION**                             **EXAMPLE IN DOCKER COMPOSE**
   grpc                             4317            High performance binary protocol         - OTEL_EXPORTER_OTLP_ENDPOINT=jaeger:4317
   http(HTTP/protobuf)              4318            Simpler for Debug, supported by Jaeger   - OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger:4318


docker-compose up -d --build


            
           
              