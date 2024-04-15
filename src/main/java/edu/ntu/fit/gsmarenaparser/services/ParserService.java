package edu.ntu.fit.gsmarenaparser.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import edu.ntu.fit.gsmarenaparser.models.ParsedProductData;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ParserService {
    public ParsedProductData parse(String url) throws IOException, IllegalArgumentException {
        if (!url.contains("gsmarena.com")) {
            throw new IllegalArgumentException("The provided URL does not refer to 'gsmarena.com' resource.");
        }
        Document doc = Jsoup.connect(url).get();
        Element specsList = doc.getElementById("specs-list");
        if (specsList == null) {
            throw new IOException("Element 'specs-list' was not found");
        }
        Element productNameElement = doc.getElementsByClass("specs-phone-name-title").first();
        String productName;
        if (productNameElement != null) {
            productName = productNameElement.text();
        }
        else {
            productName = "N/A";
        }
        Element productReleaseDateElement = doc.select("span[data-spec=\"released-hl\"]").first();
        String productReleaseDate;
        if (productReleaseDateElement != null) {
            productReleaseDate = productReleaseDateElement.text().replace("Released", "").trim()
                    .replace(",", "");
        }
        else {
            productReleaseDate = "N/A";
        }
        Elements specsListTables = specsList.getElementsByTag("table");

        ParsedProductData parsedData = new ParsedProductData();
        Map<String, String> deviceInfo = new LinkedHashMap<>();
        deviceInfo.put("Name", productName);
        deviceInfo.put("Release Date", productReleaseDate);
        parsedData.getContent().put("Device Info", deviceInfo);

        for (Element table: specsListTables) {
            Elements tableRows = table.getElementsByTag("tr");
            Element rowElement = tableRows.first();
            String rowHeader;
            if (rowElement != null) {
                Element rowHeaderElement = rowElement.getElementsByTag("th").first();
                if (rowHeaderElement != null) {
                    rowHeader = rowHeaderElement.text();
                }
                else {
                    rowHeader = "N/A";
                }
            }
            else {
                rowHeader = "N/A";
            }

            HashMap<String, String> rowsContent = new LinkedHashMap<>();

            for (Element row: tableRows) {
                String title = row.select(".ttl a").text();
                if (title.isEmpty()) {
                    continue;
                }
                String info = row.select(".nfo").text();
                rowsContent.put(title, info);
            }

            parsedData.getContent().put(rowHeader, rowsContent);
        }

        return parsedData;
    }
}
