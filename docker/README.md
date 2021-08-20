# phoss SMP Docker configuration

This folder contains the Docker configuration files for phoss SMP.
It is based on the official `tomcat:9-jre11` image since v5.1.2.
It was previously based on the official `tomcat:8.5` image.

Prebuild images are available from:
* https://hub.docker.com/r/phelger/

**Note:** the `smp` directory contains current build SNAPSHOTs as well as old XML binary releases. For the most up-to-date versions use `phoss-smp-(xml|sql|mongodb)` folders.

**Note:** The SMP comes pretty unconfigured

Note: the `Dockerfile-release-binary-xml` builds the latest release from binaries with XML backend.

Note: the `Dockerfile-release-binary-sql` builds the latest release from binaries with SQL backend

Note: the `Dockerfile-release-binary-mongodb` builds the latest release from binaries with MongoDB backend (since v5.2.0)

Note: the `Dockerfile-release-from-source-xml` build the latest release from GitHub sources with XML backend

Note: the `Dockerfile-snapshot-from-source-xml` build the latest snapshot from GitHub sources with XML backend

Note: the `Dockerfile-snapshot-from-source-sql` build the latest snapshot from GitHub sources with SQL backend

Note: the `Dockerfile-snapshot-from-source-mongodb` build the latest snapshot from GitHub sources with MongoDB backend

## Release Binary, XML Backend

Use an existing binary release, with the XML backend.

To build, run and stop the SMP image with XML backend use the following command:

```
docker build --pull -t phoss-smp-release-binary-xml -f Dockerfile-release-binary-xml .
docker run -d --name phoss-smp-release-binary-xml -p 8888:8080 phoss-smp-release-binary-xml
docker stop phoss-smp-release-binary-xml
docker rm phoss-smp-release-binary-xml
```

It exposes port 8888 where Tomcat is running successfully.
Open `http://localhost:8888` in your browser.

## Release Binary, SQL backend

Use an existing binary release, with the SQL backend.

To build the SMP image with SQL backend use the following command:

```
docker build --pull -t phoss-smp-release-binary-sql -f Dockerfile-release-binary-sql .
docker run -d --name phoss-smp-release-binary-sql -p 8888:8080 phoss-smp-release-binary-sql
docker stop phoss-smp-release-binary-sql
docker rm phoss-smp-release-binary-sql
```

It exposes port 8888 where Tomcat is running successfully.
Open `http://localhost:8888` in your browser.


## Release Binary, MongoDB backend

Use an existing binary release, with the MongoDB backend.

To build the SMP image with MongoDB backend use the following command:

```
docker build --pull -t phoss-smp-release-binary-mongodb -f Dockerfile-release-binary-mongodb .
docker run -d --name phoss-smp-release-binary-mongodb -p 8888:8080 phoss-smp-release-binary-mongodb
docker stop phoss-smp-release-binary-mongodb
docker rm phoss-smp-release-binary-mongodb
```

It exposes port 8888 where Tomcat is running successfully.
Open `http://localhost:8888` in your browser.

## Release from source, XML Backend

Build the SMP from source with the XML backend using the tag of the last release.

```
docker build --pull -t phoss-smp-release-from-source-xml -f Dockerfile-release-from-source-xml .
docker run -d --name phoss-smp-release-from-source-xml -p 8888:8080 phoss-smp-release-from-source-xml
docker stop phoss-smp-release-from-source-xml
docker rm phoss-smp-release-from-source-xml
```

It exposes port 8888 where Tomcat is running successfully.
Open `http://localhost:8888` in your browser.

## Latest snapshot version from source, XML Backend

Build the SMP from source with the XML backend using the HEAD version of the master branch (SNAPSHOT version).

```
docker build --pull -t phoss-smp-snapshot-from-source-xml -f Dockerfile-snapshot-from-source-xml .
docker run -d --name phoss-smp-snapshot-from-source-xml -p 8888:8080 phoss-smp-snapshot-from-source-xml
docker stop phoss-smp-snapshot-from-source-xml
docker rm phoss-smp-snapshot-from-source-xml
```

