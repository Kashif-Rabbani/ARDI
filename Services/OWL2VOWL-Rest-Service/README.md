# OWL2VOWL Service

### Getting Started

This is a service which is used by BDI project to convert OWL/RDFS to VOWL (JSON) format which is a specific format requried by the VOWL visualization framework. 

It contains OWL2VOWL as external library and provides a rest service to request with two parameters i.e. RDFS-format-file-path & VOWL-json-file-path in post request and returns a JSON response with the file-name and path of the VOWL-json-file.   

#### Libraries 

 - **OWL2VOWL external dependency library**
    - Jar file is generated from https://github.com/Kashif-Rabbani/OWL2VOWL (This project is forked fromhttps://github.com/VisualDataWeb/OWL2VOWL). 
    
    - Updates are made in the forked project. For more details see the commits history. 
    
    - Command to generate jar with dependencies: `mvn clean install` and `mvn clean compile assembly:single` 
    - Jar file: OWL2VOWL-0.3.6.jar 
    
    -- OPTIONAL: Jar file is available in the lib folder. Use the following `command` to add it as a maven dependency.
    ```sh
    mvn install:install-file -Dfile=/homePath/BDI/RDFS/lib/OWL2VOWL-0.3.6.jar -DgroupId=org.visualdataweb.vowl.owl2vowl -DartifactId=OWL2VOWL -Dversion=0.3.6 -Dpackaging=jar
     ```
    
