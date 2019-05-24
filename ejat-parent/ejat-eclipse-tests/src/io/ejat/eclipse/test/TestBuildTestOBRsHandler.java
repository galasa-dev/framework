package io.ejat.eclipse.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.junit.BeforeClass;
import org.junit.Test;

import io.ejat.eclipse.BuildTestOBRsHandler;

public class TestBuildTestOBRsHandler {

	private static Logger logger = Logger.getLogger(TestBuildTestOBRsHandler.class.getName());
	private static ICommandService commandService;
	private static ExecutionEvent executionEvent;
	private static String projectName = "CirilloProject";
	private static String workspaceLocation;

	@BeforeClass
	public static void setup() throws IOException, CoreException, InterruptedException {

    	// Create Eclipse environment
		IEvaluationContext context = new EvaluationContext(null, new Object());
		Map<String, String> parameters = new HashMap<>();
		executionEvent = new ExecutionEvent(null, parameters, null, context);
		assertEquals("Eclipse workbench not running", true, PlatformUI.isWorkbenchRunning());
		
		IWorkbench workbench = PlatformUI.getWorkbench();
        commandService = workbench.getAdapter(ICommandService.class);
        
        // Create Cirillo project
		workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		ZipInputStream zipInputStream = new ZipInputStream(TestBuildTestOBRsHandler.class.getClassLoader().getResourceAsStream(projectName + ".zip"));
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
   			File entryDestination = new File(workspaceLocation,  zipEntry.getName());
   			if (zipEntry.isDirectory()) {
   				entryDestination.mkdirs();
   			} else {
   				entryDestination.getParentFile().mkdirs();
   				OutputStream out = new FileOutputStream(entryDestination);
   				IOUtils.copy(zipInputStream, out);
   				out.close();
   			}
   		}
		logger.log(Level.INFO, "Test project created");
    	
		// Open project
   		IProjectDescription projectDescription= ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
   		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
   		project.create(projectDescription, new NullProgressMonitor());
   		project.open(new NullProgressMonitor());
   		int sec = 0;
   		while (!project.isOpen() && sec  <= 20) {
   			Thread.sleep(1000);
			sec ++;
   		}
   		
   		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		} catch (Exception e) {
			// NOOP
		}
	}
	

	/**
	 * Run the {@link BuildTestOBRsHandler}
	 * @throws ExecutionException
	 * @throws NotDefinedException
	 * @throws NotEnabledException
	 * @throws NotHandledException
	 * @throws InterruptedException 
	 */
	@Test
	public void testHandlerExecute() throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException, InterruptedException {
		Command command = commandService.getCommand("io.ejat.eclipse.commands.buildTestOBRsCommand");
		command.executeWithChecks(executionEvent);
		
		String jobState = BuildTestOBRsHandler.getJobState();
		int sec = 0;
		while ((jobState == null || !jobState.equals("done")) && sec  <= 30) {
			Thread.sleep(1000);
			jobState = BuildTestOBRsHandler.getJobState();
			sec ++;
		}
		
		assertFalse("Build job still running after " + (sec-1) + "s", sec >= 30);		
		
		assertEquals("OBR Build failed.", BuildTestOBRsHandler.isBuildSuccess(), true);
		logger.log(Level.INFO, "OBR build successful");
		
	
		command.undefine();
		
		String obrFileName = workspaceLocation + "/.metadata/.plugin/" + projectName + "/"  + "repository.obr"; 
		File obrFile = new File(obrFileName);
		assertEquals("OBR file not created - " + obrFileName, obrFile.exists(), true);
		logger.log(Level.INFO, "OBR file created {0}", obrFileName);
	}
}