It exposes port 8888 where Tomcat is running successfully.
Open `http://localhost:8888` in your browser.

# Misc Docker related stuff

## Version change

To change the version build of binary release versions you can specify the version on the commandline when building:

```
docker build --build-arg SMP_VERSION=5.3.3 -t phoss-smp-release-binary-xml-5.3.3 -f Dockerfile-release-binary-xml .
```

Note: since the file system layout changed between 5.0.0 and 5.0.1, the current version is only applicable to versions &ge; 5.0.1

Note: up to and including v5.1.1 the variable `SMP_VERSION` was called `VERSION` 

## Running pre-build image from Docker Hub

Running a pre-build image (XML backend only):

```
docker run -d --name phoss-smp-release-binary-xml-5.3.3 -p 8888:8080 phelger/smp:5.3.3
docker stop phoss-smp-release-binary-xml-5.3.3
docker rm phoss-smp-release-binary-xml-5.3.3
```

It exposes port 8888 where Tomcat is running successfully.
Open `http://localhost:8888` in your browser.

## Docker cheatsheet

Short explanation on docker running
  * `-d` - run in daemon mode
  * `--name phoss-smp` - internal nice name for `docker ps`, `docker logs` etc.
  * `-p 8888:8080` - proxy container port 8080 to host port 8888
  * `phoss-smp` - the tag to be run

Upon successful completion opening http://localhost:8888 in your browser should show you the start page of phoss SMP.

Default credentials are in the Wiki at https://github.com/phax/phoss-smp/wiki/Running#default-login

The data directory inside the Docker image where the data is usually stored is `/home/git/conf`.
 
To check the log file use `docker logs phoss-smp`. There is no `catalina.out` file - only a `catalina.out.yyyy-mm-dd`.

To open a shell in the docker image use `docker exec -it phoss-smp-snapshot-from-source-xml bash` where `phoss-smp-snapshot-from-source-xml` is the name of the machine.
 
## Pushing changes

Once a new version is available the image needs to be build and pushed to Docker hub:

```
docker login
docker tag phoss-smp-release-binary-xml-x.y.z phelger/smp:x.y.z
docker push phelger/smp:x.y.z
docker tag phoss-smp-release-binary-xml-x.y.z phelger/smp:latest
docker push phelger/smp:latest
docker logout
```

See file `build-release-latest.cmd` for the effectice build script.

## SMP file storage

### Referencing configuration files

My suggestion is to create a child dockerfile like `Example-Dockerfile-with-configuration`.
It sets the system properties to the SMP configuration files in the virtual path `/config`.
See https://docs.docker.com/storage/volumes/ for the Docker configuration on volumes and mount points.

The main change is the `-v` parameter that mounts a local directory into the running image. `/host-directory/config` in the example down below must be changed to an existing directory containing the files `smp-server.properties`, `webapp.properties` and `pd-client.properties` (as named in the example dockerfile).

```
docker build --pull -t phoss-smp-with-config -f Example-Dockerfile-with-configuration .
docker run -d --name phoss-smp-with-config -p 8888:8080 -v /host-directory/config:/config phoss-smp-with-config
docker stop phoss-smp-with-config
docker rm phoss-smp-with-config
```

Note: if you change `/config` as the image directory to something else, please ensure to change the paths in the `example-config-dir/` properties files as well. 

### Persistent data storage for XML backend 

To persistently save all the data stored by the SMP add another volume that mounts the docker directory `/home/git/conf` to a local directory as in `-v /host-directory/data:/home/git/conf`.

### All setup together

On my Windows machine I use the following command to run the whole SMP on port 8888 with the correct configuration and the persistent storage like this: 

```
docker run -d --name phoss-smp-with-config -p 8888:8080 -v c:\dev\git\phoss-smp\docker\example-config-dir:/config -v C:\dev\git\phoss-smp\docker\persistent\:/home/git/conf phoss-smp-with-config
```
