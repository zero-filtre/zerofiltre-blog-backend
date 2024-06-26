package tech.zerofiltre.blog.domain.article;

public interface ArticleByDatesProvider {

    int countByUser(String dateStart, String dateEnd, long authorId);

}
