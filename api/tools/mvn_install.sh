CURRENT_DIR=$(cd `dirname $0`; pwd)
INFERRED_ROOT=$(cd `dirname $0`; cd .. ; cd ..; cd mvn_repo; pwd)

echo "usage: ./mvn_install.sh group-id artifact-id jar-file"

echo mvn install:install-file -DgroupId=$1 -DartifactId=$2 -Dfile=$3 -DpomFile=pom.xml -DlocalRepositoryPath=${INFERRED_ROOT}

mvn install:install-file -DgroupId=$1 -DartifactId=$2 -Dfile=$3 -DpomFile=pom.xml -DlocalRepositoryPath=${INFERRED_ROOT}

