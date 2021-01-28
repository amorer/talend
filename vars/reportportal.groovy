import groovy.json.JsonSlurper

def call(Map args = [:]) {
       println(args.toString())
    if (args.size() < 7) {
     
        throw new Exception('Missing input parameter')
        
    }
    }
    
   // String uploadResult = copyJunitResult(args.zipFileName, args.studioPath, args.host, args.token)
  //  addAttribute(uploadResult, args.host, args.token, args.release, args.product)
}


def copyJunitResult(zipFileName, studioPath, host, token) {
    try {
        sh "zip ${zipFileName} ${studioPath}/studio-junit.xml"
        upload_result = sh(
                script: "curl -X POST '${host}/api/v1/monthly_studio_release/launch/import' -H 'accept: */*' -H 'Content-Type: multipart/form-data' -H 'Authorization: bearer ${token}' -F file=@${zipFileName}",
                returnStdout: true
        ).trim()
        return upload_result
    } catch (Exception error) {
        println error.getMessage()
    } finally {
        if (fileExists("${zipFileName}")) {
            sh "rm -f ${zipFileName}"
        } else {
            println "...No ${zipFileName} founded"
        }
    }
}


def addAttribute(result, host, token, release, product) {

    try {
        //find uuid from zip upload result info
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

        if (launchid > 0) {   //add attribute and description for launch
            sh "curl -X PUT '${host}/api/v1/monthly_studio_release/launch/${launchid}/update' -H 'accept: */*' -H  'Content-Type: application/json' -H 'Authorization: bearer ${token}' -d '{\"attributes\": [{\"key\": \"release\",\"value\": \"${release}\"}], \"description\": \"${release} monthly studio ${product} test\", \"mode\": \"DEFAULT\"}'"
        }
    } catch (Exception error) {
        println error.getMessage()
    }

}
