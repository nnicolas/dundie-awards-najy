# Implementing `/give-dundie-awards/{organizationId}` End-Point

## Implementation Details
I have implemented `/give-dundie-awards/{organizationId}` end-point in: `AwardsController`
- `AwardsController` calls `AwardsService.giveAwards`

```java
    @PostMapping(value = "/give-dundie-awards/{organizationId}" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GiveAwardsResponse> giveDundieAwards(@PathVariable long organizationId) {
        try {
            int numAwards = awardsService.giveAwards(organizationId);
            Logger.getGlobal().log(Level.INFO, String.format("Awards submitted for organizationId: %d", organizationId));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GiveAwardsResponse(organizationId, numAwards));
        } catch (Exception ex) {
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to submit Awards for organizationId: %d", organizationId));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
```


- `AwardsService.giveAwards`, This method is `@Transactional`, it:
  - Increment awards for all org employees by calling: `employeeRepository.incrementDundieAwardsForOrgEmployees`
  - And publishes an event by calling: `publisher.publishEvent`. The AwardsEvent is handled asynchronously in the `AwardsEventListener`
  - If publishing the AwardsEvent fails, the increment is rolled back because the method is `@Transactional`
  - The end-state after calling `AwardsService.giveAwards` is:
    - All `employee.dundeeAwards` for the org are incremented by 1 and then event is published
    - or No event is published and no `employee.dundeeAwards` are incremented

```java
    @Transactional
    public int giveAwards(long organizationId) {
        int changedRows;
        try {
            changedRows = employeeRepository.incrementDundieAwardsForOrgEmployees(organizationId);
            publisher.publishEvent(new AwardsEvent(this, changedRows, organizationId));
            Logger.getGlobal().log(Level.INFO, String.format("Awards given and event published for organizationId: %d", organizationId));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to submit Awards for organizationId: %d", organizationId));
            throw new FailedToGiveAwardsException(e);
        }
    
        return changedRows;
    }
```

- Event handler: `AwardsEventListener`
  - Creates an activity for the Award event
  - If the creation of the activity fails, the increment is rolled back by calling `awardsService.rollbackAwards` which decrements the `Employee.dundeeAwards` for all org employees

```java
    @EventListener
    public void handleAwardsGivenToOrgMembers(AwardsEvent event) {
        try {
            Activity activity = activityService.createActivityForAwardsGiven(event.getOrganizationId(), event.getNumAwards());
            Logger.getGlobal().log(Level.INFO, String.format("Activity created for awards given event %s", activity.getEvent()));
        } catch (Exception e) {
            int rolledBackAwards = awardsService.rollbackAwards(event.getOrganizationId());
            Logger.getGlobal().log(Level.SEVERE, String.format("Error creating activity for orgId=%d. Rolled back %d awards. Details: %s %n", event.getOrganizationId(), rolledBackAwards, e.getMessage()), e);
        }
    }
```
## Integration Tests
I added 3 integration tests for the award/rollback feature
- `AwardServiceSuccessIntegrationTest` The happy path where:
  - We increment Employee.dundeeAward successfully
  - We publish the event successfully
  - We save the activity successfully in the event handler
- `AwardServiceFailedToPublishEventIntegrationTest`
  - We increment Employee.dundeeAward successfully
  - We fail to publish the event
  - The increments are rolled back automatically part of the `@Transactional`
- `AwardServiceFailedToCreateActivityIntegrationTest`
    - We increment Employee.dundeeAward successfully
    - We publish the event successfully
    - We fail to save the Activity
    - We rollback by decrementing Employee.dundeeAward of the org


## Notes
I didn't use an external message broker for this assignment to keep things simple.
In a production system I would use a message broker like `kafka` where we would try to re-process the event before rolling back.
Using kafka would make sure that the message is processed or put on a `DLQ` after failed reprocessing attempts
  

## Limitations of this solution:

### Events are lost if the server crashes
If the server crashes before the message is processed, the message is lost. That's why an external message broker like `kafka` would be a good choice here.

### Inconsistent state if we fail to rollback
With the current implementation, if we fail to create an Activity and fail to rollback, we don't reprocess the message, and we are left in an inconsistent state. 

We have incremented the `dundeeAwards` but we didn't create an activity.

### Employees created between increment and rollback
#### Problem
- An employee could be added to the org between the time we increment and the time we rollback (decrement), such that the employee is decremented but not incremented. 
- They will end up having a negative dundeeAwards which would violate the db constraint @Min(0) and would result in not rolling back all employees. Or if we remove the @Min(0) they will endup with a negative dundeeAwards saved int he DB

#### Solution (One of many)
We can solve this problem by adding a new db table that would keep track of all employees that were part of the increment so that only these employees are rolled back.


# Code Improvements

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
- Enabled Actuator
  - Added actuator dependency to `build.grale`
  - Added actuator configurations to `application.yml`
  - Actuator URLs:
    - Base URL: http://localhost:3000/actuator
    - Health check: http://localhost:3000/actuator/health
    - Metrics: http://localhost:3000/actuator/metrics
    - Environment info: http://localhost:3000/actuator/env
    - Thread dump: http://localhost:3000/actuator/threaddump
  
- Enabled `OpenAPI Documenation` `http://localhost:3000/swagger-ui.html`
- Used constructors to initialize dependencies instead of `@Autowired`
- Added logging using native Java logging `import java.util.logging.Logger;` in production I would use `SLF4J` but I didn't want to include the library for this assignment 
- When deleting an employee:
    - Changed `employeeRepository.findById(id)` to `employeeRepository.existsById(id)`
    - Changed `employeeRepository.delete(employee)` to `employeeRepository.deleteById(id)`

### Things I didn't change
Some changes I didn't make, since they might be out of scope for this assignment, but it is worth mentionting them.
- I would add Unit and Integration tests
- I didn't you this change in case you wanted to test against existing URLs
  - `@RequestMapping()` can be given a prefix, e.g. `@RequestMapping("/api")` 
  - We could add a version to the api e.g. `"/api/v1/employee`
- Adding `pagination` to end-points that get all employees and all activities  
