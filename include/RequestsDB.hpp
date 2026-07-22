#ifndef REQUESTDB_HPP
#define REQUESTDB_HPP
#include "../include/core.hpp"
#include <pqxx/pqxx>
#include <nlohmann/json.hpp>

class RequestsDB {
private:
    
public:
    RequestsDB() = default;
    ~RequestsDB() = default;

    std::optional<pqxx::row> login(std::string email, std::string pass);
    bool register_(std::string name, std::string email, int age, std::string pass1, std::string pass2);
    
    nlohmann::json get_projects(int user_id);
    nlohmann::json get_tasks(int user_id);
    nlohmann::json get_pomodorosessions(int user_id);

    nlohmann::json new_task(std::string title, std::string description, std::string task_date, std::string user_id, std::string priority);
    nlohmann::json change_task_state(std::string task_id);
    
    nlohmann::json new_project(std::string title, std::string description, std::string start_date, std::string delivery_date, std::string creator_id, std::string members);
    nlohmann::json change_project_state(std::string project_id);

    nlohmann::json new_pomodoro(std::string title, std::string description, std::string user_id, std::string blocks, std::string short_pause, std::string big_pause);

    std::string getMembersIds(std::string membersEmails, pqxx::work& connection_);

};


#endif