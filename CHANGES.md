
# Changes

### Config
- The config file had 2 nested parent nodes `spring:` I removed one of them


### Service Layer
- Added a service layer and moved all the business logic from the controller to the service
- Added Services:
  - `EmployeeService` interface and implementing class `EmployeeServiceImpl`
  - `ActivityService` interface and implementing class `ActivityServiceImpl`
  - `AwardsService` interface and implementing class `AwardsServiceImpl`
  
### DTOs & Validation
- Added DTOs for all entities
- Added separate DTOs for get, create and update in the case of Employee due to different states that these end-points get/modify/create
- Added validation on DTOs
- Added db constraints on Entities
- Added `GlobalExceptionHandler` to provide consistent 400 validation error responses across all controllers
- Standardized validation messages across DTOs and externalized them to `messages.properties` for i18n support

### Controllers Changes
- Used `@RestController` instead of `@Controller` and removed method-level `@ResponseBody` in both `EmployeeController.java` and `AwardsController.java`
- Using proper HTTP semantics
    -  Changed `POST /employees`: to return `201 Created` and include a `Location` header pointing to `/employees/{id}` instead of only returning `200 OK`
    -  Changed `DELETE /employees/{id}`: to use `204 No Content` instead of returning a map with `{ "deleted": true }`.
    -  Returning the proper HTTP codes
- Added `consumes = "application/json"` and `produces = "application/json"` on mappings for clarity.

### Domain Changes
- Changed `Employee.dundeeAwards` to `int` instead of `Integer` since we want it to initially be `0`
- Changed `Employee.organization` to `FetchType.LAZY`, it had a default `FetchType.EAGER`. EAGER on many-to-one is common but can cause N+1 queries when listing employees.
- Added `equals` and `hashCode` to all entities

### Other
- Enabled `OpenAPI Documenation` `http://localhost:3000/swagger-ui.html`
- Used constructors to initialize dependencies instead of `@Autowired`
- When deleting an employee instead of:
    - Changed `employeeRepository.findById(id)` to `employeeRepository.existsById(id)`
    - Changed `employeeRepository.delete(employee)` to `employeeRepository.deleteById(id)`
- Added logging using native Java logging `import java.util.logging.Logger;` in production I would use `SLF4J` but I didn't want to include the library for this assignment 

### Things I didn't change
Some changes I didn't make, since they might be out of scope for this assignment, but it is worth mentionting them.
- I would add Unit and Integration tests
- I didn't you this change in case you wanted to test against existing URLs
  - `@RequestMapping()` can be given a prefix, e.g. `@RequestMapping("/api")` 
  - We could add a version to the api e.g. `"/api/v1/employee`
- Adding `pagination` to end-points that get all employees and all activities  
