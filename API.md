# API do FocusFlow

Servidor REST em C++ (Crow), rodando por padrão em `http://<ip-do-servidor>:18080`.

Todas as rotas (exceto `/`) usam **POST** e trocam dados em **JSON**.

## Autenticação

O login devolve um **token JWT** válido por 24 horas. Todas as rotas protegidas
exigem que esse token seja enviado no campo `token` do próprio corpo JSON da requisição:

```json
{ "id": "12", "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

Além de validar o token, o servidor confere se o usuário autenticado é o **dono**
do recurso antes de alterar ou apagar. Nas rotas de criação, o dono é sempre
extraído do token — qualquer `creator_id` enviado no corpo é ignorado.

## Códigos de resposta

| Código | Significado |
|--------|-------------|
| `200` | Sucesso |
| `400` | Corpo da requisição não é um JSON válido |
| `401` | Token ausente, inválido, expirado — ou credenciais incorretas no login |
| `404` | Recurso não encontrado, ou não pertence ao usuário autenticado |
| `422` | Dados inválidos (campo vazio, data no passado, formato incorreto) |

Respostas de erro sempre têm o formato:

```json
{ "error": "mensagem descritiva" }
```

---

## Rotas

### `GET /`

Verifica se o servidor está no ar. Retorna o texto `Servidor ativo`.

---

### `POST /login`

Autentica o usuário e devolve todos os seus dados de uma vez, junto do token.

**Requisição**
```json
{ "email": "usuario@exemplo.com", "pass": "senha123" }
```

**Resposta `200`**
```json
{
  "id": 1,
  "name": "Luiz",
  "email": "usuario@exemplo.com",
  "age": 22,
  "projects": "[...]",
  "tasks": "[...]",
  "pomodorosessions": "[...]",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Os campos `projects`, `tasks` e `pomodorosessions` são **strings** contendo
arrays JSON serializados, não arrays diretos.

**Erros:** `400`, `401` (credenciais incorretas), `422` (falta `email` ou `pass`)

---

### `POST /register`

Cadastra um novo usuário. A senha é armazenada como hash Argon2id.

**Requisição**
```json
{
  "name": "Luiz",
  "email": "usuario@exemplo.com",
  "age": 22,
  "pass1": "senha123",
  "pass2": "senha123"
}
```

**Resposta `200`**
```json
{ "accepted": "Dados recebidos com sucesso!" }
```

**Erros:** `400`, `422` (senhas diferentes, e-mail inválido, e-mail já cadastrado, campo vazio, idade negativa)

---

### `POST /new_task` 🔒

Cria uma tarefa. `task_date` não pode ser uma data passada.

**Requisição**
```json
{
  "title": "Estudar C++",
  "description": "Revisar ponteiros",
  "task_date": "2026-08-01",
  "priority": "Alta",
  "token": "..."
}
```

**Resposta `200`**
```json
{ "id": 42 }
```

**Erros:** `400`, `401`, `422`

---

### `POST /new_project` 🔒

Cria um projeto. `members` é uma string com os e-mails dos membros separados por
espaço; o criador é adicionado automaticamente. `delivery_date` não pode estar no passado.

**Requisição**
```json
{
  "title": "TCC",
  "description": "Aplicativo de produtividade",
  "start_date": "2026-07-26",
  "delivery_date": "2026-12-01",
  "members": "colega1@exemplo.com colega2@exemplo.com",
  "token": "..."
}
```

**Resposta `200`**
```json
{
  "id": 7,
  "members": [
    { "name": "Colega 1", "email": "colega1@exemplo.com" },
    { "name": "Colega 2", "email": "colega2@exemplo.com" }
  ]
}
```

**Erros:** `400`, `401`, `422`

---

### `POST /new_pomodoro` 🔒

Cria uma sessão Pomodoro. Os tempos usam o formato `HH:MM:SS`.

**Requisição**
```json
{
  "title": "Foco da manhã",
  "description": "Estudar backend",
  "blocks": "00:25:00",
  "short_pause": "00:05:00",
  "big_pause": "00:15:00",
  "token": "..."
}
```

**Resposta `200`**
```json
{
  "id": 15,
  "date": "",
  "start_time": "08:30:00",
  "total_focus": "00:00:00"
}
```

**Erros:** `400`, `401`, `422`

---

### `POST /end_task` 🔒

Alterna o estado da tarefa (a fazer ↔ finalizada). Ao concluir, grava data e
hora de conclusão; ao reabrir, limpa esses campos.

**Requisição**
```json
{ "id": "42", "token": "..." }
```

**Resposta `200`**
```json
{
  "id": 42,
  "task_state": "t",
  "completion_date": "2026-07-26",
  "completion_time": "14:35:00"
}
```

**Erros:** `400`, `401`, `404`, `422`

---

### `POST /end_project` 🔒

Alterna o estado do projeto (em andamento ↔ finalizado).

**Requisição**
```json
{ "id": "7", "token": "..." }
```

**Resposta `200`**
```json
{
  "id": 7,
  "project_state": "t",
  "completion_date": "2026-07-26"
}
```

**Erros:** `400`, `401`, `404`, `422`

---

### `POST /end_pomodoro_session` 🔒

Encerra uma sessão Pomodoro, gravando o horário de término e o foco acumulado.

**Requisição**
```json
{
  "id": "15",
  "total_focus": "01:20:00",
  "timer": "00:12:30",
  "token": "..."
}
```

**Resposta `200`**
```json
{
  "id": 15,
  "end_time": "10:15:00",
  "date": "2026-07-26",
  "total_focus": "01:20:00",
  "timer": "00:12:30"
}
```

**Erros:** `400`, `401`, `404`, `422`

---

### `POST /standby_pomodoro` 🔒

Salva o progresso de uma sessão em andamento, para que ela possa ser retomada
do ponto onde parou. Chamada quando o app vai para segundo plano.

**Requisição**
```json
{
  "id": "15",
  "total_focus": "00:45:00",
  "timer": "00:08:20",
  "small_pauses": "2",
  "big_pauses": "1",
  "is_pause": "false",
  "token": "..."
}
```

`small_pauses` e `big_pauses` são contadores inteiros (enviados como string);
`total_focus` e `timer` usam o formato `HH:MM:SS`.

**Resposta `200`**
```json
{
  "id": 15,
  "timer": "00:08:20",
  "total_focus": "00:45:00",
  "small_pauses": "2",
  "big_pauses": "1",
  "is_pause": "f"
}
```

**Erros:** `400`, `401`, `404`, `422`

---

### `POST /delete_task` 🔒
### `POST /delete_project` 🔒
### `POST /delete_pomodoro_session` 🔒

Apagam permanentemente o recurso. As três rotas têm o mesmo formato.

**Requisição**
```json
{ "id": "42", "token": "..." }
```

**Resposta `200`**
```json
{ "id": 42 }
```

**Erros:** `400`, `401`, `404`, `422`

---

## Estados

Os campos de estado são retornados como os literais booleanos do PostgreSQL:

| Valor | Projeto | Tarefa |
|-------|---------|--------|
| `"f"` | Em andamento | À fazer |
| `"t"` | Finalizado | Finalizada |

🔒 = rota protegida, exige token válido.