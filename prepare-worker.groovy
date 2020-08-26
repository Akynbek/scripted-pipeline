node {
    stage('Init') {
        withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-master-ssh-key', keyFileVariable: 'SSHKEY', passphraseVariable: '', usernameVariable: 'SSHUSERNAME')]) {
            sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY root@104.131.110.80 yum install epel-release -y'
        }
    }
    stage("Install git"){
            sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY root@104.131.110.80 yum install git -y'

    }
        stage("Install Java"){
            sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY root@104.131.110.80 yum install java-1.8.0-openjdk-devel -y'
        
    }
}