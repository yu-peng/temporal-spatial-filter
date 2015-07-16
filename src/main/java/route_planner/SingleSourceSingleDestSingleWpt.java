package route_planner;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/SingleSourceSingleDestSingleWpt")
public class SingleSourceSingleDestSingleWpt {	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject computeRoutes(
			@DefaultValue("") @QueryParam("origin") String origin,
			@DefaultValue("") @QueryParam("waypoint") String waypoint,
			@DefaultValue("") @QueryParam("destination") String destination,
			@DefaultValue("") @QueryParam("pois") String pois,
			@DefaultValue("true") @QueryParam("returnPolyline") boolean returnPolyline,
			@DefaultValue("false") @QueryParam("returnInstruction") boolean returnInstruction,
			@DefaultValue("") @QueryParam("mode") String mode) throws JSONException {
		
		
		JSONObject result = new JSONObject();
		result.put("type", "SingleSourceSingleDestinationSingleWaypoint");
		result.put("waypoint", origin);
		result.put("origin", origin);
		result.put("destination", destination);
		result.put("pois",pois);
		result.put("mode",mode);

	    JSONArray result_array = new JSONArray();
		ArrayList<Future<JSONObject>> solutions = new ArrayList<Future<JSONObject>>();
		ExecutorService executor = Executors.newFixedThreadPool(16);		

		if (origin.isEmpty()){
			
			result.put("Status","Route generator error. Origin is not specified.");
			return result;
			
		} else if (pois.isEmpty()){
					
			result.put("Status","Route generator error. POI is not specified.");
			return result;

		} else if (waypoint.isEmpty()){
			
			result.put("Status","Route generator error. POI is not specified.");
			return result;

		} else if (destination.isEmpty()){
			
			result.put("Status","Route generator error. Destination is not specified.");
			return result;

		} else if (mode.isEmpty()){
			
			result.put("Status","Route generator error. Mode is not specified.");
			return result;
		}
		
		String[] originCoordinates = origin.split(",");
		double originLat = Double.parseDouble(originCoordinates[0]);
		double originLon = Double.parseDouble(originCoordinates[1]);	
		
		String[] wptCoordinates = waypoint.split(",");
		double wptLat = Double.parseDouble(wptCoordinates[0]);
		double wptLon = Double.parseDouble(wptCoordinates[1]);
			
		String[] destCoordinates = destination.split(",");
		double destLat = Double.parseDouble(destCoordinates[0]);
		double destLon = Double.parseDouble(destCoordinates[1]);	
		
		// first compute a direct route between the two destinations
		SingleSourceSingleDestSolver direct_solver = new SingleSourceSingleDestSolver(originLat,originLon, wptLat, wptLon, destLat, destLon, returnPolyline, returnInstruction, mode);
		JSONObject direct_route = direct_solver.solve();
		result.put("DirectRoute", direct_route);
		direct_solver = null;
		
		String[] poiCoordinates = pois.split(",");
		for (int i = 0; i < poiCoordinates.length; i = i + 2){		
			
			try {
				
				double poiLat = Double.parseDouble(poiCoordinates[i]);
				double poiLon = Double.parseDouble(poiCoordinates[i+1]);
				
				if (poiLat > -90 && poiLat < 90 && poiLon > -180 && poiLon < 180){
					
					// always multi threading for fast response
						
					SingleSourceSingleDestSingleWptSolver solver = new SingleSourceSingleDestSingleWptSolver(originLat, originLon, wptLat, wptLon, poiLat, poiLon, destLat, destLon, 
							returnPolyline, returnInstruction, mode);				    
					Future<JSONObject> solution = executor.submit(solver);
					solutions.add(solution);						
					
					solver = null;					
					
				} else {
					result_array.put("Route generator error. POI input "+poiLat+","+poiLon+" is out of range.");
				}
				
			} catch (ArrayIndexOutOfBoundsException e){

				result_array.put("Route generator error. POI input is invalid.");	
				
			} catch (Exception e){
				
				result_array.put("Route generator error. POI input is invalid.");	
	
			}
		}			
				
		executor.shutdown();
		
	    try {
			
	    	executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	    	for (Future<JSONObject> solution : solutions){
	    		result_array.put(solution.get());		
	    	}
	    	
	    	executor = null;

		} catch (InterruptedException e) {
			e.printStackTrace();
			result.put("Status","Error Occurred. Cannot complete routing.");

		} catch (ExecutionException e) {
			e.printStackTrace();
			result.put("Status","Error Occurred. Cannot complete routing.");
		}
		
	    
		result.put("routes", result_array);


		return result;
	}

}
