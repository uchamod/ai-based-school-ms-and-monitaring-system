*put in controllers*
###### **@RestController** - Marks a class as a REST API controller. It tells Spring that this class will handle HTTP requests (GET, POST, PUT, DELETE) and automatically return JSON/XML responses instead of HTML.

*Models and DTOs*
###### **@Data(Lombok)**- Generates boilerplate code (getters, setters, toString(), equals(), hashCode(), and a constructor for final fields).Reduces verbose code in POJOs, improving readability and maintainability.
###### **@Builder(Lombok)**- Implements the Builder pattern for object creation.Simplifies constructing complex objects with many optional parameters, improving code clarity and immutability.
```

without builder
User user = new User(
        1L,
        "John",
        "john@gmail.com",
        "Admin"
);
with builder
User user = User.builder()
        .id(1L)
        .name("John")
        .email("john@gmail.com")
        .role("Admin")
        .build();

```
###### **@JsonBackReference**(Jackson) - Prevents infinite recursion during JSON serialization by marking a back-reference side of a bidirectional relationship (e.g., parent-child in JPA entities).Avoids StackOverflowError in cyclic object graphs, ensuring smooth JSON serialization.
```
Suppose
class Department {                

    List<Employee> employees;
}

class Employee {

    Department department;
}

Result
Department

Employee

Department

Employee

Department

Employee
...

Solution
class Department {

    @JsonManagedReference
    List<Employee> employees;
}

class Employee {

    @JsonBackReference
    Department department;
}

```

*configuration related files (ex-:kafkaconfig,securityconfig)*
###### **@Configuration** - Marks a class as a Spring configuration class.Spring reads it during application startup.Declares a class as a source of bean definitions (via @Bean methods).
###### **@Bean** -Registers an object into the Spring IoC Container.Defines a Spring-managed bean inside a @Configuration class.Allows explicit creation of custom or third-party objects (e.g., RestTemplate, KafkaProducer) for dependency injection.
```
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

use anywhere
private RestTemplate restTemplate;
```

*service files*
###### **@Service** - Marks a class as the business logic layer. Indicates that the class holds business logic, making it a core component in the service layer architecture.
###### **@Transactional** - Runs a method inside a database transaction.Ensures data consistency by managing commit/rollback automatically, especially for JPA/Hibernate operations.Ensure Atomicity, Consistency

```
@Transactional
public void transferMoney() {

    withdraw();

    deposit();
}
without
Withdraw successful
       |
Deposit failed
       | 
Money lost

with
Withdraw successful
        |
Deposit failed
        |
    Rollback
        |
Nothing changes
```
*repositories*
###### **@Repository** -Marks a class as a Data Access Object (DAO) or repository.Enables Spring Data JPA to auto-implement interfaces, and translates database-specific exceptions into Spring’s DataAccessException hierarchy.

*Entry point(where main function)*
###### **@EnableConfigurationProperties** - Enables binding configuration properties from application.yml or application.properties into Java classes.Provides type-safe access to configuration values instead of repeatedly using @Value.

###### **@EnableDiscoveryClient** - Allows the application to register itself with a service discovery server (such as Eureka, Consul, or ZooKeeper) and discover other services dynamically.Enable dynamic routing and load balancing.

###### **@EnableEurekaServer** - Turns a Spring Boot application into a Eureka Server, which acts as a registry where microservices register themselves.Used only in discovery server apps to enable service registration/deregistration for clients.

*Generic / Utility Annotations*
###### **@Value** - Injects values from configuration files or environment variables.
###### **@RequiredArgsConstructor(Lombok)** - Generates a constructor for all final and @NonNull fields.Simplifies dependency injection (constructor-based), making classes immutable and testable.
```
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
}

Lombok generates:
public UserService(UserRepository repository) {
    this.repository = repository;
}
```

###### **@Component** - Marks a class as a Spring-managed component (generic stereotype).Spring creates and manages an instance of this class, allowing it to be injected elsewhere.
```
@Component
public class EmailValidator {

}
```
###### **@Slf4j(Lombok)** - Injects a SLF4J logger instance (log) into the class.automatically creates an SLF4J logger for the class.
```
Without
private static final Logger logger =
        LoggerFactory.getLogger(UserService.class);

With
@Slf4j
@Service
public class UserService {

    public void createUser() {
        log.info("Creating user");
    }
}        
```
