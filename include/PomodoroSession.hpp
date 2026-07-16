#ifndef POMODOROSESSION_HPP
#define POMODOROSESSION_HPP
#include "../include/core.hpp"

class PomodoroSession
{
private:
    static int id_;
    std::string title_, description_;
    int time_, short_pause_, big_pause_, blocks_, start_time_, end_time_;
    unsigned int user_id_;

public:
    PomodoroSession(/* args */);
    ~PomodoroSession();
};



#endif