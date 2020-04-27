insert into ROLE select * from (
select 1, 'ADMIN' union
select 2, 'USER'
) x where not exists(select * from ROLE);

delete from LOBBY_GAME;
delete from LOBBY_USER;
delete from GAME;
delete from LOBBY;