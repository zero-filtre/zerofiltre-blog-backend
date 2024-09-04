package tech.zerofiltre.blog.infra.entrypoints.rest.article;

import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.article.use_cases.EditTag;
import tech.zerofiltre.blog.domain.error.ResourceAlreadyExistException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.infra.entrypoints.rest.article.model.EditTagVM;

import javax.validation.Valid;
import java.util.List;

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
                .orElseThrow(() -> new ResourceNotFoundException("The tag with id: " + tagId + " does not exist", String.valueOf(tagId)));
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
