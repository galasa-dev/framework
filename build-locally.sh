#! /usr/bin/env bash 

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Build this repository code locally.
#
# Environment variable over-rides:
# LOGS_DIR - Optional. Where logs are placed. Defaults to creating a temporary directory.
# SOURCE_MAVEN - Optional. Where a maven repository is from which the build will draw artifacts.
# DEBUG - Optional. Defaults to 0 (off)
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
#
# Set Colors
#
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
#
# Headers and Logging
#
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
    info "Syntax: build-locally.sh [OPTIONS]"
    cat << EOF
Options are:
-c | --clean : Do a clean build. One of the --clean or --delta flags are mandatory.
-d | --delta : Do a delta build. One of the --clean or --delta flags are mandatory.
EOF
}


#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
build_type=""

while [ "$1" != "" ]; do
    case $1 in
        -c | --clean )          build_type="clean"
                                ;;
        -d | --delta )          build_type="delta"
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

if [[ "${build_type}" == "" ]]; then
    error "Need to use either the --clean or --delta parameter."
    usage
    exit 1  
fi


#-----------------------------------------------------------------------------------------                   
# Main logic.
#-----------------------------------------------------------------------------------------                   

source_dir="galasa-parent"

project=$(basename ${BASEDIR})
h1 "Building ${project}"
info "Build type is --'${build_type}'"

# Debug or not debug ? Override using the DEBUG flag.
if [[ -z ${DEBUG} ]]; then
    export DEBUG=0
    # export DEBUG=1
    info "DEBUG defaulting to ${DEBUG}."
    info "Over-ride this variable if you wish. Valid values are 0 and 1."
else
    info "DEBUG set to ${DEBUG} by caller."
fi

# Over-rode SOURCE_MAVEN if you want to build from a different maven repo...
if [[ -z ${SOURCE_MAVEN} ]]; then
    export SOURCE_MAVEN=https://galasadev-cicsk8s.hursley.ibm.com/main/maven/obr/
    info "SOURCE_MAVEN repo defaulting to ${SOURCE_MAVEN}."
    info "Set this environment variable if you want to over-ride this value."
else
    info "SOURCE_MAVEN set to ${SOURCE_MAVEN} by caller."
fi

# Create a temporary dir.
# Note: This bash 'spell' works in OSX and Linux.
if [[ -z ${LOGS_DIR} ]]; then
    export LOGS_DIR=$(mktemp -d 2>/dev/null || mktemp -d -t "galasa-logs")
    info "Logs are stored in the ${LOGS_DIR} folder."
    info "Over-ride this setting using the LOGS_DIR environment variable."
else
    mkdir -p ${LOGS_DIR} 2>&1 > /dev/null # Don't show output. We don't care if it already existed.
    info "Logs are stored in the ${LOGS_DIR} folder."
    info "Over-ridden by caller using the LOGS_DIR variable."
fi

info "Using source code at ${source_dir}"
cd ${BASEDIR}/${source_dir}
if [[ "${DEBUG}" == "1" ]]; then
    OPTIONAL_DEBUG_FLAG="-debug"
else
    OPTIONAL_DEBUG_FLAG="-info"
fi

# auto plain rich or verbose
CONSOLE_FLAG=--console=plain

log_file=${LOGS_DIR}/${project}.txt
info "Log will be placed at ${log_file}"

if [[ "${build_type}" == "delta" ]]; then
    info "Skipping clean phase because this is a delta build."
else
    h2 "Cleaning..."
    gradle --no-daemon \
    ${CONSOLE_FLAG} \
    --warning-mode=all --debug \
    -Dorg.gradle.java.home=${JAVA_HOME} \
    -PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
    clean \
    2>&1 >> ${log_file}
    rc=$? ; if [[ "${rc}" != "0" ]]; then cat ${log_file} ; error "Failed to clean ${project}" ; exit 1 ; fi
    success "Cleaned OK"
fi

h2 "Removing .m2 artifacts"
rm -fr ~/.m2/repository/dev/galasa/dev.galasa.framework*
rm -fr ~/.m2/repository/dev/galasa/dev.galasa
rm -fr ~/.m2/repository/dev/galasa/galasa-boot
rm -fr ~/.m2/repository/dev/galasa/galasa-testharness
success "OK"

h2 "Building..."
cat << EOF

Using this command:

gradle --build-cache  \
${CONSOLE_FLAG} \
--warning-mode=all \
-Dorg.gradle.java.home=${JAVA_HOME} \
-PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
build check \
2>&1 >> ${log_file}

EOF

gradle --build-cache --parallel \
${CONSOLE_FLAG} \
--warning-mode=all \
-Dorg.gradle.java.home=${JAVA_HOME} \
-PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
build check \
2>&1 >> ${log_file}
rc=$? ; if [[ "${rc}" != "0" ]]; then cat ${log_file} ; error "Failed to build ${project}" ; exit 1 ; fi
success "Built OK"



h2 "Publishing to local maven repo..."
gradle --build-cache --parallel \
${CONSOLE_FLAG} \
--warning-mode=all --debug  \
-Dorg.gradle.java.home=${JAVA_HOME} \
-PsourceMaven=${SOURCE_MAVEN} ${OPTIONAL_DEBUG_FLAG} \
publishToMavenLocal \
2>&1 >> ${log_file}
rc=$? ; if [[ "${rc}" != "0" ]]; then cat ${log_file} ; error "Failed to publish ${project}" ; exit 1 ; fi
success "Published OK"

success "Project ${project} built - OK - log is at ${log_file}"
