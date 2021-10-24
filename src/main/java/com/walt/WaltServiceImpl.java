package com.walt;

import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class WaltServiceImpl implements WaltService {
	
	@Autowired
	private DriverRepository driverRepository;
	
	@Autowired
	private DeliveryRepository deliveryRepository;
	
    private Random rand = new Random();
	
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
    	
    	List<Driver> driversAvailable = new ArrayList<>();
    	Delivery delivery = null;
		Driver chosenDriver = null;
    	City customerCity = customer.getCity();
    	City restaurantCity = restaurant.getCity();
   		
    	if (customerCity.getName().equals(restaurantCity.getName())) {    		
    		List<Driver> driversInCity = driverRepository.findAllDriversByCity(restaurantCity);
    		
    		for (Driver driver : driversInCity) {
    			List<Delivery> driverDeliveries = deliveryRepository.findByDriver(driver);   
    			// Remove all the deliveries in a different times.
    			driverDeliveries.removeIf(d -> d.getDeliveryTime().compareTo(deliveryTime) == 1);    			
    			// Check if there is no deliveries left - no delivery at the same time as wanted.
    			if (driverDeliveries.isEmpty()) {
    				driversAvailable.add(driver);
    			}
    		}
    	}
    	    	
    	if (!driversAvailable.isEmpty()) {
        	if (driversAvailable.size() == 1) {
        		chosenDriver = driversAvailable.get(0);
        	}
        	else {
        		long countHistoryDelivery = 0;
        		long minHistoryDelivery = Integer.MAX_VALUE;
        		
        		for (Driver driver : driversAvailable)
        		{
        			countHistoryDelivery = deliveryRepository.findByDriver(driver).size();
        			
        			if (countHistoryDelivery < minHistoryDelivery) {
        				minHistoryDelivery = countHistoryDelivery;
        				chosenDriver = driver;
        			}
        		}
        	}
        	
    		delivery = new Delivery(chosenDriver, restaurant, customer, deliveryTime);
    		delivery.setDistance(getRandom());
    	}

		return delivery;
    }
    
    private double getRandom() {
        double leftLimit = 0D;
        double rightLimit = 20D;
        return leftLimit + rand.nextDouble() * (rightLimit - leftLimit);
    }

    @Override
    // Returns only drivers who did at least one delivery.
    public List<DriverDistance> getDriverRankReport() {    	
		List<Delivery> allDeliveries = new ArrayList<>();
		deliveryRepository.findAll().forEach(allDeliveries::add);	
		
		List<DriverDistance> drivers = createDriversList(allDeliveries);
		
		Collections.sort(drivers, Collections.reverseOrder((d1, d2) -> d1.getTotalDistance().compareTo(d2.getTotalDistance())));
        return drivers;
    }

	@Override
	// Returns only drivers who did at least one delivery.
    public List<DriverDistance> getDriverRankReportByCity(City city) {    	
		List<Delivery> allDeliveries = new ArrayList<>();
		deliveryRepository.findAll().forEach(allDeliveries::add);
		allDeliveries.removeIf(delivery -> !delivery.getDriver().getCity().getId().equals(city.getId()));
		
		List<DriverDistance> drivers = createDriversList(allDeliveries);
		
		Collections.sort(drivers, Collections.reverseOrder((d1, d2) -> d1.getTotalDistance().compareTo(d2.getTotalDistance())));
        return drivers;
    }
	
	private List<DriverDistance> createDriversList(List<Delivery> allDeliveries) {
    	List<DriverDistance> drivers = new ArrayList<>();
    	Map<Long, Double> driverDistanceMap = new HashMap<>();
    	
    	allDeliveries.forEach(delivery -> {
			Long driverId = delivery.getDriver().getId();
			if (driverDistanceMap.containsKey(driverId)) {
				double totalDistance = driverDistanceMap.get(driverId);
				totalDistance += delivery.getDistance();
				driverDistanceMap.replace(driverId, totalDistance);
			}
			else {
				driverDistanceMap.put(driverId, delivery.getDistance());
			}
		});
		
		driverDistanceMap.forEach((driverId, totalDistance) -> {
			Driver driver = driverRepository.findById(driverId).orElse(null);
			if (driver != null) {
				DriverWork driverWork = new DriverWork(driver, totalDistance.longValue());
				drivers.add(driverWork);				
			}
		});		
		
		return drivers;
	}
}
