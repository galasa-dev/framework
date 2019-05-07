package io.ejat.eclipse;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * UI handler for the Build Test OBRs extension point.
 * 
 * Finds all Cirillo projects (Maven project with a packaging type of bundle and a
 * dependency on ejat:ejat) <br>
 * and builds the OSGi Bundle Repository resource. OBR files are written to
 * [workspace]/.metadata/.plugin/[plugin-name].
 * 
 */
public class BuildTestOBRsHandler extends AbstractHandler {
	
	private static String productName;
	public static void setProductName(String productName) {
		BuildTestOBRsHandler.productName = productName;
	}

	private static String processTitle;	
	public static void setProcessTitle(String processTitle) {
		BuildTestOBRsHandler.processTitle = processTitle;
	}

	private static final String OUTPUT_DIR_PROPERTY = "outputDir";

	private static final String MAVEN_BUILD_TEST_OBR_GOAL = "ejat:ejat-maven-plugin:0.2.0-SNAPSHOT:obrresources";

	private static final String MAVEN_NATURE = "org.eclipse.m2e.core.maven2Nature";

	private static final String CIRILLO_GROUPID = "ejat";

	private static final String CIRILLO_ARTIFACT_ID = "ejat";

	private static final String BUILD_FAILURE = "BUILD FAILURE";

	private static final String BUILD_SUCCESS = "BUILD SUCCESS";

	private static final String PROJECT = "Project";

	private static final String MESSAGE_INFO = "[INFO] ";

	private static final String MESSAGE_DEBUG = "[DEBUG] ";

	private static final String MESSAGE_ERROR = "[ERROR] ";
	
	private Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

	private static boolean debug;
	public static void setDebug(boolean debug) {
		BuildTestOBRsHandler.debug = debug;
	}

	private static PrintStream consoleOut;
	public static void setConsoleOut(PrintStream consoleOut) {
		BuildTestOBRsHandler.consoleOut = consoleOut;
	}

	private static PrintStream consoleOutRed;
	public static void setConsoleOutRed(PrintStream consoleOutRed) {
		BuildTestOBRsHandler.consoleOutRed = consoleOutRed;
	}
	private static PrintStream consoleOutBlue;
	public static void setConsoleOutBlue(PrintStream consoleOutBlue) {
		BuildTestOBRsHandler.consoleOutBlue = consoleOutBlue;
	}

	private static int maxProjectNameLength;	
	public static void setMaxProjectNameLength(int length) {
		if (length == 0 || length > BuildTestOBRsHandler.maxProjectNameLength) {
			BuildTestOBRsHandler.maxProjectNameLength = length;
		}
	}

	private boolean buildSuccess;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		buildSuccess = true;
		
		enableDebug(event);
		
		try {
			setProductName(event.getCommand().getCategory().getName());
			setProcessTitle(event.getCommand().getName());
		} catch (NotDefinedException e) {
			throw new ExecutionException("Error getting event command or category", e);
		}
		
		activateMessageConsole();
		
		runBuildJob();
		
