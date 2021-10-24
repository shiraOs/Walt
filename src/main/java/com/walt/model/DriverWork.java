package com.walt.model;

public class DriverWork implements DriverDistance{

	private Driver driver;    
	private Long totalDistance; 
	
	public DriverWork(Driver driver, Long totalDistance) {
		super();
		this.driver = driver;
		this.totalDistance = totalDistance;
	}

	@Override
	public Driver getDriver() {
		return driver;
	}

	@Override
	public Long getTotalDistance() {
		return totalDistance;
	}

	@Override
	public String toString() {
		return "Driver Name: " + driver.getName() + ", Total Distance: " + totalDistance;
	}
}
