-- SQL script for inserting a vacancy into the database

INSERT INTO vacancies (id,
                       company_name,
                       position,
                       technologies,
                       source,
                       url,
                       applied_at,
                       status,
                       contact_person,
                       contact_email,
                       deadline,
                       remote_allowed,
                       created_at,
                       updated_at)
VALUES (uuid_generate_v4(),
        ?, -- company_name
        ?, -- position
        ?, -- technologies
        ?, -- source
        ?, -- url
        ?, -- applied_at
        ?, -- status
        ?, -- contact_person
        ?, -- contact_email
        ?, -- deadline
        ?, -- remote_allowed
        NOW(), -- created_at
        NOW() -- updated_at
       );