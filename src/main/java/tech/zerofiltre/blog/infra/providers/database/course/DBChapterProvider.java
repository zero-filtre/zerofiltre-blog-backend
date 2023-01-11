package tech.zerofiltre.blog.infra.providers.database.course;

import org.mapstruct.factory.*;
import tech.zerofiltre.blog.domain.course.*;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.*;

import java.util.*;

public class DBChapterProvider implements ChapterProvider {

    private final ChapterJPARepository chapterJPARepository;
    private final ChapterJPAMapper chapterJPAMapper = Mappers.getMapper(ChapterJPAMapper.class);

    public DBChapterProvider(ChapterJPARepository chapterJPARepository) {
        this.chapterJPARepository = chapterJPARepository;
    }

    @Override
    public Optional<Chapter> chapterOfId(long id) {
        return chapterJPARepository.findById(id).map(chapterJPAMapper::toChapter);
    }

    @Override
    public Chapter save(Chapter chapter) {
        return chapterJPAMapper.toChapter(chapterJPARepository.save(chapterJPAMapper.toChapterJPA(chapter)));
    }

    @Override
    public void delete(Chapter chapter) {
        chapterJPARepository.delete(chapterJPAMapper.toChapterJPA(chapter));
    }
}
