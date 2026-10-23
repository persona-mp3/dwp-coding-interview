import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

class TicketServiceImplTest {
	TicketPaymentService ticketPaymentService;
	SeatReservationService seatReservationService;
	TicketServiceImpl ticketService;

	// these values were chosen at random
	Long validAccountId = 99L;
	Long invalidAccountIdZero = 0L;
	Long invalidAccountIdNegative = -109L;

	@BeforeEach
	void setUp() {
		ticketPaymentService = mock(TicketPaymentService.class);
		seatReservationService = mock(SeatReservationService.class);

		ticketService = new TicketServiceImpl(seatReservationService, ticketPaymentService);
	}

	@Test
	void validatesAgainstMaximumPurchaseAmount() {
		TicketTypeRequest[] requests = { new TicketTypeRequest(Type.ADULT, 10), new TicketTypeRequest(Type.ADULT, 23) };
		int totalTicketsPurchased = 33;

		InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(validAccountId, requests));

		String expectedErrMsg = String.format("Cannot purchase more than %d tickets at once. You made %d purchases",
				ticketService.getMaximumTicketPurchaseAmount(), totalTicketsPurchased);

		assertEquals(expectedErrMsg, exception.getMessage());

	}

	@Test
	void throwsInvalidPurchaseExceptionWithAnInvalidAccountId() {
		TicketTypeRequest[] requests = { new TicketTypeRequest(Type.ADULT, 10) };

		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(invalidAccountIdZero, requests));

		assertThrows(InvalidPurchaseException.class,
				() -> ticketService.purchaseTickets(invalidAccountIdNegative, requests));

		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(null, requests));

	}

	@Test
	void throwsInvalidPurchaseExceptionWithZeroNumberOfTicketsPurchased() {
		TicketTypeRequest[] requests = { new TicketTypeRequest(null, 0) };

		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(validAccountId, requests));

	}

	@Test
	void throwsInvalidPurchaseExceptionWithInvalidTicketType() {
		TicketTypeRequest[] requests = { new TicketTypeRequest(null, 10) };

		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(validAccountId, requests));

	}
}
