package be.ordina.msdashboard.security.strategies;

import be.ordina.msdashboard.security.strategy.SecurityProtocol;
import be.ordina.msdashboard.security.strategy.SecurityStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

public class SecurityStrategyFactoryTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private final StrategyFactory factory = new StrategyFactory(applicationContext);

    Map<String, Object> annotatedBeans = new HashMap<>();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        annotatedBeans.put("bean_oauth2", new OAuth2TestStrategy());
        annotatedBeans.put("bean_jwt", new JWTTestStrategy());
        annotatedBeans.put("bean_basic", new BasicTestStrategy());
    }

    @Test
    public void shouldGetOAuth2Strategy() {
        when(applicationContext.getBeansWithAnnotation(SecurityStrategy.class)).thenReturn(annotatedBeans);

        factory.init();

        TestStrategy strategy = factory.getStrategy(TestStrategy.class, SecurityProtocol.OAUTH2);
        assertEquals("SecurityStrategy returned was for the wrong profile", strategy.getClass(), OAuth2TestStrategy.class);
    }

    @Test
    public void shouldGetJWTStrategy() {
        when(applicationContext.getBeansWithAnnotation(SecurityStrategy.class)).thenReturn(annotatedBeans);

        factory.init();

        TestStrategy strategy = factory.getStrategy(TestStrategy.class, SecurityProtocol.JWT);
        assertEquals("SecurityStrategy returned was for the wrong profile", strategy.getClass(), JWTTestStrategy.class);
    }

    @Test
    public void shouldGetBasicStrategy() {
        when(applicationContext.getBeansWithAnnotation(SecurityStrategy.class)).thenReturn(annotatedBeans);

        factory.init();

        TestStrategy strategy = factory.getStrategy(TestStrategy.class, SecurityProtocol.BASIC);
        assertEquals("SecurityStrategy returned was for the wrong profile", strategy.getClass(), BasicTestStrategy.class);
    }


    private interface TestStrategy {
    }

    @SecurityStrategy(type = TestStrategy.class, protocol = SecurityProtocol.BASIC)
    private static class BasicTestStrategy implements TestStrategy {
    }

    @SecurityStrategy(type = TestStrategy.class, protocol = SecurityProtocol.JWT)
    private static class JWTTestStrategy implements TestStrategy {
    }

    @SecurityStrategy(type = TestStrategy.class, protocol = SecurityProtocol.OAUTH2)
    private static class OAuth2TestStrategy implements TestStrategy {
    }

}