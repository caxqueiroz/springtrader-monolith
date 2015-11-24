package io.pivotal.springtrader.services;


import io.pivotal.springtrader.domain.CompanyInfo;
import io.pivotal.springtrader.domain.Quote;
import io.pivotal.springtrader.exceptions.SymbolNotFoundException;
import io.pivotal.springtrader.domain.MarketSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class QuotesFetchingService {

	private static final Logger logger = LoggerFactory.getLogger(QuotesFetchingService.class);
	private final static Integer QUOTES_NUMBER = 3;
	
	//10 minutes in milliseconds
	private final static long REFRESH_PERIOD = 600000l;

	@Autowired
	private QuoteService quoteService;

	
	private static List<String> symbolsIT = Arrays.asList("EMC", "ORCL", "IBM", "INTC", "AMD", "HPQ", "CSCO", "AAPL");
	private static List<String> symbolsFS = Arrays.asList("JPM", "MS", "BAC", "GS", "WFC","BK");
	
	private MarketSummary summary = new MarketSummary();
	
	public MarketSummary getMarketSummary() {
		logger.debug("Retrieving Market Summary");
		
		return summary;
	}
	

	public Quote getQuote(String symbol) {
		logger.debug("Fetching quote: " + symbol);
		try {
			return quoteService.getQuote(symbol);
		} catch (SymbolNotFoundException e) {
			logger.error(e.getMessage(),e);
			return null;
		}

	}
	
	private Quote getQuoteFallback(String symbol) {
		logger.debug("Fetching fallback quote for: " + symbol);
		Quote quote = new Quote();
		quote.setSymbol(symbol);
		quote.setStatus("FAILED");
		return quote;
	}

	public List<CompanyInfo> getCompanies(String name) {
		logger.debug("Fetching companies with name or symbol matching: " + name);
		return quoteService.getCompanyInfo(name);

	}

	private List<CompanyInfo> getCompaniesFallback(String name) {
		List<CompanyInfo> infos = new ArrayList<>();
		return infos;
	}

	
	//TODO: prime location for a redis/gemfire caching service!
	@Scheduled(fixedRate = REFRESH_PERIOD)
	protected void retrieveMarketSummary() {
		logger.debug("Scheduled retrieval of Market Summary");
		List<Quote> quotesIT = pickRandomThree(symbolsIT).parallelStream().map(symbol -> getQuote(symbol)).collect(Collectors.toList());
		List<Quote> quotesFS = pickRandomThree(symbolsFS).parallelStream().map(symbol -> getQuote(symbol)).collect(Collectors.toList());
		summary.setTopGainers(quotesIT);
		summary.setTopLosers(quotesFS);
	}
	
	private List<String> pickRandomThree(List<String> symbols) {
		Collections.shuffle(symbols);
	    return symbols.subList(0, QUOTES_NUMBER);

	}
}
