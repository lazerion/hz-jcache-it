FROM openjdk:8-jre
ENV HZ_VERSION 3.9.2
ENV HZ_HOME /opt/hazelcast/
RUN mkdir -p $HZ_HOME
WORKDIR $HZ_HOME
ADD http://download.hazelcast.com/enterprise/hazelcast-enterprise-$HZ_VERSION.zip $HZ_HOME
RUN unzip hazelcast-enterprise-$HZ_VERSION.zip

ADD server.sh /$HZ_HOME/server.sh
RUN chmod +x /$HZ_HOME/server.sh
EXPOSE 5701
# Start hazelcast standalone server.
CMD ["./server.sh"]
