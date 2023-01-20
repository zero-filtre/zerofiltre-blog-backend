package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;

@Component
@Transactional
@RequiredArgsConstructor
public class DBChapterProvider implements ChapterProvider {

    private final ChapterJPARepository chapterJPARepository;
    private final ChapterJPANumberRepository chapterJPANumberRepository;
    private final ChapterJPAMapper chapterJPAMapper = Mappers.getMapper(ChapterJPAMapper.class);


    @Override
    public Optional<Chapter> chapterOfId(long id) {
        return chapterJPARepository.findById(id).map(chapterJPAMapper::toChapter);
    }

    @Override
    public Chapter save(Chapter chapter) {
        ChapterJPA chapterJPA = chapterJPAMapper.toChapterJPA(chapter);
        if (chapter.getNumber() == 0) {
            ChapterJPANumber number = chapterJPANumberRepository.save(new ChapterJPANumber());
            chapterJPA.setNumber(number);
        }
        return chapterJPAMapper.toChapter(chapterJPARepository.save(chapterJPA));
    }

    @Override
    public void delete(Chapter chapter) {
        chapterJPARepository.delete(chapterJPAMapper.toChapterJPA(chapter));
    }
}
