package com.tngtech.archunit.core.domain;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.testutil.Assertions.ConversionResultAssertion;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Test;

import static com.tngtech.archunit.core.domain.Dependency.Functions.GET_ORIGIN_CLASS;
import static com.tngtech.archunit.core.domain.Dependency.Functions.GET_TARGET_CLASS;
import static com.tngtech.archunit.core.domain.Dependency.Predicates.dependency;
import static com.tngtech.archunit.core.domain.Dependency.Predicates.dependencyOrigin;
import static com.tngtech.archunit.core.domain.Dependency.Predicates.dependencyTarget;
import static com.tngtech.archunit.core.domain.TestUtils.importClassWithContext;
import static com.tngtech.archunit.core.domain.TestUtils.simulateCall;
import static com.tngtech.archunit.testutil.Assertions.assertThat;
import static com.tngtech.archunit.testutil.Assertions.assertThatConversionOf;

public class DependencyTest {
    @Test
    public void Dependency_from_access() {
        JavaMethodCall call = simulateCall().from(getClass(), "toString").to(Object.class, "toString");

        Dependency dependency = Dependency.from(call);
        assertThat(dependency.getTargetClass()).as("target class").isEqualTo(call.getTargetOwner());
        assertThat(dependency.getDescription())
                .as("description").isEqualTo(call.getDescription());
    }

    @Test
    public void Dependency_from_origin_and_target() {
        JavaClass origin = importClassWithContext(getClass());
        JavaClass target = importClassWithContext(DependencyClass.class);
        Dependency dependency = createDependency(origin, target);
        assertThat(dependency.getDescription()).as("description")
                .contains("Class <" + origin.getName() + "> extends class <" + target.getName() + ">");

        target = importClassWithContext(DependencyInterface.class);
        dependency = createDependency(origin, target);
        assertThat(dependency.getDescription()).as("description")
                .contains("Class <" + origin.getName() + "> implements interface <" + target.getName() + ">");

        origin = importClassWithContext(DependencySubInterface.class);
        dependency = createDependency(origin, target);
        assertThat(dependency.getDescription()).as("description")
                .contains("Interface <" + origin.getName() + "> extends interface <" + target.getName() + ">");
    }

    private Dependency createDependency(JavaClass origin, JavaClass target) {
        Dependency dependency = Dependency.fromInheritance(origin, target);
        assertThat(dependency.getOriginClass()).as("origin class").isEqualTo(origin);
        assertThat(dependency.getTargetClass()).as("target class").isEqualTo(target);
        return dependency;
    }

    @Test
    public void origin_predicates_match() {
        assertThatDependency(Origin.class, Target.class)
                .matchesOrigin(Origin.class)
                .doesntMatchOrigin(Target.class);
    }

    @Test
    public void origin_predicates_descriptions() {
        assertThat(dependencyOrigin(Origin.class).getDescription())
                .isEqualTo(dependencyOrigin(Origin.class.getName()).getDescription())
                .isEqualTo("origin " + Origin.class.getName());

        assertThat(dependencyOrigin(predicateWithDescription("foo")).getDescription())
                .isEqualTo("origin foo");
    }

    @Test
    public void target_predicates_match() {
        assertThatDependency(Origin.class, Target.class)
                .matchesTarget(Target.class)
                .doesntMatchTarget(Origin.class);
    }

    @Test
    public void target_predicates_descriptions() {
        assertThat(dependencyTarget(Target.class).getDescription())
                .isEqualTo(dependencyTarget(Target.class.getName()).getDescription())
                .isEqualTo("target " + Target.class.getName());

        assertThat(dependencyTarget(predicateWithDescription("foo")).getDescription())
                .isEqualTo("target foo");
    }

    @Test
    public void dependency_predicates_match() {
        assertThatDependency(Origin.class, Target.class)
                .matches(Origin.class, Target.class)
                .doesntMatch(Origin.class, Origin.class)
                .doesntMatch(Target.class, Target.class);
    }

    @Test
    public void dependency_predicates_descriptions() {
        assertThat(dependency(Origin.class, Target.class).getDescription())
                .isEqualTo(dependency(Origin.class.getName(), Target.class.getName()).getDescription())
                .isEqualTo("dependency " + Origin.class.getName() + " -> " + Target.class.getName());

        assertThat(dependency(predicateWithDescription("first"), predicateWithDescription("second")).getDescription())
                .isEqualTo("dependency first -> second");
    }

    @Test
    public void functions() {
        assertThat(GET_ORIGIN_CLASS.apply(createDependency(Origin.class, Target.class))).matches(Origin.class);
        assertThat(GET_TARGET_CLASS.apply(createDependency(Origin.class, Target.class))).matches(Target.class);
    }

    @Test
    public void convert_dependency_from_access() {
        JavaMethodCall call = simulateCall().from(getClass(), "toString").to(Object.class, "toString");

        Dependency dependency = Dependency.from(call);

        assertThatConversionOf(dependency)
                .isNotPossibleTo(JavaClass.class)
                .isNotPossibleTo(JavaFieldAccess.class)
                .isPossibleToSingleElement(Object.class, equalTo(call))
                .isPossibleToSingleElement(JavaAccess.class, equalTo(call))
                .isPossibleToSingleElement(JavaMethodCall.class, equalTo(call));
    }

