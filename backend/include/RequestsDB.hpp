#ifndef REQUESTDB_HPP
#define REQUESTDB_HPP
#include "../include/core.hpp"
#include <pqxx/pqxx>
#include <nlohmann/json.hpp>
#include <sodium.h>

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
    nlohmann::json end_task(std::string task_id);
    nlohmann::json delete_task(std::string task_id);
    
    nlohmann::json new_project(std::string title, std::string description, std::string start_date, std::string delivery_date, std::string members, std::string user_id);
    nlohmann::json end_project(std::string project_id);
    nlohmann::json delete_project(std::string project_id);

    nlohmann::json new_pomodoro(std::string title, std::string description, std::string user_id, std::string blocks, std::string short_pause, std::string big_pause);
    nlohmann::json end_pomodoro_session(std::string session_id, std::string total_focus, std::string timer_);
    nlohmann::json standby_pomodoro(std::string session_id, std::string total_focus, std::string timer, std::string small_pauses, std::string big_pauses, std::string is_pause);
    nlohmann::json delete_pomodoro_session(std::string session_id);

    std::string getMembersIds(std::string membersEmails, pqxx::work& connection_, std::string creator_id);

    std::string hash_password(const std::string& password);
    bool verify_password(const std::string& password, const std::string& stored_hash);


    bool is_valid_email(const std::string& email);
    bool has_a_empty_field(std::vector<std::string> values);
    bool is_in_past(const std::string& dataStr);
    bool is_a_valid_time(const std::string& time);

};


#endif