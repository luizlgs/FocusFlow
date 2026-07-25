#include "../include/RequestsDB.hpp"

std::optional<pqxx::row> RequestsDB::login(std::string email, std::string pass) {
    //CROW_LOG_INFO << email << " " << pass;
    try {
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");

        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec_params(
            "SELECT * FROM Users WHERE email = $1;",
            email
        );
        connection_.commit();
        
        if(rows.empty()){
            return std::nullopt;
        }
        if(rows.size() == 1)
            return rows[0];
        else
            return std::nullopt; //caso em que ha mais de um úsuario com o mesmo email (se acontecer há algo de errado)     
        
    }
    catch(const std::exception &e){
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
    }
    return std::nullopt;
}

bool RequestsDB::register_(std::string name, std::string email, int age, std::string pass1, std::string pass2){
    bool request_validation = false;
    if(pass1 != pass2){
        return false;
    }
    try {
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");

        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec_params(
            "SELECT * FROM Users WHERE email = $1;",
            email
        );
        if(!rows.empty()){
            return false;
        }
        else{
            pqxx::result rows2 = connection_.exec_params(
                "insert into Users (name, age, email, password) values ($1, $2, $3, $4);",
                name, age, email, pass1
            );
        }
        
        connection_.commit();
        return true;
    }
    catch(const std::exception &e){
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
    }
    return false;

}

nlohmann::json RequestsDB::get_projects(int user_id){
    
    try {
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");

        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec_params(
            "SELECT * FROM Projects WHERE $1 = ANY(members);",
            user_id
        );
        
        nlohmann::json projectsArr = nlohmann::json::array();

        if(rows.empty()){
            return nlohmann::json::array();
        }

        for (auto row : rows){
            nlohmann::json currentProject;

            currentProject["id"] = row["id"].as<int>();
            currentProject["title"] = row["title"].c_str();
            currentProject["description"] = row["description"].c_str();
            currentProject["start_date"] = row["start_date"].c_str();
            currentProject["delivery_date"] = row["delivery_date"].c_str();
            currentProject["project_state"] = row["project_state"].c_str();
            currentProject["completion_date"] = row["completion_date"].c_str();


            std::string user_id = row["user_id"].c_str();
            pqxx::result project_creator = connection_.exec_params(
                "SELECT name FROM Users WHERE id = $1;",
                user_id
            );
            currentProject["creator_name"] = project_creator[0]["name"].c_str();


            std::string members_index = row["members"].c_str();
            nlohmann::json members_info = nlohmann::json::array();
            members_index = members_index.substr(1, members_index.length() - 2); // remove as chaves "{}" da lista de ID's

            
            if(!members_index.empty()) {
                std::string ids_str = members_index;

                pqxx::result request_members = connection_.exec_params(
                    "SELECT name, email FROM Users WHERE id = ANY(string_to_array($1, ',')::int[]);",
                    ids_str
                );

                //adicionas os membros encontrados na consulta request_members no array de json members_info
                for (auto const &member_row : request_members) {
                    nlohmann::json member_json;
                    member_json["name"] = member_row["name"].as<std::string>();
                    member_json["email"] = member_row["email"].as<std::string>();
        
                    members_info.push_back(member_json); // adiciona o membro atual no array de json
                }

            }
            currentProject["members"] = members_info;
            projectsArr.push_back(currentProject);
        }

        return projectsArr;
        
        
    }
    catch(const std::exception &e){
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
    }
    return nullptr;
    
}


nlohmann::json RequestsDB::get_tasks(int user_id){
    try {
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);
        pqxx::result rows = connection_.exec_params(
            "SELECT * FROM Tasks WHERE user_id = $1;",
            user_id
        );

        nlohmann::json tasksArr = nlohmann::json::array();

        if(rows.empty()){
            return nlohmann::json::array();
        }

        for(auto row: rows){
            nlohmann::json currentTask;
            currentTask["id"] = row["id"].as<int>();
            currentTask["title"] = row["title"].c_str();
            currentTask["description"] = row["description"].c_str();
            currentTask["task_date"] = row["task_date"].c_str();
            currentTask["user_id"] = row["user_id"].as<int>();
            currentTask["priority"] = row["priority"].c_str();
            currentTask["task_state"] = row["task_state"].c_str();
            currentTask["completion_date"] = row["completion_date"].c_str();
            currentTask["completion_time"] = row["completion_time"].c_str();

            tasksArr.push_back(currentTask);
        }

        return tasksArr;
    }
    catch(const std::exception &e){
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
        return nullptr;
    }
}

