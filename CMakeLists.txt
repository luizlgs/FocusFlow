cmake_minimum_required(VERSION 3.15)
project(step2pomodoro)

# O Crow exige pelo menos C++11 (Você está usando 20, ótimo!)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 1. Adiciona a pasta clonada do Crow ao projeto
add_subdirectory(Crow)

find_package(nlohmann_json REQUIRED)


# 2. Cria o executável compilando os DOIS arquivos juntos
# (Se o seu RequestsDB.cpp estiver em outra pasta, ajuste o caminho aqui)
add_executable(main src/main.cpp src/RequestsDB.cpp)

# 3. "Linka" a biblioteca do Crow E as bibliotecas do PostgreSQL (pqxx e pq)
target_link_libraries(main PUBLIC Crow::Crow pqxx pq nlohmann_json::nlohmann_json)