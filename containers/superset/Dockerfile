FROM apache/superset
# Switching to root to install the required packages
USER root

COPY prod.txt ./
RUN pip install --upgrade pip && pip install --no-cache-dir -r prod.txt

COPY superset-init.sh /superset-init.sh
RUN chmod +x /superset-init.sh

# admin auth details
ENV ADMIN_USERNAME admin
ENV ADMIN_FIRST_NAME admin
ENV ADMIN_LAST_NAME admin
ENV ADMIN_EMAIL admin@admin.com
ENV ADMIN_PWD admin
ENV SUP_ROW_LIMIT 5000
ENV SUP_WEBSERVER_THREADS 8
ENV SUP_WEBSERVER_WORKERS 10
ENV SUP_WEBSERVER_PORT 8088
ENV SUP_WEBSERVER_TIMEOUT 60
ENV SUP_SECRET_KEY 'thisismysecretkey'
ENV SUP_META_DB_URI "sqlite:///${SUPERSET_HOME}/superset.db"
ENV SUP_CSRF_ENABLED True
ENV SUP_CSRF_EXEMPT_LIST []
ENV MAPBOX_API_KEY ''

# Switching back to using the `superset` user
USER superset

# since this can be used as a base image adding the file /docker-entrypoint.sh
# is all you need to do and it will be run *before* Superset is set up
ENTRYPOINT [ "/superset-init.sh" ]
