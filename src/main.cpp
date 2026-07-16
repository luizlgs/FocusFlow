#include <iostream>
#include <pqxx/pqxx>

int main(){
    try {
        //conexão com o banco de dados
        pqxx::connection db("dbname=postgres user=postgres password=1234 host=localhost port=5432");

        pqxx::work connection_(db);

        pqxx::result rows = connection_.exec("SELECT * FROM Users");

        for (auto row : rows) {
            std::cout << "Nome: " << row["name_"].c_str() << std::endl 
            << "Idade: " << row["age"].c_str() << std::endl 
            << "Email: " << row["email"].c_str() <<std::endl 
            << "Password: " << row["password"].c_str() << std::endl;

            if(row != rows.back())
                std::cout << std::endl;
        }
        
        connection_.commit();
    }
    catch(const std::exception &e){
        std::cerr << "Error to connect to db: " << e.what() << std::endl;
    }
    return 0;
}
