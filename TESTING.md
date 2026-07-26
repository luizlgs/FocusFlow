# Testes

A validação do FocusFlow foi feita de forma **manual**, exercitando o app real
contra o servidor rodando localmente e conferindo o resultado diretamente no
banco de dados via SQL. Esta seção descreve os cenários testados e o que se
esperava de cada um.

## Ambiente de teste

- Servidor Crow rodando em `localhost:18080`.
- Banco PostgreSQL populado com o script `backend/db/tabelas.sql`.
- App Android rodando em dispositivo físico na mesma rede do servidor.
- Verificação das respostas pelos logs do Crow (`CROW_LOG_INFO`) e pelo Logcat do Android.
- Verificação do estado final sempre por consulta SQL direta às tabelas.

---

## 1. Criação de projetos com múltiplos membros

**Objetivo:** verificar se um projeto criado por um usuário aparece corretamente
nas contas dos demais membros informados.

**Procedimento**

1. Cadastrar (ou usar) pelo menos três contas distintas.
2. Logar com o usuário A e criar um projeto informando os e-mails dos usuários
   B e C no campo de membros.
3. Conferir no banco se o array `members` do projeto contém os três `id`s:

```sql
SELECT id, title, user_id, members FROM Projects ORDER BY id DESC LIMIT 1;
```

4. Deslogar e entrar com o usuário B, depois com o C, verificando se o projeto
   aparece na listagem de projetos de cada um.

**Resultado esperado**

- O array `members` contém o criador e os dois membros convidados.
- O projeto aparece na tela de projetos das três contas.
- O nome exibido como criador é o do usuário A em todas elas.
- Membros com e-mail inexistente são simplesmente ignorados, sem quebrar a criação.

---

## 2. Campos vazios e valores inválidos

**Objetivo:** garantir que o servidor rejeita dados inconsistentes em vez de
gravá-los no banco, e que o app trata a rejeição sem travar.

**Procedimento**

Tentar submeter cada formulário do app com dados inválidos:

| Cenário | Onde | Esperado |
|---------|------|----------|
| Todos os campos vazios | Criação de tarefa, projeto e sessão | `422`, nada gravado |
| Data de entrega no passado | Criação de projeto | `422`, nada gravado |
| Data da tarefa no passado | Criação de tarefa | `422`, nada gravado |
| E-mail sem formato válido | Cadastro e login | `422` (cadastro) / `401` (login) |
| Senhas diferentes | Cadastro | `422`, usuário não criado |
| E-mail já cadastrado | Cadastro | `422`, usuário não duplicado |
| Tempos fora do formato `HH:MM:SS` | Criação de sessão Pomodoro | `422`, nada gravado |
| Campos de pausa em branco | Criação de sessão Pomodoro | Mensagem de erro na tela, sem crash |
| ID inexistente | Exclusão de tarefa/projeto/sessão | `404`, nada apagado |
| ID de outro usuário | Exclusão e conclusão | `404`, recurso preservado |
| Requisição sem token | Qualquer rota protegida | `401` |

Após cada tentativa, confirmar que nenhuma linha nova apareceu:

```sql
SELECT COUNT(*) FROM Tasks;
SELECT COUNT(*) FROM Projects;
SELECT COUNT(*) FROM PomodoroSessions;
```

**Resultado esperado**

- Nenhum dado inválido chega ao banco.
- O app exibe a mensagem de erro na própria tela (`TextView` de erro), sem
  fechar a Activity e sem lançar exceção.
- Campos numéricos vazios não causam `NumberFormatException`.

---

## 3. Persistência do tempo ao sair e voltar de uma sessão Pomodoro

**Objetivo:** verificar se o progresso de uma sessão em andamento é salvo no
servidor quando o app sai de primeiro plano, e restaurado ao retornar.

**Procedimento**

1. Criar e iniciar uma sessão Pomodoro.
2. Deixar o cronômetro correr por um tempo observável (ex.: alguns minutos,
   passando por pelo menos uma pausa curta).
3. Sair da tela do timer (voltar, trocar de app ou bloquear a tela), o que
   dispara `onPause()` e a chamada a `/standby_pomodoro`.
4. Conferir imediatamente no banco se os valores foram gravados:

```sql
SELECT id, timer, total_focus, small_pauses, big_pauses, is_pause
FROM PomodoroSessions
ORDER BY id DESC LIMIT 1;
```

5. Reabrir a sessão no app e verificar se o cronômetro retoma do ponto salvo.
6. Encerrar a sessão e conferir se `end_time`, `date` e `total_focus` foram gravados.

**Resultado esperado**

- Ao sair, `timer`, `total_focus`, `small_pauses`, `big_pauses` e `is_pause`
  refletem o estado exato em que a sessão estava.
- Ao reabrir, o timer continua do valor salvo, e não do início.
- O contador de pausas curtas e longas é preservado, mantendo o ciclo correto.
- Ao encerrar, a sessão recebe horário de término e data, e sai da
  `current_session` no cache local.

---

## 4. Reflexo dos dados no histórico e nas estatísticas

**Objetivo:** verificar se projetos, tarefas e sessões concluídos aparecem
corretamente no histórico e nos gráficos de estatísticas.

**Procedimento**

1. Popular o banco com dados de teste espalhados pelos últimos seis meses
   (script de povoamento em `backend/db/`), com quantidades variadas de itens
   concluídos por usuário.
2. Logar e abrir a tela de histórico, conferindo se todos os itens concluídos
   aparecem listados.
3. Abrir a tela de estatísticas e conferir os três gráficos (tarefas, sessões
   e projetos concluídos).
4. Comparar a contagem exibida com a do banco:

```sql
SELECT completion_date, COUNT(*) FROM Tasks
WHERE task_state = true AND completion_date >= CURRENT_DATE - INTERVAL '6 months'
GROUP BY completion_date ORDER BY completion_date;
```

5. Concluir um novo item pelo app e verificar se ele aparece no histórico e no
   gráfico sem precisar deslogar.

**Resultado esperado**

- Todos os itens concluídos dentro da janela de seis meses aparecem nos gráficos.
- Nenhum item concluído dentro do período fica de fora (o corte da janela
  corresponde ao período realmente exibido).
- Itens ainda não concluídos não aparecem nas estatísticas.
- Um item concluído durante a sessão aparece imediatamente após a tela ser
  recriada, refletindo o cache local atualizado.
- As datas dos pontos no gráfico batem com as `completion_date` do banco.