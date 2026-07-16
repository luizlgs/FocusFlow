#ifndef TASK_HPP
#define TASK_HPP
#include "../include/core.hpp";

class Task {
private:
    static int id_;
    std::string title_, description_;
    std::chrono::year_month_day date_;
    unsigned int user_id_, card_id_;
  
public:
    Task();
    ~Task();
};


#endif