#!/usr/bin/env bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home
mvn exec:java -Dexec.mainClass="org.hypher.gradientea.artnet.player.DomeAnimationServerMain" -Dexec.args="127.0.0.1 prototypeDome.properties minidomepi.local testMiniDome.properties" &

MAVEN_OPTS="-Djava.library.path=target/natives/" mvn exec:java -Dexec.mainClass="org.hypher.gradientea.artnet.player.controller.PrototypeController"
