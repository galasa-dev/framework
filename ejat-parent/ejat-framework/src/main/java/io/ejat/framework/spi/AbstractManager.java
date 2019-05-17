package io.ejat.framework.spi;

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

/**
 * An abstract manager which attempts to provide all the boilerplate code
 * necessary to write a Manager in eJAT.
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
     *
     * @param managerAnnotation The Annotation for those annotated fields we are
     *                          interested in
     */
    protected void generateAnnotatedFields(Class<? extends Annotation> managerAnnotation) {
        final List<AnnotatedField> foundAnnotatedFields = findAnnotatedFields(managerAnnotation);
        if (foundAnnotatedFields.isEmpty()) { // *** No point doing anything
            return;
        }

        for (final AnnotatedField entry : foundAnnotatedFields) {
            final Field field = entry.getField();
            final List<Annotation> annotations = entry.getAnnotations();
            
            //*** Check to see if it is already generated
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
                            // *** Chdeck the method returns the right type and can be passed the correct
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
                this.logger.error("Problem generating Test Class fields", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#extraBundles(io.ejat.framework.spi.IFramework)
     */
    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws ManagerException {
        return null; //NOSONAR
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#initialise(io.ejat.framework.spi.IFramework,
     * java.util.List, java.util.List, java.lang.Class)
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
     * @see io.ejat.framework.spi.IManager#youAreRequired(java.util.List,
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
     * io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework
     * .spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#anyReasonTestClassShouldBeIgnored()
     */
    @Override
    public String anyReasonTestClassShouldBeIgnored() throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#provisionBuild()
     */
    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#provisionStart()
     */
    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#startOfTestClass()
     */
    @Override
    public void startOfTestClass() throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#fillAnnotatedFields()
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
     * io.ejat.framework.spi.IManager#anyReasonTestMethodShouldBeIgnored(java.lang.
     * reflect.Method)
     */
    @Override
    public String anyReasonTestMethodShouldBeIgnored(@NotNull Method method) throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod() throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#endOfTestMethod(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public String endOfTestMethod(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#testMethodResult(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public void testMethodResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#endOfTestClass(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public String endOfTestClass(@NotNull String currentResult, Throwable currentException) throws ManagerException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#testClassResult(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#provisionStop()
     */
    @Override
    public void provisionStop() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#provisionDiscard()
     */
    @Override
    public void provisionDiscard() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#performFailureAnalysis()
     */
    @Override
    public void performFailureAnalysis() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#endOfTestRun()
     */
    @Override
    public void endOfTestRun() {
    }

}
