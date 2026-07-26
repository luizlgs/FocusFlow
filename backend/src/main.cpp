#include "../include/core.hpp"
#include "../include/RequestsDB.hpp"


int main() {

    crow::SimpleApp app;

    //conexao inicial com o servidor
    CROW_ROUTE(app, "/")([](){
        return "Servidor ativo";
    });


    //dados de login
    CROW_ROUTE(app, "/login").methods(crow::HTTPMethod::POST)([](const crow::request& req){
        // Analisa o JSON que o Java enviou
        nlohmann::json err;
        auto user_data = crow::json::load(req.body);
        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }
        if(user_data.has("email") && user_data.has("pass")){
            RequestsDB request;
            std::optional<pqxx::row> user_info_from_db = request.login(user_data["email"].s(), user_data["pass"].s());
            if(user_info_from_db){
                crow::json::wvalue response;
                response["id"] = (*user_info_from_db)["id"].as<int>();   
                response["name"] = (*user_info_from_db)["name"].c_str();
                response["email"] = (*user_info_from_db)["email"].c_str();
                response["age"] = (*user_info_from_db)["age"].as<int>();

                int user_id = (*user_info_from_db)["id"].as<int>();

                nlohmann::json projects = request.get_projects(user_id);
                response["projects"] = projects.dump(); // .dump transformar um nlohmann::json em string

                nlohmann::json tasks = request.get_tasks(user_id);
                response["tasks"] = tasks.dump();

                nlohmann::json pomodorosessions = request.get_pomodorosessions(user_id);
                response["pomodorosessions"] = pomodorosessions.dump();
                
                std::string token = request.generate_token(user_id);
                response["token"] = token;

                return crow::response(crow::status::OK, response);
            }
            else{
                err["error"] = "Não Autorizado";
                return crow::response(crow::status::UNAUTHORIZED, err.dump());  //retorna 401
            }
        }
        else{
            err["error"] = "Dados invalidos";
            return crow::response(422, err.dump()); // algum campo inválido
        }
    });


    //dados de registro
    CROW_ROUTE(app, "/register").methods(crow::HTTPMethod::POST)([](const crow::request& req){
        // Analisa o JSON que o Java enviou
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;
        
        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB register_;
        bool regiser_stat = register_.register_(user_data["name"].s(),
                                                user_data["email"].s(), 
                                                user_data["age"].i(), 
                                                user_data["pass1"].s(), 
                                                user_data["pass2"].s());
        if(!regiser_stat){
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        
        err["accepted"] = "Dados recebidos com sucesso!";
        return crow::response(crow::status::OK, err.dump());
    });


    //criar task
    CROW_ROUTE(app, "/new_task").methods(crow::HTTPMethod::POST)([](const crow::request& req){
        // Analisa o JSON que o Java enviou
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }
        
        //CROW_LOG_INFO << user_data["title"].s() << " " << user_data["description"].s();

        nlohmann::json taskID = requests_db.new_task(user_data["title"].s(), 
                                    user_data["description"].s(), 
                                    user_data["task_date"].s(), 
                                    std::to_string(*auth_user_id), 
                                    user_data["priority"].s());
        if(taskID == nullptr){
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        
        return crow::response(crow::status::OK, taskID.dump());
    });


    CROW_ROUTE(app, "/new_project").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        // Analisa o JSON que o Java enviou
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        
        CROW_LOG_INFO << req.body;

        nlohmann::json projectID = requests_db.new_project(
                                    user_data["title"].s(),
                                    user_data["description"].s(),
                                    user_data["start_date"].s(),
                                    user_data["delivery_date"].s(),
                                    user_data["members"].s(),
                                    std::to_string(*auth_user_id));  

        if (projectID == nullptr){
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }

        return crow::response(crow::status::OK, projectID.dump()); //possui o id do projeto e um array de json com usuarios que possuem chave nome e email
    });


    CROW_ROUTE(app, "/end_project").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        // Analisa o JSON enviado pelo Android
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        nlohmann::json return_ = requests_db.end_project(user_data["id"].s(), *auth_user_id);

        if (return_ == nullptr) {
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        else if (return_.empty()) {
            err["error"] = "Tarefa não encontrada";
            return crow::response(crow::status::NOT_FOUND, err.dump());  // 404
        }

        nlohmann::json response;
        response["id"] = user_data["id"].i();
        response["project_state"] = return_["project_state"];
        response["completion_date"] = return_["completion_date"];

        return crow::response(crow::status::OK, response.dump());
    });


    
    CROW_ROUTE(app, "/end_task").methods(crow::HTTPMethod::POST)([](const crow::request& req) {

        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        nlohmann::json return_ = requests_db.end_task(user_data["id"].s(), *auth_user_id);

        if(return_ == nullptr) {
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        else if (return_.empty()) {
            err["error"] = "Tarefa não encontrada";
            return crow::response(crow::status::NOT_FOUND, err.dump());  // 404
        }

        nlohmann::json response;
        response["id"] = user_data["id"].i();
        response["task_state"] = return_["task_state"];
        response["completion_date"] = return_["completion_date"];
        response["completion_time"] = return_["completion_time"];

        return crow::response(crow::status::OK, response.dump());
    });



    CROW_ROUTE(app, "/end_pomodoro_session").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }


        nlohmann::json completed = requests_db.end_pomodoro_session(user_data["id"].s(), user_data["total_focus"].s(), user_data["timer"].s(), *auth_user_id);

        if (completed == nullptr) {
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        else if (completed.empty()) {
            err["error"] = "Tarefa não encontrada";
            return crow::response(crow::status::NOT_FOUND, err.dump());  // 404
        }

        nlohmann::json response;
        response["id"] = user_data["id"].i();
        response["end_time"] = completed["end_time"];
        response["date"] = completed["date"];
        response["total_focus"] = completed["total_focus"];
        response["timer"] = completed["timer"];

        return crow::response(crow::status::OK, response.dump());
    });


    CROW_ROUTE(app, "/new_pomodoro").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        nlohmann::json return_ = requests_db.new_pomodoro(
            user_data["title"].s(),
            user_data["description"].s(),
            std::to_string(*auth_user_id),
            user_data["blocks"].s(),
            user_data["short_pause"].s(),
            user_data["big_pause"].s()
        );

        if (return_ == nullptr) {
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }

        return crow::response(crow::status::OK, return_.dump());
    });


    CROW_ROUTE(app, "/standby_pomodoro").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        
        nlohmann::json result = requests_db.standby_pomodoro(
            user_data["id"].s(), 
            user_data["total_focus"].s(), 
            user_data["timer"].s(), 
            user_data["small_pauses"].s(), 
            user_data["big_pauses"].s(), 
            user_data["is_pause"].s(), *auth_user_id);

        if (result == nullptr){
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        else if (result.empty()) {
            err["error"] = "Tarefa não encontrada";
            return crow::response(crow::status::NOT_FOUND, err.dump());  // 404
        }


        nlohmann::json response;
        response["id"] = user_data["id"].i();
        response["timer"] = result["timer"];
        response["total_focus"] = result["total_focus"];
        response["small_pauses"] = result["small_pauses"];
        response["big_pauses"] = result["big_pauses"];
        response["is_pause"] = result["is_pause"];
        return crow::response(crow::status::OK, response.dump());
    });


    CROW_ROUTE(app, "/delete_pomodoro_session").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        nlohmann::json deleted = requests_db.delete_pomodoro_session(user_data["id"].s(), *auth_user_id);

        if (deleted == nullptr) {
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        else if (deleted.empty()) {
            err["error"] = "Tarefa não encontrada";
            return crow::response(crow::status::NOT_FOUND, err.dump());  // 404
        }

        nlohmann::json response;
        response["id"] = deleted["id"];

        return crow::response(crow::status::OK, response.dump());
    });


    CROW_ROUTE(app, "/delete_task").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        nlohmann::json deleted = requests_db.delete_task(user_data["id"].s(), *auth_user_id);

        if (deleted == nullptr) {
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        else if (deleted.empty()) {
            err["error"] = "Tarefa não encontrada";
            return crow::response(crow::status::NOT_FOUND, err.dump());  // 404
        }

        nlohmann::json response;
        response["id"] = deleted["id"];

        return crow::response(crow::status::OK, response.dump());
    });


    CROW_ROUTE(app, "/delete_project").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
        auto user_data = crow::json::load(req.body);
        nlohmann::json err;

        if (!user_data) {
            err["error"] = "O pedido não pôde ser entregue";
            return crow::response(crow::status::BAD_REQUEST, err.dump()); //retorna 400
        }

        RequestsDB requests_db;
        auto auth_user_id = requests_db.authenticate(user_data);
        if (!auth_user_id) {
            err["error"] = "Não autorizado";
            return crow::response(crow::status::UNAUTHORIZED, err.dump());
        }

        nlohmann::json deleted = requests_db.delete_project(user_data["id"].s(), *auth_user_id);

        if (deleted == nullptr) {
            err["error"] = "Inconsistencia nos dados recebidos";
            return crow::response(422, err.dump());
        }
        else if (deleted.empty()) {
            err["error"] = "Tarefa não encontrada";
            return crow::response(crow::status::NOT_FOUND, err.dump());  // 404
        }

        nlohmann::json response;
        response["id"] = deleted["id"];

        return crow::response(crow::status::OK, response.dump());
    });

    try{
        //roda o servidor do crow na porta 18080
        app.port(18080).multithreaded().run();
    }
    catch(const std::invalid_argument &e){
        std::cout << "Erro ao inicializar o servidor:" << e.what() << std::endl;
    }
}