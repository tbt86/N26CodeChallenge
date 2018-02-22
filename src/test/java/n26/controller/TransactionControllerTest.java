package n26.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import n26.model.InvalidTimestampException;
import n26.model.Transaction;
import n26.service.TransactionStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionStore transactionStore;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void addTransactionTest_validTransaction() throws Exception {
        Transaction transaction = new Transaction(0, 1.0);

        mockMvc.perform(post("/transactions").content(mapper.writeValueAsString(transaction))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isCreated());
    }

    @Test
    public void addTransactionTest_invalidTransaction() throws Exception {
        Transaction transaction = new Transaction(0, 1.0);

        doThrow(new InvalidTimestampException("Invalid transaction")).when(transactionStore)
                .addTransaction(any());

        mockMvc.perform(post("/transactions").content(mapper.writeValueAsString(transaction))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isNoContent());
    }

}
