#ifndef USUARIO_HPP
#define USUARIO_HPP
#include "../include/core.hpp";

class Usuario {
private:
    static int id_;
    std::string name_;
    int age_;
    std::string email_;
    //std::string password_;

public:
    Usuario();
    ~Usuario();
};

#endif