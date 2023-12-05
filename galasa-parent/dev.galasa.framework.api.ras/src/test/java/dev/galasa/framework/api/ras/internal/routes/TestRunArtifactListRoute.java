package dev.galasa.framework.api.ras.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import dev.galasa.framework.api.ras.internal.RasServletTest;

public class TestRunArtifactListRoute extends RasServletTest{
    /*
	* REGEX TESTS
	*/
	@Test
	public void TestRunArtifactsListRouteRegexWithGenericExpectedInput() throws Exception {

		String testInput = "/runs/TEST123/artifacts";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithMissingInitialSlashDoesntMatch() throws Exception {

		String testInput = "runs/OHNO123/artifacts";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithJargonFirstSection() throws Exception {

		String testInput = "/thisisjargon/OHNO456/artifacts";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithJargonLastSection() throws Exception {

		String testInput = "/runs/OHNO789/morejargon";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithSlashOnEndWorks() throws Exception {

		String testInput = "/runs/TEST456/artifacts/";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithMissingSecondSlashDoesntMatch() throws Exception {

		String testInput = "/runsOHNO111/artifacts";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithMissingThirdSlashDoesntMatch() throws Exception {

		String testInput = "/runs/OHNO112artifacts";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithCompleteJargonButSlashesDoesntMatch() throws Exception {

		String testInput = "/gszjnkjasfdfd/alkjdfg/asdkadg";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithJargonAtStartDoesntMatch() throws Exception {

		String testInput = "JARGON/runs/OHNO113/artifacts";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsListRouteRegexWithJargonAtEndDoesntMatch() throws Exception {

		String testInput = "/runs/OHNO114/artifacts/MOAR_JARGON";

		Pattern pattern = Pattern.compile(RunArtifactsListRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}
    
}
