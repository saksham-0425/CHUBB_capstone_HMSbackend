package com.booking.bookingservice.repository.report;

import com.booking.bookingservice.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingReportRepository extends JpaRepository<Reservation, Long> {

	@Query("""
		    SELECT b.status, COUNT(b)
		    FROM Reservation b
		    WHERE b.hotelId = :hotelId
		      AND b.checkInDate <= :to
		      AND b.checkOutDate >= :from
		    GROUP BY b.status
		""")
    List<Object[]> getOccupancyByStatus(
            @Param("hotelId") Long hotelId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
    
    @Query("""
    	    SELECT r.hotelId, COALESCE(SUM(r.totalAmount), 0)
    	    FROM Reservation r
    	    WHERE r.hotelId = :hotelId
    	      AND r.paymentStatus = 'PAID'
    	      AND r.status = 'CHECKED_OUT'
    	      AND r.checkInDate <= :to
    	      AND r.checkOutDate >= :from
    	    GROUP BY r.hotelId
    	""")
    	List<Object[]> getRevenueByHotel(
    	        @Param("hotelId") Long hotelId,
    	        @Param("from") LocalDate from,
    	        @Param("to") LocalDate to
    	);
    	
    	@Query("""
    		    SELECT EXTRACT(MONTH FROM r.checkOutDate),
    		           COALESCE(SUM(r.totalAmount), 0)
    		    FROM Reservation r
    		    WHERE r.hotelId = :hotelId
    		      AND r.paymentStatus = 'PAID'
    		      AND r.status = 'CHECKED_OUT'
    		      AND EXTRACT(YEAR FROM r.checkOutDate) = :year
    		    GROUP BY EXTRACT(MONTH FROM r.checkOutDate)
    		    ORDER BY EXTRACT(MONTH FROM r.checkOutDate)
    		""")
    		List<Object[]> getMonthlyRevenue(
    		        @Param("hotelId") Long hotelId,
    		        @Param("year") int year
    		);
    		
    		
      @Query("""
    			    SELECT r.hotelId,
    			           EXTRACT(MONTH FROM r.checkOutDate),
    			           COALESCE(SUM(r.totalAmount), 0)
    			    FROM Reservation r
    			    WHERE r.paymentStatus = 'PAID'
    			      AND r.status = 'CHECKED_OUT'
    			      AND EXTRACT(YEAR FROM r.checkOutDate) = :year
    			    GROUP BY r.hotelId, EXTRACT(MONTH FROM r.checkOutDate)
    			    ORDER BY r.hotelId, EXTRACT(MONTH FROM r.checkOutDate)
    			""")
    			List<Object[]> getMonthlyRevenueForAllHotels(@Param("year") int year);
    			
    			
    			@Query("""
    				    SELECT r.hotelId, AVG(r.totalAmount)
    				    FROM Reservation r
    				    WHERE r.hotelId = :hotelId
    				      AND r.paymentStatus = 'PAID'
    				      AND r.status = 'CHECKED_OUT'
    				    GROUP BY r.hotelId
    				""")
    				List<Object[]> getAverageRevenuePerBooking(@Param("hotelId") Long hotelId);
    				
    				@Query("""
    					    SELECT EXTRACT(MONTH FROM r.checkInDate),
    					           COUNT(r)
    					    FROM Reservation r
    					    WHERE r.hotelId = :hotelId
    					      AND r.status IN ('CHECKED_IN', 'CHECKED_OUT')
    					      AND EXTRACT(YEAR FROM r.checkInDate) = :year
    					    GROUP BY EXTRACT(MONTH FROM r.checkInDate)
    					    ORDER BY EXTRACT(MONTH FROM r.checkInDate)
    					""")
    					List<Object[]> getMonthlyOccupancy(
    					        @Param("hotelId") Long hotelId,
    					        @Param("year") int year
    					);


}
