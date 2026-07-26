cmake_minimum_required(VERSION 3.15)
project(focusflow)

# O Crow exige pelo menos C++11
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# 1. Adiciona a pasta clonada do Crow ao projeto
add_subdirectory(Crow)

find_package(nlohmann_json REQUIRED)

find_package(PkgConfig REQUIRED)
pkg_check_modules(SODIUM REQUIRED libsodium)


find_package(OpenSSL REQUIRED)

include(FetchContent)
FetchContent_Declare(
  jwt-cpp
  GIT_REPOSITORY https://github.com/Thalhammer/jwt-cpp.git
  GIT_TAG v0.7.0
)
FetchContent_MakeAvailable(jwt-cpp)


# 2. Cria o executável compilando os DOIS arquivos juntos
# (Se o seu RequestsDB.cpp estiver em outra pasta, ajuste o caminho aqui)
add_executable(main src/main.cpp src/RequestsDB.cpp)

# 3. "Linka" a biblioteca do Crow E as bibliotecas do PostgreSQL (pqxx e pq)
target_include_directories(main PUBLIC ${SODIUM_INCLUDE_DIRS})
target_link_libraries(main PUBLIC Crow::Crow pqxx pq nlohmann_json::nlohmann_json ${SODIUM_LIBRARIES} jwt-cpp::jwt-cpp OpenSSL::Crypto)


