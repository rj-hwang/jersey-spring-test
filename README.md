# The better way to integrate JerseyTest with Spring

- No inheritance from JerseyTest in junit test class
- No dependencies on Jersey in junit test class, just pure jax-rs dependency
- Use Spring's FactoryBean to create a WebTarget instance for junit test (simple inject)
- perfectly integrate SpringJUnit4ClassRunner and @ContextConfiguration
- Run `'mvn test'` to see what's happened

## Unit test class code example
``` java
// HelloResourceTest.java
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import static org.junit.Assert.assertEquals;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-test.xml")
public class HelloResourceTest {
  @Inject
  public WebTarget target;

  @Test
  public void hello() {
    assertEquals("Hello", target.path("hello").request().get(String.class));
  }
}

// HelloResource.java
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
@Named
@Singleton
@Path("hello")
public class HelloResource {
  @GET
  public String hello() {
    return "Hello";
  }
}
```

## Spring config code example
``` xml
<!-- spring-test.xml -->
<bean name="webTarget" class="com.rongjih.rest.WebTargetFactoryBean">
  <!--<property name="componentPackages">
    <list>
      <value>com.rongjih</value>
    </list>
  </property> or -->
  <property name="componentClasses">
    <list>
      <value>com.rongjih.rest.HelloResource</value>
    </list>
  </property>
</bean>
```

## The core technology in WebTargetFactoryBean
``` java
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import javax.ws.rs.client.WebTarget;
public class WebTargetFactoryBean implements FactoryBean<WebTarget>, InitializingBean, DisposableBean, ApplicationContextAware {
  private ApplicationContext context;
  private JerseyTest jerseyTest;

  @Override
  public void afterPropertiesSet() throws Exception {
    jerseyTest = new JerseyTest() {
      @Override
      protected Application configure() {
        ResourceConfig rc = new ResourceConfig();

        // inject spring context, avoid JerseyTest create a new one.
        // see org.glassfish.jersey.server.spring.SpringComponentProvider.createSpringContext()
        rc.property("contextConfig", WebTargetFactoryBean.this.context);

        // other init
        WebTargetFactoryBean.this.configure(rc);
        return rc;
      }
    };

    // setUp jerseyTest instance
    jerseyTest.setUp();

    // create WebTarget instance
    target = jerseyTest.target();
  }

  @Override
  public void destroy() throws Exception {
    // tearDown jerseyTest instance
    jerseyTest.tearDown();
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.context = context;
  }

  @Override
  public WebTarget getObject() throws Exception {
    return target;
  }
}
```