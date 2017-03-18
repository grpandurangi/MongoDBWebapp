FROM docker.io/bbytes/tomcat7
MAINTAINER grpandurangi@gmail.com

ADD target/MongoDBWebapp.war /var/lib/tomcat7/webapps/ 
