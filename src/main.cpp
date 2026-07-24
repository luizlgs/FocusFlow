#include "../include/core.hpp"
#include "../include/RequestsDB.hpp"


int main() {

    try{
        crow::SimpleApp app;

        //conexao inicial com o servidor
        CROW_ROUTE(app, "/")([](){
            return "Servidor ativo";
        });


        //dados de login
        CROW_ROUTE(app, "/login").methods(crow::HTTPMethod::POST)([](const crow::request& req){
            // Analisa o JSON que o Java enviou
            auto user_data = crow::json::load(req.body);
            if (!user_data) {
                return crow::response(400, "JSON Invalido");
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
                    

                    return crow::response(200, response);
                }
            }
            return crow::response(406, "Not Acceptable");
        });

        //dados de registro
        CROW_ROUTE(app, "/register").methods(crow::HTTPMethod::POST)([](const crow::request& req){
            // Analisa o JSON que o Java enviou
            auto user_data = crow::json::load(req.body);
            
            if (!user_data) {
                return crow::response(400, "JSON Invalido");
            }

            RequestsDB register_;
            bool regiser_stat = register_.register_(user_data["name"].s(),
                                                    user_data["email"].s(), 
                                                    user_data["age"].i(), 
                                                    user_data["pass1"].s(), 
                                                    user_data["pass2"].s());
            if(!regiser_stat){
                return crow::response(422, "Dados de registro inválidos");
            }
            
            return crow::response(200, "Dados recebidos com sucesso!");
        });


        //criar task
        CROW_ROUTE(app, "/new_task").methods(crow::HTTPMethod::POST)([](const crow::request& req){
            // Analisa o JSON que o Java enviou
            auto user_data = crow::json::load(req.body);
            
            if (!user_data) {
                return crow::response(400, "JSON Invalido");
            }
            //CROW_LOG_INFO << user_data["title"].s() << " " << user_data["description"].s();
            RequestsDB register_new_task;

            nlohmann::json taskID = register_new_task.new_task(user_data["title"].s(), 
                                       user_data["description"].s(), 
                                       user_data["task_date"].s(), 
                                       user_data["creator_id"].s(), 
                                       user_data["priority"].s());
            if(taskID == nullptr)
                return crow::response(400, "Dados inválidos");
            
            return crow::response(200, taskID.dump());
        });

        CROW_ROUTE(app, "/new_project").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
            // Analisa o JSON que o Java enviou
            auto user_data = crow::json::load(req.body);

            if (!user_data) {
                return crow::response(400, "JSON Invalido");
            }

            RequestsDB register_new_project;
            CROW_LOG_INFO << req.body;

            nlohmann::json projectID = register_new_project.new_project(
                                        user_data["title"].s(),
                                        user_data["description"].s(),
                                        user_data["start_date"].s(),
                                        user_data["delivery_date"].s(),
                                        user_data["members"].s(),
                                        user_data["creator_id"].s());  

            if (projectID == nullptr)
                return crow::response(400, "Dados inválidos");

            return crow::response(200, projectID.dump()); //possui o id do projeto e um array de json com usuarios que possuem chave nome e email
        });

        CROW_ROUTE(app, "/end_project").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
            // Analisa o JSON enviado pelo Android
            auto user_data = crow::json::load(req.body);

            if (!user_data) {
                return crow::response(400, "JSON Invalido");
            }

            RequestsDB requests_db;

            nlohmann::json return_ = requests_db.end_project(user_data["id"].s());

            if (return_ == nullptr) {
                return crow::response(400, "Não foi possível concluir o projeto");
            }

            nlohmann::json response;
            response["id"] = user_data["id"].i();
            response["project_state"] = return_["project_state"];
            response["completion_date"] = return_["completion_date"];

            return crow::response(200, response.dump());
        });

        
        CROW_ROUTE(app, "/end_task").methods(crow::HTTPMethod::POST)([](const crow::request& req) {

            auto user_data = crow::json::load(req.body);

            if (!user_data) {
                return crow::response(400, "JSON Invalido");
            }

            RequestsDB requests_db;

            nlohmann::json return_ = requests_db.end_task(user_data["id"].s());

            if (return_ == nullptr) {
                return crow::response(400, "Não foi possível alterar o estado da task");
            }

            nlohmann::json response;
            response["id"] = user_data["id"].i();
            response["task_state"] = return_["task_state"];
            response["completion_date"] = return_["completion_date"];
            response["completion_time"] = return_["completion_time"];

            return crow::response(200, response.dump());
        });


        CROW_ROUTE(app, "/end_pomodoro_session").methods(crow::HTTPMethod::POST)([](const crow::request& req) {

            auto user_data = crow::json::load(req.body);

            if (!user_data) {
                return crow::response(400, "JSON Invalido");
            }

            RequestsDB requests_db;

            nlohmann::json completed = requests_db.end_pomodoro_session(user_data["id"].s(), user_data["total_focus"].s(), user_data["timer"].s());

            if (completed == nullptr) {
                return crow::response(400, "Não foi possível encerrar a sessão pomodoro");
            }

            nlohmann::json response;
            response["id"] = user_data["id"].i();
            response["end_time"] = completed["end_time"];
            response["date"] = completed["date"];
            response["total_focus"] = completed["total_focus"];
            response["timer"] = completed["timer"];

            return crow::response(200, response.dump());
        });

        CROW_ROUTE(app, "/new_pomodoro").methods(crow::HTTPMethod::POST)([](const crow::request& req) {

            auto user_data = crow::json::load(req.body);

            if (!user_data) {
                return crow::response(400, "JSON Invalido");
            }

            RequestsDB requests_db;

            nlohmann::json return_ = requests_db.new_pomodoro(
                user_data["title"].s(),
                user_data["description"].s(),
                user_data["creator_id"].s(),
                user_data["blocks"].s(),
                user_data["short_pause"].s(),
                user_data["big_pause"].s()
            );

            if (return_ == nullptr) {
                return crow::response(400, "Não foi possível criar a sessão pomodoro");
            }

            return crow::response(200, return_.dump());
        });

        CROW_ROUTE(app, "/standby_pomodoro").methods(crow::HTTPMethod::POST)([](const crow::request& req) {
            auto user_data = crow::json::load(req.body);
            if (!user_data) return crow::response(400, "JSON Invalido");

            RequestsDB requests_db;
            nlohmann::json result = requests_db.standby_pomodoro(
                user_data["id"].s(), 
                user_data["total_focus"].s(), 
                user_data["timer"].s(), 
                user_data["small_pauses"].s(), 
                user_data["big_pauses"].s(), 
                user_data["is_pause"].s());

            if (result == nullptr)
                return crow::response(400, "Não foi possível salvar o estado da sessão");

            nlohmann::json response;
            response["id"] = user_data["id"].i();
            response["timer"] = result["timer"];
            response["total_focus"] = result["total_focus"];
            response["small_pauses"] = result["small_pauses"];
            response["big_pauses"] = result["big_pauses"];
            response["is_pause"] = result["is_pause"];
            return crow::response(200, response.dump());
        });

        //roda o servidor do crow na porta 18080
        app.port(18080).multithreaded().run();

    }
    catch(const std::invalid_argument &e){
        std::cout << "Erro ao inicializar o servidor:" << e.what() << std::endl;
    }
}