package tech.zerofiltre.blog.infra.providers.certificate;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class  PDFCertificateEngine {

    private final ITemplateEngine templateEngine;
    private final InfraProperties infraProperties;

    public PDFCertificateEngine(ITemplateEngine templateEngine, InfraProperties infraProperties) {
        this.templateEngine = templateEngine;
        this.infraProperties = infraProperties;
    }

    public byte[] process(Locale locale, String fullName, String courseTitle, String pdfFileName, String uuid) throws IOException, ZerofiltreException, WriterException, NoSuchAlgorithmException {
        LocalDateTime generationDateTime = LocalDateTime.now();

        Resource attachment = new ClassPathResource("mail_header_image.png");

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("attachement", attachment.getURL().toString());
        templateModel.put("fullName", fullName);
        templateModel.put("courseTitle", courseTitle);
        templateModel.put("today", generationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE) + "|" + generationDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME));

        String qrCodeBase64 = generateQrCodeImageAsBase64(uuid, fullName, courseTitle);

        log.debug("Qrcode_base64  = {}", qrCodeBase64);
        templateModel.put("path_to_qrcode_image", "data:image/png;base64," + qrCodeBase64);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        thymeleafContext.setLocale(locale);

        String certificateHtml = templateEngine.process("certificate_course_completed.html", thymeleafContext);

        log.debug("certificateHtml = {}", certificateHtml);

        if (certificateHtml == null) throw new ZerofiltreException("An error occurred during certificate generation.");

        return convertHtmlToPdf(certificateHtml, pdfFileName);
    }

    private byte[] convertHtmlToPdf(String certificateHtml, String pdfFileName) throws ZerofiltreException {
        org.jsoup.nodes.Document documentJsoup = Jsoup.parse(certificateHtml);
        documentJsoup.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            renderer.setDocumentFromString(documentJsoup.html());
            renderer.layout();
            renderer.createPDF(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new ZerofiltreException("An error occurred during certificate generation.", e);
        }

        log.info("Pdf conversion completed for course completion certificate for file: {}.", pdfFileName);

        return outputStream.toByteArray();
    }

    private String generateQrCodeImageAsBase64(String uuid, String fullName, String courseTitle) throws WriterException, IOException {
        BufferedImage qrCodeImage = generateQrCode(uuid, fullName, courseTitle);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(qrCodeImage, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes).replaceAll("\\s", "");
    }

    public BufferedImage generateQrCode(String uuid, String fullName, String courseTitle) throws WriterException {

        String encodedFullName = URLEncoder.encode(fullName, StandardCharsets.UTF_8);
        String encodedCourseTitle = URLEncoder.encode(courseTitle, StandardCharsets.UTF_8);
        String encodedUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv()) + "/certificate/verification?fullname=" + encodedFullName + "&courseTitle=" + encodedCourseTitle + "&uuid=" + uuid;
        BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

        int width = 285;
        int height = 285;

        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        BitMatrix matrix = barcodeWriter.encode(encodedUrl, barcodeFormat, width, height);

        return MatrixToImageWriter.toBufferedImage(matrix);
    }

}
