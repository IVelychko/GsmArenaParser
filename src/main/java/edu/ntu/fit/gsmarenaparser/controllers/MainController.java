package edu.ntu.fit.gsmarenaparser.controllers;

import edu.ntu.fit.gsmarenaparser.models.ParsedProductData;
import edu.ntu.fit.gsmarenaparser.services.ExcelWriterService;
import edu.ntu.fit.gsmarenaparser.services.ParserService;
import edu.ntu.fit.gsmarenaparser.services.JsonSerializerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Controller
public class MainController {
    private static int parsedContentCounter = 1;
    @Autowired
    ParserService parser;

    @Autowired
    JsonSerializerService serializer;

    @Autowired
    ExcelWriterService excelService;

    @Value("${spring.application.name}")
    String appName;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        return "home";
    }

    @PostMapping("/parse")
    public ResponseEntity<String> parseAction(@RequestParam String urlInput, HttpServletRequest request) {
        String regex = "^(https?://)?([\\da-z.-]+\\.)*gsmarena\\.com/.*$";
        if (!urlInput.matches(regex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parsing failed: Unable to parse the content.");
        }
        try {
            ParsedProductData parsedContent = parser.parse(urlInput);
            String parsedDataJson = serializer.serializeToJson(parsedContent);
            HttpSession session = request.getSession();
            session.setAttribute("parsedContent" + parsedContentCounter++, parsedDataJson);
        }
        catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Parsing failed: Unable to parse the content.");
        }

        return ResponseEntity.ok("The content was parsed successfully");
    }

    @PostMapping("/downloadExcel")
    public ResponseEntity<ByteArrayResource> downloadExcelFile(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Enumeration<String> attributeNames = session.getAttributeNames();
        List<String> attributesToDelete = new ArrayList<>();
        List<ParsedProductData> parsedDataList = new ArrayList<>();

        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            attributesToDelete.add(attributeName);
            Object attributeValue = session.getAttribute(attributeName);
            ParsedProductData deserializedValue = serializer.deserializeFromJson((String)attributeValue);
            parsedDataList.add(deserializedValue);
        }
        if (parsedDataList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ByteArrayResource(new byte[0]));
        }
        byte[] excelFileContent;
        try {
            excelFileContent = excelService.getExcelFile(parsedDataList);
            if (excelFileContent.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ByteArrayResource(new byte[0]));
            }
        }
        catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ByteArrayResource(new byte[0]));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "device_specs.xlsx");
        for (String name: attributesToDelete) {
            session.removeAttribute(name);
        }
        parsedContentCounter = 1;

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelFileContent.length)
                .body(new ByteArrayResource(excelFileContent));
    }
}
