package tech.zerofiltre.blog.infra.providers.storage;

import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.storage.CertificatesStorageProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryCertificatesStorageProvider implements CertificatesStorageProvider {

    private Map<String, File> certificates = new HashMap<>();

    @Override
    public void store(File file) { certificates.put(file.getName(), file); }

    @Override
    public Optional<File> get(String key) { return Optional.ofNullable(certificates.get(key)); }

    @Override
    public void delete(String key) { certificates.remove(key); }

}
