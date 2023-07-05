#! /usr/bin/env bash 

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Sets the version number of this component.
#
# Environment variable over-rides:
# None
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
    h1 "Syntax"
    cat << EOF
set-version.sh [OPTIONS]
Options are:
-v | --version xxx : Mandatory. Set the version number to something explicitly. 
    Re-builds the release.yaml based on the contents of sub-projects.
    For example '--version 0.29.0'
EOF
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
component_version=""

while [ "$1" != "" ]; do
    case $1 in
        -v | --version )        shift
                                export component_version=$1
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

if [[ -z $component_version ]]; then 
    error "Missing mandatory '--version' argument."
    usage
    exit 1
fi


function update_release_yaml {

    source_file=$1
    target_file=$2

    # Read through the release yaml and set the version of the framework bundle explicitly.
    # It's on the line after the line containing 'artifact: dev.galasa.framework'
    is_line_supressed=false
    while IFS= read -r line
    do
        regex="^.*dev.galasa.framework[ ]*$"
        if [[ "$line" =~ $regex ]]; then
            # We found the marker, so the next line needs supressing.
            echo "$line"
            is_line_supressed=true
        else
            if [[ $is_line_supressed == true ]]; then
                # Don't echo this line, but we only want to supress one line.
                is_line_supressed=false
                echo "    version: $component_version"
            else
                # Nothing special about this line, so echo it.
                echo "$line"
            fi
        fi

    done < $source_file > $target_file
}

temp_dir=$BASEDIR/temp/version_bump
mkdir -p $temp_dir

# The galasa-parent/dev.galasa.framework/build.gradle file is where the 'master' version number 
# of the framework component lives.
# For example: version = "0.29.0"
cat $BASEDIR/galasa-parent/dev.galasa.framework/build.gradle | sed "s/^[ ]*version[ ]*=.*/version = \"$component_version\"/1" > $temp_dir/framework-build.gradle

# The galasa-parent/dev.galasa.framework/settings.gradle file has a bundleVersion number which needs
# to be bumped also...
# eg: bundleVersion = 0.27.0
cat $BASEDIR/galasa-parent/dev.galasa.framework/settings.gradle | sed "s/^[ ]*bundleVersion[ ]*=.*/bundleVersion = $component_version/1" > $temp_dir/framework-settings.gradle


update_release_yaml ${BASEDIR}/release.yaml $temp_dir/release.yaml

# Copy the temp files back to where they belong...

cp $temp_dir/framework-build.gradle $BASEDIR/galasa-parent/dev.galasa.framework/build.gradle
cp $temp_dir/framework-settings.gradle $BASEDIR/galasa-parent/dev.galasa.framework/settings.gradle
cp $temp_dir/release.yaml ${BASEDIR}/release.yaml

