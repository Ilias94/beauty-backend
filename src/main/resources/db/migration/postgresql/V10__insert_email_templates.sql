INSERT INTO beautypg.template (name, subject, body)
VALUES (
  'Account Creation',
  'Beauty app account creation',
  '<!DOCTYPE html>
   <html xmlns:th="http://www.thymeleaf.org" lang="en">
   <body>
     <h1 th:text="''Welcome to our platform, '' + ${firstName} + ''!''">
       Welcome
     </h1>
     <p th:text="${firstName} + '' '' + ${lastName}">User</p>
   </body>
   </html>'
);
