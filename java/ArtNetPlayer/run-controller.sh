#!/usr/bin/env bash

cd $(dirname $0)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home
MAVEN_OPTS="-Djava.library.path=target/natives/" mvn -o exec:java -Dexec.mainClass="org.hypher.gradientea.artnet.player.controller.PrototypeController"
