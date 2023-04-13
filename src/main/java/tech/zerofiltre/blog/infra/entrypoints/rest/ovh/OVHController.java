package tech.zerofiltre.blog.infra.entrypoints.rest.ovh;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.infra.providers.api.ovh.*;
import tech.zerofiltre.blog.infra.providers.api.ovh.model.*;

@Slf4j
@RestController
@RequestMapping("/ovh")
@RequiredArgsConstructor
public class OVHController {

    private final OVHTokenProvider ovhTokenProvider;

    @GetMapping("/token")
    public OVHToken init() throws Exception {
        return ovhTokenProvider.getToken();
    }
}
