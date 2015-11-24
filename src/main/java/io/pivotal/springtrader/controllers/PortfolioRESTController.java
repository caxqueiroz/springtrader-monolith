package io.pivotal.springtrader.controllers;


import io.pivotal.springtrader.domain.Order;
import io.pivotal.springtrader.domain.Portfolio;
import io.pivotal.springtrader.services.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Provides the REST API for the portfolio portfolioService.
 * 
 * Provides the following endpoints:
 * <p><ul>
 * <li>GET <code>/portfolio/{id}</code> retrieves the portfolio with given account id.
 * <li>POST <code>/portfolio{id}</code> adds an order to the portfolio with the given account id.
 * </ul><p>
 * 
 * @author David Ferreira Pinto
 *
 */
@RestController
public class PortfolioRESTController {

	private static final Logger logger = LoggerFactory.getLogger(PortfolioRESTController.class);

	/**
	 * the portfolioService to delegate to.
	 */
	@Autowired
	private PortfolioService portfolioService;

	/**
	 * Retrieves the portfolio for the given account.
	 * @param accountId the account to retrieve the portfolio for.
	 * @return The portfolio with HTTP OK.
	 */
	@RequestMapping(value = "/portfolio/{id}", method = RequestMethod.GET)
	public ResponseEntity<Portfolio> getPortfolio(@PathVariable("id") final String accountId) {
		logger.debug("PortfolioController: Retrieving portfolio with user id:" + accountId);
		Portfolio folio = portfolioService.getPortfolio(accountId);
		logger.debug("PortfolioController: Retrieved portfolio:" + folio);
		return new ResponseEntity<>(folio, getNoCacheHeaders(), HttpStatus.OK);
	}
	
	private HttpHeaders getNoCacheHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Cache-Control", "no-cache");
		return responseHeaders;
	}
	/**
	 * Adds an order to the portfolio of the given account.
	 * 
	 * @param accountId the account to add the order to.
	 * @param order The order to add.
	 * @param builder
	 * @return The order with HTTP CREATED or BAD REQUEST if it couldn't save.
	 */
	@RequestMapping(value = "/portfolio/{id}", method = RequestMethod.POST)
	public ResponseEntity<Order> addOrder(@PathVariable("id") final String accountId, @RequestBody final Order order, UriComponentsBuilder builder) {
		logger.debug("Adding Order: " + order);
		
		//TODO: can do a test to ensure accountId == order.getAccountId();
		
		Order savedOrder = portfolioService.addOrder(order);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setLocation(builder.path("/portfolio/{id}")
				.buildAndExpand(accountId).toUri());
		logger.debug("Order added: " + savedOrder);
		if (savedOrder != null && savedOrder.getOrderId() != null) {
			return new ResponseEntity<Order>(savedOrder, responseHeaders, HttpStatus.CREATED);
		} else {
			logger.warn("Order not saved: " + order);
			return new ResponseEntity<Order>(savedOrder, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
