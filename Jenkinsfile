Boolean is_reportportal_active=false
String REPORTPORTAL_URL="http://18.182.252.2:8080"
Boolean reportportal_active=true
def studioPath="/opt/rh"

pipeline {
   agent any
   
    environment {
        //rp_token1 = credentials('rp-token')
        String rp_token1="47a6a4f0-9764-4523-ae46-8cfcec9d32b7"
    }

   stages {
      stage('Hello') {
         steps {
            echo "Hello World"
        
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
                        
                          def zipFileName="${studioPath}/studio-junit.xml"
            
            if (fileExists("${zipFileName}")) {
                echo "${zipFileName} is thererr...."
            } 
            
                        //echo ${WORKSPACE}
                        
                        def folder="maintenance_7.3"
            
                              
                    echo "${env.JOB_NAME}${env.BUILD_NUMBER}"
                  
                  echo "folder${env.BUILD_NUMBER}"
                  
                  def zpname="${env.JOB_NAME}-${env.BUILD_NUMBER}-Junit.zip"
                   
                  echo "${folder.length() > 0 ? folder+'/' : ''}${env.JOB_NAME}/${env.BUILD_NUMBER}"
                   
                 try{
                      
                           
                           upRp.result2ReportPortalServer(zpname,studioPath,REPORTPORTAL_URL,rp_token1,"734","tup")
                          
                           if(is_reportportal_active==true) {
                               
                               upload_result = sh (
                                script: "curl -X POST 'http://18.182.252.2:8080/api/v1/monthly_studio_release/launch/import' -H 'accept: */*' -H 'Content-Type: multipart/form-data' -H 'Authorization: bearer ${rp_token1}' -F file=@${zpname}",
                                returnStdout: true
                            ).trim()
                            echo "Git committer email: ${upload_result}"
                            
                            if("${upload_result}.contains('successfully imported')"){
                                print  "find importttt....."
                                
                            }

                              // sh "curl -X POST 'http://18.182.252.2:8080/api/v1/monthly_studio_release/launch/import' -H 'accept: */*' -H 'Content-Type: multipart/form-data' -H 'Authorization: bearer ${rp_token1}' -F file=@${zpname}" 
                               echo "curl....."
                           }
                       //  }
                       
                 } catch (Exception error) {
                            print error.getMessage()
                             echo "Failed......."
                            echo error.getStackTrace()
                           
                            //currentBuild.result = 'FAILURE'
                           // currentBuild.result = 'SUCCESS'
                        }finally{
                            echo "finally......."
                        }
                        //sh "curl -X POST 'http://192.168.17.33:8080/api/v1/monthly_studio_release/launch/import' -H 'accept: */*' -H 'Content-Type: multipart/form-data' -H 'Authorization: bearer 905244a2-a608-4fb1-8065-1930f40b9ab8' -F file=@${zpname}"
                        //zip archive: true, dir: '.', glob: 'tuj.xml', zipFile: 'coverage-files.zip'
                    }
                }
            }
            
            stage ('Starting ART job') {
                 steps {
                    echo "building.. ART ....."
                  
                    //build job: "test_github_local", parameters: [string(name: "FromUpstream",  value: "Upstream")]
                 }
                     
                 }
    }
    
    
    
    post {
        success {
            script {
                if(!false) {
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
