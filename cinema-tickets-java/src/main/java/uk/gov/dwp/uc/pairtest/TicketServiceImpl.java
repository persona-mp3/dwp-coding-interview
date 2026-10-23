package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;
import java.util.List;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
	private SeatReservationService seatReservationService;
	private TicketPaymentService ticketPaymentServiceImpl;

	public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {

		this.seatReservationService = seatReservationService;
		this.ticketPaymentServiceImpl = ticketPaymentService;
	}

	/** requirements mention that an account id greater than 0 is valid */
	private final int INVALID_ACCOUNT_ID = 0;

	// currency is pounds
	private final int CHILD_TICKET_PRICE = 15;
	private final int ADULT_TICKET_PRICE = 25;

	// requirments have it that a single purchase must not exceed 25 tickets
	private final int MAXIMUM_TICKET_PURCHASE_AMOUNT = 25;

	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
		if (accountId == null || accountId <= INVALID_ACCOUNT_ID) {
			throw new InvalidPurchaseException(String.format("Invalid account ID provided: %d", accountId));
		}

		validateTicketRequests(ticketTypeRequests);

	}

	private void validateTicketRequests(TicketTypeRequest... requests) throws InvalidPurchaseException {
		int totalTicketsPurchased = 0;

		for (TicketTypeRequest ticket : requests) {
			int noOfTickets = ticket.getNoOfTickets();
			if (noOfTickets <= 0) {
				throw new InvalidPurchaseException(
						String.format("Cannot purchase less than 1 ticket. TicketRequest: %s", ticket));
			}

			totalTicketsPurchased += noOfTickets;
		}

		if (totalTicketsPurchased > MAXIMUM_TICKET_PURCHASE_AMOUNT) {
			throw new InvalidPurchaseException(
					String.format("Cannot purchase more than %d tickets at once. You made %d purchases",
							MAXIMUM_TICKET_PURCHASE_AMOUNT, totalTicketsPurchased));
		}

		// check child or infant tickets(if any) are purchased with adult tickets
		List<TicketTypeRequest> ticketRequests = Arrays.asList(requests);

		boolean hasAdultTickets = ticketRequests.stream().anyMatch(ticket -> ticket.getTicketType() == Type.ADULT);

		boolean hasChildOrInfactTickets = ticketRequests.stream()
				.anyMatch(ticket -> ticket.getTicketType() == Type.CHILD || ticket.getTicketType() == Type.INFANT);

		if (hasChildOrInfactTickets && !hasAdultTickets) {
			String errMsg = String.format("Cannot purchase children or infant tickets without an adult ticket");
			throw new InvalidPurchaseException(errMsg);
		}

	}

	public int getMaximumTicketPurchaseAmount() {
		return this.MAXIMUM_TICKET_PURCHASE_AMOUNT;
	}

}
