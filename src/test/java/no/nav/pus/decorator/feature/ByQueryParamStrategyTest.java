package no.nav.pus.decorator.feature;


import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ByQueryParamStrategyTest {

    private HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    private Provider<HttpServletRequest> httpServletRequestProvider = () -> httpServletRequest;
    private ByQueryParamStrategy byQueryParamStrategy = new ByQueryParamStrategy(httpServletRequestProvider);
    private Map<String, String> strategyProperties = new HashMap<>();

    @Before
    public void setUp() {
        strategyProperties.clear();
    }

    @Test
    public void isEnabled__default_false() {
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isFalse();
    }

    @Test
    public void isEnabled__false_with_no_strategy() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"testValue"});
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isFalse();
    }

    @Test
    public void isEnabled__false_with_no_matching_strategy() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"testValue"});
        strategyProperties.put("queryParam", "other");
        strategyProperties.put("queryValues", "otherValue,otherValue2");
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isFalse();
    }

    @Test
    public void isEnabled__false_with_no_matching_value_in_matching_strategy() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"testValue"});
        strategyProperties.put("queryParam", "test");
        strategyProperties.put("queryValues", "otherValue,otherValue2");
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isFalse();
    }

    @Test
    public void isEnabled__false_with_matching_value_from_other_strategy() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"otherValue"});
        strategyProperties.put("queryParam", "other");
        strategyProperties.put("queryValues", "otherValue,otherValue2");
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isFalse();
    }

    @Test
    public void isEnabled__true_with_matching_strategy() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"testValue"});
        strategyProperties.put("queryParam", "test");
        strategyProperties.put("queryValues", "testValue");
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isTrue();
    }

    @Test
    public void isEnabled__true_with_at_least_one_matching_strategy() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"testValue", "testValue2"});
        strategyProperties.put("queryParam", "test");
        strategyProperties.put("queryValues", "testValue");
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isTrue();
    }

    @Test
    public void isEnabled__true_with_one_of_several_matching_strategies() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"testValue"});
        strategyProperties.put("queryParam", "test");
        strategyProperties.put("queryValues", "testValue,testValue2");
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isTrue();
    }

    @Test
    public void isEnabled__true_with_several_matching_strategies() {
        when(httpServletRequest.getParameterValues("test")).thenReturn(new String[]{"testValue", "testValue2"});
        strategyProperties.put("queryParam", "test");
        strategyProperties.put("queryValues", "testValue,testValue2");
        assertThat(byQueryParamStrategy.isEnabled(strategyProperties)).isTrue();
    }

}
