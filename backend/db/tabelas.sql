--tabelas do sistema (versão atualizada)

create table Users (
	id int generated always as identity primary key,
	name varchar(100) not null,
	age int not null,
	email varchar(100) not null,
	password varchar(255) not null
);

create table Projects (
	id int generated always as identity primary key,
	title varchar(100) not null,
	description varchar(1000),
	start_date date not null,
	delivery_date date not null,
	members integer[],
	project_state boolean not null default false,
	completion_date date,

	user_id int not null,
	foreign key (user_id) references Users(id)
);

create table Tasks (
	id int generated always as identity primary key,
	title varchar(100) not null,
	description varchar(1000),
	task_date date not null,
	priority varchar(10) not null,
	task_state boolean not null default false,
	completion_date date,
	completion_time time,

	user_id int not null,
	foreign key (user_id) references Users(id)
);

create table PomodoroSessions (
	id int generated always as identity primary key,
	title varchar(100) not null,
	description varchar(1000),
	short_pause time not null,
	big_pause time not null,
	blocks time not null,
	start_time time not null,
	end_time time,
	date date,
	total_focus time default '00:00:00',
	timer time default '00:00:00',
	small_pauses int default 0,
	big_pauses int default 0,
	is_pause boolean not null default false,

	user_id int not null,
	foreign key (user_id) references Users(id)
);