    @Test
    public void dependency_not_from_access_cannot_be_converted() {
        Dependency dependency = createDependency(Origin.class, Target.class);

        assertThatConversionOf(dependency).isNotPossibleTo(JavaClass.class);
    }

    private DescribedPredicate<JavaClass> predicateWithDescription(String description) {
        return DescribedPredicate.<JavaClass>alwaysTrue().as(description);
    }

    private static DependencyAssertion assertThatDependency(Class<?> originClass, Class<?> targetClass) {
        return new DependencyAssertion(createDependency(originClass, targetClass));
    }

    private static Dependency createDependency(Class<?> originClass, Class<?> targetClass) {
        return Dependency.fromInheritance(
                importClassWithContext(originClass), importClassWithContext(targetClass));
    }

    private ConversionResultAssertion<Object> equalTo(final JavaMethodCall call) {
        return new ConversionResultAssertion<Object>() {
            @Override
            public void assertResult(Object access) {
                assertThat(access).isEqualTo(call);
            }
        };
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }

    private static class DependencyClass {
    }

    private interface DependencySubInterface extends DependencyInterface {
    }

    private interface DependencyInterface {
    }

    private static class Origin {
    }

    private static class Target {
    }

    private static class DependencyAssertion {
        private final Dependency input;

        DependencyAssertion(Dependency input) {
            this.input = input;
        }

        DependencyAssertion matches(Class<?> originClass, Class<?> targetClass) {
            for (AbstractBooleanAssert<?> dependencyAssert : dependencyMatches(originClass, targetClass)) {
                dependencyAssert.isTrue();
            }
            return this;
        }

        DependencyAssertion doesntMatch(Class<?> originClass, Class<?> targetClass) {
            for (AbstractBooleanAssert<?> dependencyAssert : dependencyMatches(originClass, targetClass)) {
                dependencyAssert.isFalse();
            }
            return this;
        }

        private List<AbstractBooleanAssert<?>> dependencyMatches(Class<?> originClass, Class<?> targetClass) {
            return ImmutableList.of(
                    assertThat(dependency(originClass, targetClass).apply(input))
                            .as("Dependency matches '%s.class' -> '%s.class'", originClass.getSimpleName(), targetClass.getSimpleName()),
                    assertThat(dependency(originClass.getName(), targetClass.getName()).apply(input))
                            .as("Dependency matches '%s.class' -> '%s.class'", originClass.getSimpleName(), targetClass.getSimpleName()),
                    assertThat(dependency(
                            HasName.Predicates.name(originClass.getName()),
                            HasName.Predicates.name(targetClass.getName())).apply(input))
                            .as("Dependency matches '%s.class' -> '%s.class'", originClass.getSimpleName(), targetClass.getSimpleName()));
        }

        DependencyAssertion matchesOrigin(Class<?> originClass) {
            for (AbstractBooleanAssert<?> dependencyOriginAssert : dependencyMatchesOrigin(originClass)) {
                dependencyOriginAssert.isTrue();
            }
            return this;
        }

        void doesntMatchOrigin(Class<?> originClass) {
            for (AbstractBooleanAssert<?> dependencyOriginAssert : dependencyMatchesOrigin(originClass)) {
                dependencyOriginAssert.isFalse();
            }
        }

        private List<AbstractBooleanAssert<?>> dependencyMatchesOrigin(Class<?> originClass) {
            return ImmutableList.of(
                    assertThat(dependencyOrigin(originClass).apply(input))
                            .as("Dependency origin matches '%s.class'", originClass.getSimpleName()),
                    assertThat(dependencyOrigin(originClass.getName()).apply(input))
                            .as("Dependency origin matches '%s.class'", originClass.getSimpleName()),
                    assertThat(dependencyOrigin(HasName.Predicates.name(originClass.getName())).apply(input))
                            .as("Dependency origin matches '%s.class'", originClass.getSimpleName()));
        }

        DependencyAssertion matchesTarget(Class<?> targetClass) {
            for (AbstractBooleanAssert<?> dependencyTargetAssert : dependencyMatchesTarget(targetClass)) {
                dependencyTargetAssert.isTrue();
            }
            return this;
        }

        void doesntMatchTarget(Class<?> targetClass) {
            for (AbstractBooleanAssert<?> dependencyTargetAssert : dependencyMatchesTarget(targetClass)) {
                dependencyTargetAssert.isFalse();
            }
        }

        private List<AbstractBooleanAssert<?>> dependencyMatchesTarget(Class<?> targetClass) {
            return ImmutableList.of(
                    assertThat(dependencyTarget(targetClass).apply(input))
                            .as("Dependency target matches '%s.class'", targetClass.getSimpleName()),
                    assertThat(dependencyTarget(targetClass.getName()).apply(input))
                            .as("Dependency target matches '%s.class'", targetClass.getSimpleName()),
                    assertThat(dependencyTarget(HasName.Predicates.name(targetClass.getName())).apply(input))
                            .as("Dependency target matches '%s.class'", targetClass.getSimpleName()));
        }
    }
}
