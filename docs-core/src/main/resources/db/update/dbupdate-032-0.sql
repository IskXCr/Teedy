create cached table T_CHAT_MESSAGE ( MSG_ID_C varchar(36) not null, MSG_IDUSER_C varchar(36) not null, MSG_CONTENT_C varchar(4000) not null, MSG_CREATEDATE_D datetime, MSG_DELETEDATE_D datetime, primary key (MSG_ID_C) );
alter table T_CHAT_MESSAGE add constraint FK_MSG_IDUSER_C foreign key (MSG_IDUSER_C) references T_USER (USE_ID_C) on delete restrict on update restrict;
update T_CONFIG set CFG_VALUE_C = '32' where CFG_ID_C = 'DB_VERSION';
