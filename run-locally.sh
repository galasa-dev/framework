#! /usr/bin/env bash 

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Run the framework code from local .m2 repository
#
# Environment variable over-rides:
# Invoke with --help and read the output.
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
# cd "${BASEDIR}"

cd "${BASEDIR}/.."
WORKSPACE_DIR=$(pwd)


#-----------------------------------------------------------------------------------------                   
# Set Colors
#-----------------------------------------------------------------------------------------                   
bold=$(tput bold)
underline=$(tput sgr 0 1)
reset=$(tput sgr0)
red=$(tput setaf 1)
green=$(tput setaf 76)
white=$(tput setaf 7)
tan=$(tput setaf 202)
blue=$(tput setaf 25)

#-----------------------------------------------------------------------------------------                   
# Headers and Logging
#-----------------------------------------------------------------------------------------                   
underline() { printf "${underline}${bold}%s${reset}\n" "$@" ;}
h1() { printf "\n${underline}${bold}${blue}%s${reset}\n" "$@" ;}
h2() { printf "\n${underline}${bold}${white}%s${reset}\n" "$@" ;}
debug() { printf "${white}%s${reset}\n" "$@" ;}
info() { printf "${white}➜ %s${reset}\n" "$@" ;}
success() { printf "${green}✔ %s${reset}\n" "$@" ;}
error() { printf "${red}✖ %s${reset}\n" "$@" ;}
warn() { printf "${tan}➜ %s${reset}\n" "$@" ;}
bold() { printf "${bold}%s${reset}\n" "$@" ;}
note() { printf "\n${underline}${bold}${blue}Note:${reset} ${blue}%s${reset}\n" "$@" ;}


#-----------------------------------------------------------------------------------------                   
# Functions
#-----------------------------------------------------------------------------------------                   
function usage {
    info "Syntax: run-locally.sh [OPTIONS]"
    cat << EOF
Options are:
--api : Run the framework API server
--couchdb : Run the couchdb server in a docker container
--etcd : Run etcd inside docker.
--dex : Run dex inside docker.
-h | --help : get this help text.

Environment variables:
GALASA_BOOTSTRAP - Optional. 
    Controls where the galasa bootstrap information can be found.
    Defaults to file://${HOME}/.galasa/bootstrap.properties
None
EOF
}


#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
run_component=""

while [ "$1" != "" ]; do
    case $1 in
             --api  )           run_component="api"
                                ;;
            --couchdb )         run_component="couchdb"
                                ;;
            --etcd )            run_component="etcd"
                                ;;
            --dex )             run_component="dex"
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     error "Unexpected argument $1"
                                usage
                                exit 1
    esac
    shift
done

if [[ "${run_component}" == "" ]]; then
    error "Need to indicate which component is to be run. eg: --api"
    usage
    exit 1  
fi


#-----------------------------------------------------------------------------------------                   
# Main logic.
#-----------------------------------------------------------------------------------------                   

# Work out where the framework jar has been built locally...
BOOT_FOLDER="${BASEDIR}/galasa-parent/galasa-boot/build/libs"
boot_jar_name=$(ls ${BOOT_FOLDER}/galasa-boot-*.jar | grep -v "sources" | grep -v "javadoc")
info "Boot jar is at ${BOOT_FOLDER}/${boot_jar_name}"

# Work out where the locally-build-OBR is held...
OBR_VERSION="0.31.0"

M2_PATH=~/.m2

cd $BASEDIR
rm -fr temp
mkdir -p temp
cd temp




function set_up_bootstrap {

    export GALASA_HOME="${BASEDIR}/temp/home"
    info "Environment variable GALASA_HOME is ${GALASA_HOME}"

    export GALASA_BOOTSTRAP="file://${GALASA_HOME}/bootstrap.properties"
    info "Environment variable GALASA_BOOTSTRAP is $GALASA_BOOTSTRAP"

    h1 "Setting up the bootstrap to refer to the prod ecosystem"
    galasactl local init
    echo >> ${GALASA_HOME}/bootstrap.properties
    echo "framework.config.store=etcd:http://galasa-galasa-prod.cicsk8s.hursley.ibm.com:32189" >> ${GALASA_HOME}/bootstrap.properties
    echo "framework.extra.bundles=dev.galasa.cps.etcd,dev.galasa.ras.couchdb,dev.galasa.phoenix2.manager" >> ${GALASA_HOME}/bootstrap.properties
}


function launch_api_server {
    h2 "Launching API server"

    cmd="${JAVA_HOME}/bin/java \
    -jar ${boot_jar_name} \
    --localmaven file:${M2_PATH}/repository/ \
    --bootstrap ${GALASA_BOOTSTRAP} \
    --overrides file://${GALASA_HOME}/overrides.properties \
    --obr mvn:dev.galasa/dev.galasa.uber.obr/${OBR_VERSION}/obr \
    --trace \
    --api"

    info "Command is ${cmd}"

    ${cmd} 2>&1 > log.txt

    success "Launched OK"
}

function loop_get_url_until_success {
    url=$1
    h2 "Looping to wait for things to start up"
    http_status=""
    while [[ "$http_status" != "200" ]]; do
        info "sleeping for 5 secs..."
        sleep 5
        info "querying $url to see if things are started yet..."
        http_status=$(curl -s -o /dev/null -w "%{http_code}" $url)
        info "returned http status code was $http_status"
    done
    success "Things are started"
}

