package no.nav.pus.decorator;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static no.nav.pus.decorator.ConfigurationService.Feature.DECORATOR;
import static no.nav.pus.decorator.ConfigurationService.Feature.UNLEASH;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class ConfigurationService {

    public static boolean isEnabled(Feature feature) {
        return !isDisabled(feature);
    }

    public static boolean isDisabled(Feature feature) {
        return getOptionalProperty(feature.propertyName).map(Boolean::parseBoolean).orElse(false);
    }

    public enum Feature {
        PROXY("DISABLE_PROXY"),
        DECORATOR("DISABLE_DECORATOR"),
        UNLEASH("DISABLE_UNLEASH"),
        FRONTEND_LOGGER("DISABLE_FRONTEND_LOGGER"),
        ;

        final String propertyName;

        Feature(String propertyName) {
            this.propertyName = propertyName;
        }

    }

    private static class AbstractCondition implements Condition {
        private final Feature feature;

        private AbstractCondition(Feature feature) {
            this.feature = feature;
        }

        @Override
        public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            return isEnabled(feature);
        }
    }

    public static class UnleashEnabled extends AbstractCondition{
        protected UnleashEnabled() {
            super(UNLEASH);
        }
    }

    public static class DecoratorEnabled extends AbstractCondition {
        public DecoratorEnabled() {
            super(DECORATOR);
        }
    }
}
