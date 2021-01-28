@Library('myShareLibrary') _


String REPORTPORTAL_URL = "http://18.182.252.2:8080"
Boolean reportportal_active = true
def studioPath = "/opt/rh"

pipeline {
    agent any

    environment {
        String rp_token1 = credentials('rp-token')
        //String rp_token1 = "47a6a4f0-9764-4523-ae46-8cfcec9d32b7"
    }

    stages {
        stage('Hello') {
            steps {
                echo "Hello World"
                //getUser
            }
        }

        stage('Example Build') {
            steps {
                echo "Running ${env.BUILD_NUMBER} ... ${env.BUILD_ID} on ${env.JENKINS_URL}"

            }
        }

        stage('Create file') {
            steps {
                script {
                    echo 'comment send to slack'
                    timedCommand("sleep 10")
   


                    def zipFileName = "${studioPath}/studio-junit.xml"

                    if (fileExists("${zipFileName}")) {
                        echo "${zipFileName} is thererr...."
                    }

                    //echo ${WORKSPACE}


                    def zpname = "${env.JOB_NAME}-${env.BUILD_NUMBER}-Junit.zip"

                    try {

                        if (reportportal_active == true) {
                            //upRp.result2ReportPortalServer(zpname, studioPath, REPORTPORTAL_URL, rp_token1, "734", "tup")
                            //upRp (zipFileName: "${zpname}", studioPath:"${studioPath}",host: ${REPORTPORTAL_URL},token: ${rp_token1}, release: "734", product:"tup")                               
                             reportportal ([zipFileName: zpname, 
                                            studioPath:studioPath,
                                            host: REPORTPORTAL_URL,
                                            token: rp_token1, 
                                            release: "732", 
                                            product:"tupp",
                                            jenkins:"${env.BUILD_NUMBER}"
                                            ])
                            
                            // reportportal(zpname,studioPath,REPORTPORTAL_URL,rp_token1,"731","DI")      
                               
                        }

                    } catch (Exception error) {
                        print error.getMessage()
                        echo "Failed......."
                        echo error.getStackTrace()

                    } finally {
                        echo "finally......."
                    }
              
                }
            }
        }

        stage('Starting ART job') {
            steps {
                echo "building.. ART ....."

            }

        }
    }


    post {
        success {
            script {
                if (!false) {
                    echo 'comment send to slack success'
                }
            }
        }
        unstable {
            script {
                echo 'comment send to slack unstable'
            }
        }
    }
}
