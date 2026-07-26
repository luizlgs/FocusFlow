# FocusFlow

Aplicativo de produtividade Android que une organização de projetos e 
tarefas com a técnica Pomodoro, permitindo acompanhar o progresso ao 
longo do tempo através de estatísticas.

## Funcionalidades

- Criação e acompanhamento de **projetos**, com membros e prazos de entrega.
- Gerenciamento de **tarefas**, com prioridade e data de conclusão.
- **Sessões Pomodoro** com temporizador, pausas curtas/longas e histórico de foco.
- **Estatísticas** com gráficos de tarefas, sessões e projetos concluídos nos últimos meses.
- Histórico unificado de tudo que foi concluído.
- **Autenticação por token JWT**, com senhas armazenadas como hash Argon2id.

## Tecnologias

**Frontend (Android)**
- Java
- Android Studio
- MPAndroidChart (gráficos)

**Backend**
- C++17
- [Crow](https://github.com/CrowCpp/Crow) (framework web)
- libpqxx (acesso ao PostgreSQL)
- nlohmann/json
- libsodium (hash de senhas com Argon2id)
- [jwt-cpp](https://github.com/Thalhammer/jwt-cpp) (geração e validação de tokens JWT)
- OpenSSL (assinatura HMAC dos tokens)

**Banco de dados**
- PostgreSQL

## Segurança

- Todas as consultas ao banco usam **queries parametrizadas** (`exec_params`), evitando SQL injection.
- Senhas nunca são armazenadas em texto puro — são salvas como hash Argon2id via libsodium.
- Após o login, o servidor devolve um **token JWT** com validade de 24 horas, que o app anexa a todas as requisições seguintes.
- Além de autenticar, o backend verifica se o usuário do token é o **dono** do recurso antes de alterar ou apagar projetos, tarefas e sessões.

## Estrutura do repositório

```
FocusFlow/
├── app/          # Projeto Android (Java)
└── backend/      # Servidor C++ (Crow + libpqxx)
    ├── Crow/     # Submódulo do framework Crow
    ├── db/       # Scripts SQL do banco
    ├── include/  # Headers (RequestsDB.hpp, core.hpp)
    └── src/      # Código fonte (main.cpp, RequestsDB.cpp)
```

## Como rodar o backend

1. Instale as dependências: PostgreSQL, libpqxx, nlohmann-json, libsodium, OpenSSL e CMake.
   O jwt-cpp é baixado automaticamente pelo CMake via `FetchContent`.
2. Clone o repositório com os submódulos:
```bash
   git clone --recurse-submodules https://github.com/luizlgs/FocusFlow.git
```
3. Crie o banco de dados executando o script em `backend/db/tabelas.sql`.
4. Defina a chave secreta usada para assinar os tokens JWT:
```bash
   export JWT_SECRET="sua_chave_secreta_aqui"
```
5. Compile:
```bash
   cd backend
   mkdir build && cd build
   cmake ..
   make
```
6. Rode o servidor:
```bash
   ./main
```
   O servidor sobe na porta `18080`.

## Como rodar o app Android

1. Abra a pasta `app/` no Android Studio.
2. Ajuste o IP do servidor em `DataRequests.java` para o IP da máquina rodando o backend.
3. Rode o app num emulador ou dispositivo físico na mesma rede.

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

```
MIT License

Copyright (c) 2026 Luiz Gustavo Santos

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```