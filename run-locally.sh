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
            --couchdb )  run_component="couchdb"
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
    h1 "Launching API server"

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
    info "Looping to wait for things to start up"
    http_status=""
    while [[ "$http_status" != "200" ]]; do
        info "sleeping for 5 secs..."
        sleep 5
        info "querying to see if things are started yet..."
        http_status=$(curl -s -o /dev/null -w "%{http_code}" $url)
    done
    success "Things are started"
}

function launch_couchdb_in_docker {
    h1 "Launching Couchdb inside docker"
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

set_up_bootstrap

case $run_component in
  api ) launch_api_server
  ;;
  couchdb ) launch_couchdb_in_docker
  ;;
esac
