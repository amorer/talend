#!/usr/bin/groovy
/**
 * Runtime integration tests build
 * @param config Map having optional values
 * <ul>
 * <li>POM_FILE: string, maven pom file to use, defaults to ./runtime/pom.xml</li>
 * <li>SUITE_PROFILE: string, suite execution between elementary or full, defaults to elementary
 * <li>PRODUCT_PROFILE: string, product to test between talend-se, talend-runtime, talend-esb, defaults to talend-runtime</li>
 * <li>JDK: string, JDK to use, either JDK8 or JDK11 (default)
 * <li>SLACK_CHANNEL: string, slack channel to send notifications to, defaults to eng-esb-build (declared in Jenkins conf)</li>
 * <li>TIMEOUT: int, timeout in hours before build considered as failed, defaults to 2</li>
 * <li>LOG_ROTATOR: int, number of builds to keep, defaults to 5</li>
 * <li>ARTIFACTS: string, wildcarded comma-separated list of artifacts to keep
 * <li>JUNIT_REPORTS: string, wildcarded comma-separated list of junit results to keep
 * </ul>
 * @return
 */
List createChoicesWithPreviousChoice(List defaultChoices, String previousChoice) {
    if (previousChoice == null) {
        return defaultChoices
    }
    choices = defaultChoices.minus(previousChoice)
    choices.add(0, previousChoice)
    return choices
}


