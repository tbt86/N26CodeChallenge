package n26.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import n26.model.Statistics;
import n26.model.Transaction;
import n26.service.TransactionStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(StatisticsController.class)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionStore transactionStore;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getStatisticsTest_oneTransaction() throws Exception {
        Statistics statistics = new Statistics();
        statistics.addTransaction(new Transaction(0, 1.0));

        when(transactionStore.getStatistics(any())).thenReturn(statistics);
        mockMvc.perform(get("/statistics")).andExpect(status().isOk())
                .andExpect(content().string(equalTo(mapper.writeValueAsString(statistics))));
    }

    @Test
    public void getStatisticsTest_noTransactions() throws Exception {
        Statistics statistics = new Statistics();

        when(transactionStore.getStatistics(any())).thenReturn(statistics);
        mockMvc.perform(get("/statistics")).andExpect(status().isNoContent());
    }

}
