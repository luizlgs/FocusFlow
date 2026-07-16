#ifndef CARD_HPP
#define CARD_HPP
#include "../include/core.hpp"

class Card {
private:
    static int id_;
    std::string title_, description;
    std::vector<int> members_;
    std::chrono::year_month_day start_date_, delivery_date_;
    std::string comments_;
    unsigned int project_id_;
public:
    Card();
    ~Card();
};


#endif