/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.framework.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;

/**
 * An abstract manager which attempts to provide all the boilerplate code
 * necessary to write a Manager in Galasa.
 *
 * @author Michael Baylis
 *
 */
public abstract class AbstractManager implements IManager {

    private IFramework                   framework;
    private Class<?>                     testClass;

    private final HashMap<Field, Object> annotatedFields = new HashMap<>();

    private final Log                    logger          = LogFactory.getLog(getClass());

    /**
     * Register an Manager annotated field, for automatic filling during
     * fillAnnotatedFields()
     *
     * @param field The field to fill
     * @param value The
     */
    protected void registerAnnotatedField(Field field, Object value) {
        this.annotatedFields.put(field, value);
    }

    /**
     * Retrieve the generated object for an annotated field
     * 
     * @param annotated field to retrieve
     * @return the generated object or null if it has not been generated yet
     */
    protected Object getAnnotatedField(Field field) {
        return this.annotatedFields.get(field);
    }

    /**
     * <p>
     * Helper method to obtain a list of annotated fields this manager is interested
     * in.
     * </p>
     *
     * <p>
     * This method will return a list of fields that has an annotation that is in
     * turn annotated with the managerAnnotation
     * </p>
     *
     * @param managerAnnotation - The annotation that other annotations are
     *                          annotated with.
     * @return A list of fields the manager should be interested in
     */
    protected List<AnnotatedField> findAnnotatedFields(Class<? extends Annotation> managerAnnotation) {
        final ArrayList<AnnotatedField> foundFields = new ArrayList<>();

        // *** Work our way throw the class inheritance
        for (Class<?> lookClass = this.testClass; lookClass != null; lookClass = lookClass.getSuperclass()) {
            // *** Go through the PUBLIC fields
            for (final Field field : lookClass.getFields()) {
                final ArrayList<Annotation> fieldAnnotations = new ArrayList<>(); // *** For saving valid annotations
                for (final Annotation annotation : field.getAnnotations()) {
                    // *** If the annotation is annotated with the manager annotation, ie the
                    // manager is interested in it
                    if (annotation.annotationType().isAnnotationPresent(managerAnnotation)) {
                        final ValidAnnotatedFields validClasses = annotation.annotationType()
                                .getAnnotation(ValidAnnotatedFields.class);
                        // *** If there is a Valid classes annotation, make sure the field is of a valid
                        // type
                        if (validClasses != null) {
                            boolean found = false;
                            for (final Class<?> k : validClasses.value()) {
                                if (k == field.getType()) {
                                    fieldAnnotations.add(annotation);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                this.logger.warn("Field " + field.getName() + " has an invalid type, ignoring");
                            }
                        } else {
                            fieldAnnotations.add(annotation);
                        }
                    }
                }

                if (!fieldAnnotations.isEmpty()) {
                    foundFields.add(new AnnotatedField(field, fieldAnnotations));
                }
            }
        }

        return foundFields;
    }

    /**
     * Will call {@link GenerateAnnotatedField} methods in the parent class to
     * generate instances for each of the Test Class fields
     * <p>
     * 
     * The annotated methods in the parent class must:
     * <p>
     * <ul>
     * <li>be public
     * <li>return the interface of the field annotation
     * <li>have 2 parameters of {@link java.lang.reflect.Field} and
     * {@link java.util.List}
     *
     * @param managerAnnotation The Annotation for those annotated fields we are
     *                          interested in
     * @throws ManagerException
     */
    protected void generateAnnotatedFields(Class<? extends Annotation> managerAnnotation) throws ManagerException {
        final List<AnnotatedField> foundAnnotatedFields = findAnnotatedFields(managerAnnotation);
        if (foundAnnotatedFields.isEmpty()) { // *** No point doing anything
            return;
        }

        for (final AnnotatedField entry : foundAnnotatedFields) {
            final Field field = entry.getField();
            final List<Annotation> annotations = entry.getAnnotations();

            // *** Check to see if it is already generated
            if (this.annotatedFields.containsKey(field)) {
                continue;
            }

            try {
                // *** work our way through the fields looking for @GenerateAnnotatedField
                for (final Method method : getClass().getMethods()) {
                    final GenerateAnnotatedField genField = method.getAnnotation(GenerateAnnotatedField.class);

                    if (genField != null) {
                        boolean foundAnnotation = false;
                        // *** See if the annotation is on the list of annotations for that field
                        for (final Annotation annotation : annotations) {
                            if (annotation.annotationType() == genField.annotation()) {
                                foundAnnotation = true;
                                break;
                            }
                        }
                        if (foundAnnotation) {
                            // *** Check the method returns the right type and can be passed the correct
                            // types
                            final Class<?>[] parameterTypes = method.getParameterTypes();
                            if ((parameterTypes != null) && (parameterTypes.length == 2)
                                    && (parameterTypes[0] == Field.class) && (parameterTypes[1] == List.class)
                                    && (method.getReturnType() == field.getType())) {
                                // *** Call it and save the value for fillAnnotatedFields
                                final Object response = method.invoke(this, field, annotations);
                                if (response != null) {
                                    this.annotatedFields.put(field, response);
                                }
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                throw new ManagerException("Problem generating Test Class fields", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#extraBundles(dev.galasa.framework.spi.
     * IFramework)
     */
    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws ManagerException {
        return null; // NOSONAR
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#initialise(dev.galasa.framework.spi.
     * IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<?> testClass) throws ManagerException {
        this.framework = framework;
        this.testClass = testClass;
    }

    /**
     * @return The Framework
     */
    public IFramework getFramework() {
        return this.framework;
    }

    /**
     * @return The Test Class
     */
    public Class<?> getTestClass() {
        return this.testClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#youAreRequired(java.util.List,
     * java.util.List)
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers)
            throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.
     * framework .spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#anyReasonTestClassShouldBeIgnored()
     */
    @Override
    public String anyReasonTestClassShouldBeIgnored() throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#provisionBuild()
     */
    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#provisionStart()
     */
    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestClass()
     */
    @Override
    public void startOfTestClass() throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#fillAnnotatedFields()
     */
    @Override
    public void fillAnnotatedFields(Object instanstiatedTestClass) throws ManagerException {
        for (final Entry<Field, Object> annotatedField : this.annotatedFields.entrySet()) {
            final Field field = annotatedField.getKey();
            final Object value = annotatedField.getValue();

            try {
                field.set(instanstiatedTestClass, value);
            } catch (final Throwable e) {
                throw new ManagerException("Unable to fill Test Class field " + field.getName(), e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IManager#anyReasonTestMethodShouldBeIgnored(java.
     * lang. reflect.Method)
     */
    @Override
    public String anyReasonTestMethodShouldBeIgnored(@NotNull Method method) throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull Method testMethod) throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestMethod(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public String endOfTestMethod(@NotNull Method testMethod, @NotNull String currentResult, Throwable currentException)
            throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#testMethodResult(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public void testMethodResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestClass(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#testClassResult(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#provisionStop()
     */
    @Override
    public void provisionStop() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#provisionDiscard()
     */
    @Override
    public void provisionDiscard() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#performFailureAnalysis()
     */
    @Override
    public void performFailureAnalysis() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestRun()
     */
    @Override
    public void endOfTestRun() {
    }

    /**
     * Helper method to find managers that implement an interface and tell them they
     * are required
     * 
     * @param allManagers        All available managers
     * @param activeManagers     The currently active managers
     * @param dependentInterface The interface the manager needs to implement
     * @return
     * @throws ManagerException If the required manager can't be added to the active
     *                          list
     */
    protected <T extends Object> T addDependentManager(@NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull Class<T> dependentInterface) throws ManagerException {
        for (IManager manager : allManagers) {
            if (dependentInterface.isAssignableFrom(manager.getClass())) {
                manager.youAreRequired(allManagers, activeManagers);
                return dependentInterface.cast(manager);
            }
        }
        return null;
    }

    /**
     * null a String is if it is empty
     * 
     * TODO Needs to be moved to a more appropriate place as non managers use this,
     * a stringutils maybe
     * 
     * @param value
     * @return a trimmed String or a null if emtpy or null
     */
    public static String nulled(String value) {
        if (value == null) {
            return null;
        }

        value = value.trim();
        if (value.isEmpty()) {
            return value;
        }
        return value;
    }

    public static List<String> split(String value) {
        ArrayList<String> values = new ArrayList<>();

        if (value == null) {
            return values;
        }

        String[] parts = value.split(",");

        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                values.add(part);
            }
        }

        return values;
    }

    public static String defaultString(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return defaultValue;
        }

        return value;
    }

    public static List<String> trim(String[] array) {
        ArrayList<String> trimmed = new ArrayList<>();
        for (String s : array) {
            if (s != null) {
                s = s.trim();
                if (!s.isEmpty()) {
                    trimmed.add(s);
                }
            }
        }

        return trimmed;
    }
    
    @Override
    public void shutdown() {
    }
    
    @Override
    public boolean doYouSupportSharedEnvironments() {
        return false; //*** Managers by default will not support shared environments
    }

}
