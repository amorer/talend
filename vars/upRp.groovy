def copyInfo(message) {
    echo "INFO: ${message}"
}

def anotherMethord(message) {
    echo "WARNING: ${message}"
}

def sendWarning(message) {
    echo "this is the message: ${message}"
}


def onlyOneParameter(zipFileName) {
     println "...CopyJunit2Reportportal... ${zipFileName}... ${reportport_Host},,,${reportPortal_Token} " 
     try {
                            
                            sh "zip ${zipFileName} ${studioPath}/studio-junit.xml"
                           //  sh "zip ${zipFileName} ${studioPath}/studio-junit.xml"
                          // sh "curl -X POST '${reportport_Host}/api/v1/monthly_studio_release/launch/import' -H 'accept: */*' -H 'Content-Type: multipart/form-data' -H 'Authorization: bearer ${reportPortal_Token}' -F file=@${zipFileName}"
                            // sh "curl -X POST 'http://192.168.17.33:9528/api/v1/monthly_studio_release/launch/import' -H 'accept: */*' -H 'Content-Type: multipart/form-data' -H 'Authorization: bearer ${reportPortal_Token}' -F file=@${zipFileName}"
                          echo "posted......."
                        } catch (Exception error) {
                            //error e.getMessage()
                            print error.getMessage()
                            //currentBuild.result = 'FAILURE'
                        }finally{
                            echo "finally......."
                            sh "rm -f ${zipFileName}"
                        }
   }