@SuppressWarnings('GroovyAssignabilityCheck')
def call(Map config = [:]) {
    // mandatory
    REPO_NAME = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').last().split("\\.git")[0]
    if (REPO_NAME == null) {
        throw new IllegalArgumentException('REPO_NAME is mandatory')
    }

    PRODUCT_VERSION_PROFILE = config.PRODUCT_VERSION_PROFILE ?: '7.4.1'
    SUITE_PROFILE = config.SUITE_PROFILE ?: 'ufpsimple,ufp,runtimecore,em,tdm'
    PRODUCT_PROFILE = config.PRODUCT_PROFILE ?: 'talend-runtime'
    COMPLETE_TESTS_LIST = ''
    MANDATORY_TESTS_LIST = 'ApplyPatchITest'
    TESTS_LIST = config.TESTS_LIST ?: ''
    CUSTOM_JDK = config.JDK ?: 'JDK11'
    PATCH_APPLY = config.PATCH_APPLY ?: true
    PATCH_NAME = config.PATCH_NAME ?: 'LATEST'
    PATCH_LOCATION = config.PATCH_LOCATION ?: 'LATEST'
    PATCH_TDM_VERSION = config.PATCH_TDM_VERSION ?: ''
    PATCH_JOBSERVER_VERSION = config.PATCH_JOBSERVER_VERSION ?: ''

    defaultChoicesProductVersion = ['7.0.1', '7.1.1', '7.2.1', '7.3.1', '7.4.1']
    choicesProductVersion = createChoicesWithPreviousChoice(defaultChoicesProductVersion, PRODUCT_VERSION_PROFILE)

    // must match profiles declared in maven pom.xml
    defaultChoicesProduct = ['talend-runtime', 'talend-esb', 'talend-se']
    choicesProduct = createChoicesWithPreviousChoice(defaultChoicesProduct, PRODUCT_PROFILE)

    // optional
    POM_FILE = config.POM_FILE ?: './runtime/pom.xml'

    //SLACK_CHANNEL = config.SLACK_CHANNEL ?: ''
    SLACK_CHANNEL = monthly_dashboard

    TIMEOUT = config.TIMEOUT ?: 3
    LOG_ROTATOR = config.LOG_ROTATOR ? config.LOG_ROTATOR.toString() : '10'

    // calculated
    GIT_URL = "https://github.com/Talend/${REPO_NAME}.git"
    JOB_NAME = env.JOB_NAME.replaceAll("%2F", "/")
    BRANCH_NAME = env.BRANCH_NAME
    GIT_REPO_LINK = "https://github.com/Talend/${REPO_NAME}/tree/${BRANCH_NAME}"
    GString POD_LABEL = "${REPO_NAME}-${UUID.randomUUID().toString()}"

    // SLACK_INFO PART
    SLACK_INFO = "ESB ITEST `${JOB_NAME}` (<${env.BUILD_URL}console|#${env.BUILD_NUMBER}> | <${GIT_REPO_LINK}|github>)"
    SLACK_INDEXING = ":wink: ${SLACK_INFO} INDEXING"
    SLACK_STARTING = ":crossed_fingers: ${SLACK_INFO} STARTING"
    SLACK_SUCCESS = ":heavy_check_mark: ${SLACK_INFO} SUCCESS"
    SLACK_UNSTABLE = ":thinking_face: ${SLACK_INFO} UNSTABLE"
    SLACK_FAILURE = ":scream: ${SLACK_INFO} FAILURE"
    SLACK_ABORTED = ":zipper_mouth_face: ${SLACK_INFO} ABORTED"
    SLACK_ADDITIONAL = ''

    // branch name is master
    isMaster = (BRANCH_NAME == 'master')

    // branch name starts with maintenance/X.Y
    isMaintenance = BRANCH_NAME.matches('^maintenance/[0-9][.][0-9]')

    // branch name ends with /PROJECT-XXXXX
    isJIRARelated = BRANCH_NAME.matches('.*/[A-Z]+-[0-9]{1,5}$')

    // ARTIFACTS PART
    ARTIFACTS = config.ARTIFACTS ?: '**/target/exam/*/log/tesb.log,**/target/exam/*/log/tesb-command.log,**/target/exam/itests/**/*.log,**/target/exam/*/patches/**/*.log,**/target/services/**/*-startup.log,**/target/services/**/data/**/*,**/target/services/**/logs/*'

    // JUNIT RESULTS
    JUNIT_REPORTS = config.JUNIT_REPORTS ?: '**/target/failsafe-reports/**/*.xml'

    // MAVEN PART
    // use batch mode
    // set timestamp logs
    MAVEN_CUSTOM_OPTIONS = "-B -f ${POM_FILE} -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss,SSS"

    K8S_IMAGE = 'jenkinsxio/builder-maven:0.1.211'
    if (CUSTOM_JDK == 'JDK11') {
        K8S_IMAGE = 'jenkinsxio/builder-maven-java11:0.1.275'
    }

    // execute if current branch is master
    // OR upstream trigger OR manual trigger
    notIndexing = currentBuild.getBuildCauses('jenkins.branch.BranchIndexingCause').isEmpty()
    isUpstreamCause = !currentBuild.getBuildCauses('hudson.model.Cause$UpstreamCause').isEmpty()
    isUserIdCause = !currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').isEmpty()
    executionCondition = isMaster || isUserIdCause || isUpstreamCause

    pipeline {
        agent {
            kubernetes {
                label POD_LABEL
                yaml """
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: maven
                    image: ${K8S_IMAGE}
                    tty: true
                    command:
                    - cat
                    volumeMounts:
                    - name: docker
                      mountPath: /var/run/docker.sock
                  volumes:
                  - name: docker
                    hostPath:
                      path: /var/run/docker.sock
            """
            }
        }

        triggers {
            pollSCM ''
        }

        parameters {
            choice(
                name: 'PRODUCT_PROFILE',
                choices: choicesProduct,
                description: 'Product profile to test'
            )
            choice(
                name: 'PRODUCT_VERSION_PROFILE',
                choices: choicesProductVersion,
                description: 'Product version profile to execute'
            )
            string(
                name: 'SUITE_PROFILE',
                defaultValue: SUITE_PROFILE,
                description: 'Suite profiles to execute'
            )
            string(
                name: 'TESTS_LIST',
                defaultValue: TESTS_LIST,
                description: 'Specific list of tests, separated with commas, accept wildcards. SUITE_PROFILE will be ignored.\nExample: *.em.*ITest,BundleCheckITest\nSee https://maven.apache.org/surefire/maven-failsafe-plugin/examples/single-test.html'
            )
            booleanParam(
                name: 'PATCH_APPLY',
                defaultValue: PATCH_APPLY,
                description: 'Apply patch'
            )
            string(
                name: 'PATCH_NAME',
                defaultValue: PATCH_NAME,
                description: 'Patch name, ie.: LATEST, or Patch_20201023_R2020-10_v1-RT-7.3.1'
            )
            string(
                name: 'PATCH_LOCATION',
                defaultValue: PATCH_LOCATION,
                description: 'Patch location, ie.: LATEST, or URI with protocol file, mvn, http, https'
            )
            string(
                name: 'PATCH_TDM_VERSION',
                defaultValue: PATCH_TDM_VERSION,
                description: 'Expected tdm version in patch. Can be empty'
            )
            string(
                name: 'PATCH_JOBSERVER_VERSION',
                defaultValue: PATCH_JOBSERVER_VERSION,
                description: 'Expected jobserver version in patch. Can be empty'
            )
        }

        options {
            buildDiscarder(logRotator(numToKeepStr: LOG_ROTATOR))
            timeout(time: TIMEOUT, unit: 'HOURS')
        }

        stages {
            stage('Notification') {
                when {
                    expression { executionCondition }
                }
                steps {
                    script {
                        if (notIndexing) {
                            TESTS_LIST_MSG = params.TESTS_LIST? ", TESTS_LIST: `${params.TESTS_LIST}`": ''

                            SLACK_ADDITIONAL = "${SLACK_ADDITIONAL}\n" +
                                    ":gear: PRODUCT_PROFILE: `${params.PRODUCT_PROFILE}`, PRODUCT_VERSION_PROFILE: `${params.PRODUCT_VERSION_PROFILE}`\n" +
                                    "SUITE_PROFILE: `${params.SUITE_PROFILE}`${TESTS_LIST_MSG}\n" +
                                    "JDK: `${CUSTOM_JDK}`\n" +
                                    "PATCH_APPLY: `${params.PATCH_APPLY}`, PATCH_NAME: `${params.PATCH_NAME}`"

                            slackSend(channel: SLACK_CHANNEL, message: "${SLACK_STARTING}${SLACK_ADDITIONAL}")
                            buildName "#${BUILD_NUMBER} ${params.PRODUCT_PROFILE}/${params.PRODUCT_VERSION_PROFILE} ${CUSTOM_JDK} ${params.SUITE_PROFILE} ${params.TESTS_LIST} ${params.PATCH_NAME}"
                        } else {
                            // indexing
                            slackSend(channel: SLACK_CHANNEL, message: SLACK_INDEXING)
                        }
                        container('maven') {
                            withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh', keyFileVariable: 'id_rsa')]) {
                                sh """
                            mvn --version
                            mkdir -p ~/.ssh && echo "Host *" > ~/.ssh/config && echo " StrictHostKeyChecking no" >> ~/.ssh/config
                            cp \$id_rsa ~/.ssh/id_rsa
                            ssh-agent
                            eval \$(ssh-agent) && ssh-add ~/.ssh/id_rsa && ssh-add -l
                            """
                            }
                        }
                    }
                }
            }


            stage('Resolve dependencies') {
                when {
                    expression { notIndexing && executionCondition }
                }

                steps {
                    container('maven') {
                        configFileProvider([configFile(fileId: 'maven-settings-nexus-zl', variable: 'MAVEN_SETTINGS')]) {
                           sh """
                               # additional copy for paxexam to retrieve credentials
                               mkdir ~/.m2
                               cp ${MAVEN_SETTINGS} ~/.m2/settings.xml
                               mvn dependency:resolve dependency:resolve-plugins -s ${MAVEN_SETTINGS} ${MAVEN_CUSTOM_OPTIONS} \
                                -P${params.PRODUCT_PROFILE},${params.PRODUCT_VERSION_PROFILE},${params.SUITE_PROFILE}
                           """
                        }
                    }
                }
            }

            /**
             * Major stage
             * Executes only not indexing
             */
            stage('Integration test') {
                when {
                    expression { notIndexing && executionCondition }
                }

                steps {
                    script {
                        if (params.TESTS_LIST && params.PATCH_APPLY) {
                            COMPLETE_TESTS_LIST = "${MANDATORY_TESTS_LIST},${params.TESTS_LIST}"
                        } else {
                            COMPLETE_TESTS_LIST = params.TESTS_LIST
                        }
                    }
                    container('maven') {
                        configFileProvider([configFile(fileId: 'maven-settings-nexus-zl', variable: 'MAVEN_SETTINGS')]) {
                            withCredentials([usernamePassword(credentialsId: 'nexus-talend-update-credentials', passwordVariable: 'CREDS_PWD', usernameVariable: 'CREDS_USR'),
                                             usernamePassword(credentialsId: 'tesb-qa-runtime-aws', passwordVariable: 'AWS_SECRET_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
                            sh """
                               mvn verify -s ${MAVEN_SETTINGS} ${MAVEN_CUSTOM_OPTIONS} \
                                -Ditests.patch.apply=${params.PATCH_APPLY} -Ditests.patch.name=${params.PATCH_NAME} -Ditests.patch.location=${params.PATCH_LOCATION} \
                                -Ditests.patch.username=\$CREDS_USR -Ditests.patch.password=\$CREDS_PWD \
                                -Ditests.patch.tdm.version=${params.PATCH_TDM_VERSION} -Ditests.patch.jobserver.version=${params.PATCH_JOBSERVER_VERSION} \
                                -Ditests.jdk=${CUSTOM_JDK} -Ditests.suites=${params.SUITE_PROFILE} \
                                -Ditests.aws.access.key.id=\$AWS_ACCESS_KEY_ID -Ditests.aws.secret.key=\$AWS_SECRET_KEY \
                                -P${params.PRODUCT_PROFILE},${params.PRODUCT_VERSION_PROFILE},${params.SUITE_PROFILE} \
                                -Dit.test=${COMPLETE_TESTS_LIST}
                               """
                            }
                        }
                    }
                }
            }

            stage('Generate report') {
                when {
                    expression { notIndexing && executionCondition }
                }

                steps {
                    container('maven') {
                        configFileProvider([configFile(fileId: 'maven-settings-nexus-zl', variable: 'MAVEN_SETTINGS')]) {
                            sh """
                           mvn site -s ${MAVEN_SETTINGS} ${MAVEN_CUSTOM_OPTIONS}
                           """
                        }
                    }
                }
            }
        }

        post {
            always {
                script {
                    if (notIndexing && executionCondition) {
                        // store artifacts
                        if (ARTIFACTS) {
                            archiveArtifacts artifacts: ARTIFACTS
                        }
                        junit JUNIT_REPORTS
                        publishHTML (target : [allowMissing: false,
                                               alwaysLinkToLastBuild: true,
                                               keepAll: true,
                                               reportDir: 'runtime/target/site',
                                               reportFiles: 'failsafe-report.html',
                                               reportName: 'runtime-report',
                                               reportTitles: 'Runtime Report'])
                    }
                }
            }

            success {
                script {
                    if (notIndexing && executionCondition) {
                        slackSend(color: '#36a64f', channel: SLACK_CHANNEL, message: "${SLACK_SUCCESS}${SLACK_ADDITIONAL}")
                    }
                }
            }

            unstable {
                slackSend(color: 'warning', channel: SLACK_CHANNEL, message: "${SLACK_UNSTABLE}${SLACK_ADDITIONAL}")
            }

            failure {
                slackSend(color: '#e81f3f', channel: SLACK_CHANNEL, message: "${SLACK_FAILURE}${SLACK_ADDITIONAL}")
            }

            aborted {
                slackSend(color: 'warning', channel: SLACK_CHANNEL, message: "${SLACK_ABORTED}${SLACK_ADDITIONAL}")
            }
        }
    }

}