nlohmann::json RequestsDB::get_pomodorosessions(int user_id){
    try {
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);
        pqxx::result rows = connection_.exec_params(
            "SELECT * FROM PomodoroSessions WHERE user_id = $1;",
            user_id
        );

        nlohmann::json sessionsArr = nlohmann::json::array();

        if(rows.empty()){
            return nlohmann::json::array();
        }

        for(auto row: rows){
            nlohmann::json currentSession;
            currentSession["id"] = row["id"].as<int>();
            currentSession["title"] = row["title"].c_str();
            currentSession["description"] = row["description"].c_str();
            currentSession["short_pause"] = row["short_pause"].c_str();
            currentSession["big_pause"] = row["big_pause"].c_str();
            currentSession["blocks"] = row["blocks"].c_str();
            currentSession["start_time"] = row["start_time"].c_str();
            currentSession["end_time"] = row["end_time"].c_str();
            currentSession["user_id"] = row["user_id"].as<int>();
            currentSession["date"] = row["date"].c_str();
            currentSession["total_focus"] = row["total_focus"].c_str();
            currentSession["timer"] = row["timer"].c_str();

            currentSession["small_pauses"] = row["small_pauses"].c_str();
            currentSession["big_pauses"] = row["big_pauses"].c_str();
            currentSession["is_pause"] = row["is_pause"].c_str();

            sessionsArr.push_back(currentSession);
        }

        return sessionsArr;
    }
    catch(const std::exception &e){
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
        return nullptr;
    }
}

nlohmann::json RequestsDB::new_task(std::string title, std::string description, std::string task_date, std::string user_id, std::string priority){
    try{
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);
        pqxx::result rows = connection_.exec_params(
            "INSERT INTO Tasks (title, description, task_date, user_id, priority, task_state) "
            "VALUES ($1, $2, $3, $4, $5, false) "
            "RETURNING id;",
            title, description, task_date, user_id, priority
        );
                                             

        if (rows.empty()) {
            throw std::runtime_error("Falha ao inserir tarefa.");
        }

        int id = rows[0]["id"].as<int>();
        nlohmann::json taskID;
        taskID["id"] = id;

        connection_.commit();


        return taskID;
    } catch(const std::exception &e) {
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
        return nullptr;
    }
}

std::string RequestsDB::getMembersIds(std::string membersEmails, pqxx::work& connection_) {
    std::stringstream ss(membersEmails);
    std::string email;
    std::vector<int> ids;

    while (ss >> email) {
        pqxx::result rows = connection_.exec_params(
            "SELECT id FROM Users WHERE email = $1;",
            email
        );

        if (!rows.empty()) {
            ids.push_back(rows[0]["id"].as<int>());
        }
    }

    std::string membersArray = "{";

    for (size_t i = 0; i < ids.size(); i++) {
        membersArray += std::to_string(ids[i]);

        if (i < ids.size() - 1)
            membersArray += ",";
    }

    membersArray += "}";

    return membersArray;
}

nlohmann::json RequestsDB::new_project(std::string title, std::string description, std::string start_date, std::string delivery_date, std::string members, std::string user_id) {
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);

        std::string membersIds = getMembersIds(members, connection_);

        pqxx::result rows = connection_.exec_params(
            "INSERT INTO Projects (title, description, start_date, delivery_date, members, user_id, project_state) "
            "VALUES ($1, $2, $3, $4, $5, $6, false) "
            "RETURNING id;",
            title, description, start_date, delivery_date, membersIds, user_id
        );

        if (rows.empty()) {
            throw std::runtime_error("Falha ao inserir projeto.");
        }

        int id = rows[0]["id"].as<int>();

        nlohmann::json projectID;
        projectID["id"] = id;


        pqxx::result membersRows = connection_.exec_params(
            "SELECT email, name FROM Users WHERE email = ANY(string_to_array($1, ' '));",
            members
        );


        nlohmann::json membersArray = nlohmann::json::array();

        for (auto row : membersRows) {
            nlohmann::json member;

            member["email"] = row["email"].c_str();
            member["name"] = row["name"].c_str();

            membersArray.push_back(member);
        }

        projectID["members"] = membersArray;

        connection_.commit();

        return projectID;

    } catch(const std::exception &e) {
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
        return nullptr;
    }
}

nlohmann::json RequestsDB::new_pomodoro(std::string title, std::string description, std::string user_id, std::string blocks, std::string short_pause, std::string big_pause){
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);


        pqxx::result rows = connection_.exec_params(
            "INSERT INTO PomodoroSessions (title, description, short_pause, big_pause, blocks, user_id, start_time) "
            "VALUES ($1, $2, $3, $4, $5, $6, CURRENT_TIME(0)) "
            "RETURNING id, date, start_time, total_focus;",
            title, description, short_pause, big_pause, blocks, user_id
        );


        if (rows.empty()) {
            throw std::runtime_error("Task não encontrada.");
        }


        nlohmann::json response;

        response["id"] = rows[0]["id"].as<int>();
        response["date"] = rows[0]["date"].c_str();
        response["start_time"] = rows[0]["start_time"].c_str();
        response["total_focus"] = rows[0]["total_focus"].c_str();
        connection_.commit();

        return response;


    } catch (const std::exception &e) {

        std::cerr << "Error to connect to db: " << e.what() << std::endl;

        return nullptr;
    }
}


