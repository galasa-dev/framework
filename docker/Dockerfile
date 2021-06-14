ARG mavenVersion
ARG dockerRepository
ARG branch
FROM ${dockerRepository}/galasadev/galasa-maven-maven:${mavenVersion}-${branch}

RUN sed -i 's/\/maven"/\/framework"/' /usr/local/apache2/conf/httpd.conf

COPY repo/ /usr/local/apache2/htdocs/
COPY framework.githash /usr/local/apache2/htdocs/framework.githash
