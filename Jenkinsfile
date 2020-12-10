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
                }
            }
        }
    }
}
