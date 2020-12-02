FROM salimelakoui/hive-base:latest-salim

HEALTHCHECK CMD curl -f http://localhost:8042/ || exit 1

ENV NODE_MANAGER_HOST=localhost
ENV NODE_MANAGER_PORT=8042
ENV YARN_CONF_yarn_namemanager_hostname=$NODE_MANAGER_HOST
ENV YARN_CONF_yarn_namemanager_webapp_address=$NODE_MANAGER_HOST:$NODE_MANAGER_PORT

ADD run.sh /run.sh
RUN chmod a+x /run.sh

EXPOSE 8042

CMD ["/run.sh"]
