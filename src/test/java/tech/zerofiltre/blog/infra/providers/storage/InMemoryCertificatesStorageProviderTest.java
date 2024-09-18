package tech.zerofiltre.blog.infra.providers.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
class InMemoryCertificatesStorageProviderTest {

    private InMemoryCertificatesStorageProvider inMemoryCertificatesStorageProvider;

    @BeforeEach
    void init() {
        inMemoryCertificatesStorageProvider = new InMemoryCertificatesStorageProvider();
    }

    @Test
    void when_file_is_not_stored_then_Optional_isPresent_is_false() {
        //ARRANGE
        //ACT
        Optional<File> response = inMemoryCertificatesStorageProvider.get("clé inexistante");

        //ASSERT
        assertThat(response.isPresent()).isFalse();
    }

    @Test
    void when_file_is_stored_then_Optional_isPresent_is_true() {
        //ARRANGE
        File file1 = new File("file1.pdf");
        inMemoryCertificatesStorageProvider.store(file1);

        File file2 = new File("file2.pdf");
        inMemoryCertificatesStorageProvider.store(file2);

        //ACT
        Optional<File> response = inMemoryCertificatesStorageProvider.get(file1.getName());

        //ASSERT
        assertThat(response.isPresent()).isTrue();
        assertThat(response.get().getName()).isEqualTo(file1.getName());
    }

    @Test
    void when_file_is_stored_and_deleted_then_Optional_isPresent_is_false() {
        //ARRANGE
        File file1 = new File("file1.pdf");
        inMemoryCertificatesStorageProvider.store(file1);

        File file2 = new File("file2.pdf");
        inMemoryCertificatesStorageProvider.store(file2);
        inMemoryCertificatesStorageProvider.delete(file2.getName());

        //ACT
        Optional<File> response = inMemoryCertificatesStorageProvider.get(file2.getName());

        //ASSERT
        assertThat(response.isPresent()).isFalse();
    }
}