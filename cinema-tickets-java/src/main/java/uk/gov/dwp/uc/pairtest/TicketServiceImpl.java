package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
  /** Should only have private methods other than the one below. */

  /** accountIds not greater than 0 are considered invalid */
  private final int INVALID_ACCOUNT_ID = 0;

  private final int CHILD_TICKET_PRICE = 15;
  private final int ADULT_TICKET_PRICE = 25;
  private final int MAXIMUM_TICKET_PURCHASE_AMOUNT = 25;

  private final SeatReservationServiceImpl seatReservationService =
      new SeatReservationServiceImpl();
  private final TicketPaymentServiceImpl ticketPaymentService = new TicketPaymentServiceImpl();

  @Override
  public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
      throws InvalidPurchaseException {
    if (accountId <= INVALID_ACCOUNT_ID) {
      System.out.printf("invalid account received:: %d\n", accountId);
      throw new InvalidPurchaseException();
    }
    ;

    boolean requestContainsAdultTicket =
        Arrays.stream(ticketTypeRequests)
            .anyMatch(request -> request.getTicketType().equals(Type.ADULT));

    boolean requestContainsInfantChildTicket =
        Arrays.stream(ticketTypeRequests)
            .anyMatch(
                request -> {
                  return request.getTicketType().equals(Type.INFANT)
                      || request.getTicketType().equals(Type.CHILD);
                });

    if (!requestContainsAdultTicket && requestContainsInfantChildTicket) {
      String errMsg =
          String.format("Cannot purchase child or infant tickets without adult tickets");
      System.err.println(errMsg);
      throw new InvalidPurchaseException();
    }

    // calculate total
    int totalPrice = 0;
    int totalSeatReservations = 0;

    for (TicketTypeRequest request : ticketTypeRequests) {
      int noOfTickets = request.getNoOfTickets();

      if (noOfTickets <= 0) {
        String errMsg =
            String.format("invalid number of tickets from request: %s", request.toString());
        System.err.println(errMsg);
        throw new InvalidPurchaseException();
      } else if (noOfTickets > MAXIMUM_TICKET_PURCHASE_AMOUNT) {
        String errMsg = String.format("maxium purchase amount reached for: ", request);
        System.err.println(errMsg);
        throw new InvalidPurchaseException();
      }

      switch (request.getTicketType()) {
        case Type.INFANT -> {
          System.out.println("infant ticket skipping");
        }
        case Type.CHILD -> {
          totalPrice += noOfTickets * CHILD_TICKET_PRICE;
          totalSeatReservations += noOfTickets;
        }
        case Type.ADULT -> {
          totalPrice += noOfTickets * ADULT_TICKET_PRICE;
          totalSeatReservations += noOfTickets;
        }
        case null, default -> {
          System.out.printf("expected a valid ticktetType got:", request.getTicketType());
          throw new InvalidPurchaseException();
        }
      }
      ;
    }
    ;

    seatReservationService.reserveSeat(accountId, totalSeatReservations);
    ticketPaymentService.makePayment(accountId, totalPrice);

    String debugMsg =
        String.format(
            "total_reservations: %d, total_price: %d\n", totalSeatReservations, totalPrice);
    System.out.println(debugMsg);
  }
}
