package n26.controller;

import java.time.Instant;
import n26.model.NoStatisticsException;
import n26.model.Statistics;
import n26.service.TransactionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {

    @Autowired
    TransactionStore transactionStore;

    @RequestMapping(path = "/statistics", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Statistics getStatistics() {
        Statistics result = transactionStore.getStatistics(Instant.now());

        if (result.getCount() == 0) {
            throw new NoStatisticsException("No transactions recorded for the last 60 seconds.");
        }

        return result;
    }
}