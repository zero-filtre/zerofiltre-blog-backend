package tech.zerofiltre.blog.domain.article;

public interface ArticleViewByDatesProvider {

    int countByUser(String dateStart, String dateEnd, long viewerId);

}
