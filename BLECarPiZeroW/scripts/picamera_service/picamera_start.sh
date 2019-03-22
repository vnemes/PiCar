#!/bin/bash
# sudo raspivid -t 0 -l -o tcp://0.0.0.0:80
while true; do
    raspivid -t 0 -w 1280 -h 720 -fps 25 -l -o tcp://0.0.0.0:80
done
