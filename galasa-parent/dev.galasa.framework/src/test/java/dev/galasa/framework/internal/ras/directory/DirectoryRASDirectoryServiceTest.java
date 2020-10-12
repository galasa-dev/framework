package dev.galasa.framework.internal.ras.directory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class DirectoryRASDirectoryServiceTest {
   
//   @Test
//   public void testCacheIsBeingReloaded() throws ResultArchiveStoreException {
//      ArrayList<DirectoryRASRunResult> testArray = new ArrayList<>();
//      
//      Instant start = Instant.parse("2020-02-03T11:25:30.00Z");
//      Instant end = Instant.parse("2020-10-03T11:26:30.00Z");
//      
//      TestStructure struc = new TestStructure();
//      struc.setTestName("Test");
//      struc.setRequestor("Bob");
//      struc.setStartTime(start);
//      struc.setEndTime(end);
//      
//      TestStructure struc2 = new TestStructure();
//      struc2.setTestName("Test2");
//      struc2.setRequestor("Jim");
//      struc2.setStartTime(start);
//      struc2.setEndTime(end);
//      
//      TestStructure struc3 = new TestStructure();
//      struc3.setTestName("Test3");
//      struc3.setRequestor("Simon");
//      struc3.setStartTime(start);
//      struc3.setEndTime(end);
//      
//      DirectoryRASRunResult res = new DirectoryRASRunResult() {
//          @Override
//          public TestStructure getTestStructure() {
//              return struc;
//          }
//      };
//      
//      DirectoryRASRunResult res2 = new DirectoryRASRunResult() {
//          @Override
//          public TestStructure getTestStructure() {
//              return struc2;
//          }
//      };
//      
//      DirectoryRASRunResult res3 = new DirectoryRASRunResult() {
//          @Override
//          public TestStructure getTestStructure() {
//              return struc3;
//          }
//      };
//      
//      testArray.add(res);
//      testArray.add(res2);
//      testArray.add(res3);
//      
//   DirectoryRASDirectoryService dummy = new DirectoryRASDirectoryService(null, null);
//   
//   List<DirectoryRASRunResult> firstList = dummy.getAllRuns();
//   
//   TestStructure struc4 = new TestStructure();
//   struc3.setTestName("Test4");
//   struc3.setRequestor("Simone");
//   struc3.setStartTime(start);
//   struc3.setEndTime(end);
//   
//   }

}
