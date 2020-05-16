import cmanager.geo.AllTestsGeo;
import cmanager.okapi.AllTestsOKAPI;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({AllTestsGeo.class, AllTestsOKAPI.class})
public class AllTests {}
