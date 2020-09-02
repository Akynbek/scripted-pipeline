properties([
    parameters([
        booleanParam(defaultValue: true, description: 'Do you want to run terrform apply', name: 'terraform_apply'),
        booleanParam(defaultValue: false, description: 'Do you want to run terrform destroy', name: 'terraform_destroy'),
        choice(choices: ['dev', 'qa', 'prod'], description: '', name: 'environment'),
        string(defaultValue: '', description: 'Provide AMI ID', name: 'ami_id', trim: false)
    ])
])
def aws_region_var = ''
if(params.environment == "dev"){
    aws_region_var = "us-east-1"
}
else if(params.environment == "qa"){
    aws_region_var = "us-east-2"
}
else if(params.environment == "prod"){
    aws_region_var = "us-west-2"
}
def tf_vars = """
    s3_bucket = \"terraform-state-april-class-akynbek\"
    s3_folder_project = \"terraform_ec2\"
    s3_folder_region = \"us-east-1\"
    s3_folder_type = \"class\"
    s3_tfstate_file = \"infrastructure.tfstate\"
    environment = \"${params.environment}\"
    region      = \"${aws_region_var}\"
    public_key  = \"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDHtJP3KbflhOlU2diz1WEjkpeFaP9MKsAwJMhtSe/LqTg10wmdGys4IniKttaz5ESmvb+4Eg4BttqgbDp5/0aROC2BHm1ekLTijZpqN1LzUnSfIu4cHCza6C50ZEpO7fymFbaNSklxIg78dVwVL7xju146WJw7dyxGTQ2K+Adkgq2dggmf19V6xCfevFq1SfP/ir0O9933ueQ+0/+94sGr79yGZk2DSAOKIdILgWDeN8xVpCeToc4LnPO5pm1y2lIwfHq06GwausPo8sYTG4cS5likBJ5km5nIYPVYJ7abVvbyAIBePltLVkp0TidtfK5hZuS76beSSQ4M/4DxV23N2+i08ziKPOqWfs3DiCUGDLOvxSHZI1TS0qr+I4WEDSbJT08cwwFw+NkythlDehdjqKrwNEOwApQmmf//BiZgshiXOT+lbEF1wzuKAaknCGSvA6bEj2NhlrzAY0V17k8mA0HJCZlNEPUnyDGZqN5Wu8C8HP8e7LYlfE6bMldUai0= akyn@Akynbeks-MacBook-Air.local\"
    ami_id      = \"${params.ami_id}\"
"""
node{
    stage("Pull Repo"){
        cleanWs()
        git url: 'https://github.com/ikambarov/terraform-ec2.git'
    }
    withCredentials([usernamePassword(credentialsId: 'jenkins-aws-access-key', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        withEnv(["AWS_REGION=${aws_region_var}"]) {
            stage("Terrraform Init"){
                writeFile file: "${params.environment}.tfvars", text: "${tf_vars}"
                sh """
                    bash setenv.sh ${environment}.tfvars
                    terraform-0.13 init
                """
            }        
            if (terraform_apply.toBoolean()) {
                stage("Terraform Apply"){
                    sh """
                        terraform-0.13 apply -var-file ${environment}.tfvars -auto-approve
                    """
                }
            }
            else if (terraform_destroy.toBoolean()) {
                stage("Terraform Destroy"){
                    sh """
                        terraform-0.13 destroy -var-file ${environment}.tfvars -auto-approve
                    """
                }
            }
            else {
                stage("Terraform Plan"){
                    sh """
                        terraform-0.13 plan -var-file ${environment}.tfvars
                    """
                }
            }
        }        
    }    
}