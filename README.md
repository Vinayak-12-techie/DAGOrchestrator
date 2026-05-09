# DAG Orchestrator

A robust workflow orchestration system built with Spring Boot that manages Directed Acyclic Graph (DAG) based workflows with task dependencies and execution tracking.

## Features

- **Workflow Definition**: Create workflows with JSON-based task definitions
- **Task Dependencies**: Support for complex task dependency chains
- **Asynchronous Execution**: Scheduled task execution with configurable polling
- **Execution Tracking**: Complete audit trail of workflow and task executions
- **REST API**: Full RESTful API for workflow management
- **Database Persistence**: PostgreSQL-backed data persistence with JPA
- **Comprehensive Logging**: Detailed execution logs for monitoring and debugging

## Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.5
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Logging**: SLF4J with Logback
- **JSON Processing**: Jackson
- **Object Mapping**: ModelMapper
- **Code Generation**: Lombok

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd dag-orchestrator
```

### 2. Database Setup
Create a PostgreSQL database named `dag_orchestrator`:
```sql
CREATE DATABASE dag_orchestrator;
```

### 3. Configure Database Connection
Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dag_orchestrator
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build the Application
```bash
mvn clean compile
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Create Workflow
```http
POST /workflow
Content-Type: application/json

{
  "name": "Sample Workflow",
  "definitionJson": "{\"tasks\": [{\"name\": \"task1\", \"dependencies\": []}, {\"name\": \"task2\", \"dependencies\": [\"task1\"]}]}"
}
```

### Execute Workflow
```http
POST /workflow/{id}/execute
```

## Workflow Definition Format

Workflows are defined using JSON with the following structure:

```json
{
  "tasks": [
    {
      "name": "task1",
      "dependencies": []
    },
    {
      "name": "task2",
      "dependencies": ["task1"]
    },
    {
      "name": "task3",
      "dependencies": ["task1", "task2"]
    }
  ]
}
```

## Architecture

### Core Components

- **WorkFlowController**: REST API endpoints for workflow operations
- **WorkFlowService**: Business logic for workflow creation and execution
- **WorkerService**: Scheduled service for task execution polling
- **Entities**: JPA entities for data persistence
- **Repositories**: Spring Data JPA repositories for database operations

### Execution Flow

1. **Workflow Creation**: Define workflow structure with tasks and dependencies
2. **Workflow Execution**: Initialize execution and create task instances
3. **Task Scheduling**: Mark independent tasks as READY
4. **Task Execution**: Worker service polls and executes READY tasks
5. **Dependency Resolution**: Update dependent tasks when prerequisites complete
6. **Completion**: Mark workflow as COMPLETED when all tasks finish

## Configuration

### Application Properties
Key configuration options in `application.properties`:

- `spring.jpa.hibernate.ddl-auto`: Database schema management
- `spring.jpa.show-sql`: SQL query logging
- `spring.jpa.properties.hibernate.format_sql`: Formatted SQL output

### Logging Configuration
Configure logging levels and output in `src/main/resources/logback.xml`

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package
```

### Code Style
- Uses Lombok for boilerplate code reduction
- Follows Spring Boot conventions
- Comprehensive logging throughout the application

## Monitoring

The application provides detailed logs for:
- Workflow creation and execution
- Task state transitions
- Dependency resolution
- Execution timing
- Error handling

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

---

**Last Updated**: April 25, 2026