		return null;
	}

	/**
	 * Run the request as a Job
	 */
	private void runBuildJob() {
		Job job = new Job(processTitle) {

			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				IStatus runStatus = doMavenBuild(progressMonitor);
				syncWithUi(runStatus);

				return runStatus;
			}

		};
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Run the Maven OBR builds
	 * @param progressMonitor
	 * @return
	 */
	private IStatus doMavenBuild(IProgressMonitor progressMonitor) {

		List<IProject> projects = getProjectList(progressMonitor);
		Map<String, String> results = new LinkedHashMap<>();

		for (IProject project : projects) {
			if (progressMonitor.isCanceled()) {
				writeError(productName + " " + processTitle + " Cancelled");
				return Status.CANCEL_STATUS;
			}

			writeInfo("Building project \"" + project.getName() + "\" ...\n");
			try {	
				// Create Maven request 
				InvocationRequest invocationRequest = new DefaultInvocationRequest();
				File baseDirectory = new File(project.getLocationURI());
				invocationRequest.setBaseDirectory(baseDirectory);
				String outputDirectory = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/.metadata/.plugin/" + project.getProject().getName();
				Properties invocationRequestProperties = new Properties();
				invocationRequestProperties.put(OUTPUT_DIR_PROPERTY, outputDirectory);
				invocationRequest.setProperties(invocationRequestProperties);
				Boolean batchMode = true;
				invocationRequest.setBatchMode(batchMode);
				invocationRequest.setDebug(debug);
				invocationRequest.setShowErrors(debug);
				List<String> goals = new ArrayList<>();
				goals.add(MAVEN_BUILD_TEST_OBR_GOAL);
				invocationRequest.setGoals(goals);
				writeDebug("  Maven configuration:");						
				writeDebug("    base directory set to " + baseDirectory.getAbsolutePath());
				writeDebug("    outputDirectory set to " + new File(outputDirectory).getAbsolutePath());
				writeDebug("    batch mode set to " + batchMode);
				writeDebug("    show errors set to " + batchMode);
				writeDebug("    goals set to " + goals);

				Invoker invoker = new DefaultInvoker();
				invoker.setOutputHandler(new PrintStreamHandler(consoleOut, true));
				InvocationResult invocationResult = invoker.execute(invocationRequest);
				CommandLineException commandLineException = invocationResult.getExecutionException();
				if (commandLineException != null) {
					buildSuccess = false;
					commandLineException.printStackTrace(consoleOutRed);
					results.put(project.getName(), BUILD_FAILURE + " - " + commandLineException.getMessage());
				} else {
					int exitCode = invocationResult.getExitCode();
					if (exitCode == 0) {
						writeInfo(BUILD_SUCCESS + ": " + PROJECT + " \"" + project.getName() + "\"\n");
						results.put(project.getName(), BUILD_SUCCESS);
					} else {
						buildSuccess = false;
						writeError(BUILD_FAILURE + ": " + PROJECT + " \"" + project.getName() + "\"" + " exit code " + exitCode + "\n");
						results.put(project.getName(), BUILD_FAILURE + " - exit code " + exitCode);
					}
				}
			} catch (MavenInvocationException e) {
				buildSuccess = false;
				writeError(BUILD_FAILURE + ": " + PROJECT + " \"" + project.getName() + "\"", e);
				results.put(project.getName(), BUILD_FAILURE + " - " + e.getMessage());
			}
		}
		
		return reportResults(results);
	}

	/**
	 * Report the build results
	 * @param results
	 * @return
	 */
	private IStatus reportResults(Map<String, String> results) {
		if (results.size() > 0) {
			writeInfo(StringUtils.rightPad("", 100, "-"));
			writeInfo(" " + productName + " " + processTitle + " results:");
			writeInfo("");
			writeInfo(" " + StringUtils.rightPad(PROJECT, maxProjectNameLength + 1) + "Result");
			writeInfo(" " + StringUtils.rightPad("", 100, "-"));
			for (Map.Entry<String, String> resultsEntry : results.entrySet()) {
				if (resultsEntry.getValue().startsWith(BUILD_SUCCESS)) {
					writeInfo(" " + StringUtils.rightPad(resultsEntry.getKey(), maxProjectNameLength + 1) + resultsEntry.getValue());
				} else {
					writeError(StringUtils.rightPad(resultsEntry.getKey(), maxProjectNameLength + 1) + resultsEntry.getValue());
				}
			}
		} else {
			writeError(productName + " " + processTitle + " Cancelled - No " + productName + " projects found");
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;		
	}

	/**
	 * Return a list of Cirillo Test projects
	 * @param progressMonitor
	 * @return project list
	 */
	private static List<IProject> getProjectList(IProgressMonitor progressMonitor) {
		setMaxProjectNameLength(0);
		writeInfo("Finding " + productName + " test projects ...\n");
		
		// Get the list of workspace projects
		List<IProject> projectList = new LinkedList<>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] workspaceProjects = workspaceRoot.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			writeDebug("Checking project \"" + workspaceProject.getName() + "\"");
			
			if (!workspaceProject.isOpen()) {
				writeInfo(PROJECT + " \"" + workspaceProject.getName() + "\" is closed and will be not built.");
			} else if (!isMavenBundle(workspaceProject)) {
				writeInfo(PROJECT + " \"" + workspaceProject.getName() + "\" is not a Maven Bundle project and will not be built.");
			} else if (!hasCirilloDependency(workspaceProject, progressMonitor)) {
				writeInfo(PROJECT + " \"" + workspaceProject.getName() + "\" does not have a Maven dependency on " + CIRILLO_GROUPID + ":" + CIRILLO_ARTIFACT_ID + " and will not be built.");
			} else {
				projectList.add(workspaceProject);
				setMaxProjectNameLength(workspaceProject.getName().length());
				writeInfo(PROJECT + " \"" + workspaceProject.getName() + "\" is a " + productName + " test project and will be built.");
			}
		}	
		return projectList;
	}

	/**
	 * Returns true if project is a Maven project
	 * @param workspaceProject
	 * @return
	 */
	private static boolean isMavenBundle(IProject workspaceProject) {
		try {
			return (workspaceProject.hasNature(MAVEN_NATURE) && workspaceProject.hasNature(IBundleProjectDescription.PLUGIN_NATURE));
		} catch (CoreException e) {
			writeError("Unable to confirm project natures", e);
			return false;
		}
	}

	/**
	 * Returns true if project has Cirillo Maven Dependency
	 * @param workspaceProject
	 * @param progressMonitor
	 * @return
	 */
	private static boolean hasCirilloDependency(IProject workspaceProject, IProgressMonitor progressMonitor) {

		IMavenProjectFacade mavenProjectFacade = MavenPlugin.getMavenProjectRegistry().getProject(workspaceProject);
		MavenProject mavenProject = null;
		if (mavenProjectFacade != null) {
			try {
				mavenProject = mavenProjectFacade.getMavenProject(progressMonitor);
			} catch (CoreException e) {
				writeError("Unable to get MavenProject object for project \"" + workspaceProject.getName() + "\". Does project need rebuilding? OBR for Project will not be built.", e);
			}
		}
		if (mavenProjectFacade == null || mavenProject == null) {
			writeError("Unable to get MavenProject object of \"" + workspaceProject.getName() + "\". Does project need rebuilding? OBR for Project will not be built.");
			return false;
		}
		
		boolean hasCirilloDependency = false;
		Model mavenModel = mavenProject.getModel();
		List<Dependency> dependancies = mavenModel.getDependencies();
		writeDebug("    Dependencies:");
		for (Dependency dependecy : dependancies) {
			writeDebug("      " + dependecy.getGroupId() + ":" + dependecy.getArtifactId() + ":" + dependecy.getVersion());
			if (dependecy.getGroupId().equals(CIRILLO_GROUPID) && dependecy.getArtifactId().equals(CIRILLO_ARTIFACT_ID)) {
				hasCirilloDependency = true;
			}
		}
		return hasCirilloDependency;
	}

	/**
	 * Notify user at job end
	 * @param runStatus
	 */
	private void syncWithUi(IStatus runStatus) {
		Runnable runnable = () -> {
			if (runStatus.getSeverity() == IStatus.CANCEL) {
				MessageDialog.openError(activeShell, productName, processTitle + " cancelled");
				return;
			}
			if (buildSuccess) {
				MessageDialog.openInformation(activeShell, productName, processTitle + " complete - " + BUILD_SUCCESS);
			} else {
				MessageDialog.openError(activeShell, productName, processTitle + " complete - " + BUILD_FAILURE + "\n\nSee console log for details");
			}
		};
		Display.getDefault().asyncExec(runnable);
	}

	/**
	 * Activate message console
	 */
	private void activateMessageConsole() {
		MessageConsole messageConsole = null;
		String consoleName = productName + " " + processTitle;
		
		// Look for existing console
		ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = consolePlugin.getConsoleManager();
		IConsole[] existingConsoles = consoleManager.getConsoles();
		for (IConsole existingConsole : existingConsoles) {
			if (existingConsole.getName().equals(consoleName)) {
				messageConsole = (MessageConsole) existingConsole;
				break;
			}
		}
		
		// Not found, create a new one
		if (messageConsole == null) {
			messageConsole = new MessageConsole(consoleName, null);
			consoleManager.addConsoles(new IConsole[] { messageConsole });
		}
	
		// Clear and activate console
		messageConsole.clearConsole();
		messageConsole.activate();
		
		// Create the default PrintStream
		MessageConsoleStream  messageConsoleStreamDefault = messageConsole.newMessageStream();
		messageConsoleStreamDefault.setColor(null);
		setConsoleOut(new PrintStream(messageConsoleStreamDefault, true));
	
		// Create a PrintStream for Red text
		MessageConsoleStream  messageConsoleStreamRed = messageConsole.newMessageStream();
		messageConsoleStreamRed.setColor(new Color(null, new RGB(255, 0, 0)));
		setConsoleOutRed(new PrintStream(messageConsoleStreamRed, true));
	
		// Create a PrintStream for Blue text
		MessageConsoleStream  messageConsoleStreamBlue = messageConsole.newMessageStream();
		messageConsoleStreamBlue.setColor(new Color(null, new RGB(0, 0, 255)));
		setConsoleOutBlue(new PrintStream(messageConsoleStreamBlue, true));
	}

	/**
	 * Has debug been requested
	 * @return
	 */
	private void enableDebug(ExecutionEvent event) {		
		if (event.getCommand().getId().endsWith("Debug")) {
			setDebug(true);
			return;
		}
		setDebug(false);
	}

	/**
	 * Write an info message to the console
	 * @param message
	 */
	private static void writeInfo(String message) {
		consoleOutBlue.println(MESSAGE_INFO + message);
	}

	/**
	 * Write a debug message to the console
	 * @param message
	 */
	private static void writeDebug(String message) {
		if (debug) {
			consoleOutBlue.println(MESSAGE_DEBUG + message);
		}
	}

	/**
	 * Write an error message to the console
	 * @param message
	 */
	private static void writeError(String message) {
		consoleOutRed.println(MESSAGE_ERROR + message);
	}

	/**
	 * Write an error message to the console
	 * @param message
	 * @param e
	 */
	private static void writeError(String message, Exception e) {
		consoleOutRed.println(MESSAGE_ERROR + message);
		e.printStackTrace(consoleOutRed);
	}
}
