import groovy.json.JsonSlurper

def result2ReportPortalServer (zipFileName,studioPath,host,token,release,product){
    String uploadResult=copyJunit2ReportPortal(zipFileName,studioPath,host,token)
     def filer = new File(zipFileName)
        filer.delete()
        if(filer.exists()){
            println " result2ReportPortalServer ...not removed... " + zipFileName 
        }else{
             println " result2ReportPortalServer ...removedddd... " + zipFileName 
        }
    addAttr2Launch(uploadResult,host,token,release,product)
    
     if(filer.exists()){
            println " result2ReportPortalServer not removed... " + zipFileName 
        }else{
             println " result2ReportPortalServer removedddd... " + zipFileName 
        }
    

}


def copyJunit2ReportPortal(zipFileName,studioPath,host,token) {
    try {
        sh "zip ${zipFileName} ${studioPath}/studio-junit.xml"
        upload_result = sh (
                script: "curl -X POST '${host}/api/v1/monthly_studio_release/launch/import' -H 'accept: */*' -H 'Content-Type: multipart/form-data' -H 'Authorization: bearer ${token}' -F file=@${zipFileName}",
                returnStdout: true
        ).trim()
        return upload_result
    } catch (Exception error) {
        println error.getMessage()
    }finally{
      println " ...finally... " + zipFileName
       if (fileExists("${zipFileName}")) {
								sh "rm -f ${zipFileName}"
            println " remove... " + zipFileName 
							} else {
								echo "...No ${zipFileName} founded"
							}			
            
    }
}


def addAttr2Launch(result, host, token, release, product) {
    try {
        //find uuid from upload zip result message
        int start = result.indexOf("id =")
        int end = result.indexOf("is successfully imported")
        def uuid = result.substring(start + 5, end).trim()

        //find launch id
        def launch = sh(
                script: "curl -X GET '${host}/api/v1/monthly_studio_release/launch/?filter.eq.uuid=${uuid}' -H 'accept: */*' -H 'Authorization: bearer ${token}'",
                returnStdout: true
        ).trim()

        def status = new JsonSlurper().parseText(launch)
        int launchid = status.content.id[0]
        println launchid
        if (launchid > 0) {   //add attribute and description for the launch
             sh "curl -X PUT '${host}/api/v1/monthly_studio_release/launch/${launchid}/update' -H 'accept: */*' -H  'Content-Type: application/json' -H 'Authorization: bearer ${token}' -d '{\"attributes\": [{\"key\": \"release\",\"value\": \"${release}\"}], \"description\": \"${release} monthly studio ${product} test\", \"mode\": \"DEFAULT\"}'"
        }
    } catch (Exception error) {
        print error.getMessage()
    }
}
