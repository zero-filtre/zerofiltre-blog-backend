package tech.zerofiltre.blog.doubles;

import org.springframework.data.domain.*;
import org.springframework.data.repository.query.*;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.course.model.*;

import java.util.*;
import java.util.function.*;

public class DummyChapterJPARepository implements ChapterJPARepository {
    @Override
    public List<ChapterJPA> findAll() {
        return null;
    }

    @Override
    public List<ChapterJPA> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<ChapterJPA> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<ChapterJPA> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(ChapterJPA entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends ChapterJPA> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends ChapterJPA> S save(S entity) {
        return entity;
    }

    @Override
    public <S extends ChapterJPA> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<ChapterJPA> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends ChapterJPA> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends ChapterJPA> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<ChapterJPA> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public ChapterJPA getOne(Long aLong) {
        return null;
    }

    @Override
    public ChapterJPA getById(Long aLong) {
        return null;
    }

    @Override
    public <S extends ChapterJPA> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends ChapterJPA> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends ChapterJPA> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends ChapterJPA> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ChapterJPA> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ChapterJPA> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends ChapterJPA, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
