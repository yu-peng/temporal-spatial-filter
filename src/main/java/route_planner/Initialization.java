package route_planner;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Functions that are required for initializing the
 * path planner.
 * 
 * @author Peng Yu
 *
 */
public class Initialization implements ServletContextListener{

	static String hopperMapPrefix = "/localhost/route-planner/WEB-INF/resources/maps/";
	static String hopperOSMFile = "/localhost/route-planner/WEB-INF/resources/maps/";
	public static PathPlanner path_planner = null;
	
	public void contextInitialized(ServletContextEvent arg0) {

		ServletContext context = arg0.getServletContext();
		hopperMapPrefix = context.getRealPath("/WEB-INF/resources/") + "/maps/";
		hopperOSMFile = context.getRealPath("/WEB-INF/resources/") + "/maps/";
		
		loadCaliforniaMap();
//		loadMichiganMap();		

	}
	
	public void loadCaliforniaMap(){
		path_planner = new PathPlanner(hopperMapPrefix+"california",hopperOSMFile+"california-latest.osm");
	}

	public void loadMichiganMap(){
		path_planner = new PathPlanner(hopperMapPrefix+"michigan",hopperOSMFile+"michigan-latest.osm");
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {

		Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				LOG.log(Level.INFO, String.format("deregistering jdbc driver: %s", driver));
			} catch (SQLException e) {
				LOG.log(Level.SEVERE, String.format("Error deregistering driver %s", driver), e);
			}

		}

	}
}
