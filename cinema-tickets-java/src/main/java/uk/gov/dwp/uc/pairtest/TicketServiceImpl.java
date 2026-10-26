package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
	private final SeatReservationService seatReservationService;
	private final TicketPaymentService ticketPaymentService;

	public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {
		this.seatReservationService = seatReservationService;
		this.ticketPaymentService = ticketPaymentService;
	}

	// According to the spec, any account ID greater than 0 is considered a valid
	// account and has sufficient funds to make a purchase
	private final int INVALID_ACCOUNT_ID = 0;

	// infant tickets are not paid for, they are required to sit on the laps of
	// adults
	private final int INFANT_TICKET_PRICE_POUNDS = 0;
	private final int CHILD_TICKET_PRICE_POUNDS = 15;
	private final int ADULT_TICKET_PRICE_POUNDS = 25;

	// According to the spec, maximum ticket purchase for a single request is 25
	private final int MAXIMUM_TICKET_PURCHASE_AMOUNT = 25;

	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
		if (accountId == null || accountId <= INVALID_ACCOUNT_ID) {
			throw new InvalidPurchaseException(String.format("Invalid account ID provided: %d", accountId));
		}

		TicketTypeRequest[] validatedTickets = validateTicketRequests(ticketTypeRequests);
		Map<Type, Integer> ticketTotals = summariseTickets(validatedTickets);

		int totalSeatsToAllocate = calculateTotalSeats(ticketTotals);
		int totalAmountToPay = calculateTotalPrice(ticketTotals);

		seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
		ticketPaymentService.makePayment(accountId, totalAmountToPay);
	}

	/**
	 * Returns an array of valid ticket requests by checking against the following
	 * rules:
	 * <ol>
	 * <li>Total tickets purchased is not more than
	 * {@link #MAXIMUM_TICKET_PURCHASE_AMOUNT}</li>
	 * <li>Child or infant tickets must have an adult ticket purchased</li>
	 * <li>No negative amount of tickets can be bought</li>
	 * </ol>
	 * 
	 * @param requests
	 * @return TicketTypeRequest[]
	 * @throws InvalidPurchaseException
	 */
	private TicketTypeRequest[] validateTicketRequests(TicketTypeRequest... requests) throws InvalidPurchaseException {
		int totalTicketsPurchased = 0;

		for (TicketTypeRequest ticket : requests) {
			Type ticketType = ticket.getTicketType();

			switch (ticketType) {
			case Type.ADULT, Type.CHILD, Type.INFANT -> {
			}
			case null, default -> {
				throw new InvalidPurchaseException(String.format("invalid ticket type of: %s", ticketType));
			}
			}

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
		boolean hasAdultTickets = Arrays.stream(requests).anyMatch(ticket -> ticket.getTicketType() == Type.ADULT);

		boolean hasChildOrInfantTickets = Arrays.stream(requests)
				.anyMatch(ticket -> ticket.getTicketType() == Type.CHILD || ticket.getTicketType() == Type.INFANT);

		if (hasChildOrInfantTickets && !hasAdultTickets) {
			String errMsg = String.format("Cannot purchase children or infant tickets without an adult ticket");
			throw new InvalidPurchaseException(errMsg);
		}

		return requests;
	}

	/**
	 * Returns a map containing each ticket type mapped to the number of tickets
	 * bought.
	 * 
	 * @param ticketRequests
	 * @return Map<Type, Integer>
	 */
	private Map<Type, Integer> summariseTickets(TicketTypeRequest... ticketRequests) {
		Map<Type, Integer> ticketSummary = new EnumMap<>(Type.class);

		for (TicketTypeRequest ticket : ticketRequests) {
			Type type = ticket.getTicketType();
			int noOfTickets = ticket.getNoOfTickets();
			int currentTotal = ticketSummary.getOrDefault(type, 0);
			ticketSummary.put(type, currentTotal + noOfTickets);
		}

		return ticketSummary;
	}

	/**
	 * Calculates the total seats to reserve. It totals the number of tickets for
	 * <b>children</b> and <b>adults</b>, but excludes <b>infant</b> tickets.
	 * Infants are required to seat on the laps of adults.
	 * 
	 * @param ticketSummary
	 * @return
	 */
	private int calculateTotalSeats(Map<Type, Integer> ticketSummary) {
		return ticketSummary.getOrDefault(Type.CHILD, 0) + ticketSummary.getOrDefault(Type.ADULT, 0);
	}

	/**
	 * Calculates the total price for the request. It calculates the total amount to
	 * pay for each ticket type by multiplying the number of tickets purchased for a
	 * ticket by the <b>Types's</b> price. Infant ticket types are excluded as
	 * infant tickets are not paid for.
	 * 
	 * @param ticketSummary
	 * @return
	 */
	private int calculateTotalPrice(Map<Type, Integer> ticketSummary) {
		return (ticketSummary.getOrDefault(Type.CHILD, 0) * CHILD_TICKET_PRICE_POUNDS)
				+ (ticketSummary.getOrDefault(Type.ADULT, 0) * ADULT_TICKET_PRICE_POUNDS);
	}

	public int getMaximumTicketPurchaseAmount() {
		return this.MAXIMUM_TICKET_PURCHASE_AMOUNT;
	}

	public int getChildTicketPricePounds() {
		return this.CHILD_TICKET_PRICE_POUNDS;
	}

	public int getAdultTicketPricePounds() {
		return this.ADULT_TICKET_PRICE_POUNDS;
	}

	public int getInfantTicketPricePounds() {
		return this.INFANT_TICKET_PRICE_POUNDS;
	}
}
