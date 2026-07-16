#ifndef PROJECT_HPP
#define PROJECT_HPP
#include "../include/core.hpp";
#include <vector>

class Project {
private:
    static int id_;
    std::string title_;
    std::string description_;
    std::chrono::year_month_day start_date_, delivery_date_;
    std::vector<int> members;
    unsigned int user_id_;

public:
    Project();
    ~Project();
};



#endif