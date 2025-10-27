package com.kafka.hubordereventpushing.config;

import com.github.javafaker.Faker;
import com.kafka.hubordereventpushing.common.EventType;
import com.kafka.hubordereventpushing.entity.OrderEvent;
import com.kafka.hubordereventpushing.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    private final OrderEventRepository orderEventRepository;

    @Override
    public void run(String... args) {
        if (orderEventRepository.count() < 9000) {
            Faker faker = new Faker();
            Random random = new Random();

            for (int i = 1; i <= 9000; i++) {

                // Random date trong khoảng 3 tháng gần đây
                LocalDate randomDate = LocalDate.now().minusDays(random.nextInt(90)); // 90 ngày ~ 3 tháng
                Date dateOnly = Date.from(randomDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                OrderEvent event = OrderEvent.builder()
                        .dateOnly(dateOnly)
                        .dateTime(LocalDateTime.now())
                        .agentCode("AGENT_" + faker.number().numberBetween(100, 999))
                        .service(faker.company().industry())
                        .objectType("ORDER")
                        .orderId((long) faker.number().numberBetween(1000, 9999))
                        .orderSubId(random.nextBoolean() ? (long) faker.number().numberBetween(1, 500) : null)
                        .eventType(EventType.values()[random.nextInt(EventType.values().length)])
                        .content(faker.lorem().sentence())
                        .amount(BigDecimal.valueOf(faker.number().randomDouble(2, 100, 10000)))
                        .amountPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 50, 5000)))
                        .amountFee(BigDecimal.valueOf(faker.number().randomDouble(2, 1, 100)))
                        .amountDiscount(BigDecimal.valueOf(faker.number().randomDouble(2, 0, 50)))
                        .discountCode(random.nextBoolean() ? "DISC" + faker.number().numberBetween(100, 999) : null)
                        .pushStatus(0L)
                        .pushDateTime(null)
                        .pushError(null)
                        .build();

                orderEventRepository.save(event);
            }
            log.info("Đã thêm 9000 bản ghi giả vào bảng ORDER_EVENT!");
        } else {
            log.info("Dữ liệu đã tồn tại!");
        }
    }

}
