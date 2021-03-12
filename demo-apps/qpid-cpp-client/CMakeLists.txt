cmake_minimum_required(VERSION 3.1...3.13)

project(
  qpid-cpp-client
  VERSION 1.0
  DESCRIPTION "AMQP c++ client example"
  LANGUAGES CXX)

file(GLOB SOURCES
    src/*.cpp
)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/target)

link_directories ("/usr/lib/x86_64-linux-gnu/")

include_directories(include/${CMAKE_PROJECT_NAME})

add_executable (${CMAKE_PROJECT_NAME} ${SOURCES})
target_link_libraries(${CMAKE_PROJECT_NAME} optimized libqpid-proton-cpp.so)




