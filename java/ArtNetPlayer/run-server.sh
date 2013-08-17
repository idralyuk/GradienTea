#!/usr/bin/env bash

cd $(dirname $0)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home
mvn exec:java -o -Dexec.mainClass="org.hypher.gradientea.artnet.player.DomeAnimationServerMain" -Dexec.args="127.0.0.1 prototypeDome.properties minidomepi.local testMiniDome.properties" 

