1st


## Update the etc/hosts On all nodes and master

Disable SELINUX on all nodes

~~~~~~~~~~~
vi /etc/selinux/config

# and edit SELINUX=disabled

~~~~~~~~~

# append to /etc/hosts
masterip kube-master
minion1ip kube-node1
minion2ip kube-node2


command to do the same

~~~~~~~~~
# echo '192.168.110.198 kube-master
192.168.110.199 kube-node1
192.168.110.202 kube-node2' >> /etc/hosts

~~~~~~~~~


Disable firewalld on all nodes and master:

~~~~~~~~~~~~~
systemctl disable firewalld
systemctl stop firewalld

~~~~~~~~~~~~~~

##Kubernetes master

#Install Kubernetes master packages:

~~~~~~~
yum install etcd kubernetes-master -y 

~~~~~~~~


Configuration:

~~~~~~~~~~~~
# /etc/etcd/etcd.conf
# leave rest of the lines unchanged
ETCD_LISTEN_CLIENT_URLS="http://0.0.0.0:2379"
ETCD_LISTEN_PEER_URLS="http://localhost:2380"
ETCD_ADVERTISE_CLIENT_URLS="http://0.0.0.0:2379"

# /etc/kubernetes/config
# leave rest of the lines unchanged
KUBE_MASTER="--master=http://kube-master:8080"

# /etc/kubernetes/apiserver
# leave rest of the lines unchanged
KUBE_API_ADDRESS="--address=0.0.0.0"
KUBE_ETCD_SERVERS="--etcd_servers=http://kube-master:2379"

~~~~~~~~~~~


Start Etcd:
~~~~~~~~~
systemctl start etcd

~~~~~~~~~


Install and configure Flannel overlay network fabric (this is needed so that containers running on different servers can see each other):

~~~~~~
yum install flannel -y 

~~~~~~

Set the Flannel configuration in the Etcd server:
~~~~~~~~~~~
etcdctl mk /atomic.io/network/config '{"Network":"172.17.0.0/16"}'

~~~~~~~~~~~


Point Flannel to the Etcd server:

~~~~~~~~~~~~~~
# /etc/sysconfig/flanneld
FLANNEL_ETCD="http://kube-master:2379"
Enable services so that they start on boot:

systemctl enable etcd
systemctl enable kube-apiserver
systemctl enable kube-controller-manager
systemctl enable kube-scheduler
systemctl enable flanneld

~~~~~~~~~~~~~~

Reboot server

~~~~~~~~~~~
reboot

~~~~~~~~~~~

## Kubernetes node


Install Kubernetes node packages:

~~~~~~~~~
yum install docker kubernetes-node -y

~~~~~~~~~


The next two steps will configure Docker to use overlayfs for better performance

Delete the current docker storage directory:

~~~~~~~~~~~~
systemctl stop docker
rm -rf /var/lib/docker

~~~~~~~~~~~~


Change configuration files:

~~~~~~~~~~~~
# /etc/sysconfig/docker
# leave rest of lines unchanged
OPTIONS='--selinux-enabled=false'

# /etc/sysconfig/docker
# leave rest of lines unchanged
DOCKER_STORAGE_OPTIONS=-s overlay

~~~~~~~~~~~


Configure kube-nodes  to use our previously configured master:

~~~~~~~~~~~~~~~
# /etc/kubernetes/config
# leave rest of lines unchanged
KUBE_MASTER="--master=http://kube-master:8080"

# /etc/kubernetes/kubelet
# leave rest of the lines unchanged
KUBELET_ADDRESS="--address=0.0.0.0"
# comment this line, so that the actual hostname is used to register the node
# KUBELET_HOSTNAME="--hostname_override=127.0.0.1"
KUBELET_API_SERVER="--api_servers=http://kube-master:8080"

~~~~~~~~~~~~



Install and configure Flannel overlay network fabric (again - this is needed so that containers running on different servers can see each other):

~~~~~~~~~~
yum install flannel -y 

~~~~~~~~~~


Point Flannel to the Etcd server:

~~~~~~~~~~~~~~
# /etc/sysconfig/flanneld
FLANNEL_ETCD="http://kube-master:2379"

~~~~~~~~~~~~~~


Enable services:

~~~~~~~~~~~~
systemctl enable docker
systemctl enable flanneld
systemctl enable kubelet
systemctl enable kube-proxy

~~~~~~~~~~~~



Reboot the nodes

~~~~~~~~
restart

~~~~~~~~~



















2nd Debugging

Debugging service-account

http://stackoverflow.com/questions/33528398/why-dont-i-have-a-default-serviceaccount-on-kubernetes



