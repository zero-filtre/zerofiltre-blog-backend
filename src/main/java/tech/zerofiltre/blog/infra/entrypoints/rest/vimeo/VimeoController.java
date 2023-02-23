package tech.zerofiltre.blog.infra.entrypoints.rest.vimeo;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.infra.providers.api.vimeo.*;

@Slf4j
@RestController
@RequestMapping("/vimeo")
@RequiredArgsConstructor
public class VimeoController {

    private final VimeoProvider vimeoService;

    @PostMapping("/init")
    public String init(@RequestParam long size) {
        return vimeoService.init(size);
    }
}
