package com.tngtech.archunit.core;

import java.io.Serializable;

import org.junit.Test;

import static com.tngtech.archunit.core.JavaConstructor.CONSTRUCTOR_NAME;
import static com.tngtech.archunit.core.JavaStaticInitializer.STATIC_INITIALIZER_NAME;
import static com.tngtech.archunit.testutil.Conditions.containing;
import static com.tngtech.archunit.testutil.Conditions.methodWithSignature;
import static org.assertj.core.api.Assertions.assertThat;

public class JavaClassTest {

    @Test
    public void finds_fields_and_methods() {
        JavaClass javaClass = new JavaClass.Builder().withType(ClassWithTwoFieldsAndTwoMethods.class).build();

        assertThat(javaClass.reflect()).isEqualTo(ClassWithTwoFieldsAndTwoMethods.class);
        assertThat(javaClass.getFields()).hasSize(2);
        assertThat(javaClass.getProperMethods()).hasSize(2);

        for (JavaField field : javaClass.getFields()) {
            assertThat(field.getOwner()).isSameAs(javaClass);
        }
        for (JavaMethodLike<?, ?> method : javaClass.getMethods()) {
            assertThat(method.getOwner()).isSameAs(javaClass);
        }
    }

    @Test
    public void finds_constructors() {
        JavaClass javaClass = new JavaClass.Builder().withType(ClassWithSeveralConstructors.class).build();

        assertThat(javaClass.getConstructors()).hasSize(3);
        assertThat(javaClass.getConstructors()).is(containing(methodWithSignature(CONSTRUCTOR_NAME)));
        assertThat(javaClass.getConstructors()).is(containing(methodWithSignature(CONSTRUCTOR_NAME, String.class)));
        assertThat(javaClass.getConstructors()).is(containing(methodWithSignature(CONSTRUCTOR_NAME, int.class, Object[].class)));
    }

    @Test
    public void finds_static_Initializer() {
        JavaClass javaClass = new JavaClass.Builder().withType(Object.class).build();

        assertThat(javaClass.getStaticInitializer()).isNotNull();
        assertThat(javaClass.getStaticInitializer().getName()).isEqualTo(STATIC_INITIALIZER_NAME);
    }

    @Test
    public void equals_works() {
        JavaClass javaClass = new JavaClass.Builder().withType(ClassWithTwoFieldsAndTwoMethods.class).build();
        JavaClass equalClass = new JavaClass.Builder().withType(ClassWithTwoFieldsAndTwoMethods.class).build();
        JavaClass differentClass = new JavaClass.Builder().withType(SuperClassWithFieldAndMethod.class).build();

        assertThat(javaClass).isEqualTo(javaClass);
        assertThat(javaClass).isEqualTo(equalClass);
        assertThat(javaClass).isNotEqualTo(differentClass);
    }

    @Test
    public void anonymous_class_has_package_of_declaring_class() {
        JavaClass anonymous = new JavaClass.Builder()
                .withType(new Serializable() {
                }.getClass())
                .build();

        assertThat(anonymous.getPackage()).isEqualTo(getClass().getPackage().getName());
    }

    @Test
    public void inner_class_has_package_of_declaring_class() {
        JavaClass anonymous = new JavaClass.Builder()
                .withType(ClassWithInnerClass.Inner.class)
                .build();

        assertThat(anonymous.getPackage()).isEqualTo(getClass().getPackage().getName());
    }

    @Test
    public void Array_class_has_default_package() {
        JavaClass arrayType = new JavaClass.Builder().withType(JavaClassTest[].class).build();

        assertThat(arrayType.getPackage()).isEmpty();
    }

    static class ClassWithTwoFieldsAndTwoMethods extends SuperClassWithFieldAndMethod {
        String stringField;
        private int intField;

        void voidMethod() {
        }

        protected String stringMethod() {
            return null;
        }
    }

    static abstract class SuperClassWithFieldAndMethod {
        private Object objectField;

        private Object objectMethod() {
            return null;
        }
    }

    static class ClassWithSeveralConstructors {
        private ClassWithSeveralConstructors() {
        }

        ClassWithSeveralConstructors(String string) {
        }

        public ClassWithSeveralConstructors(int number, Object[] objects) {
        }
    }

    static class ClassWithInnerClass {
        class Inner {
        }
    }
}