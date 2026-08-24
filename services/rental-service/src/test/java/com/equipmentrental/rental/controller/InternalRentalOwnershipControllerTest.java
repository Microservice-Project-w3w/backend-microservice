package com.equipmentrental.rental.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.rental.dto.response.RentalOwnershipResponse;
import com.equipmentrental.rental.repository.RentalOrderRepository;
import com.equipmentrental.rental.service.RentalOwnershipService;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class InternalRentalOwnershipControllerTest {

    @Test
    void usesCustomerIdClaimInsteadOfUserIdForOwnership() {
        InternalRentalOwnershipController controller = controllerWithMissingOrder();
        JwtAuthenticationToken authentication = authentication(9L, 77L);

        RentalOwnershipResponse response = controller.verifyOwnership(101L, 202L, authentication);

        assertThat(response.owned()).isFalse();
        assertThat(response.customerId()).isEqualTo(77L);
    }

    @Test
    void rejectsCustomerTokenWithoutCustomerId() {
        InternalRentalOwnershipController controller = controllerWithMissingOrder();
        JwtAuthenticationToken authentication = authentication(9L, null);

        assertThatThrownBy(() -> controller.verifyOwnership(101L, 202L, authentication))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(CommonErrorCode.AUTH_DATA_SCOPE_DENIED));
    }

    private InternalRentalOwnershipController controllerWithMissingOrder() {
        RentalOrderRepository repository = (RentalOrderRepository) Proxy.newProxyInstance(
                RentalOrderRepository.class.getClassLoader(),
                new Class<?>[] {RentalOrderRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.empty();
                    }
                    return null;
                }
        );
        RentalOwnershipService service = new RentalOwnershipService(
                repository,
                new InventoryClient("http://localhost")
        );
        return new InternalRentalOwnershipController(service);
    }

    private JwtAuthenticationToken authentication(Long userId, Long customerId) {
        Jwt.Builder jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("userId", userId);
        if (customerId != null) {
            jwt.claim("customerId", customerId);
        }
        return new JwtAuthenticationToken(jwt.build());
    }
}
