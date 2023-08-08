#!/bin/bash

echo "Script executed from: ${PWD}"

File=.snippet-licenses
if grep -rq RECIPROCAL "$File"; then
  echo "Found reciprocal"
fi

if grep -rq PERMISSIVE "$File"; then
  echo "Found permissive"
fi


