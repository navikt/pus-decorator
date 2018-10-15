package no.nav.pus.decorator.feature;

import no.finn.unleash.strategy.Strategy;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class ByQueryParamStrategy implements Strategy {

    private final Provider<HttpServletRequest> httpServletRequestProvider;

    public ByQueryParamStrategy(Provider<HttpServletRequest> httpServletRequestProvider) {
        this.httpServletRequestProvider = httpServletRequestProvider;
    }

    @Override
    public String getName() {
        return "byQueryParam";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        String queryParam = parameters.get("queryParam");
        String[] parameterValues = httpServletRequestProvider.get().getParameterValues(queryParam);
        String[] queryValues = parameters.getOrDefault("queryValues", "").split(",");
        return parameterValues != null && queryValues != null && !Collections.disjoint(Arrays.asList(parameterValues), Arrays.asList(queryValues));
    }

}
