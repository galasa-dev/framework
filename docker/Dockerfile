ARG dockerRepository
ARG branch
FROM ${dockerRepository}/galasadev/galasa-maven-maven:${branch}

COPY repo/ /usr/local/apache2/htdocs/
COPY framework.githash /usr/local/apache2/htdocs/framework.githash
