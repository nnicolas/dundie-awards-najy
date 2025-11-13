# Implementing `/give-dundie-awards/{organizationId}` endpoint

## Implementation Details
I have implemented `/give-dundie-awards/{organizationId}` endpoint in: `AwardsController`
- `AwardsController` calls `AwardsService.giveAwards`.

```
    @PostMapping(value = "/give-dundie-awards/{organizationId}" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GiveAwardsResponse> giveDundieAwards(@PathVariable long organizationId) {
        ...
    }
```


- `AwardsService.giveAwards`:
  - Increments awards for all employees in the organization via `employeeRepository.incrementDundieAwardsForOrgEmployees(...)`.
  - Updates cache
  - And publishes an event by calling: `publisher.publishEvent`. The event is handled asynchronously in the `AwardsEventListener` (using `@Async`).
  - If `publishEvent` fails, `compensateAwardsInDbAndCache(...);` is called to compensate, decrement awards that have been given in both DB and cache
  - Intended end state (assuming compensation succeeds):
    - Either all relevant employee.dundieAwards are incremented, cache is updated, and the event is published, or non of them happens 
    - Note: If compensation itself fails, a partially updated state is possible.


```
    public int giveAwards(long organizationId) {
        ...
    }
```

- Event handler: `AwardsEventListener`
  - Creates an activity for the Award event
  - If activity creation fails, it calls `awardsService.compensateAwardsInDbAndCache` to compensate by decrementing `Employee.dundieAwards` for the organization. This is a compensating action, not a rollback of the original transaction.

```java
    @Async
    @EventListener
    public void handleAwardsGivenToOrgMembers(AwardsEvent event) {
        try {
            Activity activity = activityService.createActivityForAwardsGiven(event.getOrganizationId(), event.getNumAwards());
            Logger.getGlobal().log(Level.INFO, String.format("Activity created for awards given event %s", activity.getEvent()));
        } catch (Exception e) {
            int compensatedAwards = awardsService.compensateAwardsInDbAndCache(event.getOrganizationId());
            Logger.getGlobal().log(Level.SEVERE, String.format("Error creating activity for orgId=%d. Compensating %d awards. Details: %s %n", event.getOrganizationId(), compensatedAwards, e.getMessage()), e);
        }
    }
```
## Integration Tests
I added 2 integration tests for the award/compensate feature
- `AwardServiceSuccessIntegrationTest` The happy path where:
  - We increment Employee.dundieAwards successfully
  - We publish the event successfully
  - We save the activity successfully in the event handler
- `AwardServiceFailedToPublishEventIntegrationTest`
  - We increment Employee.dundieAwards successfully
  - We fail to publish the event. Using a mock to simulate failure.
  - We compensate by decrementing `Employee.dundieAwards` of the org by calling `awardsService.compensateAwards`


## Notes
I didn't use an external message broker for this assignment to keep things simple.
In a production system I would use a message broker like `Kafka` where we would try to re-process the event before compensating given awards.
Using `Kafka` would make sure that the message is processed or put on a `DLQ` after failed reprocessing attempts
  

## Limitations of this solution:

### Events are lost if the server crashes
If the server crashes before the message is processed, the message is lost. That's why an external message broker like `Kafka` would be a good choice here.

### Inconsistent state if we fail to compensate
With the current implementation, if we fail to create an Activity and fail to compensate the awards, we don't reprocess the message, and we are left in an inconsistent state. 

We have incremented the `dundieAwards` but we didn't create an activity.

### Employees created between increment and compensate
#### Problem
- An employee could be added to the org between the time we increment and the time we compensate (decrement), such that the employee is decremented but not incremented. 
- They will end up with a negative `dundieAwards`, violating the `@Min(0)` constraint and preventing compensating from completing for all employees. If `@Min(0)` were removed, they would end up with a negative `dundieAwards` stored in the DB.

#### Solution (One of many)
We can solve this problem by adding a new DB table that would keep track of all employees that were part of the increment so that only these employees are compensated.


# Code Improvements

### Config
- The config file had two nested `spring:` parent nodes; I removed one.


### Service Layer
- Added a service layer and moved all the business logic from the controller to the service
- Added Services:
  - `EmployeeService` interface and implementing class `EmployeeServiceImpl`
  - `ActivityService` interface and implementing class `ActivityServiceImpl`
  - `AwardsService` interface and implementing class `AwardsServiceImpl`
  
### DTOs & Validation
- Added DTOs for all entities
- Added separate DTOs for get, create and update in the case of Employee due to different states that these endpoints get/modify/create
- Added validation on DTOs
- Added DB constraints on Entities
- Added `GlobalExceptionHandler` to provide consistent 400 validation error responses across all controllers
- Standardized validation messages across DTOs and externalized them to `messages.properties` for i18n support

### Controller Changes
- Used `@RestController` instead of `@Controller` and removed method-level `@ResponseBody` in both `EmployeeController.java` and `AwardsController.java`
- Used proper HTTP semantics
    -  Changed `POST /employees`: to return `201 Created` and include a `Location` header pointing to `/employees/{id}` instead of only returning `200 OK`
    -  Changed `DELETE /employees/{id}`: to use `204 No Content` instead of returning a map with `{ "deleted": true }`.
    -  Return appropriate HTTP status codes.
- Added `consumes = "application/json"` and `produces = "application/json"` on mappings for clarity.

### Domain Changes
- Changed `Employee.dundieAwards` to `int` instead of `Integer` since we want it to initially be `0`
- Changed `Employee.organization` to `FetchType.LAZY`, it had a default `FetchType.EAGER`. EAGER on many-to-one is common but can cause N+1 queries when listing employees.
- Added `equals` and `hashCode` to all entities.

### Other
- Enabled Actuator
  - Added actuator dependency to `build.gradle`
  - Added actuator configurations to `application.yml`
  - Actuator URLs:
    - Base URL: http://localhost:3000/actuator
    - Health check: http://localhost:3000/actuator/health
    - Metrics: http://localhost:3000/actuator/metrics
    - Environment info: http://localhost:3000/actuator/env
    - Thread dump: http://localhost:3000/actuator/threaddump
  
- Enabled OpenAPI Documentation at `http://localhost:PORT/swagger-ui.html`
- Used constructors to initialize dependencies instead of `@Autowired`
- Added logging using native Java logging `import java.util.logging.Logger;` In production, I would use SLF4J. I avoided adding the dependency for this assignment. 
- When deleting an employee:
    - Changed `employeeRepository.findById(id)` to `employeeRepository.existsById(id)`
    - Changed `employeeRepository.delete(employee)` to `employeeRepository.deleteById(id)`

### Things I didn't change
Some changes I didn't make, since they might be out of scope for this assignment, but it is worth mentioning them.
- I would add more unit and integration tests.
- I didn’t make this change in case you wanted to test against existing URLs
  - `@RequestMapping()` can be given a prefix, e.g. `@RequestMapping("/api")` 
  - We could add a version to the api e.g. `/api/v1/employee`
- Adding `pagination` to endpoints that get all employees and all activities  
