#!/usr/bin/env bash

cd src/main/cpp/
# 1. vollständig neues Build-Verzeichnis
rm -rf build
# 2. konfigurieren – Generator explizit angeben
cmake -S . -B build
# 3. bauen – über cmake, nicht direkt über ninja
cmake --build build -j

