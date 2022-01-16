package at.rtr.rmbt.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.google.common.base.CaseFormat;

import java.util.HashMap;

public class JacksonAwareSnakeCaseJavaBeanResolver extends JavaBeanValueResolver {
    public JacksonAwareSnakeCaseJavaBeanResolver() {
        super();
    }

    /**
     * Get the property name for a given method,
     * either from the @JsonProperty annotation, or from translating to snake_case
     *
     * @param member
     * @return
     */
    @Override
    protected String memberName(final java.lang.reflect.Method member) {
        if (member.getDeclaringClass().isInstance(new HashMap<>())) {
            return super.memberName(member);
        }

        JsonProperty annotation = member.getAnnotation(JsonProperty.class);
        if (annotation != null) {
            return annotation.value();
        }

        String withoutGetterIs = super.memberName(member);

        String otherName = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(withoutGetterIs);
        return otherName;
    }

    /**
     * Is invoked to check if the methode actually is a getter or setter
     *
     * @param method Method to check
     * @param name   Translated name of the method (already being snake_case)
     * @return
     */
    @Override
    public boolean matches(final java.lang.reflect.Method method, final String name) {
        if (method.getDeclaringClass().isInstance(new HashMap<>())) {
            return super.matches(method, name);
        }

        //if it matches the annotation - it matches
        JsonProperty annotation = method.getAnnotation(JsonProperty.class);
        if (annotation != null && name.equals(annotation.value())) {
            return true;
        }

        //name is here the "translated" name - translate back to get if it matches
        String otherName = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(name);
        return super.matches(method, otherName) || super.matches(method, name);
    }
}