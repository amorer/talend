@Library('myShareLibrary') _

pipeline {
    agent any
    stages {
        stage ('Example') {
            steps {
                // up2rp.info 'Starting' 
                script { 
                    up2rp.copyInfo 'Starting'
                    up2rp.sendWarning 'Nothing to do!'
                    def zipFileName="kk"
                    def studioPath="ss"
                    def RP_HOST_PORT="1.1.1.1"
                    def RP_ACCESS_TOKEN="1234567890"
                    //up2rp.copyJunit2Reportportal(zipFileName,studioPath,RP_HOST_PORT,RP_ACCESS_TOKEN) 
                     sayHello 'Dave'
                }
            }
        }
        
        stage ('Buildddddd') {
            steps {
                // up2rp.info 'Starting' 
                script { 
                    up2rp.copyInfo 'Builingddddddd'
                    up2rp.sendWarning 'warning.......!'
                    def zipFileName="dd"
                    def studioPath="cc"
                    def RP_HOST_PORT="2.2.2.2"
                    def RP_ACCESS_TOKEN="1234567890"
                    //up2rp.copyJunit2Reportportal(zipFileName,studioPath,RP_HOST_PORT,RP_ACCESS_TOKEN) 
                     sayHello ('TOM')
                }
            }
        }
    }
}
