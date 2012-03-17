CURRENT_DIR=$(cd `dirname $0`; pwd)
INFERRED_ROOT=$(cd `dirname $0`; cd .. ; pwd)

export ZOLODECK_ENV=development
export ZOLODECK_HOME=${INSTAFUN_HOME:-"${CURRENT_DIR}"}

lein repl


