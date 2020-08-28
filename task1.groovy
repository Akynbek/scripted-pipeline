properties([
    parameters([
        string(defaultValue: '', description: 'Please enter VM IP', name: 'nodeIP', trim: true)
        ])
    ])
if (nodeIP?.trim()) {
    node {
        withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-master-ssh-key', keyFileVariable: 'SSHKEY', passphraseVariable: '', usernameVariable: 'SSHUSERNAME')]) {
            stage('Git Pull') {
                git changelog: false, poll: false, url: 'https://github.com/ikambarov/melodi.git'
            }
            stage('Install Apache') {
                sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} yum install httpd -y'
            }
            stage("Start Apache") {
                sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} systemctl start httpd'
            }
            stage("Enable Apache") {
                sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} systemctl enable httpd'
            }
            stage("Copy Files") {
                sh 'scp -r -o StrictHostKeyChecking=no -i $SSHKEY *  $SSHUSERNAME@${nodeIP}:/var/www/html/'
            }
            stage("Change ownership") {
                sh 'ssh -o StrictHostKeyChecking=no -i $SSHKEY $SSHUSERNAME@${nodeIP} chown -R apache:apache /var/www/html'
            }
            stage("Clean Workspace"){
                'cleanWs()'
            }
        }
    }
}
else {
    error 'Please enter valid IP address'
}