package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.*;
import tech.zerofiltre.blog.domain.article.use_cases.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.*;

import javax.validation.*;
import java.util.*;

@RestController
public class TagController {

    private final TagProvider tagProvider;
    private final EditTag editTag;

    public TagController(TagProvider tagProvider) {
        this.tagProvider = tagProvider;
        this.editTag = new EditTag(tagProvider);
    }

    @GetMapping("/tag")
    public List<Tag> tags() {
        return tagProvider.tags();
    }

    @GetMapping("/tag/{id}")
    public Tag tagOfId(@PathVariable("id") long tagId) throws ResourceNotFoundException {
        return tagProvider.tagOfId(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("The tag with id: " + tagId + " does not exist", String.valueOf(tagId), Domains.TAG.name()));
    }

    @PostMapping("/admin/tag")
    public Tag createTag(@RequestBody @Valid EditTagVM tagVM) throws ResourceAlreadyExistException {
        Tag tag = new Tag();
        tag.setName(tagVM.getName());
        tag.setColorCode(tagVM.getColorCode());
        return editTag.create(tag);
    }

    @PatchMapping("/admin/tag")
    public Tag updateTag(@RequestBody @Valid EditTagVM tagVM) throws ResourceNotFoundException {
        Tag tag = new Tag();
        tag.setId(tagVM.getId());
        tag.setName(tagVM.getName());
        tag.setColorCode(tagVM.getColorCode());
        return editTag.update(tag);
    }
}
