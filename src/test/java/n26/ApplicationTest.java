package n26;

import static org.assertj.core.api.Assertions.assertThat;

import n26.controller.StatisticsController;
import n26.controller.TransactionController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired
    TransactionController transactionController;

    @Autowired
    StatisticsController statisticsController;

    @Test
    public void contextLoads() throws Exception {
        assertThat(transactionController).isNotNull();
        assertThat(statisticsController).isNotNull();
    }
}