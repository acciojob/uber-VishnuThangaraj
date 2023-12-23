package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers = driverRepository2.findAll();
		Driver rider = null;
		int id = Integer.MAX_VALUE;
		for(Driver driver : drivers){
			if(driver.getDriverId() < id){
				Cab c = driver.getCab();
				if(c.getAvailable()){
					id = driver.getDriverId();
					rider = driver;
				}
			}
		}

		if(rider == null){
			throw new Exception("No cab available!");
		}

		Optional<Customer> c = customerRepository2.findById(customerId);
		Customer customer = c.get();

		TripBooking trip = new TripBooking();

		trip.setFromLocation(fromLocation);
		trip.setToLocation(toLocation);
		trip.setCustomer(customer);
		trip.setDistanceInKm(distanceInKm);
		trip.setDriver(rider);
		trip.setBill(trip.getDistanceInKm() * rider.getCab().getPerKmRate());
		trip.setStatus(TripStatus.CONFIRMED);

		customerRepository2.save(customer);
		driverRepository2.save(rider);

		return trip;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBookingOptional =
				tripBookingRepository2.findById(tripId);
		TripBooking trip = tripBookingOptional.get();

		trip.setStatus(TripStatus.CANCELED);
		trip.setBill(0);

		tripBookingRepository2.save(trip);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBookingOptional =
				tripBookingRepository2.findById(tripId);
		TripBooking trip = tripBookingOptional.get();

		trip.setStatus(TripStatus.COMPLETED);

		tripBookingRepository2.save(trip);
	}
}
