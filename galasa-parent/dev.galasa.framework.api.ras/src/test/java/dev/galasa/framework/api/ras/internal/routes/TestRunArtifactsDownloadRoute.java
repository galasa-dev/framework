package dev.galasa.framework.api.ras.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestRunArtifactsDownloadRoute {
    /*
	* REGEX TESTS
	*/
	@Test
	public void TestRunArtifactsDownloadRouteRegexWithGenericExpectedInput() throws Exception {

		String testInput = "/runs/TEST123/files/artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithMissingInitialSlashDoesntMatch() throws Exception {

		String testInput = "runs/OHNO123/files/artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithJargonFirstSection() throws Exception {

		String testInput = "/thisisjargon/OHNO456/files/artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithJargonLastSection() throws Exception {

		String testInput = "/runs/OHNO789/morejargon/artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithSlashOnEndWorks() throws Exception {

		String testInput = "/runs/TEST456/files/artifact.path/";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithMissingSecondSlashDoesntMatch() throws Exception {

		String testInput = "/runsOHNO111/files/artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithMissingThirdSlashDoesntMatch() throws Exception {

		String testInput = "/runs/OHNO112files/artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithCompleteJargonButSlashesDoesntMatch() throws Exception {

		String testInput = "/gszjnkjasfdfd/alkjdfg/asdkadg/adfgsdfg";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithJargonAtStartDoesntMatch() throws Exception {

		String testInput = "JARGON/runs/OHNO113/files/artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexWithLongArtifactPathMatches() throws Exception {

		String testInput = "/runs/OHNO114/files/artifact.path/more.artifact.path";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isTrue();
	}

	@Test
	public void TestRunArtifactsDownloadRouteRegexMissingArtifactPathDoesntMatch() throws Exception {

		String testInput = "JARGON/runs/OHNO113/files/";

		Pattern pattern = Pattern.compile(RunArtifactsDownloadRoute.ROUTE_REGEX);

		Matcher matcher = pattern.matcher(testInput);
		boolean matchFound = matcher.matches();

		assertThat(matchFound).isFalse();
	}
}
