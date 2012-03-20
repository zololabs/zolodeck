mkdir ~/.zolo.mvn

LOCAL_REPO=/Users/$(whoami)/.zolo.mvn
echo $LOCAL_REPO

mvn install:install-file -Dfile=resources/jars/datomic-0.1.2678.jar -DartifactId=peer -Dversion=0.1.2678 -DgroupId=datomic -Dpackaging=jar -DlocalRepositoryPath=${LOCAL_REPO}

