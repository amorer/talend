import groovy.json.JsonSlurper

def call(Map args = [:]) {
     println args
     println 'in call methord'
     
     
     if(args.size()<6) {
          println(args.toString())
          throw new Exception('Missing input parameter')
     }
     String uploadResult=copyJunit2ReportPortal(args.zipFileName,args.studioPath,args.host,args.token)
     addAttr2Launch(args.uploadResult,args.host,args.token,args.release,args.product)
}

//def result2ReportPortalServer (zipFileName,studioPath,host,token,release,product){
//    String uploadResult=copyJunit2ReportPortal(zipFileName,studioPath,host,token)
//    addAttr2Launch(uploadResult,host,token,release,product)

//}


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
        if (fileExists("${zipFileName}")) {
            sh "rm -f ${zipFileName}"            
        } else {
            println "...No ${zipFileName} founded"
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
        
        if (launchid > 0) {   //add attribute and description for the launch
             sh "curl -X PUT '${host}/api/v1/monthly_studio_release/launch/${launchid}/update' -H 'accept: */*' -H  'Content-Type: application/json' -H 'Authorization: bearer ${token}' -d '{\"attributes\": [{\"key\": \"release\",\"value\": \"${release}\"}], \"description\": \"${release} monthly studio ${product} test\", \"mode\": \"DEFAULT\"}'"    
        }
    } catch (Exception error) {
        println error.getMessage()
    }
   
}
