rm /tmp/openni-osx-natives.jar
zip -j /tmp/openni-osx-natives.jar /usr/local/Cellar/openni/stable-1.5.2.23/lib/libOpenNI.jni.dylib /usr/local/Cellar/openni/stable-1.5.2.23/lib/libOpenNI.dylib
mvn install:install-file -Dfile=/tmp/openni-osx-natives.jar -DgroupId=org.openni -DartifactId=openni -Dversion=1.5.2.23 -Dclassifier=natives-osx -Dpackaging=jar
mvn install:install-file -Dfile=/usr/local/Cellar/openni/stable-1.5.2.23/share/java/org.OpenNI.jar -DgroupId=org.openni -DartifactId=openni -Dversion=1.5.2.23 -Dpackaging=jar
