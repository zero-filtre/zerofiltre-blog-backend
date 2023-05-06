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
    private final ChapterJPAMapper chapterJPAMapper = Mappers.getMapper(ChapterJPAMapper.class);


    @Override
    public Optional<Chapter> chapterOfId(long id) {
        return chapterJPARepository.findById(id).map(chapterJPAMapper::toChapter);
    }

    @Override
    public Chapter save(Chapter chapter) {
        ChapterJPA chapterJPA = chapterJPAMapper.toChapterJPA(chapter);
        return chapterJPAMapper.toChapter(chapterJPARepository.save(chapterJPA));
    }

    @Override
    public void delete(Chapter chapter) {
        chapterJPARepository.delete(chapterJPAMapper.toChapterJPA(chapter));
    }

    @Override
    public List<Chapter> ofCourseId(long courseId) {
        return chapterJPAMapper.toChapters(chapterJPARepository.findAllByCourseIdOrderByNumberAsc(courseId));
    }

    @Override
    public List<Chapter> saveAll(List<Chapter> chapters) {
        List<ChapterJPA> chapterJPA = chapterJPAMapper.toChaptersJPA(chapters);
        return chapterJPAMapper.toChapters(chapterJPARepository.saveAll(chapterJPA));
    }
}
