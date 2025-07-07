cmake_minimum_required(VERSION 3.31.6)

# -----------------------------------------------------------------------------
# Projekt & Grund­einstellungen
# -----------------------------------------------------------------------------
project(AdsLibSwig VERSION 0.1 LANGUAGES C CXX Java)

option(ADS_SWIG_FORCE_64BIT "Erzwingt 64-Bit-Typen in SWIG" ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)      # für alle Targets

# Log-Level für Bibliothek
set(CONFIG_DEFAULT_LOGLEVEL 4 CACHE STRING "Default log level (0-4)")
add_compile_definitions(CONFIG_DEFAULT_LOGLEVEL=${CONFIG_DEFAULT_LOGLEVEL})

# Java-Compiler-Optionen für Parameternamen-Erhaltung
set(CMAKE_Java_COMPILE_FLAGS "-parameters -g")

# Pfade für Java-Artefakte
set(CMAKE_JAVA_TARGET_OUTPUT_DIR "${CMAKE_CURRENT_BINARY_DIR}/java")
set(SWIG_JAVA_OUT                "${CMAKE_CURRENT_BINARY_DIR}/generated/java")
file(MAKE_DIRECTORY "${CMAKE_JAVA_TARGET_OUTPUT_DIR}" "${SWIG_JAVA_OUT}")

# -----------------------------------------------------------------------------
# Abhängigkeiten
# -----------------------------------------------------------------------------
find_package(SWIG 4.0 REQUIRED COMPONENTS java)
find_package(Java  REQUIRED COMPONENTS Development)
find_package(JNI   REQUIRED)
find_package(Threads REQUIRED)

include(UseSWIG)           # richtet Funktion swig_add_library ein
include("${SWIG_USE_FILE}") # stellt u. a. SWIG_ADD_SOURCE_TO_MODULE bereit
include(UseJava)

# -----------------------------------------------------------------------------
# Quell­dateien
# -----------------------------------------------------------------------------
set(ADSLIB_SOURCES
        AdsDef.cpp AdsDevice.cpp AdsFile.cpp AdsLib.cpp ECatAccess.cpp
        Frame.cpp LicenseAccess.cpp Log.cpp MasterDcStatAccess.cpp RTimeAccess.cpp
        RegistryAccess.cpp RouterAccess.cpp Sockets.cpp SymbolAccess.cpp
        bhf/ParameterList.cpp
        standalone/AdsLib.cpp standalone/AmsConnection.cpp standalone/AmsNetId.cpp
        standalone/AmsPort.cpp standalone/AmsRouter.cpp standalone/NotificationDispatcher.cpp
)

# -----------------------------------------------------------------------------
# Kern­bibliothek (Objekte → STATIC/SHARED)
# -----------------------------------------------------------------------------
add_library(ads_obj OBJECT ${ADSLIB_SOURCES})
target_include_directories(ads_obj PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

add_library(ads_static STATIC $<TARGET_OBJECTS:ads_obj>)
set_target_properties(ads_static PROPERTIES OUTPUT_NAME ads)

add_library(ads_shared SHARED $<TARGET_OBJECTS:ads_obj>)
set_target_properties(ads_shared PROPERTIES OUTPUT_NAME ads)
