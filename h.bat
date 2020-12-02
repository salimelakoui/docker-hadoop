@echo off

if "%1"=="" goto fin
    docker run --volume "%cd%:/project" --network dp-network --env-file ./hadoop.env -e DEBUG_DOCKER=0 salimelakoui/hadoop-base:latest-salim %*
