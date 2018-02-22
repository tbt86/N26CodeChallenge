package n26.controller;

import lombok.extern.slf4j.Slf4j;
import n26.model.Transaction;
import n26.service.TransactionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TransactionController {

    @Autowired
    TransactionStore transactionStore;

    @RequestMapping(path = "/transactions", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void postTransaction(@RequestBody Transaction transaction) {
        log.debug("Adding transaction {}", transaction.toString());
        transactionStore.addTransaction(transaction);
    }
}