#!/bin/bash

if [ ! -z $1 ]; then
    brand=$1
    echo $brand > building
elif [ -f building ]; then
    brand=$(cat building)
else
    brand="generic"
    echo $brand > building
fi

echo $brand