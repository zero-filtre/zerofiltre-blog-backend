package tech.zerofiltre.blog.domain.article.use_cases;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit.jupiter.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.error.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EditTagTest {

    public static final String NAME = "java";
    public static final String NEW_NAME = "new name";
    Tag tag;
    private EditTag editTag;
    @MockBean
    private TagProvider tagProvider;

    @BeforeEach
    void init() {
        tag = new Tag(0, NAME);
        editTag = new EditTag(tagProvider);
        when(tagProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    @Test
    void create_savesProperly() throws ResourceAlreadyExistException {
        //ARRANGE
        when(tagProvider.tagOfName(any())).thenReturn(Optional.empty());

        //ACT
        Tag savedTag = editTag.create(tag);

        //ASSERT
        verify(tagProvider, times(1)).save(tag);
        assertThat(savedTag.getName()).isEqualTo(NAME);
    }

    @Test
    void create_ThrowsExceptionIfTagExist() {
        //ARRANGE
        when(tagProvider.tagOfName(any())).thenReturn(Optional.of(tag));

        //ACT & ASSERT
        assertThatExceptionOfType(ResourceAlreadyExistException.class)
                .isThrownBy(() -> editTag.create(tag));
    }

    @Test
    void update_savesProperly() throws ResourceNotFoundException {
        //ARRANGE
        tag.setId(2); //already registered tag
        tag.setName(NEW_NAME);
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(tag));

        //ACT
        Tag updatedTag = editTag.update(tag);

        //ASSERT
        assertThat(updatedTag.getId()).isEqualTo(2);
        assertThat(updatedTag.getName()).isEqualTo(NEW_NAME);
    }

    @Test
    void update_ThrowsExceptionIfTagNotFound() {
        //ARRANGE
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> editTag.update(tag));
    }
}