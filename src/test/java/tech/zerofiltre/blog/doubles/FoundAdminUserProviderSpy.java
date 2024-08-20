package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserEmail;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoundAdminUserProviderSpy implements UserProvider {

    public boolean userOfIdCalled;

    @Override
    public Optional<User> userOfId(long userId) {
        userOfIdCalled = true;
        return Optional.of(ZerofiltreUtils.createMockUser(true));
    }

    @Override
    public Optional<User> userOfSocialId(String userSocialId) {
        return Optional.empty();
    }

    @Override
    public List<UserEmail> allEmails() {
        return new ArrayList<>();
    }

    @Override
    public List<User> users() {
        return null;
    }

    @Override
    public User save(User user) {
        return null;
    }

    @Override
    public Optional<User> userOfEmail(String email) {
        return Optional.empty();
    }

    @Override
    public List<User> nonActiveUsers() {
        return null;
    }

    @Override
    public void deleteUser(User user) {

    }
}

