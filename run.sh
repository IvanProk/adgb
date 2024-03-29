#!/bin/bash
if [ $# -lt 3 ]; then
  echo "This script requires the next parameters:";
  echo "- absolute path to apk file";
  echo "- filter (can be a package name or 'nofilter' string)";
  echo "- true or false (where true means that you want to see inner classes on your graph)";
  echo "Examples:";
  echo "./run.sh full/path/to/the/apk/app-release.apk com.example.test true";
  echo "./run.sh full/path/to/the/apk/app-release.apk nofilter false";
  exit 1;
fi
fileName="$1"
xbase=${fileName##*/}
xpref=${xbase%.*}

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
outPath=${dir}"/output/"${xpref}
jsonPath=${dir}"/gui/analyzed.js"

exec "java -jar ${dir}'/build/libs/adgb.jar' -i ${outPath} -o ${jsonPath} -f $2 -d $3 -a $1"