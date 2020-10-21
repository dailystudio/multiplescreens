#!/bin/bash

function print_usage {
  echo "Usage:"
  echo
  echo "  $0 [-options] -m MODULE -s SEVER_PATH [-options]"
  echo "    This script will deploy a application to run as a service after boot"
  echo ""
  echo "    -m MODULE:                       the module name to be online"
  echo "    -s SEVER_PATH:                   the absolute path of server deployment"
  echo "    -k KEY0:VAL0 [KEY1:VAL1 ...]:    the key:value pairs for script arguments"
  echo "    -u USER:                         the user for executing the crontab"
  echo "    -t:                              test outputs only"
  echo
}

function exit_abnormal {
	print_usage
	exit 1
}

test_outputs=false
test_dir="./outputs"

while getopts :m:s:k:u:thH opt; do
  case ${opt} in
    m)
	    module=${OPTARG}
    	;;
    s)
      srvdir=${OPTARG}
      ;;
    k)
      kv_pairs=${OPTARG}
    	;;
    u)
      user=${OPTARG}
    	;;
    t)
      test_outputs=true
      ;;
    h|H)
      print_usage
      exit 2
      ;;
    :)
	    echo "[ERROR] $0: -${OPTARG} requires an argument."
      exit_abnormal
      ;;
    *)
	    echo "[ERROR] $0: -${OPTARG} is unsuppported."
      exit_abnormal
      ;;
  esac
done

if [ -z "${module}" ] || [ -z "${srvdir}" ]; then
    echo "[ERROR] required options is missing."
    exit_abnormal
fi

echo "module: ${module}"
echo "server path: ${srvdir}"

IFS=',' kv_array=(${kv_pairs})
ncount=${#kv_array[@]}
i=0

sed_str="s/%server_path%/${srvdir//\//\\/}/g;"
if (( ${ncount} > 0 )); then
    echo "key-values: ${ncount}"
    for kv in ${kv_array[@]}; do
        key=$(echo ${kv%%:*} | xargs)
        val=$(echo ${kv#*:} | xargs)
        if [ ${i} == $((ncount - 1)) ]; then
            echo "\`- [${i}] Key: [${key}], Value: [${val}]"
        else
            echo "|- [${i}] Key: [${key}], Value: [${val}]"
        fi

        sed_str="${sed_str}""s/%${key}%/${val//\//\\/}/g;"
        i=$((i + 1))
     done
fi

echo "sed str: ${sed_str}"

root_dir="/"

if [ "$test_outputs" = true ]; then
    root_dir="./outputs"
	echo "testing outputs only, files will be generated in ${root_dir} ..."

    if [ ! -d ${root_dir} ]; then
   	    mkdir ${root_dir}
    fi
fi

systemddir="${root_dir}/etc/systemd/system"
rsyslogdir="${root_dir}/etc/rsyslog.d"

srvtmpl="$module.service.templ"
logtmpl="$module.conf.templ"
crontmpl="$module.crontab.templ"
srvdest="$module.service"
logdest="$module.conf"
crontdest="$module.crontab"


echo "generate final service: $srvtmpl -> $srvdest"
#sed ${sed_str} ${srvtmpl} > ${srvdest}
cat ${srvtmpl} | perl -pe ${sed_str} > ${srvdest}
echo "generate rsyslog conf: $logtmpl -> $logdest"
#sed ${sed_str} ${logtmpl} > ${logdest}
cat ${logtmpl} | perl -pe ${sed_str} > ${logdest}

if [ -f ${crontmpl} ]; then
  echo "generate crontab: $crontmpl -> $crontdest"
  #sed ${sed_str} ${logtmpl} > ${logdest}
  cat ${crontmpl} | perl -pe ${sed_str} > ${crontdest}
fi

echo "copy server to systemd conf directory: $systemddir"
if [ ! -d ${systemddir} ]; then
    mkdir -p ${systemddir}
fi
cp $srvdest $systemddir

if [ "$test_outputs" = false ]; then
    systemctl daemon-reload
    systemctl enable $srvdest
    systemctl stop $srvdest
    systemctl start $srvdest
    systemctl status $srvdest
    ps aux | grep node
fi

echo "copy conf to rsyslog directory: $rsyslogdir"
if [ ! -d ${rsyslogdir} ]; then
    mkdir -p ${rsyslogdir}
fi
cp $logdest $rsyslogdir

if [ "$test_outputs" = false ]; then
    systemctl restart rsyslog
fi

if [ -f ${crontdest} ]; then
  echo "schedule crontab: $crontdest, [user: ${user}]"
  if [ -z "${user}" ]; then
    crontab ${crontdest}
  else
    crontab -u ${user} ${crontdest}
  fi

fi
