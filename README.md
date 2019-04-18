# Frontend NodeJs Web Project

### Getting Started

 - Requirements: Node v8.0.0 is required. Version greater than this might encounter issues in file uploading module.

# Backend Java Maven Project - RDFS

### Getting Started

#### Libraries 

 - **OWL2VOWL external dependency library**
    - Jar file is generated from https://github.com/Kashif-Rabbani/OWL2VOWL (This project is forked fromhttps://github.com/VisualDataWeb/OWL2VOWL). 
    
    - Updates are made in the forked project. For more details see the commits history. 
    
    - Command to generate jar with dependencies: `mvn package -P standalone-release` 
    - Jar file: OWL2VOWL-0.3.6.jar 
    
    Jar file is available in the lib folder. Use the following `command` to add it as a maven dependency.
    ```sh
    mvn install:install-file -Dfile=/home/kashif/Documents/GIT/TestBDI/BDI/RDFS/lib/OWL2VOWL-0.3.6.jar -DgroupId=org.visualdataweb.vowl.owl2vowl -DartifactId=OWL2VOWL -Dversion=0.3.6 -Dpackaging=jar
     ```
    

#### Troubles
In Ubuntu/Linux OS, you might encounter JDK versioning issue. You should install open-jdk-8. Sometimes it does not install javafx with it. You might need to install it separately. Or in the worst case, you might need to follow these steps:

 -  Go to Oracle's JJava SE Development Kit 8 Downloads page (https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
 - Download the latest linux version from there (currently it is jdk-8u91-linux-x64.tar.gz).
 - When you extract the tar file you downloaded, you will find jfxrt.jar in the jre/lib/ext directory.
 - Copy jfxrt.jar file in your open-jdk-8 folder in jre/lib/ext directory. 
 - It will resolve the issue regarding import javafx.util.pair in TempFiles.java 