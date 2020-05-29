pipeline {
// Initially run on any agent
   agent {
      label 'codesigning'
   }
   environment {
//Configure Maven from the maven tooling in Jenkins
      def mvnHome = tool 'Default'
      PATH = "${mvnHome}/bin:${env.PATH}"
      
//Set some defaults
      def workspace = pwd()
   }
   stages {
// Set up the workspace, clear the git directories and setup the manve settings.xml files
      stage('prep-workspace') { 
         steps {
            configFileProvider([configFile(fileId: '86dde059-684b-4300-b595-64e83c2dd217', targetLocation: 'settings.xml')]) {
            }
            dir('repository/dev.galasa') {
               deleteDir()
            }
            dir('repository/dev/galasa') {
               deleteDir()
            }
         }
      }
      
      stage('maven') {
         steps {
            withCredentials([string(credentialsId: 'galasa-gpg', variable: 'GPG')]) {
               withFolderProperties { withSonarQubeEnv('GalasaSonarQube') {
                  dir('galasa-parent') {
                     sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
 
                     dir('dev.galasa') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Djarsigner.skip=${env.SIGN_SKIP} -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.maven.repository.spi') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Djarsigner.skip=${env.SIGN_SKIP} -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.maven.repository') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.resource.management') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.metrics') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.k8s.controller') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.docker.controller') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('galasa-demo-archetype') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.authentication') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.health') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.bootstrap') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.launcher') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.testcatalog') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.runs') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.cps') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.api.ras') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('dev.galasa.framework.obr') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('galasa-boot') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('galasautils-maven-plugin') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }

                     dir('galasastaging-maven-plugin') {
                        sh "mvn --settings ${workspace}/settings.xml -Dmaven.repo.local=${workspace}/repository -Dgpg.skip=${GPG_SKIP} -Dgpg.passphrase=$GPG -P ${MAVEN_PROFILE} -B -e -fae --non-recursive ${MAVEN_GOAL}"
                     }
                  } }
               }
            }
         }
      }
   }
   post {
       // triggered when red sign
       failure {
           slackSend (channel: '#galasa-devs', color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
       }
    }
}