cloud config file of core os

https://gist.github.com/thuey/ae327f15778040cf84fb


http://stackoverflow.com/questions/32185851/kubernetes-api-server-unable-to-listen-for-secure


No API token found for service account "default
https://github.com/kubernetes/kubernetes/issues/29549


WORKED to create secrets
	http://stackoverflow.com/questions/36228216/how-to-create-a-kubernetes-serviceaccount-with-token

API server DOCS
	http://kubernetes.io/docs/admin/kube-apiserver/

	https://github.com/kubernetes/kubernetes/issues/27973



Mar 14 12:31:54 kubemaster01 kube-apiserver[1126]: E0314 12:31:54.338817    1126 apiserver.go:269] Unable to listen for secure (open /var/run/kubernetes/apiserver.crt: no such file or directory); will try again.
Mar 14 12:32:09 kubemaster01 kube-apiserver[1126]: E0314 12:32:09.339238    1126 apiserver.go:269] Unable to listen for secure (open /var/run/kubernetes/apiserver.crt: no such file or directory); will try again.


Additional info:

This is probably because kube-apiserver runs as user kube and it doesn't have a write permission on /var/run.

As a workaround, I created the drop-in systemd config and it worked well.


https://bugzilla.redhat.com/show_bug.cgi?id=1201965





STEPS for certs files

https://github.com/kubernetes/kubernetes/issues/12186


@iremA 
use these commands to make crt and key:

openssl genrsa -out ca.key 2048

openssl req -x509 -new -nodes -key ca.key -subj "/CN=abc.com" -days 5000 -out ca.crt

openssl genrsa -out server.key 2048

openssl req -new -key server.key -subj "/CN=vm-56-65" -out server.csr
**there "/CN=vm-56-65" shuold be "/CN=[yourhostname]"
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 5000

put these keys into /var/run/kubernetes/

and start api-server as:

./kube-apiserver --logtostderr=true --log-dir=/var/log/ --v=0 --admission_control=ServiceAccount --etcd_servers=http://127.0.0.1:4001 --insecure_bind_address=0.0.0.0 --insecure_port=8080 --kubelet_port=10250 --service-cluster-ip-range=10.0.0.1/24 --allow_privileged=false --service-node-port-range='30000-35535' --client_ca_file=/var/run/kubernetes/ca.crt --tls-private-key-file=/var/run/kubernetes/server.key --tls-cert-file=/var/run/kubernetes/server.crt

start controller-manager with these two flags:

--root-ca-file="/var/run/kubernetes/ca.crt"
--service-account-private-key-file="/var/run/kubernetes/server.key"

now try to create a pod


























3rd official


Prepare the hosts:

less /etc/selinux/config
SELINUX=disabled

Create a /etc/yum.repos.d/virt7-docker-common-release.repo on all hosts - centos-{master,minion-n} with following information.
[virt7-docker-common-release]
name=virt7-docker-common-release
baseurl=http://cbs.centos.org/repos/virt7-docker-common-release/x86_64/os/
gpgcheck=0


Install Kubernetes, etcd and flannel on all hosts - centos-{master,minion-n}. This will also pull in docker and cadvisor.


yum -y install --enablerepo=virt7-docker-common-release kubernetes etcd flannel



## Update the etc/hosts On all nodes and master


command to do the same

~~~~~~~~~
# echo '192.168.110.209 centos-master
192.168.110.206 centos-minion-1' >> /etc/hosts

~~~~~~~~~


edited on KUBE_MASTER="--master=http://centos-master:8080"
in /etc/kubernetes/config


Disable firewalld on all nodes and master:

~~~~~~~~~~~~~
systemctl disable firewalld
systemctl stop firewalld

~~~~~~~~~~~~~~



##Configure the Kubernetes services on the master.

etcdctl mk /kube-centos/network/config "{ \"Network\": \"172.30.0.0/16\", \"SubnetLen\": 24, \"Backend\": { \"Type\": \"vxlan\" } }"


for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler flanneld; do
	systemctl status $SERVICES | grep "active"
done



Master

for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler flanneld; do
	systemctl restart $SERVICES
done


for SERVICES in etcd kube-apiserver kube-controller-manager kube-scheduler flanneld; do
	systemctl status $SERVICES | grep "active"
done


NODE


for SERVICES in kube-proxy kubelet flanneld docker; do
    systemctl restart $SERVICES
done


for SERVICES in kube-proxy kubelet flanneld docker; do
    systemctl status $SERVICES | grep "active"
done


kubectl apply -f "https://github.com/microservices-demo/microservices-demo/blob/master/deploy/kubernetes/complete-demo.yaml?raw=true"

