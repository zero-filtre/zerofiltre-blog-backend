package tech.zerofiltre.blog.domain.storage;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CertificatesStorageProvider {

    void store(File file);

    Optional<File> get(String key);

    void delete(String key);

}
