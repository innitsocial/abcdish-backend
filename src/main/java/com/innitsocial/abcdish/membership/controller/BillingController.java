package com.innitsocial.abcdish.membership.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.membership.dto.BillingStatusResponse;
import com.innitsocial.abcdish.membership.dto.CreateCheckoutSessionResponse;
import com.innitsocial.abcdish.auth.entity.AppUser;
import com.innitsocial.abcdish.membership.entity.MembershipStatus;
import com.innitsocial.abcdish.auth.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BillingController {

    private final AppUserRepository appUserRepository;

    @GetMapping("/status")
    public BillingStatusResponse status() {
        AppUser user = appUserRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean active = user.getMembershipStatus() == MembershipStatus.ACTIVE;

        return new BillingStatusResponse(
                user.getMembershipStatus().name(),
                active ? "ABCDish Member" : "Free Plan",
                "GBP",
                active ? 100 : 0,
                active,
                active ? "Unlimited videos enabled." : "Free plan allows 10 videos per month."
        );
    }

    @PostMapping("/create-checkout-session")
    public CreateCheckoutSessionResponse createCheckoutSession() {
        return new CreateCheckoutSessionResponse(
                "https://checkout.stripe.com/example-placeholder",
                "Stripe checkout will be connected later."
        );
    }

    @PostMapping("/webhook")
    public String webhook(@RequestBody String payload) {
        // Later: verify Stripe signature and update membership.
        return "Webhook placeholder received";
    }
}