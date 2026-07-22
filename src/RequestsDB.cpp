#include "../include/RequestsDB.hpp"

std::optional<pqxx::row> RequestsDB::login(std::string email, std::string pass) {
    //CROW_LOG_INFO << email << " " << pass;
    try {
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");

        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec("SELECT * FROM Users WHERE email = '"+email+"';");
        if(rows.empty()){
            return std::nullopt;
        }
        if(rows.size() == 1)
            return rows[0];
        else
            return std::nullopt; //caso em que ha mais de um úsuario com o mesmo email (se acontecer há algo de errado)
            
            
        connection_.commit();
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

        pqxx::result rows = connection_.exec("SELECT * FROM Users WHERE email = '"+email+"';");
        if(!rows.empty()){
            return false;
        }
        else{
            pqxx::result rows2 = connection_.exec("insert into Users (name, age, email, password) values ('"+name+"', "+std::to_string(age)+", '"+email+"', '"+pass1+"')");
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

        pqxx::result rows = connection_.exec("SELECT * FROM Projects WHERE "+std::to_string(user_id)+" = ANY(members);");
        
        nlohmann::json projectsArr = nlohmann::json::array();

        if(rows.empty()){
            return nullptr;
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
            pqxx::result project_creator = connection_.exec("SELECT name FROM Users WHERE id = "+user_id+";");
            currentProject["creator_name"] = project_creator[0]["name"].c_str();


            std::string members_index = row["members"].c_str();
            nlohmann::json members_info = nlohmann::json::array();
            members_index = members_index.substr(1, members_index.length() - 2); // remove as chaves "{}" da lista de ID's

            
            if(!members_index.empty()) {
                std::string ids_str = members_index;

                pqxx::result request_members = connection_.exec("SELECT name, email FROM Users WHERE id IN (" + ids_str + ")");

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
        pqxx::result rows = connection_.exec("SELECT * FROM Tasks WHERE user_id = '"+std::to_string(user_id)+"';");

        nlohmann::json tasksArr = nlohmann::json::array();

        if(rows.empty()){
            return nullptr;
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
        pqxx::result rows = connection_.exec("SELECT * FROM PomodoroSessions WHERE user_id = '"+std::to_string(user_id)+"';");

        nlohmann::json sessionsArr = nlohmann::json::array();

        if(rows.empty()){
            return nullptr;
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
            currentSession["currenttime"] = row["currenttime"].c_str();
            currentSession["date"] = row["date"].c_str();

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
        pqxx::result rows = connection_.exec("INSERT INTO Tasks (title, description, task_date, user_id, priority, task_state) "
                                             "VALUES ('"+ title +"', '"+ description +"', '"+ task_date +"', '"+ user_id +"', '"+ priority +"', "+"false"+") "
                                             "RETURNING id;");
                                             

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
        pqxx::result rows = connection_.exec(
            "SELECT id FROM Users WHERE email = '" + email + "';"
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

        pqxx::result rows = connection_.exec(
            "INSERT INTO Projects (title, description, start_date, delivery_date, members, user_id, project_state) "
            "VALUES ('" + title + "', '" + description + "', '" + start_date + "', '" + delivery_date + "', '" + membersIds + "', '" + user_id + "', false) "
            "RETURNING id;"
        );

        if (rows.empty()) {
            throw std::runtime_error("Falha ao inserir projeto.");
        }

        int id = rows[0]["id"].as<int>();

        nlohmann::json projectID;
        projectID["id"] = id;


        pqxx::result membersRows = connection_.exec("SELECT email, name "
                                                    "FROM Users "
                                                    "WHERE email = ANY(string_to_array('" + members + "', ' '));");


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

nlohmann::json RequestsDB::change_project_state(std::string project_id) {
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec(
            "UPDATE Projects "
            "SET project_state = NOT project_state, "
            "completion_date = CASE "
            "WHEN project_state = false THEN CURRENT_DATE "
            "ELSE NULL END "
            "WHERE id = " + project_id + " "
            "RETURNING id, project_state, completion_date;"
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

nlohmann::json RequestsDB::change_task_state(std::string task_id) {
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);


        pqxx::result rows = connection_.exec(
            "UPDATE Tasks "
            "SET task_state = NOT task_state, "
            "completion_date = CASE "
            "WHEN task_state = false THEN CURRENT_DATE "
            "ELSE NULL END, "
            "completion_time = CASE "
            "WHEN task_state = false THEN CURRENT_TIME "
            "ELSE NULL END "
            "WHERE id = " + task_id + " "
            "RETURNING id, task_state, completion_date, completion_time;"
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

nlohmann::json RequestsDB::new_pomodoro(std::string title, std::string description, std::string user_id, std::string blocks, std::string short_pause, std::string big_pause){
    try {
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");
        pqxx::work connection_(db);


        pqxx::result rows = connection_.exec(
            "INSERT INTO PomodoroSessions (title, description, short_pause, big_pause, blocks, user_id, start_time, currenttime) "
            "VALUES ('"+ title +"', '"+ description +"', '"+ short_pause +"', '"+ big_pause +"', '"+ blocks +"', '"+ user_id +"', CURRENT_TIME, CURRENT_TIME) "
            "RETURNING id, date;");


        if (rows.empty()) {
            throw std::runtime_error("Task não encontrada.");
        }


        connection_.commit();


        nlohmann::json response;

        response["id"] = rows[0]["id"].as<int>();
        response["date"] = rows[0]["date"].c_str();
        connection_.commit();

        return response;


    } catch (const std::exception &e) {

        std::cerr << "Error to connect to db: " << e.what() << std::endl;

        return nullptr;
    }
}



