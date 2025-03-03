package tech.zerofiltre.blog.domain.search;

import tech.zerofiltre.blog.domain.search.model.SearchResult;
import tech.zerofiltre.blog.domain.search.model.UserSearchResult;

import java.util.List;

public interface SearchProvider {

    SearchResult search(String keyword);

    List<UserSearchResult> searchUsers(String keyword);
}
