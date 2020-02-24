package dev.galasa.framework;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import dev.galasa.framework.spi.FrameworkException;

public class BundleManagement {
    
    private static final Log logger = LogFactory.getLog(BundleManagement.class);

    /**
     * Load a bundle from the OSGi Bundle Repository
     * 
     * @param bundleSymbolicName
     * @throws LauncherException
     */
    public static void loadBundle(RepositoryAdmin repositoryAdmin, BundleContext bundleContext, String bundleSymbolicName) throws FrameworkException {

        logger.trace("Installing bundle " + bundleSymbolicName);
        Resolver resolver = repositoryAdmin.resolver();
        String filterString = "(symbolicname=" + bundleSymbolicName + ")";
        Resource[] resources = null;
        try {
            resources = repositoryAdmin.discoverResources(filterString);
        } catch (InvalidSyntaxException e) {
            throw new FrameworkException("Unable to discover repoistory resources", e);
        }
        try {
            if (resources.length == 0) {
                throw new FrameworkException("Unable to locate bundle \"" + bundleSymbolicName + "\" in OBR repository");
            }
            // *** Only load the first one
            addResource(bundleContext, bundleSymbolicName, resolver, resources[0]);
        } catch (FrameworkException e) {
            throw new FrameworkException("Unable to install bundle \"" + bundleSymbolicName + "\" from OBR repository",
                    e);
        }
    }

    /**
     * Add the Resource to the Resolver and resolve
     * 
     * @param bundleSymbolicName
     * @param resolver
     * @param resource
     * @throws LauncherException
     */
    private static void addResource(BundleContext bundleContext, String bundleSymbolicName, Resolver resolver, Resource resource) throws FrameworkException {
        logger.trace("Resouce: " + resource);
        resolver.add(resource);

        boolean resourceHasReferenceUrl = false;
        if (resource.getURI().startsWith("reference:")) {
            resourceHasReferenceUrl = true;
        }

        if (resolver.resolve()) {
            Resource[] requiredResources = resolver.getRequiredResources();
            for (Resource requiredResource : requiredResources) {
                if (requiredResource.getURI().startsWith("reference:")) {
                    resourceHasReferenceUrl = true;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("  RequiredResource: " + requiredResource.getSymbolicName());

                }
            }
            Resource[] optionalResources = resolver.getOptionalResources();
            for (Resource optionalResource : optionalResources) {
                if (optionalResource.getURI().startsWith("reference:")) {
                    resourceHasReferenceUrl = true;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("  OptionalResource: " + optionalResource.getSymbolicName());
                }
            }


            if (!resourceHasReferenceUrl) {
                resolver.deploy(Resolver.START);
            } else {
                // *** The Resolver can't cope with reference: URIs which is valid for Felix.
                // *** So we have to manually install and start the bundles if ANY bundle
                // *** is a reference
                ArrayList<Bundle> bundlesToStart = new ArrayList<>();
                try {
                    Resource[] startRequiredResources = resolver.getRequiredResources();
                    for (Resource requiredResource : startRequiredResources) {
                        bundlesToStart.add(bundleContext.installBundle(requiredResource.getURI().toString()));
                    }
                    Resource[] startOptionalResources = resolver.getOptionalResources();
                    for (Resource optionalResource : startOptionalResources) {
                        bundlesToStart.add(bundleContext.installBundle(optionalResource.getURI().toString()));
                    }

                    bundlesToStart.add(bundleContext.installBundle(resource.getURI().toString()));
                    for (Bundle bundle : bundlesToStart) {
                        bundle.start();
                    }
                } catch (Exception e) {
                    throw new FrameworkException("Unable to install bundles outside of resolver", e);
                }
            }

            if (!isBundleActive(bundleContext, bundleSymbolicName)) {
                throw new FrameworkException("Bundle failed to install and activate");
            }

            printBundles(bundleContext);
        } else {
            logger.error("Unable to resolve " + resource.toString());
            Reason[] unsatisfiedRequirements = resolver.getUnsatisfiedRequirements();
            for (Reason reason : unsatisfiedRequirements) {
                logger.error("Unsatisfied requirement: " + reason.getRequirement());
            }
            throw new FrameworkException("Unable to resolve bundle " + bundleSymbolicName);
        }

    }

    /**
     * Is the supplied active in the OSGi framework
     * 
     * @param bundleSymbolicName
     * @return true or false
     */
    public static boolean isBundleActive(BundleContext bundleContext, String bundleSymbolicName) {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(bundleSymbolicName) && bundle.getState() == Bundle.ACTIVE) {
                return true;
            }
        }

        return false;
    }

    /**
     * Print the currently installed bundles and their state
     */
    private static void printBundles(BundleContext bundleContext) {
        if (!logger.isTraceEnabled()) {
            return;
        }
        // Get the bundles
        Bundle[] bundles = bundleContext.getBundles();
        // Format and print the bundle Id, State, Symbolic name and Version.
        StringBuilder messageBuffer = new StringBuilder(2048);
        messageBuffer.append("Bundle status:");

        for (Bundle bundle : bundles) {
            String bundleId = String.valueOf(bundle.getBundleId());
            messageBuffer.append("\n").append(String.format("%5s", bundleId)).append("|")
            .append(String.format("%-11s", getBundleStateLabel(bundle))).append("|     |")
            .append(bundle.getSymbolicName()).append(" (").append(bundle.getVersion()).append(")");
        }

        logger.trace(messageBuffer.toString());
    }

    /**
     * Convert bundle state to string
     * 
     * @param bundle
     * @return The bundle state
     */
    private static String getBundleStateLabel(Bundle bundle) {
        switch (bundle.getState()) {
            case Bundle.UNINSTALLED:
                return "Uninstalled";
            case Bundle.INSTALLED:
                return "Installed";
            case Bundle.RESOLVED:
                return "Resolved";
            case Bundle.STARTING:
                return "Starting";
            case Bundle.STOPPING:
                return "Stopping";
            case Bundle.ACTIVE:
                return "Active";
            default:
                return "<Unknown (" + bundle.getState() + ")>";
        }
    }

}
