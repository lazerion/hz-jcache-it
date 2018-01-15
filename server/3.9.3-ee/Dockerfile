FROM openjdk:8-jre
ENV HZ_VERSION 3.9.3-SNAPSHOT
ENV HZ_HOME /opt/hazelcast/
RUN mkdir -p $HZ_HOME
WORKDIR $HZ_HOME
# TODO Download hazelcast jars from maven repo.
ADD hazelcast-enterprise-all-$HZ_VERSION.jar $HZ_HOME

ADD server.sh /$HZ_HOME/server.sh
RUN chmod +x /$HZ_HOME/server.sh
EXPOSE 5701
# Start hazelcast standalone server.
CMD ["./server.sh"]
