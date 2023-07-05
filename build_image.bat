@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
  echo version is required
  exit /b 1
)

set VERSION=%~1

docker build . -t midjourney-proxy:%VERSION%

docker tag midjourney-proxy:%VERSION% iddddg/midjourney-proxy:%VERSION%
docker push iddddg/midjourney-proxy:%VERSION%
