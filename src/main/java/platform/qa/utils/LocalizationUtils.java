package platform.qa.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import platform.qa.exceptions.LocalizationExceptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to upload, get and search localization data from JSON file.
 * Currently, supports load to {@link JsonNode}, {@link String}, {@link List<String>}
 * Example of usage:
 *  <p>
 *      {@code
 *          JsonNode node = new LocalizationUtils().uploadLocalizationFile("/test/path/example.json");
 *          String localizationData = new LocalizationUtils()
 *              .getLocalizationDataForSelectedLanguageByKey("/test/path/example.json", "en", "test");
 *          String localizationData = new LocalizationUtils()
 *              .getLocalizationDataForSelectedLanguageByPath("/test/path/example.json", "en", "test", "path1", "path2");
 *          List<String> localizationData = new LocalizationUtils()
 *              .getLocalizationDataForAllLanguages("/test/path/example.json", "test");
 *      }
 *  </p>
 */
@Log4j2
public class LocalizationUtils {
    private JsonNode localizationData;

    /**
     * Upload localization data from JSON localization file to {@link JsonNode}
     * @param localizationFilePath path to JSON localization file
     * @return {@link JsonNode}
     */
    public JsonNode uploadLocalizationFile(String localizationFilePath) {
        byte[] jsonData = new byte[0];
        try {
            jsonData = Files.readAllBytes(Paths.get(localizationFilePath));
        } catch (IOException e) {
            throw new LocalizationExceptions.LocalizationFileLoadException(e);
        }
        JsonNode localizationJsonData = null;
        try {
            localizationJsonData = new ObjectMapper().readTree(jsonData);
        } catch (IOException e) {
            throw new LocalizationExceptions.LocalizationFileLoadException(e);
        }

        return localizationJsonData;
    }

    /**
     * Get localization data by key for selected language from JSON localization file to {@link String}
     * @param localizationFilePath path to JSON localization file
     * @param local language element in JSON localization file
     * @param localizationKey element to search in JSON localization file
     * @return {@link String}
     */
    public String getLocalizationDataForSelectedLanguageByKey(String localizationFilePath, String local,
                                                              String localizationKey) {
        return uploadLocalizationFile(localizationFilePath).get(local).findValue(localizationKey).asText();
    }

    /**
     * Get localization data by path for selected language from JSON localization file to {@link String}
     * @param localizationFilePath path to JSON localization file
     * @param local language element in JSON localization file
     * @param localizationKey element to search in JSON localization file
     * @param pathSegments path elements to navigate in JSON localization file
     * @return {@link String}
     */
    public String getLocalizationDataForSelectedLanguageByPath(String localizationFilePath, String local,
                                                               String localizationKey,
                                                               String... pathSegments) {
        JsonNode rootNode = null;
        rootNode = uploadLocalizationFile(localizationFilePath);
        JsonNode fileData = rootNode.path(local);

        for (String segment : pathSegments) {
            fileData = fileData.path(segment);
        }

        return fileData.path(localizationKey).asText();
    }

    /**
     * Get localization data by key for all languages from JSON localization file to {@link List} of <String>
     * @param localizationFilePath path to JSON localization file
     * @param localizationKey element to search in JSON localization file
     * @return {@link List<String>}
     */
    public List<String> getLocalizationDataForAllLanguages(String localizationFilePath, String localizationKey) {
        List<String> localizationValuesAsStrings = new ArrayList<>();
        JsonNode rootNode = null;
        rootNode = uploadLocalizationFile(localizationFilePath);

        for (JsonNode node : rootNode.findValues(localizationKey)) {
            localizationValuesAsStrings.add(node.toString());
        }

        return localizationValuesAsStrings;
    }

    /**
     * Load the flat-structure JSON file.
     */
    private void loadLocalizationData(String localizationFilePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            localizationData = mapper.readTree(new File(localizationFilePath));
            log.info("Loaded localization data from: {}", localizationFilePath);
        } catch (IOException e) {
            log.error("Failed to load localization file: {}", localizationFilePath, e);
            throw new LocalizationExceptions.LocalizationFileLoadException("Failed to load localization file", e);
        }
    }

    /**
     * Get localized value by key from the flat JSON structure.
     * Handles the structure: { "en": { "Name": "Name" }, "az": { "Name": "Ad" } }
     */
    public String getLocalizationDataFromFlatJsonForSelectedLanguageByKey(String localizationFilePath, String local,
                                                                          String key) {
        if (localizationData == null) {
            loadLocalizationData(localizationFilePath);
        }

        JsonNode localeNode = localizationData.get(local);
        if (localeNode == null) {
            log.error("Locale '{}' not found in localization file", local);
            throw new LocalizationExceptions.LocaleNotFoundException("Locale not found: " + local);
        }

        JsonNode valueNode = localeNode.get(key);
        if (valueNode == null) {
            log.error("Key '{}' not found for locale '{}'", key, local);
            throw new LocalizationExceptions.LocalizationKeyNotFoundException("Key not found: " + key + " for locale: " + local);
        }

        String value = valueNode.asText();
        log.info("Retrieved localized value for key '{}' (locale '{}'): '{}'", key, local, value);
        return value;
    }
}
