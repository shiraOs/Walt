package com.walt;

import com.walt.dao.*;
import com.walt.model.City;
import com.walt.model.Customer;
import com.walt.model.Delivery;
import com.walt.model.Driver;
import com.walt.model.DriverDistance;
import com.walt.model.Restaurant;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics(){

    	System.out.println("------------------------------");
    	testCreateDeliveries();
    	System.out.println("------------------------------");
    	testDriversRankReport();
    	System.out.println("------------------------------");
    	testDriversRankReportByCity();
    	System.out.println("------------------------------");
       	
        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

	private void testCreateDeliveries() {
    	System.out.println("\033[1mTest Create Deliveries\033[0m");
    	// Will not find a driver, customer and restaurant not in the same city.
		addDelivery("Beethoven", "meat", 12);
    	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 0);

    	addDelivery("Mozart", "meat", 17);    	
    	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 1);    	
    	
    	addDelivery("Mozart", "meat", 17);
    	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 2);
    	
     	addDelivery("Mozart", "meat", 17);
       	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 3);
       	
    	// Will not find a driver, no available driver for 17:00 o'clock in Jerusalem.
     	addDelivery("Mozart", "meat", 17);
       	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 3);
       	
     	addDelivery("Beethoven", "vegan", 17);
       	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 4);
       	
     	addDelivery("Bach", "vegan", 17);
       	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 5);
       	
     	addDelivery("Bach", "cafe", 12);
       	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 6);
       	
     	addDelivery("Bach", "chinese", 12);
       	assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 7);	
	}

	private void addDelivery(String customerName, String restaurantName, Integer deliveryHour) {
    	
    	Customer customer = customerRepository.findByName(customerName);    	
    	Restaurant restaurant = restaurantRepository.findByName(restaurantName);

    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.HOUR_OF_DAY, deliveryHour);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	Date deliveryTime = cal.getTime();
    	        
    	Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
    	try {
    		deliveryRepository.save(delivery);
    		System.out.println(customerName + ", your driver will be: " + delivery.getDriver().getName());
    	}
    	catch (Exception e){
        	System.out.println("Could not find a driver for " + customerName + "'s delivery");
    	}
    }

	private void testDriversRankReport() {
		System.out.println("\033[1mTest Drivers Rank Report\033[0m");
		List<DriverDistance> driversDistances = waltService.getDriverRankReport();
       	driversDistances.forEach(System.out::println);       	
       	assertEquals(driversDistances.size(), 6);		
	}
	
    private void testDriversRankReportByCity() {
    	System.out.println("\033[1mTest Drivers Rank Report By City - Tel-Aviv\033[0m");
       	City city = cityRepository.findByName("Tel-Aviv");
       	List<DriverDistance> driversDistancesInCity = waltService.getDriverRankReportByCity(city);
       	driversDistancesInCity.forEach(System.out::println);       	
       	assertEquals(driversDistancesInCity.size(), 3); 		
	}
}
