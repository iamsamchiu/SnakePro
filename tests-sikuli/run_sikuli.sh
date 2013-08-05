#!/bin/bash

echo "launch SIKULI script"
out=$($SIKULI_HOME/sikuli-ide.sh -s -r $1/tests-sikuli/testDpadMove.sikuli)
echo $out

if [[ "$out" == *error* ]]; then
echo "[error]error found,please check error message."
exit -1
else
echo "[info]error NOT found!"
fi
echo "[info]run sikuli successfully"