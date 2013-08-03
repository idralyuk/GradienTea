cd `dirname $0`
mvn install:install-file -Dfile=toxiclibs-0020/colorutils/library/colorutils.jar -DgroupId=toxiblibs -DartifactId=colorutils -Dversion=0020 -Dpackaging=jar
mvn install:install-file -Dfile=toxiclibs-0020/toxiclibscore/library/toxiclibscore.jar -DgroupId=toxiblibs -DartifactId=core -Dversion=0020 -Dpackaging=jar
