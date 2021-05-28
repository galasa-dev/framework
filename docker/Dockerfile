ARG mavenVersion
ARG dockerRepository
ARG branch
FROM ${dockerRepository}/galasadev/galasa-maven-maven:${mavenVersion}-${branch}

ARG gitHash

RUN sed -i 's/\/maven"/\/framework"/' /usr/local/apache2/conf/httpd.conf

COPY repo/ /usr/local/apache2/htdocs/
RUN echo -n ${gitHash} > /usr/local/apache2/htdocs/framework.githash
