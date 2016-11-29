## Setting up secure private docker registry Centos/Redhat


## Install Private Docker Registry on Centos 7

Login as root

~~~~~~
 sudo -s

~~~~~~

Update all packages and install docker registry

~~~~~~~~
 yum update -y
 yum install docker-registry -y

~~~~~~~~


Enable and start docker registry service

~~~~~~~~
 systemctl enable docker-registry.service
 service docker-registry start

~~~~~~~~

This should start your docker registry at default port 5000
You can verify it by using curl

~~~~~~
 curl <ip>:5000 

~~~~~~

Output should be "\"docker-registry server\""




#You can also customise the registry storage path if you need


Edit the file /etc/docker-registry.yml

~~~~~~~
 vi /etc/docker-registry.yml

~~~~~~~ 

search the storage path location and change it.

~~~~~~~~
local
storage_path =

~~~~~~~~

Once the changes are completed restart docker registry


#Secure Docker Private Registry

In order to use docker registry with secure URL, try to install apache and configure SSL.
install apache with mod SSL.

~~~~~~~
 yum install httpd mod_ssl -y

~~~~~~~


Create user authentication using htpasswd for docker registry

~~~~~~~
 htpasswd -c /etc/httpd/.htpassword USERNAME

~~~~~~~

# Configure SSL

First we need a self-signed SSL certificate. Since Docker currently doesn’t allow you to use self-signed SSL certificates this is a bit more complicated than usual.We will have to set up our system to act as our own certificate signing authority.

In the first step create a new root key with:

~~~~~~~
 mkdir -p /data/ssl/certs
 cd /data/ssl/certs
 openssl genrsa -out dockerCA.key 2048

~~~~~~~

Then create a root certificate, you don’t have to answer the upcoming question, just hit enter.

~~~~~
 openssl req -x509 -new -nodes -key dockerCA.key -days 3650 -out dockerCA.crt

~~~~~


Then create a private key for your Server:

~~~~~~~~
 openssl genrsa -out my.domain.ch.key 2048
 openssl genrsa -out xor.dockerrepo.com.key 2048

~~~~~~~~


Next a certificate signing request is needed. Answer the upcoming question for “Common Name” with the domain of your server, e.g: my.domain.ch. In this example you would access your private docker registry with my.domain.ch at the end. Don’t provide a challenge password.

~~~~~~~~
# openssl req -new -key my.domain.ch.key -out my.domain.ch.csr
  openssl req -new -key xor.dockerrepo.com.key -out xor.dockerrepo.com.csr

~~~~~~~~

Now we need to sign the certificate request

~~~~~~~
#  openssl x509 -req -in my.domain.ch.csr -CA dockerCA.crt -CAkey dockerCA.key -CAcreateserial -out my.domain.ch.crt -days 3650
   openssl x509 -req -in xor.dockerrepo.com.csr -CA dockerCA.crt -CAkey dockerCA.key -CAcreateserial -out xor.dockerrepo.com.crt -days 3650

~~~~~~~


Now that you have self signed certificates open your ssl.conf and add proxy settings before "< / VirtualHost>"

~~~~~~~
 vi /etc/httpd/conf.d/ssl.conf

~~~~~~~


Add the following entries before "< / VirtualHost>"

~~~~~~~
ProxyRequests off
 ProxyPreserveHost on
 ProxyPass / http://127.0.0.1:5000/
 ProxyPassReverse / http://127.0.0.1:5000/

<Location />
 Order deny,allow
 Allow from all
AuthName "Registry Authentication"
 AuthType basic
 AuthUserFile "/etc/httpd/.htpassword"
 Require valid-user
 </Location>


# Allow ping and users to run unauthenticated.
 <Location /v1/_ping>
 Satisfy any
 Allow from all
 </Location>


 # Allow ping and users to run unauthenticated.
 <Location /_ping>
 Satisfy any
 Allow from all
 </Location>
~~~~~~~

Change the valid SSL certificate paths

SSLCertificateFile
SSLCertificateKeyFile


Disable selinux  else it wont start httpd service

~~~~~~~~
 setenforce 0

~~~~~~~~

Now restart httpd service.

~~~~~~~
 service httpd restart

~~~~~~~

Since the certificates we just generated aren’t verified by any known certificate authority (e.g.: VeriSign), we need to tell any clients that are going to be using this Docker registry that this is a legitimate certificate

To do this locally so that we can use Docker from the Docker registry server itself:

~~~~~~~~
 update-ca-trust enable
 cp dockerCA.crt /etc/pki/ca-trust/source/anchors/
 update-ca-trust extract

~~~~~~~~

If your domain name doesnt have a DNS then please enter your ip and domain name in /etc/hosts

~~~~~~
 vi /etc/hosts

 #add entry (example)

 ip        xor.dockerrepo.com 
~~~~~~
After that our host accepts the certificate and we should be able to access our private docker registry with https.

~~~~~~~
# curl https://myuser:test@my.domain.ch
 curl https://anand:anand@xor.dockerrepo.com

~~~~~~~

output must be "\"docker-registry server\""


# To access repo from other clients

We need to copy the certificate on new clients.

~~~~~~~~
  scp <username>@<repo-ip>:/etc/pki/ca-trust/source/anchors/dockerCA.crt /etc/pki/ca-trust/source/anchors/
  update-ca-trust enable 
  update-ca-trust extract
  systemctl restart docker

~~~~~~~~

Update the etc/hosts file to point out the correct ip and domain

Entry should look something like 

~~~~~~
<ip> <my.domain.ch>
192.168.110.198 xor.dockerrepo.com
~~~~~~

#Check if everything is working fine on client
Login to the repo from client

~~~~~~~
# docker login https://my.domain.ch/


# docker login https://xor.dockerrepo.com/
# Enter your username and password when asked for
# Output must look like
# WARNING: login credentials saved in /root/.docker/config.json
# Login Succeeded
~~~~~~~


If login isnt successful disable firewall on the repo-server

~~~~~~~
  systemctl stop firewalld
  systemctl status firewalld

~~~~~~~

this is optional try without this step first

update servername in /etc/httpd/conf/httpd.conf

~~~~~~~~~
 vi /etc/httpd/conf/httpd.conf

# myentry
# ServerName xor.dockerrepo.com:80

~~~~~~~~~

Links for reference 
http://www.cloudkb.net/install-private-docker-registry-on-centos-7/


https://www.dropbit.ch/private-docker-registry-with-nginx-on-centos-7/
