package tech.zerofiltre.blog.domain.company;

public interface CompanyProvider {
    boolean isUserPartOfCompany(long companyId, long userId);
}
