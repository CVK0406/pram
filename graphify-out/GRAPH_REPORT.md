# Graph Report - .  (2026-07-15)

## Corpus Check
- Corpus is ~9,712 words - fits in a single context window. You may not need a graph.

## Summary
- 202 nodes · 208 edges · 99 communities (8 shown, 91 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 5 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Employee Controller Layer
- Exception Handling
- Employee Entity & Repository
- Employee Service Implementation
- Maven Wrapper
- Employee DTO & Mapper
- Application Test Suite
- Health Check Endpoint
- Application Entry Point
- Community 9
- Community 10
- Community 11
- Community 12
- Community 13
- Community 14
- Community 15
- Community 16
- Community 17
- Community 18
- Community 19
- Community 20
- Community 21
- Community 22
- Community 23
- Community 24
- Community 25
- Community 26
- Community 27
- Community 28
- Community 29
- Community 30
- Community 31
- Community 32
- Community 33
- Community 34
- Community 35
- Community 36
- Community 37
- Community 38
- Community 39
- Community 40
- Community 41
- Community 42
- Community 43
- Community 44
- Community 45
- Community 46
- Community 47
- Community 48
- Community 49
- Community 50
- Community 51
- Community 52
- Community 53
- Community 54
- Community 55
- Community 56
- Community 57
- Community 58
- Community 59
- Community 60
- Community 61
- Community 62
- Community 63
- Community 64
- Community 65
- Community 66
- Community 67
- Community 68
- Community 69
- Community 70
- Community 71
- Community 72
- Community 73
- Community 74
- Community 75
- Community 76
- Community 77
- Community 78
- Community 79
- Community 80
- Community 81
- Community 82
- Community 83
- Community 84
- Community 85
- Community 86
- Community 87
- Community 88
- Community 89
- Community 90
- Community 91
- Community 92
- Community 93
- Community 94
- Community 95
- Community 96
- Community 97
- Community 98

## God Nodes (most connected - your core abstractions)
1. `EmployeeResponse` - 19 edges
2. `Employee` - 14 edges
3. `EmployeeRequest` - 13 edges
4. `ErrorResponse` - 10 edges
5. `EmployeeController` - 8 edges
6. `EmployeeRepository` - 8 edges
7. `EmployeeService` - 8 edges
8. `EmployeeServiceImpl` - 8 edges
9. `GlobalExceptionHandler` - 6 edges
10. `DuplicateResourceException` - 5 edges

## Surprising Connections (you probably didn't know these)
- `EmployeeController` --references--> `EmployeeService`  [EXTRACTED]
  src/main/java/com/company/pram/controller/EmployeeController.java → src/main/java/com/company/pram/service/EmployeeService.java
- `EmployeeServiceImpl` --references--> `EmployeeRepository`  [EXTRACTED]
  src/main/java/com/company/pram/service/impl/EmployeeServiceImpl.java → src/main/java/com/company/pram/repository/EmployeeRepository.java
- `EmployeeRepository` --references--> `Employee`  [EXTRACTED]
  src/main/java/com/company/pram/repository/EmployeeRepository.java → src/main/java/com/company/pram/entity/Employee.java
- `EmployeeServiceImpl` --implements--> `EmployeeService`  [EXTRACTED]
  src/main/java/com/company/pram/service/impl/EmployeeServiceImpl.java → src/main/java/com/company/pram/service/EmployeeService.java

## Import Cycles
- None detected.

## Communities (99 total, 91 thin omitted)

### Community 0 - "Employee Controller Layer"
Cohesion: 0.15
Nodes (15): PostMapping, RequestMapping, EmployeeController, GetMapping, Page, RequiredArgsConstructor, ResponseEntity, RestController (+7 more)

### Community 1 - "Exception Handling"
Cohesion: 0.19
Nodes (12): ExceptionHandler, MethodArgumentNotValidException, RestControllerAdvice, ErrorResponse, AllArgsConstructor, Builder, Data, NoArgsConstructor (+4 more)

### Community 2 - "Employee Entity & Repository"
Cohesion: 0.16
Nodes (12): Entity, JpaRepository, PrePersist, PreUpdate, Repository, Employee, AllArgsConstructor, Builder (+4 more)

### Community 3 - "Employee Service Implementation"
Cohesion: 0.23
Nodes (9): Override, Service, DuplicateResourceException, EmployeeService, EmployeeServiceImpl, Page, Pageable, RequiredArgsConstructor (+1 more)

### Community 4 - "Maven Wrapper"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 5 - "Employee DTO & Mapper"
Cohesion: 0.36
Nodes (6): EmployeeRequest, AllArgsConstructor, Builder, Data, NoArgsConstructor, EmployeeMapper

### Community 6 - "Application Test Suite"
Cohesion: 0.60
Nodes (3): SpringBootTest, PramApplicationTests, Test

### Community 7 - "Health Check Endpoint"
Cohesion: 0.60
Nodes (3): GetMapping, RestController, PingController

## Knowledge Gaps
- **1 isolated node(s):** `com.company:pram`
  These have ≤1 connection - possible missing edges or undocumented components.
- **91 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `EmployeeResponse` connect `Employee Controller Layer` to `Employee Service Implementation`, `Employee DTO & Mapper`?**
  _High betweenness centrality (0.054) - this node is a cross-community bridge._
- **Why does `DuplicateResourceException` connect `Employee Service Implementation` to `Exception Handling`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Why does `EmployeeNotFoundException` connect `Exception Handling` to `Employee Service Implementation`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **What connects `com.company:pram` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Employee Controller Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.14855072463768115 - nodes in this community are weakly interconnected._