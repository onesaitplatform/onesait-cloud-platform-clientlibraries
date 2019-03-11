# Uso de la librería

###  Importar la dependencia en el fichero pom.xml

```xml
<dependency>
    <groupId>com.indra.sofia2.boot</groupId>
    <artifactId>sofia4boot</artifactId>
    <version>1.2-SNAPSHOT</version>
</dependency>
```

### Notas

Crear una clase de arranque spring boot, no usar la clase initializer. Ejemplo:

```java
@SpringBootApplication
public class App {

    public static void main (String [] args) {

        new SpringApplicationBuilder(App.class).run(args);
    }
}
``` 

### Habilitar spring boot admin

1. Añadir la anotación `@EnableSpringBootAdminRegister` en una clase de configuración de spring:

```java
@SpringBootApplication
@EnableSpringBootAdminRegister
public class App {

    public static void main (String [] args) {

        new SpringApplicationBuilder(App.class).run(args);
    }
}
```

2. Configurar las propiedades de spring boot admin

```
# Spring boot admin config
management.security.enabled = false
spring.boot.admin.url       = http://localhost:8101
spring.boot.admin.prefer-ip = true
```

### Ejemplo de uso con ontologías

**Modelo de datos de la ontología**

```java
public class Level {

    private String date;
    private Integer value;

    public Level() {

    }

    public Level(String date, Integer value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
```

**Definición de la ontología para sofia2**

```java
public class LevelOntology extends Ontology<Level> {

    @JsonProperty("Level")
    private Level level;

    public LevelOntology (String date, Integer value) {

        level = new Level(date, value);
    }

    @Override
    public void setData(Level level) {
        
        this.level = level;
    }

    @Override
    public Level getData() {
        
        return level;
    }
}
```

**Definir operaciones del repositorio**

```java
@Sofia2Repository("level")
public interface LevelRepository {

    @Sofia2Query("select * from level")
    List<LevelOntology> findAll ();

    @Sofia2Query("select * from level where Level.value=0")
    List<LevelOntology> findAllZeros ();

    @Sofia2Query("select * from level where Level.value=$value")
    List<LevelOntology> findAllWhereValueMatches (@Param("$value") Integer value);

    @Sofia2Insert
    SofiaId save(LevelOntology levelOntology);

    @Sofia2Query("db.level.remove({\"Level.value\":$value})")
    SofiaIds deleteWhereValueMatches(@Param(value = "$value") Integer value);

    @Sofia2Delete
    List<SofiaId> delete(String id);

}
```


**Ejemplo de bean que envía un mensaje al instanciarse**

```java
@Component
public class Provider {

    private static final Logger log = LoggerFactory.getLogger(Provider.class);
    private Random random = new Random();

    @Autowired
    private LevelRepository levelRepository;

    @PostConstruct
    public void sendRandomMessage () {

        try {
            log.info("Inserting data");
            // --- create new level value
            Instant now = Instant.now();
            Integer value = random.nextInt(10);

            // --- insert a random value
            SofiaId id = levelRepository.save(new LevelOntology(now.toString(), value));
            log.info("Created new record with value '{}' and id '{}'", value, id.getId().getOid());
        } catch (Exception e) {
            log.warn("Captured exception running S4B", e);
        }
    }
}

```

### Fichero `POM` de referencia


```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.6.RELEASE</version>
        <relativePath/>
    </parent>

    <groupId>com.minsait.s2</groupId>
    <version>1.0</version>
    <artifactId>demo-sofia4boot</artifactId>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.indra.sofia2.boot</groupId>
            <artifactId>sofia4boot</artifactId>
            <version>1.2-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Empaqueta junto con todas las dependencias y genera script de servicio -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <!-- Add service script -->
                <configuration>
                    <executable>true</executable>
                </configuration>
                <!-- Create a fat jar -->
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

        </plugins>
    </build>

</project>
```

### Fichero de propiedades de referencia

```
sofia2:
  host: localhost
  port: 1884
  token: 8ofe5ba4b40b4a0ea267d076f0fdd4ne
  kp: levelkp
  instance: develop
  mqtt: false
  endpoint: http://localhost:8092/iotbroker/api/api_ssap
```