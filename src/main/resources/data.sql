insert into ROLE select * from (
select 1, 'ADMIN' union
select 2, 'USER'
) x where not exists(select * from ROLE);