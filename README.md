Benchmarks for Code Offloading on the Cloud
====================================================================

The benchmarks are designed for Android phones and to run them there are following requirements:


Requirements
--------------------
1. A server having Linux x86 as operation system.
2. Dalvik VM libraries installed and configured in a file named “rund.sh”, described here: 
https://github.com/alirezaostovar/codeoffloading/blob/master/rund.sh

Requirements (Maven build)
--------------------------
Each benchmark has two versions, one for client and the other for server. The server 
version of each project is located in a directory with _Server postfix. The server application can be built by running the following maven commands within the application's root directory.

```xml
mvn clean package
mvn install
```

Then the produced apk file must be placed next to the configured rund 
file above. This apk can be run on android VM using following fixed command:

```xml
./rund.sh -cp projectname.apk MC.NetClasses.Main
```

The client version of each project is located in a directory with _Client postfix. 
Before opening and compiling the project and producing apk file, the server IP must be set.
This can be done with setting the variable called IPAddress in the file “NetInfo.java” 
inside MC package. The client application can be built and deployed to the connected device by running the following maven commands within the application's root directory.

```xml
mvn clean package
mvn install
mvn android:deploy
```

It should be noted that, as the variable IPAddress is a byte array, if the server IP 
contains numbers more than 127, this number must be presented as a negative number using 
Two’s Complement system. For example, for the number 255, it will be -1.

Each application has two buttons, one for running the test locally and the other one for 
running on the cloud. It is better to execute the tests separately, for example first 
locally, then on cloud. Depending on the resource-intensiveness of each test, it might 
take several seconds or even minutes to be executed. For running the test on the cloud, 
the server version must be running on the cloud before pressing the designated button. 
This was described above.

The network uploading and downloading rates are measured using the application called 
Internet Speed Test, available on https://play.google.com/store/apps/details?id=pl.speedtest.android&hl=en

Example
-------------------------
For example for the project called “Assignment”, there are two versions for running on
 server and client. The former is located in the directory AllApps_Server/Assignment_Server
  and the latter is located in the directory AllApps_Client/Assignment_Client.

After opening the server version in Eclipse and compiling it, the produced “Assignment.apk”
 file must be placed next to the file “rund.sh”, described above. Then, this file can be 
 executed using following command:
 
 ```xml
./rund.sh -cp Assignment.apk MC.NetClasses.Main
```

Then, the client version of project must be opened in Eclipse. The server IP must be set
 with changing the IPAddress variable located in the java file named NetInfo in the 
 package MC. Next, the project must be compiled and the file “Assignment.apk” which is 
 produced from this client version must be installed on the phone.
