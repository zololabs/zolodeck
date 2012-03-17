CURRENT_DIR=$(cd `dirname $0`; pwd)
INFERRED_ROOT=$(cd `dirname $0`; cd .. ; pwd)

export ZOLODECK_ENV=development
export ZOLODECK_HOME=${INSTAFUN_HOME:-"${CURRENT_DIR}"}

echo "usage: ./swank.sh [hostname]"

if [ -z "$1" ]; then
  h="localhost"
else
  h=`hostname`
fi

echo "Starting swank on ${h}"
lein swank 4005 ${h}


