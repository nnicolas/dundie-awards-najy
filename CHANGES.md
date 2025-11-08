
# Changes

- Used `@RestController` instead of `@Controller` and removed method-level `@ResponseBody` in both `EmployeeController.java` and `AwardsController.java`
- Using proper HTTP semantics
    -  Changed `POST /employees`: to return `201 Created` and include a `Location` header pointing to `/employees/{id}` instead of only returning `200 OK`
    -  Changed `DELETE /employees/{id}`: to use `204 No Content` instead of returning a map with `{ "deleted": true }`.
- Added `consumes = "application/json"` and `produces = "application/json"` on mappings for clarity.



# Things I didn't change
- I didn't you this change in case you wanted to test against existing URLs
  - `@RequestMapping()` can be given a prefix, e.g. `@RequestMapping("/api")` 
  - We could add a version to the api e.g. `"/api/v1/employee`
