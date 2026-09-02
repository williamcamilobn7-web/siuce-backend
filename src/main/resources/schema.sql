create database colegio_disciplina;

use colegio_disciplina;

create table colegios (
    id      bigint auto_increment primary key,
    nombre  varchar(150) not null,
    sede    varchar(100),
    nit     varchar(20) not null unique
);

create table rectores (
    id              bigint auto_increment primary key,
    nombre_completo varchar(150) not null,
    email           varchar(100) not null,
    colegio_id      bigint unique,
    foreign key (colegio_id) references colegios (id) on delete set null
);

create table acudientes (
    id              bigint auto_increment primary key,
    nombre_completo varchar(150) not null,
    telefono        varchar(20),
    email           varchar(100),
    notificado      boolean default false
);

create table entidades_salud (
    id        bigint auto_increment primary key,
    nombre    varchar(150) not null,
    telefono  varchar(20),
    direccion varchar(200)
);

create table policia (
    id                  bigint auto_increment primary key,
    nombre              varchar(150) not null,
    estacion            varchar(150),
    telefono_emergencia varchar(20)
);

create table reportes (
    id                 bigint auto_increment primary key,
    fecha_hora         datetime not null,
    lugar              varchar(200) not null,
    descripcion_hecho  text not null,
    tipo_falta         varchar(10) not null,
    estado             varchar(20) not null default 'PENDIENTE',
    firmado_por        varchar(150),
    entidad_salud_id   bigint,
    policia_id         bigint,
    foreign key (entidad_salud_id) references entidades_salud (id) on delete set null,
    foreign key (policia_id)       references policia (id) on delete set null
);

create table implicados (
    id               bigint auto_increment primary key,
    nombre_completo  varchar(150) not null,
    tipo_documento   varchar(20),
    numero_documento varchar(30),
    grado            varchar(20),
    rol              varchar(20) not null,
    colegio_id       bigint,
    acudiente_id     bigint,
    reporte_id       bigint not null,
    foreign key (colegio_id)   references colegios (id)   on delete set null,
    foreign key (acudiente_id) references acudientes (id) on delete set null,
    foreign key (reporte_id)   references reportes (id)   on delete cascade
);

insert into colegios (nombre, sede, nit) values
    ('Institucion Educativa San Jose', 'Sede Principal', '890123456-1');

insert into rectores (nombre_completo, email, colegio_id) values
    ('Ana Maria Rojas', 'rectora@sanjose.edu.co', 1);

insert into acudientes (nombre_completo, telefono, email) values
    ('Carlos Perez', '3001234567', 'carlos.perez@gmail.com'),
    ('Maria Lopez', '3119876543', 'maria.lopez@gmail.com');

insert into entidades_salud (nombre, telefono, direccion) values
    ('EPS Sanitas', '6041234567', 'Calle 30 No 15-20, Cartagena');

insert into policia (nombre, estacion, telefono_emergencia) values
    ('Policia de Infancia y Adolescencia', 'Estacion Centro', '156');
