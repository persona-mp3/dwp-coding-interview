// package uk.gov.dwp.uc.pairtest;
//
// import java.util.Arrays;
// import thirdparty.paymentgateway.TicketPaymentServiceImpl;
// import thirdparty.seatbooking.SeatReservationServiceImpl;
// import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
// import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
// import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
//
// public class TicketServiceImpl implements TicketService {
// 	/** accountIds not greater than 0 are considered invalid */
// 	private final int INVALID_ACCOUNT_ID = 0;
//
// 	private final int CHILD_TICKET_PRICE = 15;
// 	private final int ADULT_TICKET_PRICE = 25;
// 	private final int MAXIMUM_TICKET_PURCHASE_AMOUNT = 25;
//
// 	private final SeatReservationServiceImpl seatReservationService = new SeatReservationServiceImpl();
// 	private final TicketPaymentServiceImpl ticketPaymentService = new TicketPaymentServiceImpl();
//
// 	@Override
// 	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
// 		if (accountId <= INVALID_ACCOUNT_ID) {
// 			throw new InvalidPurchaseException(String.format("invalid accountId: %d", accountId));
// 		} ;
//
// 		boolean requestContainsAdultTicket = Arrays.stream(ticketTypeRequests)
// 				.anyMatch(request -> request.getTicketType().equals(Type.ADULT));
//
// 		boolean requestContainsInfantChildTicket = Arrays.stream(ticketTypeRequests).anyMatch(request -> {
// 			return request.getTicketType().equals(Type.INFANT) || request.getTicketType().equals(Type.CHILD);
// 		});
//
// 		// ticket requests with a child or infant must come with an adult ticket
// 		if (!requestContainsAdultTicket && requestContainsInfantChildTicket) {
// 			throw new InvalidPurchaseException(
// 					String.format("child or infant tickets must be purchased with an adult ticket"));
// 		}
//
// 		int totalPrice = 0;
//
// 		// TODO: think the guide meant that it should also be the sum of all
// 		// request.getNoOfTickets
// 		int totalSeatReservations = 0;
//
// 		for (TicketTypeRequest request : ticketTypeRequests) {
// 			int noOfTickets = request.getNoOfTickets();
//
// 			if (noOfTickets <= 0) {
// 				throw new InvalidPurchaseException(
// 						String.format("number of tickets to purchase must be more than 0, got: %d", noOfTickets));
//
// 			} else if (noOfTickets > MAXIMUM_TICKET_PURCHASE_AMOUNT) {
// 				throw new InvalidPurchaseException(
// 						String.format("maximum amounts ofticket to purchase is 25, got %d", noOfTickets));
// 			}
//
// 			switch (request.getTicketType()) {
// 			case Type.INFANT -> {
// 				System.out.println("infant ticket skipping");
// 			}
// 			case Type.CHILD -> {
// 				totalPrice += noOfTickets * CHILD_TICKET_PRICE;
// 				totalSeatReservations += noOfTickets;
// 			}
// 			case Type.ADULT -> {
// 				totalPrice += noOfTickets * ADULT_TICKET_PRICE;
// 				totalSeatReservations += noOfTickets;
// 			}
// 			case null, default -> {
// 				throw new InvalidPurchaseException(String.format("got unexpected TicketType. %s", request.getTicketType()));
// 			}
// 			};
// 		} ;
//
// 		seatReservationService.reserveSeat(accountId, totalSeatReservations);
// 		ticketPaymentService.makePayment(accountId, totalPrice);
//
// 		String debugMsg = String.format("totalReservations: %d, totalPrice: %d\n", totalSeatReservations, totalPrice);
// 		System.out.println(debugMsg);
// 	}
//
// }
