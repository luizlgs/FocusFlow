--tabelas do sistema

create table Users (
	id int generated always as identity primary key,
    name_ varchar(100) not null,
    age int not null,
    email varchar(100) not null,
    password varchar(20) not null
);

create table Projects (
	id int generated always as identity primary key,
	title varchar(100) not null,
	description varchar(100),
	start_date date not null,
	delivery_date date not null,
	members integer[],
	
	user_id int not null,
	foreign key (user_id) references Users(id)
); 


create table Cards (
	id int generated always as identity primary key,
	title varchar(100) not null,
	description varchar(100),
	members integer[] not null,
	start_date date not null,
	delivery_date date not null,
	comments_ varchar(1000),

	project_id int not null,
	foreign key (project_id) references Projects(id)
	
);

create table Tasks (
	id int generated always as identity primary key,
	title varchar(100) not null,
	descripton varchar(100),
	date_ date not null,
	
	user_id int null,
	foreign key (user_id) references Users(id),

	card_id int null,
	foreign key (card_id) references Cards(id)	
);

create table PomodoroSessions(
	id int generated always as identity primary key,
	title varchar(100) not null,
	description varchar(1000),
	time_ time null,
	short_pause time not null,
	big_pause time not null,
	blocks time not null,
	start_time time not null,
	end_time time not null,
	
	user_id int,
	foreign key (user_id) references Users(id)
);