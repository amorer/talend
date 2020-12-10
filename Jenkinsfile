@Library('myShareLibrary') _

pipeline {
    agent any
    stages {
        stage ('Example') {
            steps {
                // up2rp.info 'Starting' 
                script { 
                    //sayHello 'Dave'
                    
                    upRp.copyInfo 'Starting'
                    upRp.sendWarning 'Nothing to do!'
                    
                    upRp.anotherMethord 'kkkkkk'
                    //upRp.onlyOneParameter 'ZZZIPName'
                    
                    def zipFileName="kk"
                    def studioPath="ss"
                    def RP_HOST_PORT="1.1.1.1"
                    def RP_ACCESS_TOKEN="1234567890"
                    upRp.copyJunit2Reportportal(zipFileName,studioPath,RP_HOST_PORT,RP_ACCESS_TOKEN) 
                    
                }
            }
        }
        
        stage ('Buildddddd') {
            steps {
                // up2rp.info 'Starting' 
                script { 
                    upRp.copyInfo 'Builingddddddd'
                    upRp.sendWarning 'warning.......!'
                    def zipFileName="dd"
                    def studioPath="cc"
                    def RP_HOST_PORT="2.2.2.2"
                    def RP_ACCESS_TOKEN="1234567890"
                    upRp.copyJunit2Reportportal(zipFileName,studioPath,RP_HOST_PORT,RP_ACCESS_TOKEN) 
                    // sayHello ('TOM')
                }
            }
        }
    }
}
