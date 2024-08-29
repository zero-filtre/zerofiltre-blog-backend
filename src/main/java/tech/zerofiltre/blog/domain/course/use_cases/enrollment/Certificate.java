package tech.zerofiltre.blog.domain.course.use_cases.enrollment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.storage.CertificatesStorageProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class Certificate {

    private final EnrollmentProvider enrollmentProvider;
    private final CertificatesStorageProvider certificatesStorageProvider;
    private final CourseProvider courseProvider;
    private final ITemplateEngine templateEngine;

    public File giveCertificate(User user, long courseId) throws IOException, ZerofiltreException {
        if(enrollmentProvider.isCompleted(user.getId(), courseId)) {
            String fullName = user.getFullName();
            String courseTitle = courseProvider.getTitle(courseId);
            String fileNamePdf = ZerofiltreUtils.sanitizeString(fullName) + "_" + ZerofiltreUtils.sanitizeString(courseTitle) + ".pdf";

            Optional<File> storedCertificate = certificatesStorageProvider.get(fileNamePdf);
            if(storedCertificate.isPresent()) {
                return storedCertificate.get();
            }

            String language = user.getLanguage() != null ? user.getLanguage() : Locale.FRANCE.getLanguage();

            File certificate = createCertificate(new Locale(language), fullName, courseTitle, fileNamePdf);
            certificatesStorageProvider.store(certificate);

            return certificate;
        }

        throw new ZerofiltreException("The certificate cannot be issued. The course has not yet been completed.");
    }

    private File createCertificate(Locale locale, String fullName, String courseTitle, String fileNamePdf) throws IOException, ZerofiltreException {
        Resource attachement = new ClassPathResource("mail_header_image.png");

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("attachement", attachement.getURL().toString());
        templateModel.put("fullName", fullName);
        templateModel.put("courseTitle", courseTitle);
        templateModel.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        thymeleafContext.setLocale(locale);

        String certificateHtml = templateEngine.process("certificate_course_completed.html", thymeleafContext);

        log.trace("certificateHtml = {}", certificateHtml);

        if(certificateHtml == null) throw new ZerofiltreException("Error during certificate generation.");

        return convertHtmlToPdf(certificateHtml, fileNamePdf);
    }

    private File convertHtmlToPdf(String certificateHtml, String fileNamePdf) throws ZerofiltreException, IOException {
        org.jsoup.nodes.Document documentJsoup = Jsoup.parse(certificateHtml);
        documentJsoup.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        OutputStream outputStream;

        try {
            outputStream = new FileOutputStream(fileNamePdf);
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            renderer.setDocumentFromString(documentJsoup.html());
            renderer.layout();
            renderer.createPDF(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new ZerofiltreException("Error during certificate generation.", e);
        }

        log.info("Course completion certificate for file: {} converted to pdf completed.", fileNamePdf);

        return new File(fileNamePdf);
    }

}
