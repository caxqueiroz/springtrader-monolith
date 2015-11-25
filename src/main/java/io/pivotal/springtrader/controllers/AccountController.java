package io.pivotal.springtrader.controllers;


import io.pivotal.springtrader.domain.Account;
import io.pivotal.springtrader.services.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

/**
 * REST controller for the io.pivotal.springtrader.accounts.accounts microservice.
 * Provides the following endpoints:
 * <p><ul>
 * <li>GET <code>/io.pivotal.springtrader.accounts.accounts/{id}</code> retrieves the account with given id.
 * <li>POST <code>/io.pivotal.springtrader.accounts.accounts</code> stores the account object passed in body.
 * <li>GET <code>/io.pivotal.springtrader.accounts.accounts/{id}/increaseBalance/{amount}</code> increases the balance of the account with given id by amount.
 * <li>GET <code>/io.pivotal.springtrader.accounts.accounts/{id}/decreaseBalance/{amount}</code> decreases the balance of the account with given id by amount.
 * </ul><p>
 * @author David Ferreira Pinto
 * @author Maxim Avezbakiev
 *
 */
@RestController
public class AccountController {

	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	/**
	 * The accountService to delegate calls to.
	 */
	@Autowired
	private AccountService accountService;

	/**
	 * REST call to retrieve the account with the given id as userId.
	 * 
	 * @param id The id of the user to retrieve the account for.
	 * @return The account object if found.
	 */
	@RequestMapping(value = "/account/{id}", method = RequestMethod.GET)
	public ResponseEntity<Account> find(@PathVariable("id") final int id) {

		logger.info("AccountController.find: id=" + id);

		Account accountResponse = accountService.getAccount(id);
		return new ResponseEntity<>(accountResponse, getNoCacheHeaders(), HttpStatus.OK);

	}
	//TODO: do we need this? need to change web accountService to use find() above.
	@RequestMapping(value = "/account/", method = RequestMethod.GET)
	public ResponseEntity<Account> findAccount(@RequestParam(value="name") final String id) {

		logger.info("AccountController.getAccount: id=" + id);

		Account accountResponse = this.accountService.getAccount(id);
		return new ResponseEntity<>(accountResponse,getNoCacheHeaders(), HttpStatus.OK);

	}

	/**
	 * REST call to save the account provided in the request body.
	 * @param accountRequest The account to save.
	 * @param builder
	 * @return
	 */
	@RequestMapping(value = "/account", method = RequestMethod.POST)
	public ResponseEntity<String> save(@RequestBody Account accountRequest, UriComponentsBuilder builder) {

		logger.debug("AccountController.save: userId=" + accountRequest.getUserid());

		Integer accountProfileId = accountService.saveAccount(accountRequest);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setLocation(builder.path("/account/{id}").buildAndExpand(accountProfileId).toUri());

		return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
	}
	/**
	 * REST call to decrease the balance in the account.
	 * Decreases the balance of the account if the new balance is not lower than zero.
	 * Returns HTTP OK and the new balance if the decrease was successful, 
	 * or HTTP EXPECTATION_FAILED if the new balance would be negative and the old/current balance.
	 * 
	 * @param userId The id of the account.
	 * @param amount The amount to decrease the balance by.
	 * @return The new balance of the account with HTTP OK.
	 */
	@RequestMapping(value = "/accounts/{userId}/decreaseBalance/{amount}", method = RequestMethod.GET)
	public ResponseEntity<Double> decreaseBalance(@PathVariable("userId") final String userId, @PathVariable("amount") final double amount) {

		logger.debug("AccountController.decreaseBalance: id='" + userId + "', amount='"+amount+"'");

		Account accountResponse = accountService.getAccount(userId);
		
		BigDecimal currentBalance = accountResponse.getBalance();
		
		BigDecimal newBalance = currentBalance.subtract(new BigDecimal(amount));
		
		if ( newBalance.compareTo(BigDecimal.ZERO) >= 0) {
			accountResponse.setBalance(newBalance);
			accountService.saveAccount(accountResponse);
			return new ResponseEntity<>(accountResponse.getBalance().doubleValue(),
					getNoCacheHeaders(), HttpStatus.OK);

		} else {
			//no sufficient founds available 
			return new ResponseEntity<>(accountResponse.getBalance().doubleValue(),
					getNoCacheHeaders(), HttpStatus.EXPECTATION_FAILED);
		}
	
	}
	/**
	 * REST call to increase the balance in the account.
	 * Increases the balance of the account if the amount is not negative.
	 * Returns HTTP OK and the new balance if the increase was successful, 
	 * or HTTP EXPECTATION_FAILED if the amount given is negative.
	 * 
	 * @param userId The id of the account.
	 * @param amount The amount to increase the balance by.
	 * @return The new balance of the account with HTTP OK.
	 */
	@RequestMapping(value = "/accounts/{userId}/increaseBalance/{amount}", method = RequestMethod.GET)
	public ResponseEntity<Double> increaseBalance(@PathVariable("userId") final String userId, @PathVariable("amount") final double amount) {

		logger.debug("AccountController.increaseBalance: id='" + userId + "', amount='"+amount+"'");

        if (amount > 0) {

            double currentBalance = accountService.increaseBalance(amount, userId);
            logger.debug("AccountController.increaseBalance: current balance='" + currentBalance + "'.");
            return new ResponseEntity<>(currentBalance,getNoCacheHeaders(),HttpStatus.OK);

        } else {

            //amount can not be negative for increaseBalance, please use decreaseBalance
            return new ResponseEntity<>(accountService.getAccount(userId).getBalance().doubleValue(), getNoCacheHeaders(),
                    HttpStatus.EXPECTATION_FAILED);
        }
	
	}
	
	private HttpHeaders getNoCacheHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Cache-Control", "no-cache");
		return responseHeaders;
	}
}