function launch_couchdb_in_docker {
    h2 "Launching Couchdb inside docker"
    export COUCHDB_VERSION="3.3.3"
    info "Pulling the image down"
    docker pull couchdb:$COUCHDB_VERSION
    info "Removing any running instances of couchdb"
    docker stop couchdb

    # info "Creating a couchdb folder to store data in, unless it already exists."
    mkdir -p $HOME/.couchdb/data
    chmod 0777 $HOME/.couchdb
    chmod 0777 $HOME/.couchdb/data

    # -v /home/couchdb/data:/opt/couchdb/data
    info "running the image"
    image_count=$(docker ps -a | grep couchdb | wc -l | xargs)
    if [[ "$image_count" == "1" ]]; then 
        info "couchdb docker image already exists."
        docker start couchdb
    else 
        warn "coucdb docker image needs setting up using the wizard in the web UI."
        docker run -p 5984:5984 \
            -d \
            -e COUCHDB_USER=${COUCHDB_USER} \
            -e COUCHDB_PASSWORD=${COUCHDB_PASSWORD} \
            --name couchdb \
            couchdb:$COUCHDB_VERSION
    fi

    info "Waiting for couchdb to start."
    loop_get_url_until_success http://localhost:5984
    info "launching the couchdb web UI"
    open http://localhost:5984/_utils
}


function launch_etcd_in_docker {
    h2 "Launching etcd inside a docker container"

    info "pulling the docker image..."
    export ETCD_TAG="3.3.27-debian-11-r100@sha256:9031d80adc91562b634ca08dc92add303dd50502c49622e162678caa9dbe36d5"
    docker pull bitnami/etcd:${ETCD_TAG}
    


    image_count=$(docker ps -a | grep etcd | wc -l | xargs)
    if [[ "$image_count" == "1" ]]; then 
        info "etcd docker image already exists."
        docker start etcd
    else 
        info "running the image"
        docker network create app-tier --driver bridge

        docker run -d --name etcd \
            --network app-tier \
            --publish 2379:2379 \
            --publish 2380:2380 \
            --env ALLOW_NONE_AUTHENTICATION=yes \
            --env ETCD_ADVERTISE_CLIENT_URLS=http://etcd:2379 \
            --env ETCD_UNSUPPORTED_ARCH=arm64 \
            bitnami/etcd:${ETCD_TAG}
    fi

    info "Waiting for etcd to start."
    loop_get_url_until_success http://localhost:2379/version
    success "etcd started ok"
}

function launch_dex_in_docker {
    h2 "Launching Dex inside a docker image"

    info "Pulling the image down"
    docker pull ghcr.io/dexidp/dex:v2.38.0

    info "Setting up the dex admin password."
    if [[ "$DEX_ADMIN_PASSWORD" == "" ]]; then 
        info "Creating a DEX_ADMIN_PASSWORD"

        export DEX_ADMIN_PASSWORD=$(echo password | htpasswd -BinC 10 admin | cut -d: -f2)
        info "Dex admin password is $DEX_ADMIN_PASSWORD"
        warn "Put this value into your .zprofile! eg: export DEX_ADMIN_PASSWORD=\"$DEX_ADMIN_PASSWORD\""
    fi

    info "Making sure the .dex config is set up"
    mkdir -p ~/.dex
    cat << EOF >> ~/.dex/config-dev.yaml

# The base path of dex and the external name of the OpenID Connect service.
# This is the canonical URL that all clients MUST use to refer to dex. If a
# path is provided, dex's HTTP service will listen at a non-root URL.
issuer: http://127.0.0.1:5556/dex

# The storage configuration determines where dex stores its state. Supported
# options include SQL flavors and Kubernetes third party resources.
#
# See the documentation (https://dexidp.io/docs/storage/) for further information.
storage:
  type: sqlite3
  config:
    file: var/dex/dex.db

# Configuration for the HTTP endpoints.
web:
  http: 0.0.0.0:5556

# Configuration for telemetry
telemetry:
  http: 0.0.0.0:5558

# Uncomment this block to enable the gRPC API. This values MUST be different
# from the HTTP endpoints.
grpc:
  addr: 0.0.0.0:5557
  reflection: true

# Uncomment this block to enable configuration for the expiration time durations.
# Is possible to specify units using only s, m and h suffixes.
expiry:
  # deviceRequests: "5m"
  signingKeys: "6h"
  idTokens: "24h"
  refreshTokens:
    disableRotation: true
    validIfNotUsedFor: "2160h" # 90 days

# Default values shown below
oauth2:
  skipApprovalScreen: true

# Instead of reading from an external storage, use this list of clients.
#
# If this option isn't chosen clients may be added through the gRPC API.
staticClients:
- id: galasa-webui
  redirectURIs:
  - 'http://localhost:8080/auth/callback'
  name: 'Galasa Web UI'
  secret: example-webui-client-secret

# Let dex keep a list of passwords which can be used to login to dex.
enablePasswordDB: true

# A static list of passwords to login the end user. By identifying here, dex
# won't look in its underlying storage for passwords.
#
# If this option isn't chosen users may be added through the gRPC API.

staticPasswords:
- email: "admin@example.com"
  hash: "${DEX_ADMIN_PASSWORD}"
  username: "admin"
  userID: "08a8684b-db88-4b73-90a9-3cd1661f5466"

EOF

    docker stop dex
    docker rm dex
    docker run -d -v ~/.dex/config-dev.yaml:/etc/dex/config.docker.yaml -p 5556:5556 -p 5558:5558 -p 5557:5557 --name dex ghcr.io/dexidp/dex:v2.38.0
}

set_up_bootstrap

case $run_component in
  api ) launch_api_server
  ;;
  couchdb ) launch_couchdb_in_docker
  ;;
  etcd ) launch_etcd_in_docker
  ;;
  dex ) launch_dex_in_docker
esac
