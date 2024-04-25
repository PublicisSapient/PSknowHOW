SET search_path TO auth;

---
--- insert db user
---
INSERT INTO users (username, password, first_name, last_name, display_name, email, saml_email , approved, user_verified, auth_type, created_date)
	SELECT 'SUPERADMIN', 'sha512:fe08d67f12ab21191d3e665b9b360f5946068a3763be460ef0cbbbaeb2d951660d0c1568f1a74479f8c2ce83132f88693b477b316c00bc62a853d155614e4adb','SUPER', 'ADMIN', 'SUPERADMIN','superadmin@superadmin.com', '' ,true, true, 'STANDARD', CURRENT_DATE
WHERE NOT EXISTS (
	SELECT 1 FROM users WHERE username = 'SUPERADMIN'
);

---
--- resources data
---
with data(name, created_by, created_date)  as (
   values
    ('PSKnowHOW', (SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE),
	('MAP', (SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE),
	('RETRO', (SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE)
)
insert into resource (name, created_by, created_date)
select d.name, d.created_by, d.created_date
from data d
where not exists (select 1
                  from resource x
                  where x.name = d.name);

---
--- roles data
---
with data(name, description, root_user, default_role, resource_id, created_by, created_date )  as (
   values
    ('ROLE_SUPERADMIN', 'ROLE_SUPERADMIN', true , false, (SELECT id FROM resource WHERE name = 'PSKnowHOW') , (SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE),
	('ROLE_NA', 'ROLE_NA', false , true, (SELECT id FROM resource WHERE name = 'PSKnowHOW') , (SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE),
    ('ROLE_NA', 'ROLE_NA', false , true, (SELECT id FROM resource WHERE name = 'MAP') , (SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE),
    ('ROLE_NA', 'ROLE_NA', false , true, (SELECT id FROM resource WHERE name = 'RETRO') , (SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE)
)
insert into role (name, description, root_user, default_role, resource_id, created_by, created_date)
select d.name, d.description, d.root_user, d.default_role, d.resource_id, d.created_by, d.created_date
from data d
where not exists (select 1
                  from role x
                  where x.name = d.name
                  and x.resource_id = d.resource_id);

---
--- user role data
---
with data(role_id, username, created_by, created_date)  as (
   values
    ((SELECT id FROM role WHERE name = 'ROLE_SUPERADMIN'), 'SUPERADMIN',(SELECT id FROM users WHERE username = 'SUPERADMIN'), CURRENT_DATE)
)
insert into user_role (role_id, username, created_by, created_date)
select d.role_id, d.username, d.created_by, d.created_date
from data d
where not exists (select 1
                  from user_role x
                  where x.role_id = d.role_id
				  );

INSERT INTO global_config (env, email_host, email_port, from_email)
VALUES
    ('production', '', 25, 'no-reply@example.com'),
    ('development', '', 25, 'no-reply@example.com')
    ON CONFLICT (env) DO NOTHING;

--- END ---
