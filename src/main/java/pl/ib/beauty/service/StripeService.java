package pl.ib.beauty.service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.ib.beauty.config.StripeCredentialConfig;
import pl.ib.beauty.model.dao.Course;

import java.util.UUID;

@Service
public class StripeService {

    public StripeService(StripeCredentialConfig stripeCredentialConfig) {
        Stripe.apiKey = stripeCredentialConfig.getSecretKey();
    }


    @SneakyThrows
    public String createSession(Course course, UUID orderId) {

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/api/v1/payment/notify?corelationKey=" + orderId + "&status=SUCCESS")
                .setCancelUrl("http://localhost:8080/api/v1/payment/notify?corelationKey=" + orderId + "&status=FAILED")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("PLN")
                                .setUnitAmount(course.getPrice().longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(course.getTitle())
                                        .build())
                                .build())
                        .build())
                .build();
        Session session = Session.create(params);
        return session.getUrl();
    }
}
