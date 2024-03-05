package com.derster.PaypalIntegration.config;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {
    private final PaypalService paypalService;

    @GetMapping("/")
    public String home(){
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment(
            @RequestParam("method") String method,
            @RequestParam("amount") Double amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description
    ){
        String cancelUrl = "http://localhost:8080/payment/cancel";
        String successUrl = "http://localhost:8080/payment/success";

        try {
            Payment payment = paypalService.createPayment(
                    amount,
                    currency,
                    method,
                    "sale",
                    description,
                    cancelUrl,
                    successUrl
            );

            for (Links links : payment.getLinks()) {
                if ("approval_url".equals(links.getRel())) {
                    return new RedirectView(links.getHref());
                }
            }
            log.error("No approval_url found in PayPal response.");
        } catch (PayPalRESTException ex) {
            log.error("Error creating PayPal payment: " + ex.getMessage());
        }

        return new RedirectView("/payment/error");
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("payerId") String payerId
    ){
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);

            if (payment.getState().equals("approved")){
                return "paymentSuccess";
            }

        }catch (PayPalRESTException ex){
            log.error(ex.getMessage());
        }

        return "paymentSuccess";

    }

    @GetMapping("/payment/cancel")
    public String paymentCancel(
    ){

        return "paymentCancel";

    }

    @GetMapping("/payment/error")
    public String paymentError(
    ){

        return "paymentError";

    }




}
