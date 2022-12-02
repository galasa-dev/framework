#! /usr/bin/env bash 

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
underline() { printf "${underline}${bold}%s${reset}\n" "$@"
}
h1() { printf "\n${underline}${bold}${blue}%s${reset}\n" "$@"
}
h2() { printf "\n${underline}${bold}${white}%s${reset}\n" "$@"
}
debug() { printf "${white}%s${reset}\n" "$@"
}
info() { printf "${white}➜ %s${reset}\n" "$@"
}
success() { printf "${green}✔ %s${reset}\n" "$@"
}
error() { printf "${red}✖ %s${reset}\n" "$@"
}
warn() { printf "${tan}➜ %s${reset}\n" "$@"
}
bold() { printf "${bold}%s${reset}\n" "$@"
}
note() { printf "\n${underline}${bold}${blue}Note:${reset} ${blue}%s${reset}\n" "$@"
}

#-----------------------------------------------------------------------------------------                   
# Functions
#-----------------------------------------------------------------------------------------                   
function usage {
    info "Syntax: run-locally.sh [OPTIONS]"
    cat << EOF
Options are:
--api : Run the framework API server
-h | --help : get this help text.

Environment variables:
BOOTSTRAP_LOCATION - Optional. 
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
OBR_VERSION="0.25.0"

M2_PATH=~/.m2


# Allow setting the bootstrap location from the environment.
if [[ -z ${BOOTSTRAP_LOCATION} ]]; then
    BOOTSTRAP_LOCATION="file://${HOME}/.galasa/bootstrap.properties"
    info "Environment variable BOOTSTRAP_LOCATION is not set. Using default value of $BOOTSTRAP_LOCATION"
fi
info "Environment variable BOOTSTRAP_LOCATION is $BOOTSTRAP_LOCATION"


cat << EOF
Command is :

${JAVA_HOME}/bin/java \
    -jar ${boot_jar_name} \
    --localmaven file:${M2_PATH}/repository/ \
    --bootstrap ${BOOTSTRAP_LOCATION} \
    --overrides file://${HOME}/.galasa/overrides.properties \
    --obr mvn:dev.galasa/dev.galasa.uber.obr/${OBR_VERSION}/obr \
    --api

EOF

${JAVA_HOME}/bin/java \
    -jar ${boot_jar_name} \
    --localmaven file:${M2_PATH}/repository/ \
    --bootstrap ${BOOTSTRAP_LOCATION} \
    --overrides file://${HOME}/.galasa/overrides.properties \
    --obr mvn:dev.galasa/dev.galasa.uber.obr/${OBR_VERSION}/obr \
    --api

    