nlohmann::json RequestsDB::end_project(std::string project_id) {
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec_params(
            "UPDATE Projects "
            "SET project_state = NOT project_state, "
            "completion_date = CASE "
            "WHEN project_state = false THEN CURRENT_DATE "
            "ELSE NULL END "
            "WHERE id = $1 "
            "RETURNING id, project_state, completion_date;",
            project_id
        );

        if (rows.empty()) {
            throw std::runtime_error("Projeto não encontrado.");
        }

        connection_.commit();

        nlohmann::json response;
        response["id"] = rows[0]["id"].as<int>();
        response["project_state"] = rows[0]["project_state"].c_str();
        response["completion_date"] = rows[0]["completion_date"].c_str();

        return response;

    } catch (const std::exception &e) {
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
        return nullptr;
    }
}

nlohmann::json RequestsDB::end_task(std::string task_id) {
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);


        pqxx::result rows = connection_.exec_params(
            "UPDATE Tasks "
            "SET task_state = NOT task_state, "
            "completion_date = CASE "
            "WHEN task_state = false THEN CURRENT_DATE "
            "ELSE NULL END, "
            "completion_time = CASE "
            "WHEN task_state = false THEN CURRENT_TIME(0) "
            "ELSE NULL END "
            "WHERE id = $1 "
            "RETURNING id, task_state, completion_date, completion_time;",
            task_id
        );


        if (rows.empty()) {
            throw std::runtime_error("Task não encontrada.");
        }


        connection_.commit();


        nlohmann::json response;

        response["id"] = rows[0]["id"].as<int>();
        response["task_state"] = rows[0]["task_state"].c_str();
        response["completion_date"] = rows[0]["completion_date"].c_str();
        response["completion_time"] = rows[0]["completion_time"].c_str();

        return response;


    } catch (const std::exception &e) {

        std::cerr << "Error to connect to db: " << e.what() << std::endl;

        return nullptr;
    }
}

nlohmann::json RequestsDB::end_pomodoro_session(std::string session_id, std::string total_focus, std::string timer_) {
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec_params(
            "UPDATE PomodoroSessions "
            "SET end_time = CURRENT_TIME(0), "
            "date = CURRENT_DATE, "
            "total_focus = $1, "
            "timer = $2 "
            "WHERE id = $3 "
            "RETURNING id, end_time, date, total_focus, timer;",
            total_focus, timer_, session_id
        );

        if (rows.empty()) {
            throw std::runtime_error("Sessão pomodoro não encontrada.");
        }

        connection_.commit();

        nlohmann::json response;

        response["id"] = rows[0]["id"].as<int>();
        response["end_time"] = rows[0]["end_time"].c_str();
        response["date"] = rows[0]["date"].c_str();
        response["total_focus"] = rows[0]["total_focus"].c_str();
        response["timer"] = rows[0]["timer"].c_str();

        return response;

    } catch (const std::exception &e) {
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
        return nullptr;
    }
}

nlohmann::json RequestsDB::standby_pomodoro(std::string session_id, std::string total_focus, std::string timer, std::string small_pauses, std::string big_pauses, std::string is_pause) {
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec_params(
            "UPDATE PomodoroSessions "
            "SET timer = $1, "
            "total_focus = $2, "
            "small_pauses = $3, "
            "big_pauses = $4, "
            "is_pause = $5 "
            "WHERE id = $6 "
            "RETURNING id, timer, total_focus, small_pauses, big_pauses, is_pause;",
            timer, total_focus, small_pauses, big_pauses, is_pause, session_id
        );

        if (rows.empty()) throw std::runtime_error("Sessão pomodoro não encontrada.");

        connection_.commit();

        nlohmann::json response;
        response["id"] = rows[0]["id"].as<int>();
        response["timer"] = rows[0]["timer"].c_str();
        response["total_focus"] = rows[0]["total_focus"].c_str();
        response["small_pauses"] = rows[0]["small_pauses"].c_str();
        response["big_pauses"] = rows[0]["big_pauses"].c_str();
        response["is_pause"] = rows[0]["is_pause"].c_str();

        return response;
    } catch (const std::exception &e) {
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
        return nullptr;
    }
}



